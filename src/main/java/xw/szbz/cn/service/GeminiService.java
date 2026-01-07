package xw.szbz.cn.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;

import xw.szbz.cn.model.BaZiResult;

/**
 * Gemini AI 分析服务
 * 使用 Google Gemini API 对八字结果进行智能分析
 */
@Service
public class GeminiService {

    private static final Logger logger = LoggerFactory.getLogger(GeminiService.class);

    // @Value("${gemini.api.key:}")
<<<<<<< HEAD
    private String apiKey;
=======
    private String apiKey = "";
>>>>>>> 3fc5ab0c2ea3b16690d40f4c660e176304b4428b

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
        logger.info("开始分析八字，模型: {}", modelName);
        apiKey = System.getenv("GEMINI_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            logger.error("Gemini API key 未配置");
            throw new IllegalStateException("Gemini API key 未配置。请在 application.properties 中设置 gemini.api.key");
        }
        
        long startTime = System.currentTimeMillis();
        
        try {
            // 初始化 Gemini 客户端
            logger.debug("正在初始化 Gemini 客户端...");
            Client client = Client.builder()
                    .apiKey(apiKey)
                    .build();

            // 构建提示词（要求返回JSON格式）
            String prompt = buildPromptWithJsonFormat(baZiResult);
            logger.debug("提示词长度: {} 字符", prompt.length());

            // 调用 Gemini API
            logger.info("正在调用 Gemini API 进行八字分析...");
            GenerateContentResponse response = client.models.generateContent(
                    modelName,
                    prompt,
                    null);

            long requestTime = System.currentTimeMillis() - startTime;
            logger.info("Gemini API 调用完成，耗时: {} ms", requestTime);

            // 获取生成的文本
            String responseText = response.text();
            logger.debug("响应文本长度: {} 字符", responseText != null ? responseText.length() : 0);
            
            // 尝试解析为JSON对象
            try {
                // 提取JSON部分（如果响应中包含markdown代码块）
                String jsonText = extractJson(responseText);
                Object result = objectMapper.readValue(jsonText, Object.class);
                logger.info("八字分析成功，返回 JSON 格式结果");
                return result;
            } catch (Exception e) {
                // 如果解析失败，返回原始文本
                logger.warn("无法将响应解析为JSON，返回原始文本: {}", e.getMessage());
                return responseText;
            }

        } catch (Exception e) {
            long totalTime = System.currentTimeMillis() - startTime;
            
            // 记录完整的异常堆栈信息
            logger.error("八字分析失败，耗时: {} ms", totalTime);
            logger.error("异常类型: {}", e.getClass().getName());
            logger.error("异常信息: {}", e.getMessage());
            
            // 记录完整堆栈
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            logger.error("完整堆栈信息:\n{}", sw.toString());
            
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
        logger.info("开始调用 Gemini API，模型: {}, 提示词长度: {}", modelName, prompt != null ? prompt.length() : 0);
<<<<<<< HEAD
        // apiKey = System.getenv("GEMINI_API_KEY");
        apiKey = "AIzaSyBWNljJuzOdpP-gIURgQeegd4451knecaw";
=======
        apiKey = System.getenv("GEMINI_API_KEY");
>>>>>>> 3fc5ab0c2ea3b16690d40f4c660e176304b4428b
        if (apiKey == null || apiKey.isEmpty()) {
            logger.error("key 未配置");
            throw new IllegalStateException("key 未配置。请设置key");
        }

        HttpURLConnection conn = null;
        long startTime = System.currentTimeMillis();
        
        try {
            // 构建请求 URL
            String urlString = String.format(
                "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s",
                modelName, apiKey
            );
            
            logger.debug("请求 URL: {}", urlString.replace(apiKey, "***"));
            
            URL url = new URL(urlString);
            conn = (HttpURLConnection) url.openConnection();
            
            // 设置请求方法和头部
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);
            conn.setConnectTimeout(9000000);
            conn.setReadTimeout(9000000);

            // 构建请求体
            String jsonInputString = String.format(
                "{\"contents\": [{\"parts\": [{\"text\": \"%s\"}]}]}",
                escapeJson(prompt)
            );
            
            logger.debug("请求体长度: {} bytes", jsonInputString.getBytes(StandardCharsets.UTF_8).length);
            
            // 发送请求
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // 获取响应
            int responseCode = conn.getResponseCode();
            long requestTime = System.currentTimeMillis() - startTime;
            
            logger.info("Gemini API 响应码: {}, 耗时: {} ms", responseCode, requestTime);
            
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
                logger.debug("响应内容长度: {} bytes", responseBody.length());
                
                String result = extractTextFromHttpResponse(responseBody);
                logger.info("Gemini API 调用成功，返回文本长度: {}", result != null ? result.length() : 0);
                
                return result;

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
                
                String errorMsg = "Gemini API 返回错误码 " + responseCode + ": " + errorResponse.toString();
                logger.error("Gemini API 调用失败: {}", errorMsg);
                logger.error("请求详情 - 模型: {}, 提示词长度: {}, 耗时: {} ms", 
                    modelName, prompt != null ? prompt.length() : 0, requestTime);
                
                throw new RuntimeException(errorMsg);
            }

        } catch (Exception e) {
            long totalTime = System.currentTimeMillis() - startTime;
            
            // 记录完整的异常堆栈信息
            logger.error("调用 Gemini API 发生异常，耗时: {} ms", totalTime);
            logger.error("异常类型: {}", e.getClass().getName());
            logger.error("异常信息: {}", e.getMessage());
            logger.error("请求参数 - 模型: {}, 提示词长度: {}", 
                modelName, prompt != null ? prompt.length() : 0);
            
            // 记录完整堆栈
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            logger.error("完整堆栈信息:\n{}", sw.toString());
            
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
                        logger.debug("成功从响应中提取文本内容");
                        return text;
                    }
                }
            }
            
            // 如果无法解析，返回原始响应
            logger.warn("无法从标准结构中提取文本，返回原始响应");
            return jsonResponse;
        } catch (Exception e) {
            logger.error("解析 Gemini 响应失败: {}", e.getMessage(), e);
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

<<<<<<< HEAD
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

    /**
     * 流式生成内容（使用 HTTP 直接调用流式 API）
     * 
     * @param prompt 提示词
     * @param chunkCallback 文本片段回调函数
     * @throws IllegalStateException 如果 API key 未配置
     * @throws RuntimeException 如果调用 API 失败
     */
    public void generateContentStream(String prompt, java.util.function.Consumer<String> chunkCallback) {
        logger.info("开始流式调用 Gemini API，模型: {}, 提示词长度: {}", modelName, prompt != null ? prompt.length() : 0);
        apiKey = "AIzaSyBWNljJuzOdpP-gIURgQeegd4451knecaw";
        
        if (apiKey == null || apiKey.isEmpty()) {
            logger.error("Gemini API key 未配置");
            throw new IllegalStateException("Gemini API key 未配置。请在 application.properties 中设置 gemini.api.key");
        }

        HttpURLConnection conn = null;
        long startTime = System.currentTimeMillis();
        
        try {
            // 构建请求 URL（使用流式端点）
            String urlString = String.format(
                "https://generativelanguage.googleapis.com/v1beta/models/%s:streamGenerateContent?key=%s&alt=sse",
                modelName, apiKey
            );
            
            logger.debug("流式请求 URL: {}", urlString.replace(apiKey, "***"));
            
            URL url = new URL(urlString);
            conn = (HttpURLConnection) url.openConnection();
            
            // 设置请求方法和头部
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("Accept", "text/event-stream");
            conn.setDoOutput(true);
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(180000); // 3分钟读取超时

            // 构建请求体
            String jsonInputString = String.format(
                "{\"contents\": [{\"parts\": [{\"text\": \"%s\"}]}]}",
                escapeJson(prompt)
            );
            
            logger.debug("请求体长度: {} bytes", jsonInputString.getBytes(StandardCharsets.UTF_8).length);
            
            // 发送请求
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // 获取响应码
            int responseCode = conn.getResponseCode();
            logger.info("Gemini 流式 API 响应码: {}", responseCode);
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                logger.info("开始接收流式数据...");
                
                // 读取流式响应（SSE 格式）
                try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    
                    String line;
                    StringBuilder eventData = new StringBuilder();
                    int chunkCount = 0;
                    
                    while ((line = reader.readLine()) != null) {
                        // SSE 格式：data: {...}
                        if (line.startsWith("data: ")) {
                            String jsonData = line.substring(6).trim();
                            
                            // 跳过空数据
                            if (jsonData.isEmpty() || jsonData.equals("[DONE]")) {
                                continue;
                            }
                            
                            try {
                                // 解析 JSON 提取文本
                                JsonNode root = objectMapper.readTree(jsonData);
                                JsonNode candidates = root.path("candidates");
                                
                                if (candidates.isArray() && candidates.size() > 0) {
                                    JsonNode content = candidates.get(0).path("content");
                                    JsonNode parts = content.path("parts");
                                    
                                    if (parts.isArray() && parts.size() > 0) {
                                        String chunk = parts.get(0).path("text").asText();
                                        if (chunk != null && !chunk.isEmpty()) {
                                            chunkCount++;
                                            logger.debug("收到第 {} 个流式片段: {} 字符", chunkCount, chunk.length());
                                            chunkCallback.accept(chunk);
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                logger.warn("解析流式数据片段失败: {}", e.getMessage());
                            }
                        }
                    }
                    
                    long totalTime = System.currentTimeMillis() - startTime;
                    logger.info("流式生成完成，共接收 {} 个片段，总耗时: {} ms", chunkCount, totalTime);
                }

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
                
                String errorMsg = "Gemini 流式 API 返回错误码 " + responseCode + ": " + errorResponse.toString();
                logger.error("流式调用失败: {}", errorMsg);
                throw new RuntimeException(errorMsg);
            }

        } catch (Exception e) {
            long totalTime = System.currentTimeMillis() - startTime;
            
            // 记录完整的异常堆栈信息
            logger.error("流式调用 Gemini API 发生异常，耗时: {} ms", totalTime);
            logger.error("异常类型: {}", e.getClass().getName());
            logger.error("异常信息: {}", e.getMessage());
            
            // 记录完整堆栈
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            logger.error("完整堆栈信息:\n{}", sw.toString());
            
            throw new RuntimeException("调用 Gemini API 失败: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
=======
>>>>>>> 3fc5ab0c2ea3b16690d40f4c660e176304b4428b
}

