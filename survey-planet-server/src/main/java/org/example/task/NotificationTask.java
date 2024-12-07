package org.example.task;


import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.example.constant.LinkConstant;
import org.example.constant.NotificationModeConstant;
import org.example.dto.email.EmailNotifyNewSubmissionDTO;
import org.example.entity.User;
import org.example.entity.response.Response;
import org.example.entity.survey.Survey;
import org.example.service.*;
import org.example.utils.SharingCodeUtil;
import org.example.vo.NewSubmissionVO;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author chenxuanrao06@gmail.com
 * @Description:
 */
@Slf4j
@Component
public class NotificationTask {

    @Resource
    private EmailService emailService;

    @Resource
    private SurveyService surveyService;

    @Resource
    private ResponseService responseService;

    @Resource
    private UserService userService;

    @Resource
    private SiteMessageService siteMessageService;

    @Scheduled(cron = "0 0 0/12 * * ?")
    public void notifyForNewSubmission() {
        // 查询 12h 内的提交
        List<Response> responses = responseService.getRecentResponse(12 * 60);

        Map<Survey, Pair<Integer, LocalDateTime>> responseInfo = responses.stream()
                .map(response -> {
                    Survey survey = surveyService.getSurvey(response.getSid());
                    if (survey == null || (survey.getNotificationMode() & NotificationModeConstant.EMAIL) == 0) {
                        return null;
                    }
                    return new AbstractMap.SimpleEntry<>(survey, Pair.of(1, response.getCreateTime()));
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> Pair.of(oldValue.getKey() + 1,
                                oldValue.getValue().isAfter(newValue.getValue()) ? oldValue.getValue() : newValue.getValue())
                ));


        // uid -> List<NewSubmissionVO>
        HashMap<Long, EmailNotifyNewSubmissionDTO> emailNotifyNewSubmissionDTOs = new HashMap<>();
        for (Map.Entry<Survey, Pair<Integer, LocalDateTime>> entry : responseInfo.entrySet()) {
            User user = userService.getById(entry.getKey().getUid());

            if (Objects.isNull(user) || Objects.isNull(user.getEmail())) {
                log.error("user not found, uid: {}", entry.getKey().getUid());
                continue;
            }

            NewSubmissionVO newSubmissionVO = NewSubmissionVO.builder()
                    .surveyName(entry.getKey().getTitle())
                    .surveyLink(LinkConstant.FILL_SURVEY + SharingCodeUtil.encrypt(entry.getKey().getSid()))
                    .queryLink(LinkConstant.ANALYSIS_SURVEY + entry.getKey().getSid())
                    .newSubmissionNum(entry.getValue().getLeft())
                    .latestSubmissionTime(entry.getValue().getRight())
                    .build();

            if (emailNotifyNewSubmissionDTOs.containsKey(user.getUid())) {
                var emailNotifyNewSubmissionDTO = emailNotifyNewSubmissionDTOs.get(user.getUid());
                emailNotifyNewSubmissionDTO.getNewSubmissionVOs().add(newSubmissionVO);
            } else {
                List<NewSubmissionVO> newSubmissionVOs = new ArrayList<>();
                newSubmissionVOs.add(newSubmissionVO);
                emailNotifyNewSubmissionDTOs.put(user.getUid(),
                        EmailNotifyNewSubmissionDTO.builder()
                                .email(user.getEmail())
                                .username(user.getUsername())
                                .newSubmissionVOs(newSubmissionVOs)
                                .build()
                );
            }
        }

        emailNotifyNewSubmissionDTOs.values().forEach(dto -> emailService.sendNotificationForNewSubmission(dto));

        log.info("notify for new submission success : {}", emailNotifyNewSubmissionDTOs.values());
    }

    /**
     *
     */
    @Scheduled(cron = "0 0 0/24 * * ?")
    public void clearMessage() {
        Integer count = siteMessageService.deleteReadMessageOlderThan(7, null);
        if (count > 0) {
            log.info("delete {} read messages older than 7 days", count);
        }
    }

}
