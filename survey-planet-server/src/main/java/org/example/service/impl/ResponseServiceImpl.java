package org.example.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.Result.PageResult;
import org.example.context.BaseContext;
import org.example.dto.ResponseDTO;
import org.example.dto.ResponsePageQueryDTO;
import org.example.entity.question.*;
import org.example.entity.response.ResponseItem;
import org.example.entity.survey.Survey;
import org.example.entity.response.Response;
import org.example.entity.survey.SurveyState;
import org.example.entity.survey.SurveyType;
import org.example.exception.*;
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
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
            scoringService.calcScore(response);
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
    public Map<Long, Response> querySubmitHistory(Long uid) {
        return responseMapper.getSidByUid(uid).stream().collect(Collectors.toMap(Response::getSid, response -> response));
    }

    @Override
    public ResponseVO getResponseByRid(Long rid) {
        Response response = responseMapper.getByRid(rid);
        if (response == null) {
            throw new IllegalOperationException("RESPONSE_NOT_FOUND");
        }

        Survey survey = surveyMapper.getBySid(response.getSid());
        if (!Objects.equals(response.getUid(), BaseContext.getCurrentId()) && !Objects.equals(survey.getUid(), BaseContext.getCurrentId())) {
            throw new IllegalOperationException();
        } else if (!response.getFinished()) {
            throw new ResponseNotFinishedException("SUBMIT_IS_BEEN_PROCESSED");
        }

        List<ResponseItemVO> itemsVO = response.getItems().stream().map(item -> {
            ResponseItemVO itemVO = new ResponseItemVO();
            BeanUtils.copyProperties(item, itemVO);
            var question = questionService.getByQid(item.getQid());
            itemVO.setQuestion(question.toFilledQuestionVO());
            if (QuestionType.CODE.equals(itemVO.getQuestion().getType())) {
                itemVO.setJudge(judgeMapper.getJudgeBySubmitId(item.getSubmitId()));
            } else if (QuestionType.FILL_BLANK.equals(itemVO.getQuestion().getType())) {
                itemVO.setAnswer(((FillBlankQuestion) question).getAnswer());
            } else if (QuestionType.SINGLE_CHOICE.equals(itemVO.getQuestion().getType())) {
                itemVO.setAnswer(((SingleChoiceQuestion) question).getAnswer());
            } else if (QuestionType.MULTIPLE_CHOICE.equals(itemVO.getQuestion().getType())) {
                itemVO.setAnswer(((MultipleChoiceQuestion) question).getAnswer());
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
    public PageResult<Response> pageQuery(ResponsePageQueryDTO responsePageQueryDTO) {
        PageHelper.startPage(responsePageQueryDTO.getPageNum(), responsePageQueryDTO.getPageSize());
        Page<Response> responses = responseMapper.pageQuery(
                responsePageQueryDTO.getSid(),
                responsePageQueryDTO.getGradeLb(),
                responsePageQueryDTO.getGradeUb(),
                responsePageQueryDTO.getQueryMap(),
                responsePageQueryDTO.getQueryMap() == null ? 0 : responsePageQueryDTO.getQueryMap().size()
        );
        return new PageResult<>(responses.getTotal(), responses.getResult());
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
                final int pos = findQuestionIndex(questions, item.getQid()) + 2;
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

    @Override
    public List<ResponseItem> getResponseItemsByQid(Long qid) {
        return responseMapper.getByQid(qid);
    }

    @Override
    @Transactional
    public void updateResponse(Long rid, List<ResponseItem> changedItems) {
        Response response = responseMapper.getByRid(rid);
        if (response == null) {
            throw new ResponseNotFoundException("RESPONSE_NOT_FOUND");
        }
        Survey survey = surveyMapper.getBySid(response.getSid());
        if (survey == null || survey.getState() == SurveyState.DELETE) {
            throw new SurveyNotFoundException("SURVEY_NOT_FOUND");
        } else if (!Objects.equals(survey.getUid(), BaseContext.getCurrentId()) && !Objects.equals(response.getUid(), BaseContext.getCurrentId())) {
            throw new IllegalOperationException("NO_PERMISSION_TO_MODIFY");
        }

        if (survey.getType() == SurveyType.EXAM) {
            response.setFinished(false);
        }

        changedItems.forEach(item -> {
            if (response.getItems().stream().filter(i -> i.getQid().equals(item.getQid())).findFirst().isEmpty()) {
                throw new QuestionNotFoundException("QUESTION_NOT_FOUND");
            }
            item.setRid(rid);
            item.setUpdateTime(LocalDateTime.now());
        });

        responseMapper.updateResponse(response);
        responseMapper.updateItems(changedItems);

        if (survey.getType() == SurveyType.EXAM) {
            scoringService.reCalcScore(response, changedItems);
        }
    }

    @Override
    public Response getResponseBySubmitId(Long submitId) {
        return responseMapper.getBySubmitId(submitId);
    }

    private int findQuestionIndex(List<Question> questions, Long qid) {
        return questions.indexOf(questions.stream().filter(question -> question.getQid().equals(qid)).findFirst().orElseThrow());
    }
}
