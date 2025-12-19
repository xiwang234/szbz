package xw.szbz.cn.service;

import com.fasterxml.jackson.databind.ObjectMapper;
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

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 分析八字结果（返回JSON格式）
     *
     * @param baZiResult 八字计算结果
     * @return Gemini AI 的分析结果（JSON格式）
     * @throws IllegalStateException 如果 API key 未配置
     * @throws RuntimeException      如果调用 Gemini API 失败
     */
    public Object analyzeBaZi(BaZiResult baZiResult) {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("Gemini API key 未配置。请在 application.properties 中设置 gemini.api.key");
        }

        try {
            // 初始化 Gemini 客户端
            // Client client = Client.builder()
            //         .apiKey(apiKey)
            //         .build();

            // 构建提示词（要求返回JSON格式）
            // String prompt = buildPromptWithJsonFormat(baZiResult);

            // 调用 Gemini API
            // GenerateContentResponse response = client.models.generateContent(
            //         modelName,
            //         prompt,
            //         null);

            // 获取生成的文本
            // String responseText = response.text();
            String responseText = "test api";
            // 尝试解析为JSON对象
            try {
                // 提取JSON部分（如果响应中包含markdown代码块）
                String jsonText = extractJson(responseText);
                return objectMapper.readValue(jsonText, Object.class);
            } catch (Exception e) {
                // 如果解析失败，返回原始文本
                System.err.println("无法将响应解析为JSON，返回原始文本: " + e.getMessage());
                return responseText;
            }

        } catch (Exception e) {
            throw new RuntimeException("调用 Gemini API 失败: " + e.getMessage(), e);
        }
    }

    /**
     * 提取JSON内容（去除markdown代码块标记）
     */
    private String extractJson(String text) {
        // 去除可能的markdown代码块标记
        text = text.trim();
        if (text.startsWith("```json")) {
            text = text.substring(7);
        } else if (text.startsWith("```")) {
            text = text.substring(3);
        }
        if (text.endsWith("```")) {
            text = text.substring(0, text.length() - 3);
        }
        return text.trim();
    }

    /**
     * 构建发送给 Gemini 的提示词（要求返回JSON格式）
     */
    private String buildPromptWithJsonFormat(BaZiResult baZiResult) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请作为一位专业的命理大师，结合知识库的内容规则和案例分析逻辑，详细分析以下八字排盘结果：\n\n");
        prompt.append("完整八字：").append(baZiResult.getBasicInfo().toString()).append("\n\n");
        prompt.append("截止今年的大运和流年：").append(baZiResult.getDaYunStringList().toString()).append("\n\n");
        prompt.append("请从以下几个方面进行详细分析，并**严格按照JSON格式返回**：\n");
        prompt.append("1. 格局分析：格局情况\n");
        prompt.append("2. 学历情况：分析学历情况\n");
        prompt.append("3. 用神喜忌：确定该八字的用神和忌神\n");
        prompt.append("4. 性格特点：根据八字推断性格特征\n");
        prompt.append("5. 事业财运：分析事业和财运方面的特点\n");
        prompt.append("6. 健康建议：给出健康方面的注意事项\n");
        prompt.append("7. 职业情况：可能从事的职业\n");
        prompt.append("8. 综合评价：给出总体的命理评价\n\n");
        prompt.append("请用专业、通俗易懂的语言进行分析，字数控制在800字以内。\n\n");
        prompt.append("**重要：请严格按照以下JSON格式返回，不要包含任何其他文字说明：**\n");
        prompt.append("{\n");
        prompt.append("  \"格局分析\": \"...\",\n");
        prompt.append("  \"学历情况\": \"...\",\n");
        prompt.append("  \"用神喜忌\": \"...\",\n");
        prompt.append("  \"性格特点\": \"...\",\n");
        prompt.append("  \"事业财运\": \"...\",\n");
        prompt.append("  \"健康建议\": \"...\",\n");
        prompt.append("  \"职业情况\": \"...\",\n");
        prompt.append("  \"综合评价\": \"...\"\n");
        prompt.append("}");
        
        System.out.println("$$$$$$$$$$$$$$$$$$" + prompt.toString());
        return prompt.toString();
    }

    /**
     * 构建发送给 Gemini 的提示词（原有方法，保持向后兼容）
     */
    @Deprecated
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
