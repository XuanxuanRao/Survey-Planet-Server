package org.example.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.constant.LinkConstant;
import org.example.context.BaseContext;
import org.example.dto.email.EmailSendInvitationDTO;
import org.example.dto.survey.CreateSurveyDTO;
import org.example.dto.QuestionDTO;
import org.example.dto.survey.ShareSurveyDTO;
import org.example.entity.User;
import org.example.entity.message.InviteMessage;
import org.example.entity.message.MessageType;
import org.example.entity.survey.Survey;
import org.example.entity.question.Question;
import org.example.entity.survey.SurveyState;
import org.example.entity.survey.SurveyType;
import org.example.exception.IllegalOperationException;
import org.example.exception.SurveyNotFoundException;
import org.example.mapper.SurveyMapper;
import org.example.mapper.UserMapper;
import org.example.result.PageResult;
import org.example.service.QuestionService;
import org.example.service.ResponseService;
import org.example.service.SiteMessageService;
import org.example.service.SurveyService;
import org.example.utils.SharingCodeUtil;
import org.example.vo.survey.CreatedSurveyVO;
import org.example.vo.survey.FilledSurveyVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SurveyServiceImpl implements SurveyService {

    @Resource
    private SurveyMapper surveyMapper;

    @Resource
    private QuestionService questionService;

    @Resource
    private ResponseService responseService;

    @Resource
    private EmailServiceImpl emailService;

    @Resource
    private UserMapper userMapper;

    @Resource
    private SiteMessageService siteMessageService;

    @Override
    public Survey getSurvey(Long sid) {
        return surveyMapper.getBySid(sid);
    }

    @Override
    public PageResult<CreatedSurveyVO> getCreatedSurveys(Long uid, int pageNum, int pageSize, String sortBy) {
        PageHelper.startPage(pageNum, pageSize);
        var info = new PageInfo<>(surveyMapper.getCreatedList(uid, sortBy));
        return new PageResult<>(info.getTotal(),
                info.getList().stream()
                        .map(s -> {
                            CreatedSurveyVO vo = CreatedSurveyVO.builder()
                                    .type(s.getType().getValue())
                                    .state(s.getState().getValue())
                                    .link(LinkConstant.FILL_SURVEY + SharingCodeUtil.encrypt(s.getSid()))
                                    .build();
                            BeanUtils.copyProperties(s, vo);
                            return vo;
                        })
                        .collect(Collectors.toList())
        );
    }

    @Override
    public PageResult<FilledSurveyVO> getFilledSurveys(Long uid, int pageNum, int pageSize, String sortBy) {
        var responses = responseService.querySubmitHistory(uid);
        if (responses.isEmpty()) {
            return new PageResult<>(0, List.of());
        }
        PageHelper.startPage(pageNum, pageSize);
        var info = new PageInfo<>(surveyMapper.list(responses.keySet().stream().toList(), sortBy));
        return new PageResult<>(info.getTotal(),
                info.getList().stream()
                        .map(s -> {
                            FilledSurveyVO vo = FilledSurveyVO.builder()
                                    .type(s.getType().getValue())
                                    .state(s.getState().getValue())
                                    .rid(responses.get(s.getSid()).getRid())
                                    .submitTime(responses.get(s.getSid()).getUpdateTime())
                                    .build();
                            BeanUtils.copyProperties(s, vo);
                            return vo;
                        })
                        .collect(Collectors.toList())
        );
    }

    @Override
    public Survey addSurvey(CreateSurveyDTO surveyDTO) {
        Survey survey = new Survey();
        BeanUtils.copyProperties(surveyDTO, survey);
        survey.setUid(BaseContext.getCurrentId());
        survey.setType(SurveyType.fromString(surveyDTO.getType()));
        surveyMapper.insert(survey);

        return survey;
    }

    @Override
    public Survey addSurvey(CreateSurveyDTO surveyDTO, Long uid) {
        Survey survey = new Survey();
        BeanUtils.copyProperties(surveyDTO, survey);
        survey.setUid(uid);
        survey.setType(SurveyType.fromString(surveyDTO.getType()));
        surveyMapper.insert(survey);

        return survey;
    }

    @Override
    @Transactional
    public void updateSurvey(Long sid, CreateSurveyDTO createSurveyDTO) {
        Survey survey = getSurvey(sid);
        if (survey == null) {
            throw new SurveyNotFoundException("SURVEY_NOY_FOUND");
        } else if (survey.getOpenTime() != null) {
            throw new IllegalOperationException("CAN_NOT_MODIFY_OPENED_SURVEY");
        } else if (survey.getType() != SurveyType.fromString(createSurveyDTO.getType())) {
            throw new IllegalOperationException("CAN_NOT_MODIFY_SURVEY_TYPE");
        }

        List<Question> originalQuestions = questionService.getBySid(sid);
        List<QuestionDTO> newQuestions = createSurveyDTO.getQuestions();

        questionService.deleteQuestions(originalQuestions);
        questionService.addQuestions(newQuestions, sid);

        BeanUtils.copyProperties(createSurveyDTO, survey);
        surveyMapper.update(survey);
    }

    @Override
    public String shareSurvey(Long sid, ShareSurveyDTO shareSurveyDTO) {
        Survey survey = getSurvey(sid);
        if (survey == null || survey.getState() == SurveyState.DELETE || !Objects.equals(survey.getUid(), BaseContext.getCurrentId()))
        {
            throw new SurveyNotFoundException("SURVEY_NOT_FOUND");
        }

        String surveyLink = LinkConstant.FILL_SURVEY + SharingCodeUtil.encrypt(sid);

        if (survey.getState() == SurveyState.CLOSE) {
            // 修改问卷状态
            survey.setState(SurveyState.OPEN);
            // 设置问卷上次开放时间
            survey.setOpenTime(LocalDateTime.now());
            // 更新问卷
            surveyMapper.update(survey);
        }

        User sender = userMapper.getById(BaseContext.getCurrentId());
        if (shareSurveyDTO.getEmails() != null && !shareSurveyDTO.getEmails().isEmpty()) {
            shareSurveyDTO.getEmails().forEach(email -> {
                User receiver = userMapper.getByEmail(email);
                emailService.sendInvitation(EmailSendInvitationDTO.builder()
                                .from(sender.getUsername())
                                .to(receiver == null ? email.split("@")[0] : receiver.getUsername())
                                .surveyName(survey.getTitle())
                                .surveyType(survey.getType() == SurveyType.NORMAL ? "questionnaire" : "exam")
                                .surveyLink(surveyLink)
                                .invitationMessage(shareSurveyDTO.getInvitationMessage())
                                .email(email)
                                .build());
                if (receiver == null) {
                    return;
                }
                if (shareSurveyDTO.isNeedSiteNotification()) {
                    InviteMessage inviteMessage = InviteMessage.builder()
                            .senderId(sender.getUid())
                            .sid(sid)
                            .invitationMessage(shareSurveyDTO.getInvitationMessage())
                            .build();
                    inviteMessage.setReceiverId(receiver.getUid());
                    inviteMessage.setIsRead(false);
                    inviteMessage.setType(MessageType.INVITE);
                    siteMessageService.send(inviteMessage);
                }
            });
        }

        return surveyLink;
    }

    @Override
    public void closeSurvey(Long sid) {
        Survey survey = getSurvey(sid);
        if (survey == null || !Objects.equals(survey.getUid(), BaseContext.getCurrentId())) {
            throw new SurveyNotFoundException("SURVEY_NOT_FOUND");
        }

        survey.setState(SurveyState.CLOSE);
        surveyMapper.update(survey);
    }

    @Override
    public void modifyState(Long sid, SurveyState state) {
        Survey survey = getSurvey(sid);
        if (survey == null || !Objects.equals(survey.getUid(), BaseContext.getCurrentId())) {
            throw new SurveyNotFoundException("SURVEY_NOT_FOUND");
        }

        survey.setState(state);
        surveyMapper.update(survey);
    }

    @Override
    public void setNotificationMode(Long sid, Integer mode) {
        Survey survey = getSurvey(sid);
        if (survey == null || !Objects.equals(survey.getUid(), BaseContext.getCurrentId())) {
            throw new SurveyNotFoundException("SURVEY_NOT_FOUND");
        }

        survey.setNotificationMode(mode);
        surveyMapper.update(survey);
    }
}
