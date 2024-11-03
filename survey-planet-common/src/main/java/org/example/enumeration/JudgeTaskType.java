package org.example.enumeration;

public enum JudgeTaskType {
    /**
     * 用户提交问卷时的判分任务
     */
    USER_SUBMIT,
    /**
     * 问卷创建者发起的重测任务
     */
    REJUDGE,
    /**
     * 用户测试任务
     */
    USER_TEST;
}
