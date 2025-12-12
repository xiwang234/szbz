package xw.szbz.cn.service;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.openai.client.OpenAIClientAsync;
import com.openai.client.okhttp.OpenAIOkHttpClientAsync;
import com.openai.models.chat.completions.ChatCompletionCreateParams;

import jakarta.annotation.PostConstruct;
import xw.szbz.cn.model.BaZiResult;

/**
 * 阿里云通义千问服务（异步版本）
 * 使用 OpenAI 兼容接口异步调用 DashScope API 对八字结果进行智能分析
 */
@Service
public class QwenService {

    @Value("${qwen.api.key:}")
    private String apiKey;

    @Value("${qwen.model:qwen-turbo}")
    private String modelName;

    @Value("${qwen.base.url:https://dashscope.aliyuncs.com/compatible-mode/v1}")
    private String baseUrl;

    private OpenAIClientAsync client;

    /**
     * 初始化 OpenAI 客户端
     */
    @PostConstruct
    public void init() {
        if (apiKey != null && !apiKey.isEmpty()) {
            client = OpenAIOkHttpClientAsync.builder()
                    .apiKey(apiKey)
                    .baseUrl(baseUrl)
                    .build();
        }
    }

    /**
     * 异步分析八字结果
     *
     * @param baZiResult 八字计算结果
     * @return CompletableFuture 包含通义千问的分析结果
     * @throws IllegalStateException 如果 API key 未配置
     */
    public CompletableFuture<String> analyzeBaZi(BaZiResult baZiResult) {
        if (apiKey == null || apiKey.isEmpty()) {
            return CompletableFuture.failedFuture(
                    new IllegalStateException("千问 API key 未配置。请在 application.properties 中设置 qwen.api.key"));
        }

        if (client == null) {
            return CompletableFuture.failedFuture(
                    new IllegalStateException("千问客户端未初始化"));
        }

        // 构建提示词
        String userPrompt = buildPrompt(baZiResult);
        String SystemPrompt = buildSystemPrompt();
        // 创建 ChatCompletion 参数
        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model(modelName)
                .addSystemMessage(SystemPrompt)
                .addUserMessage(userPrompt)
                .build();

        // 发送异步请求并处理响应
        return client.chat().completions().create(params)
                .thenApply(chatCompletion -> {
                    if (chatCompletion.choices() != null && !chatCompletion.choices().isEmpty()) {
                        return chatCompletion.choices().get(0).message().content().orElse("无响应内容");
                    } else {
                        throw new RuntimeException("千问 API 返回结果为空");
                    }
                })
                .exceptionally(e -> {
                    throw new RuntimeException("调用千问 API 失败: " + e.getMessage(), e);
                });
    }

    /**
     * 构建发送给千问的提示词
     */
    private String buildPrompt(BaZiResult baZiResult) {
        StringBuilder prompt = new StringBuilder();
        // prompt.append("完整八字：").append(baZiResult.getBasicInfo().toString()).append("\n\n");
        prompt.append("完整八字：").append(
                "{\"gender\":\"女\",\"yearPillar\":\"辛巳\",\"monthPillar\":\"甲午\",\"dayPillar\":\"己未\",\"hourPillar\":\"己巳\"}");
        prompt.append("截止今年的大运和流年：").append(baZiResult.getDaYunStringList().toString()).append("\n\n");
        System.out.println("$$$$$$$$$$$$$$$$$$" + prompt.toString());
        return prompt.toString();
    }

    /**
     * 构建发送给千问的提示词
     */
    private String buildSystemPrompt() {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请作为一位专业的命理大师，师承国学大师，结合知识库案例分析逻辑和思路，结合对话给出的八字原局和大运流年详细分析八字，请从以下几个方面进行详细分析：");
        prompt.append("1. 格局分析：按子平真诠的格局理论分析");
        prompt.append("2. 用神喜忌：确定该八字的用神和忌神");
        prompt.append("3. 学历情况：分析学历情况，精确到专科以下，专科，本科，研究生，研究生以上在这5个学历里选择");
        prompt.append("4. 性格特点：根据八字推断性格特征");
        prompt.append("5. 事业财运：分析事业和财运方面的特点");
        prompt.append("6. 健康建议：给出健康方面的注意事项，可能存在哪方面的风险比如高血压，糖尿病，心脏病，疑难杂症等");
        prompt.append("7. 职业情况：可能从事的职业，具体到行业举例：教育行业，IT行业，金融行业，医疗行业，制造业，服务业，农业行业，旅游业，文化行业，艺术行业，体育行业，娱乐行业，其他行业等");
        prompt.append("8. 家庭出身：分析出生的家庭情况，比如父母职业，家庭经济状况，家庭文化氛围等");
        prompt.append("9. 婚姻情感：分析婚姻情况，结合大运流年分析恋爱结婚的具体年份，情感稳定情况等");
        prompt.append("请用通俗易懂的语言进行分析，不输出任何和知识库相关的内容，只在分析阶段可以参考知识库内容，字数控制在800字以内，以JSON形式返回。");
        // prompt.append("请作为一位专业的命理大师，结合知识库的内容规则和案例分析逻辑，详细分析以下八字排盘结果：\n\n");
        // prompt.append("完整八字：").append(baZiResult.getBasicInfo().toString()).append("\n\n");
        // prompt.append("截止今年的大运和流年：").append(baZiResult.getDaYunStringList().toString()).append("\n\n");
        // prompt.append("请从以下几个方面进行详细分析：\n");
        // prompt.append("1. 格局分析：格局情况\n");
        // prompt.append("2. 学历情况：分析学历情况，精确到专科，本科，研究生，研究生以上\n");
        // prompt.append("3. 用神喜忌：确定该八字的用神和忌神\n");
        // prompt.append("4. 性格特点：根据八字推断性格特征\n");
        // prompt.append("5. 事业财运：分析事业和财运方面的特点\n");
        // prompt.append("6. 健康建议：给出健康方面的注意事项，可能存在哪方面的风险比如高血压，糖尿病，心脏病，癌症等\n");
        // prompt.append("7.
        // 职业情况：可能从事的职业，具体到行业举例：教育行业，IT行业，金融行业，医疗行业，制造业，服务业，农业行业，旅游业，文化行业，艺术行业，体育行业，娱乐行业，其他行业\n");
        // prompt.append("8. 家庭出身：分析出生的家庭情况，比如父母职业，家庭经济状况，家庭文化氛围等\n");
        // prompt.append("请用专业、通俗易懂的语言进行分析，字数控制在800字以内。");
        System.out.println("##################" + prompt.toString());
        return prompt.toString();
    }

}
