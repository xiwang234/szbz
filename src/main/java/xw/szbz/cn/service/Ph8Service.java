package xw.szbz.cn.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.alibaba.dashscope.utils.JsonUtils;

import xw.szbz.cn.model.BaZiResult;

/**
 * Ph8 AI 分析服务
 * 使用 ph8.co API 对八字结果进行智能分析
 */
@Service
public class Ph8Service {

    @Value("${ph8.api.key:}")
    private String apiKey;

    @Value("${ph8.api.url:https://ph8.co/openai/v1/chat/completions}")
    private String apiUrl;

    @Value("${ph8.model:gemini-3-pro-preview:share}")
    private String modelName;

    @Value("${ph8.max.tokens:1024}")
    private int maxTokens;

    @Value("${ph8.temperature:0.0}")
    private double temperature;

    private final RestTemplate restTemplate;

    public Ph8Service() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * 分析八字结果
     *
     * @param baZiResult 八字计算结果
     * @return Ph8 AI 的分析结果
     * @throws IllegalStateException 如果 API key 未配置
     * @throws RuntimeException      如果调用 Ph8 API 失败
     */
    public String analyzeBaZi(BaZiResult baZiResult) {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("Ph8 API key 未配置。请在 application.properties 中设置 ph8.api.key");
        }

        try {
            // 构建提示词
            String prompt = buildPrompt(baZiResult);

            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", modelName);
            requestBody.put("messages", List.of(
                    Map.of("role", "user", "content", prompt)));
            requestBody.put("max_tokens", maxTokens);
            requestBody.put("temperature", temperature);
            requestBody.put("stream", false); // 使用非流式响应

            // 构建请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            // 发送请求
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            @SuppressWarnings("rawtypes")
            ResponseEntity<Map> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    entity,
                    Map.class);
            System.out.println(apiUrl);
            System.out.println(JsonUtils.toJson(response.getBody()));
            // 解析响应
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> responseBody = (Map<String, Object>) response.getBody();

                Object choicesObj = responseBody.get("choices");
                if (choicesObj instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> choices = (List<Map<String, Object>>) choicesObj;
                    if (!choices.isEmpty()) {
                        Map<String, Object> firstChoice = choices.get(0);
                        Object messageObj = firstChoice.get("message");
                        if (messageObj instanceof Map) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> message = (Map<String, Object>) messageObj;
                            return (String) message.get("content");
                        }
                    }
                }
                throw new RuntimeException("无法从响应中解析内容");
            } else {
                throw new RuntimeException("API 请求失败: " + response.getStatusCode());
            }

        } catch (Exception e) {
            throw new RuntimeException("调用 Ph8 API 失败: " + e.getMessage(), e);
        }
    }

    /**
     * 构建发送给 Ph8 的提示词
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
        System.out.println("========== Ph8 API Request Prompt ==========");
        System.out.println(prompt.toString());
        System.out.println("==========================================");
        return prompt.toString();
    }
}
