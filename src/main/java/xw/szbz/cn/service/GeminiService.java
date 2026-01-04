package xw.szbz.cn.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;

import xw.szbz.cn.model.BaZiResult;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Gemini AI 分析服务
 * 使用 Google Gemini API 对八字结果进行智能分析
 */
@Service
public class GeminiService {

    @Value("${gemini.api.key:}")
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
        apiKey = "AIzaSyA9aKhNqwaYN0bsDqzqi9cmHL84WpM-xX8";
        System.out.println("GeminiService.apiKey: " + apiKey);
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("Gemini API key 未配置。请在 application.properties 中设置 gemini.api.key");
        }
        
        try {
            // 初始化 Gemini 客户端
            Client client = Client.builder()
                    .apiKey(apiKey)
                    .build();

            // 构建提示词（要求返回JSON格式）
            String prompt = buildPromptWithJsonFormat(baZiResult);

            // 调用 Gemini API
            GenerateContentResponse response = client.models.generateContent(
                    modelName,
                    prompt,
                    null);

            // 获取生成的文本
            String responseText = response.text();
            // String responseText = "test api";
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
     * 通用文本生成方法（用于六壬预测等场景）- 使用 HTTP 直接调用
     *
     * @param prompt 提示词
     * @return Gemini AI 生成的文本
     * @throws IllegalStateException 如果 API key 未配置
     * @throws RuntimeException      如果调用 Gemini API 失败
     */
    public String generateContent(String prompt) {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("Gemini API key 未配置。请在 application.properties 中设置 gemini.api.key");
        }

        HttpURLConnection conn = null;
        try {
            // 构建请求 URL
            String urlString = String.format(
                "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s",
                modelName, apiKey
            );
            
            URL url = new URL(urlString);
            conn = (HttpURLConnection) url.openConnection();
            
            // 设置请求方法和头部
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);

            // 构建请求体
            String jsonInputString = String.format(
                "{\"contents\": [{\"parts\": [{\"text\": \"%s\"}]}]}",
                escapeJson(prompt)
            );
            
            // 发送请求
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // 获取响应
            int responseCode = conn.getResponseCode();
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // 读取响应内容
                StringBuilder response = new StringBuilder();
                try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                }

                // 解析响应 JSON，提取文本内容
                String responseBody = response.toString();
                return extractTextFromHttpResponse(responseBody);

            } else {
                // 读取错误信息
                StringBuilder errorResponse = new StringBuilder();
                try (BufferedReader errorReader = new BufferedReader(
                    new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                    String inputLine;
                    while ((inputLine = errorReader.readLine()) != null) {
                        errorResponse.append(inputLine);
                    }
                }
                throw new RuntimeException("Gemini API 返回错误码 " + responseCode + ": " + errorResponse.toString());
            }

        } catch (Exception e) {
            throw new RuntimeException("调用 Gemini API 失败: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    /**
     * 从 HTTP 响应 JSON 中提取文本内容
     */
    private String extractTextFromHttpResponse(String jsonResponse) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode candidates = root.path("candidates");
            
            if (candidates.isArray() && candidates.size() > 0) {
                JsonNode content = candidates.get(0).path("content");
                JsonNode parts = content.path("parts");
                
                if (parts.isArray() && parts.size() > 0) {
                    String text = parts.get(0).path("text").asText();
                    if (text != null && !text.isEmpty()) {
                        return text;
                    }
                }
            }
            
            // 如果无法解析，返回原始响应
            return jsonResponse;
        } catch (Exception e) {
            System.err.println("解析 Gemini 响应失败: " + e.getMessage());
            return jsonResponse;
        }
    }

    /**
     * 转义 JSON 字符串中的特殊字符
     */
    private String escapeJson(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
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
