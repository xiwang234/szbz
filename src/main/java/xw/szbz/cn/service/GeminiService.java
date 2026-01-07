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

import xw.szbz.cn.exception.ServiceException;
import xw.szbz.cn.model.BaZiResult;
import xw.szbz.cn.util.PromptTemplateUtil;

/**
 * Gemini AI 分析服务
 * 使用 Google Gemini API 对八字结果进行智能分析
 */
@Service
public class GeminiService {

    private static final Logger logger = LoggerFactory.getLogger(GeminiService.class);

    private String apiKey;

    @Value("${gemini.model:gemini-2.0-flash-exp}")
    private String modelName;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final PromptTemplateUtil promptTemplateUtil;

    public GeminiService(PromptTemplateUtil promptTemplateUtil) {
        this.promptTemplateUtil = promptTemplateUtil;
    }

    /**
     * 分析八字结果（使用HTTP方式调用API，返回文本格式）
     *
     * @param baZiResult 八字计算结果
     * @return Gemini AI 的分析结果（文本格式）
     * @throws ServiceException 如果调用失败
     */
    public Object analyzeBaZi(BaZiResult baZiResult) {
        logger.info("开始分析八字，模型: {}", modelName);
        // apiKey = System.getenv("GEMINI_API_KEY");
        apiKey = "AIzaSyALsJbsPu_4O1sbJvEQZakRmWxDKgqvscE";
        if (apiKey == null || apiKey.isEmpty()) {
            logger.error("Gemini API key 未配置");
            throw new ServiceException("系统配置异常，请联系管理员", 500);
        }

        HttpURLConnection conn = null;
        long startTime = System.currentTimeMillis();

        try {
            // 构建提示词（使用模板）
            String prompt = buildBaZiPromptFromTemplate(baZiResult);
            logger.debug("提示词长度: {} 字符", prompt.length());

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
                // 读取错误信息（仅记录到日志，不暴露给用户）
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
                    modelName, prompt.length(), requestTime);

                // 抛出用户友好的异常消息
                throw new ServiceException("解读服务暂时不可用，请稍后重试", 500);
            }

        } catch (ServiceException e) {
            // 直接抛出ServiceException，不再包装
            throw e;
        } catch (Exception e) {
            long totalTime = System.currentTimeMillis() - startTime;

            // 记录完整的异常堆栈信息到日志（仅供内部查看）
            logger.error("八字分析失败，耗时: {} ms", totalTime);
            logger.error("异常类型: {}", e.getClass().getName());
            logger.error("异常信息: {}", e.getMessage());

            // 记录完整堆栈
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            logger.error("完整堆栈信息:\n{}", sw.toString());

            // 抛出用户友好的异常消息（不暴露技术细节）
            throw new ServiceException("解读服务暂时不可用，请稍后重试", 500, e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
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
        // apiKey = System.getenv("GEMINI_API_KEY");
        apiKey = "AIzaSyALsJbsPu_4O1sbJvEQZakRmWxDKgqvscE";

        if (apiKey == null || apiKey.isEmpty()) {
            logger.error("key 未配置");
            throw new ServiceException("系统配置异常，请联系管理员", 500);
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
                // 读取错误信息（仅记录到日志，不暴露给用户）
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

                // 抛出用户友好的异常消息
                throw new ServiceException("解读服务暂时不可用，请稍后重试", 500);
            }

        } catch (ServiceException e) {
            // 直接抛出ServiceException，不再包装
            throw e;
        } catch (Exception e) {
            long totalTime = System.currentTimeMillis() - startTime;

            // 记录完整的异常堆栈信息到日志（仅供内部查看）
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

            // 抛出用户友好的异常消息（不暴露技术细节）
            throw new ServiceException("解读服务暂时不可用，请稍后重试", 500, e);
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
     * 使用模板构建八字分析提示词
     * 从BaZiResult中提取数据并使用bazi_prediction_template模板
     */
    private String buildBaZiPromptFromTemplate(BaZiResult baZiResult) {
        // 提取基本信息（四柱八字）
        String basicInfo = baZiResult.getBasicInfo() != null ?
            baZiResult.getBasicInfo().toString() : "";

        // 提取大运及流年列表（到当前年份）
        String daYunStringList = baZiResult.getDaYunStringList() != null ?
            baZiResult.getDaYunStringList().toString() : "";

        // 提取完整的10个大运及流年列表
        String daYunALLStringList = baZiResult.getDaYunALLStringList() != null ?
            baZiResult.getDaYunALLStringList().toString() : "";

        // 使用模板工具渲染提示词（background使用默认值）
        return promptTemplateUtil.renderBaZiTemplate(
            basicInfo,
            daYunStringList,
            baZiResult.getBackground() != null ? baZiResult.getBackground() : "",  // background使用默认值"无特殊背景"
            daYunALLStringList
        );
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

        if (apiKey == null || apiKey.isEmpty()) {
            logger.error("Gemini API key 未配置");
            throw new ServiceException("系统配置异常，请联系管理员", 500);
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
                // 读取错误信息（仅记录到日志，不暴露给用户）
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

                // 抛出用户友好的异常消息
                throw new ServiceException("解读服务暂时不可用，请稍后重试", 500);
            }

        } catch (ServiceException e) {
            // 直接抛出ServiceException，不再包装
            throw e;
        } catch (Exception e) {
            long totalTime = System.currentTimeMillis() - startTime;

            // 记录完整的异常堆栈信息到日志（仅供内部查看）
            logger.error("流式调用 Gemini API 发生异常，耗时: {} ms", totalTime);
            logger.error("异常类型: {}", e.getClass().getName());
            logger.error("异常信息: {}", e.getMessage());

            // 记录完整堆栈
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            logger.error("完整堆栈信息:\n{}", sw.toString());

            // 抛出用户友好的异常消息（不暴露技术细节）
            throw new ServiceException("解读服务暂时不可用，请稍后重试", 500, e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}

