package org.example.service.impl;

import jakarta.annotation.Resource;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.context.BaseContext;
import org.example.dto.ResponseDTO;
import org.example.entity.question.QuestionType;
import org.example.entity.survey.Survey;
import org.example.entity.question.Question;
import org.example.entity.response.Response;
import org.example.entity.survey.SurveyState;
import org.example.entity.survey.SurveyType;
import org.example.exception.IllegalOperationException;
import org.example.exception.ResponseNotFinishedException;
import org.example.exception.SurveyNotFoundException;
import org.example.mapper.JudgeMapper;
import org.example.mapper.ResponseMapper;
import org.example.mapper.SurveyMapper;
import org.example.service.QuestionService;
import org.example.service.ResponseService;
import org.example.service.ScoringService;
import org.example.vo.ResponseItemVO;
import org.example.vo.ResponseVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * @author chenxuanrao06@gmail.com
 * @ClassName ResponseServiceImpl
 * @description
 */
@Slf4j
@Service
public class ResponseServiceImpl implements ResponseService {

    @Resource
    private ResponseMapper responseMapper;
    @Resource
    private SurveyMapper surveyMapper;
    @Resource
    private QuestionService questionService;
    @Resource
    private ScoringService scoringService;
    @Resource
    private JudgeMapper judgeMapper;

    @Override
    @Transactional
    public Long submit(ResponseDTO responseDTO) {
        Long uid = BaseContext.getCurrentId();
        Survey survey = surveyMapper.getBySid(responseDTO.getSid());
        if (survey == null || survey.getState() == SurveyState.DELETE) {
            throw new SurveyNotFoundException("SURVEY_NOT_FOUND");
        } else if (survey.getState() == SurveyState.CLOSE) {
            throw new IllegalOperationException("SURVEY_CLOSED");
        }

        Response response = new Response();
        BeanUtils.copyProperties(responseDTO, response);
        response.setUid(uid);
        response.setCreateTime(LocalDateTime.now());
        if (survey.getType() == SurveyType.NORMAL) {
            response.setFinished(true);
        }

        responseMapper.insertRecord(response);

        response.getItems().forEach(item -> item.setRid(response.getRid()));
        responseMapper.insertItems(response.getItems());

        // 更新填写问卷的人数
        surveyMapper.addFillNum(response.getSid());

        if (survey.getType() == SurveyType.EXAM) {
            scoringService.getScore(response);
        }

        return response.getRid();
    }

    @Override
    public List<Response> getResponseBySid(Long sid) {
        return responseMapper.getBySid(sid);
    }

    @Override
    public List<Response> getResponseByUid(Long uid) {
        return responseMapper.getByUid(uid);
    }

    @Override
    public ResponseVO getResponseByRid(Long rid) {
        Response response = responseMapper.getByRid(rid);
        if (response == null) {
            throw new IllegalOperationException("RESPONSE_NOT_FOUND");
        } else if (!Objects.equals(response.getUid(), BaseContext.getCurrentId())) {
            throw new IllegalOperationException();
        } else if (!response.getFinished()) {
            throw new ResponseNotFinishedException("SUBMIT_IS_BEEN_PROCESSED");
        }

        Survey survey = surveyMapper.getBySid(response.getSid());

        List<ResponseItemVO> itemsVO = response.getItems().stream().map(item -> {
            ResponseItemVO itemVO = new ResponseItemVO();
            BeanUtils.copyProperties(item, itemVO);
            itemVO.setQuestion(questionService.getByQid(item.getQid()).toFilledQuestionVO());
            if (QuestionType.CODE.equals(itemVO.getQuestion().getType())) {
                itemVO.setJudge(judgeMapper.getJudgeBySubmitId(item.getSubmitId()));
            } else if (QuestionType.FILL_BLANK.equals(itemVO.getQuestion().getType()) || QuestionType.SINGLE_CHOICE.equals(itemVO.getQuestion().getType()) || QuestionType.MULTIPLE_CHOICE.equals(itemVO.getQuestion().getType())) {
                itemVO.setAnswer(item.getContent());
            }
            return itemVO;
        }).toList();
        return response.toVO(itemsVO, survey.getShowAnswer());
    }

    @Override
    public void deleteBySid(Long sid) {
        List<Long> rids = responseMapper.getBySid(sid).stream().map(Response::getRid).toList();
        rids.forEach(rid -> {
            responseMapper.deleteItemsByRid(rid);
            responseMapper.deleteRecordByRid(rid);
        });
    }

    @Override
    public void export(Long sid, HttpServletResponse httpServletResponse) throws IOException {
        Survey survey = surveyMapper.getBySid(sid);
        if (survey == null || survey.getState() == SurveyState.DELETE || !Objects.equals(survey.getUid(), BaseContext.getCurrentId())) {
            throw new SurveyNotFoundException("SURVEY_NOT_FOUND");
        }

        List<Question> questions = questionService.getBySid(sid).stream().sorted(Comparator.comparing(Question::getQid)).toList();
        List<Response> responses = responseMapper.getBySid(sid).stream().sorted(Comparator.comparing(Response::getUpdateTime)).toList();

        // load excel template
        // this.getClass().getClassLoader().getResourceAsStream("template/excel_template.xlsx");

        XSSFWorkbook excel = new XSSFWorkbook();
        XSSFSheet sheet = excel.createSheet("sheet1");

        // 表头
        XSSFRow header = sheet.createRow(0);
        header.createCell(0).setCellValue("序号");
        header.createCell(1).setCellValue("提交时间");
        for (int i = 0; i < questions.size(); i++) {
            header.createCell(i + 2).setCellValue(i+1 + ". " + questions.get(i).getTitle());
            header.getCell(i + 2).getCellStyle().setAlignment(HorizontalAlignment.CENTER);
        }
        // 填充各个用户的填写结果
        for (int i = 0; i < responses.size(); i++) {
            XSSFRow row = sheet.createRow(i + 1);
            row.createCell(0).setCellValue(i + 1);
            row.createCell(1).setCellValue(responses.get(i).getUpdateTime().toString());
            responses.get(i).getItems().forEach(item -> {
                int pos = findQuestionIndex(questions, item.getQid()) + 2;
                String content = String.join("┋", item.getContent());
                row.createCell(pos).setCellValue(content);
                row.getCell(pos).getCellStyle().setAlignment(HorizontalAlignment.CENTER);
            });
        }

        // 设置响应头
        httpServletResponse.setContentType("application/octet-stream;charset=utf-8");
        httpServletResponse.setHeader("Content-Disposition", "attachment; filename=a.xlsx");
        // 写入响应数据
        ServletOutputStream outputStream = httpServletResponse.getOutputStream();
        excel.write(outputStream);

        // 释放资源
        outputStream.close();
        excel.close();

        log.info("export survey {} success", sid);

    }

    private int findQuestionIndex(List<Question> questions, Long qid) {
        return questions.indexOf(questions.stream().filter(question -> question.getQid().equals(qid)).findFirst().orElseThrow());
    }
}
