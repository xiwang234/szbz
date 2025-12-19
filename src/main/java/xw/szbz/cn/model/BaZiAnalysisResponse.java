package xw.szbz.cn.model;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 八字分析响应数据（仅包含AI分析结果）
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaZiAnalysisResponse {
    
    private Object aiAnalysis;  // AI分析结果，JSON格式

    public BaZiAnalysisResponse() {
    }

    public BaZiAnalysisResponse(Object aiAnalysis) {
        this.aiAnalysis = aiAnalysis;
    }

    public Object getAiAnalysis() {
        return aiAnalysis;
    }

    public void setAiAnalysis(Object aiAnalysis) {
        this.aiAnalysis = aiAnalysis;
    }
}
