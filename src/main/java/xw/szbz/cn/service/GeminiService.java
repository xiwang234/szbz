package xw.szbz.cn.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;

import xw.szbz.cn.model.BaZiResult;

/**
 * Gemini AI 分析服务
 * 使用 Google Gemini API 对八字结果进行智能分析
 */
@Service
public class GeminiService {

    @Value("${yesCode.api.key:}")
    private String apiKey;

    @Value("${gemini.model:gemini-2.0-flash-exp}")
    private String modelName;

    /**
     * 分析八字结果
     *
     * @param baZiResult 八字计算结果
     * @return Gemini AI 的分析结果
     * @throws IllegalStateException 如果 API key 未配置
     * @throws RuntimeException      如果调用 Gemini API 失败
     */
    public String analyzeBaZi(BaZiResult baZiResult) {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("Gemini API key 未配置。请在 application.properties 中设置 gemini.api.key");
        }

        try {
            // 初始化 Gemini 客户端
            Client client = Client.builder()
                    .apiKey(apiKey)
                    .build();

            // 构建提示词
            String prompt = buildPrompt(baZiResult);

            // 调用 Gemini API
            GenerateContentResponse response = client.models.generateContent(
                    modelName,
                    prompt,
                    null);

            // 返回生成的文本
            return response.text();

        } catch (Exception e) {
            throw new RuntimeException("调用 Gemini API 失败: " + e.getMessage(), e);
        }
    }

    /**
     * 构建发送给 Gemini 的提示词
     */
    private String buildPrompt(BaZiResult baZiResult) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请作为一位专业的命理大师，结合知识库的内容规则和案例分析逻辑，详细分析以下八字排盘结果：\n\n");
        prompt.append("完整八字：").append(baZiResult.getBasicInfo().toString()).append("\n\n");
        prompt.append("截止今年的大运和流年：").append(baZiResult.getDaYunStringList().toString()).append("\n\n");
        prompt.append("请从以下几个方面进行详细分析：\n");
        prompt.append("1. 格局分析：格局情况\n");
        prompt.append("2. 学历情况：分析学历情况\n");
        prompt.append("3. 用神喜忌：确定该八字的用神和忌神\n");
        prompt.append("4. 性格特点：根据八字推断性格特征\n");
        prompt.append("5. 事业财运：分析事业和财运方面的特点\n");
        prompt.append("6. 健康建议：给出健康方面的注意事项\n");
        prompt.append("7. 职业情况：可能从事的职业\n");
        prompt.append("8. 综合评价：给出总体的命理评价\n\n");
        prompt.append("请用专业、通俗易懂的语言进行分析，字数控制在800字以内。");
        System.out.println("$$$$$$$$$$$$$$$$$$" + prompt.toString());
        return prompt.toString();
    }
}
