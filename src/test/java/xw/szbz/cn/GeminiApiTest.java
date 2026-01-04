package xw.szbz.cn;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;

/**
 * Gemini API 连接测试
 * 独立测试类，用于验证 Gemini API 是否可以正常调用
 * 
 * 使用方法：
 * 1. 确保配置了正确的 API Key
 * 2. 运行 main 方法
 * 3. 查看控制台输出
 */
public class GeminiApiTest {

    // 替换为你的 Gemini API Key
    private static final String API_KEY = "AIzaSyA9aKhNqwaYN0bsDqzqi9cmHL84WpM-xX8";
    
    // 使用的模型名称
    private static final String MODEL_NAME = "gemini-2.0-flash-exp";

    public static void main(String[] args) {
        System.out.println("==========================================");
        System.out.println("Gemini API 连接测试");
        System.out.println("==========================================\n");

        // 测试 1: 简单文本生成
        testSimpleGeneration();
        
        // 测试 2: JSON 格式响应
        testJsonGeneration();
        
        // 测试 3: 八字分析模拟
        testBaZiAnalysis();
    }

    /**
     * 测试 1: 简单文本生成
     */
    private static void testSimpleGeneration() {
        System.out.println("【测试 1】简单文本生成");
        System.out.println("----------------------------------------");
        
        try {
            // 初始化客户端
            Client client = Client.builder()
                    .apiKey(API_KEY)
                    .build();

            // 简单的提示词
            String prompt = "请用一句话介绍什么是八字命理。";
            
            System.out.println("发送请求...");
            System.out.println("提示词: " + prompt);
            System.out.println();

            // 调用 API
            GenerateContentResponse response = client.models.generateContent(
                    MODEL_NAME,
                    prompt,
                    null
            );

            // 获取响应
            String result = response.text();
            
            System.out.println("✅ 调用成功！");
            System.out.println("响应内容: " + result);
            System.out.println();

        } catch (Exception e) {
            System.err.println("❌ 调用失败！");
            System.err.println("错误信息: " + e.getMessage());
            e.printStackTrace();
            System.out.println();
        }
    }

    /**
     * 测试 2: JSON 格式响应
     */
    private static void testJsonGeneration() {
        System.out.println("【测试 2】JSON 格式响应");
        System.out.println("----------------------------------------");
        
        try {
            Client client = Client.builder()
                    .apiKey(API_KEY)
                    .build();

            // 要求返回 JSON 格式
            String prompt = "请分析以下性格特点，并严格按照 JSON 格式返回：\n" +
                    "性格：外向、乐观、善于交际\n\n" +
                    "请返回以下 JSON 格式：\n" +
                    "{\n" +
                    "  \"性格类型\": \"...\",\n" +
                    "  \"优点\": \"...\",\n" +
                    "  \"缺点\": \"...\",\n" +
                    "  \"建议\": \"...\"\n" +
                    "}";
            
            System.out.println("发送请求...");
            System.out.println("提示词: " + prompt);
            System.out.println();

            GenerateContentResponse response = client.models.generateContent(
                    MODEL_NAME,
                    prompt,
                    null
            );

            String result = response.text();
            
            System.out.println("✅ 调用成功！");
            System.out.println("响应内容: ");
            System.out.println(result);
            System.out.println();
            
            // 尝试提取 JSON
            String jsonText = extractJson(result);
            System.out.println("提取的 JSON: ");
            System.out.println(jsonText);
            System.out.println();

        } catch (Exception e) {
            System.err.println("❌ 调用失败！");
            System.err.println("错误信息: " + e.getMessage());
            e.printStackTrace();
            System.out.println();
        }
    }

    /**
     * 测试 3: 八字分析模拟
     */
    private static void testBaZiAnalysis() {
        System.out.println("【测试 3】八字分析模拟");
        System.out.println("----------------------------------------");
        
        try {
            Client client = Client.builder()
                    .apiKey(API_KEY)
                    .build();

            // 模拟八字分析提示词
            String prompt = buildBaZiPrompt();
            
            System.out.println("发送请求...");
            System.out.println("提示词长度: " + prompt.length() + " 字符");
            System.out.println();

            long startTime = System.currentTimeMillis();
            
            GenerateContentResponse response = client.models.generateContent(
                    MODEL_NAME,
                    prompt,
                    null
            );

            long endTime = System.currentTimeMillis();
            String result = response.text();
            
            System.out.println("✅ 调用成功！");
            System.out.println("响应时间: " + (endTime - startTime) + " ms");
            System.out.println("响应长度: " + result.length() + " 字符");
            System.out.println();
            System.out.println("响应内容: ");
            System.out.println(result);
            System.out.println();
            
            // 尝试提取 JSON
            try {
                String jsonText = extractJson(result);
                System.out.println("✅ JSON 提取成功！");
                System.out.println(jsonText);
            } catch (Exception e) {
                System.err.println("⚠️ JSON 提取失败，可能不是标准 JSON 格式");
            }
            System.out.println();

        } catch (Exception e) {
            System.err.println("❌ 调用失败！");
            System.err.println("错误信息: " + e.getMessage());
            e.printStackTrace();
            System.out.println();
        }
    }

    /**
     * 构建八字分析提示词
     */
    private static String buildBaZiPrompt() {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请作为一位专业的命理大师，分析以下八字排盘结果：\n\n");
        prompt.append("完整八字：\n");
        prompt.append("年柱: 甲子（木水）\n");
        prompt.append("月柱: 乙丑（木土）\n");
        prompt.append("日柱: 丙寅（火木）\n");
        prompt.append("时柱: 丁卯（火木）\n\n");
        prompt.append("大运：戊辰、己巳、庚午、辛未\n");
        prompt.append("流年：2024 甲辰年\n\n");
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
        
        return prompt.toString();
    }

    /**
     * 提取 JSON 内容（去除 markdown 代码块标记）
     */
    private static String extractJson(String text) {
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
     * 测试不同模型
     */
    private static void testDifferentModels() {
        System.out.println("【测试 4】不同模型对比");
        System.out.println("----------------------------------------");
        
        String[] models = {
            "gemini-2.0-flash-exp",
            "gemini-1.5-flash",
            "gemini-1.5-pro"
        };
        
        String prompt = "请用一句话介绍八字命理。";
        
        for (String model : models) {
            System.out.println("测试模型: " + model);
            try {
                Client client = Client.builder()
                        .apiKey(API_KEY)
                        .build();
                
                long startTime = System.currentTimeMillis();
                GenerateContentResponse response = client.models.generateContent(
                        model,
                        prompt,
                        null
                );
                long endTime = System.currentTimeMillis();
                
                System.out.println("✅ 成功！响应时间: " + (endTime - startTime) + " ms");
                System.out.println("响应: " + response.text().substring(0, Math.min(100, response.text().length())) + "...");
                
            } catch (Exception e) {
                System.err.println("❌ 失败: " + e.getMessage());
            }
            System.out.println();
        }
    }
}
