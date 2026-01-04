package xw.szbz.cn;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;

/**
 * Gemini API 快速测试
 * 最简单的测试方式，直接运行即可
 */
public class QuickGeminiTest {

    public static void main(String[] args) {
        // 配置信息
        String apiKey = "AIzaSyA9aKhNqwaYN0bsDqzqi9cmHL84WpM-xX8";
        String model = "gemini-2.0-flash-exp";
        String prompt = "请用一句话介绍什么是八字命理。";

        System.out.println("==========================================");
        System.out.println("Gemini API 快速测试");
        System.out.println("==========================================");
        System.out.println("API Key: " + apiKey.substring(0, 20) + "...");
        System.out.println("模型: " + model);
        System.out.println("提示词: " + prompt);
        System.out.println("------------------------------------------\n");

        try {
            // 创建客户端
            System.out.println("正在初始化客户端...");
            Client client = Client.builder()
                    .apiKey(apiKey)
                    .build();
            System.out.println("✅ 客户端初始化成功\n");

            // 调用 API
            System.out.println("正在调用 Gemini API...");
            long startTime = System.currentTimeMillis();
            
            GenerateContentResponse response = client.models.generateContent(
                    model,
                    prompt,
                    null
            );
            
            long endTime = System.currentTimeMillis();
            System.out.println("✅ API 调用成功！\n");

            // 输出结果
            System.out.println("------------------------------------------");
            System.out.println("响应时间: " + (endTime - startTime) + " ms");
            System.out.println("响应内容: ");
            System.out.println(response.text());
            System.out.println("------------------------------------------\n");

            System.out.println("🎉 测试完成！Gemini API 可以正常调用。");

        } catch (Exception e) {
            System.err.println("❌ 测试失败！");
            System.err.println("错误类型: " + e.getClass().getName());
            System.err.println("错误信息: " + e.getMessage());
            System.err.println("\n详细堆栈信息：");
            e.printStackTrace();
            
            System.err.println("\n可能的原因：");
            System.err.println("1. API Key 无效或已过期");
            System.err.println("2. 网络连接问题（需要访问 Google 服务）");
            System.err.println("3. 模型名称错误");
            System.err.println("4. 配额已用完");
            System.err.println("5. 需要配置代理");
        }
    }
}
