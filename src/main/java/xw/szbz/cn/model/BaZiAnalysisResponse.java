package xw.szbz.cn.model;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 八字分析响应数据
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaZiAnalysisResponse {
    
    private BaZiResult baziResult;
    private Object aiAnalysis;  // 改为Object类型，可以是String或JSON对象

    public BaZiAnalysisResponse() {
    }

    public BaZiAnalysisResponse(BaZiResult baziResult, Object aiAnalysis) {
        this.baziResult = baziResult;
        this.aiAnalysis = aiAnalysis;
    }

    public BaZiResult getBaziResult() {
        return baziResult;
    }

    public void setBaziResult(BaZiResult baziResult) {
        this.baziResult = baziResult;
    }

    public Object getAiAnalysis() {
        return aiAnalysis;
    }

    public void setAiAnalysis(Object aiAnalysis) {
        this.aiAnalysis = aiAnalysis;
    }
}
