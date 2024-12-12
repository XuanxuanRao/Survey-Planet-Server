package org.example.constant;

/**
 * @author chenxuanrao06@gmail.com
 * @Description: 跳转链接常量
 */
public class LinkConstant {
    private static final String BASE_URL = "http://59.110.163.198/";
    /**
     * 填写问卷页面
     */
    public static final String FILL_SURVEY = BASE_URL + "fill/";
    /**
     * 答卷统计分析页面
     */
    public static final String ANALYSIS_SURVEY = BASE_URL + "questionnaire/lookQuestionnaire?id=";
    /**
     * 查看答卷结果页面
     */
    public static final String VIEW_SUBMIT = BASE_URL + "viewResult?rid=";
}
