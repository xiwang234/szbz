package xw.szbz.cn;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;

/**
 * Gemini API 连接测试（支持环境变量）
 * 
 * 运行方式：
 * 1. 直接运行（使用代码中的 API Key）
 * 2. 设置环境变量后运行：
 *    Windows: set GEMINI_API_KEY=your_key && java GeminiConnectionTest
 *    Linux/Mac: export GEMINI_API_KEY=your_key && java GeminiConnectionTest
 */
public class GeminiConnectionTest {

    public static void main(String[] args) {
        // 优先从环境变量获取，否则使用默认值
        String apiKey = System.getenv("GEMINI_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            apiKey = "AIzaSyA9aKhNqwaYN0bsDqzqi9cmHL84WpM-xX8";
            System.out.println("⚠️ 未设置环境变量 GEMINI_API_KEY，使用默认值\n");
        } else {
            System.out.println("✅ 从环境变量读取 GEMINI_API_KEY\n");
        }

        // 测试连接
        testConnection(apiKey);
        
        // 测试八字分析
        testBaZiAnalysis(apiKey);
    }

    /**
     * 测试基本连接
     */
    private static void testConnection(String apiKey) {
        System.out.println("==========================================");
        System.out.println("测试 1: 基本连接测试");
        System.out.println("==========================================\n");

        try {
            Client client = Client.builder()
                    .apiKey(apiKey)
                    .build();

            String prompt = "请说一句祝福语。";
            
            System.out.println("发送请求: " + prompt);
            
            GenerateContentResponse response = client.models.generateContent(
                    "gemini-2.0-flash-exp",
                    prompt,
                    null
            );

            System.out.println("✅ 连接成功！");
            System.out.println("响应: " + response.text());
            System.out.println();

        } catch (Exception e) {
            System.err.println("❌ 连接失败！");
            System.err.println("错误: " + e.getMessage());
            e.printStackTrace();
            System.out.println();
        }
    }

    /**
     * 测试八字分析（完整流程）
     */
    private static void testBaZiAnalysis(String apiKey) {
        System.out.println("==========================================");
        System.out.println("测试 2: 八字分析测试");
        System.out.println("==========================================\n");

        try {
            Client client = Client.builder()
                    .apiKey(apiKey)
                    .build();

            // 构建八字分析提示词
            String prompt = buildPrompt();
            
            System.out.println("提示词长度: " + prompt.length() + " 字符");
            System.out.println("发送请求...\n");
            
            long startTime = System.currentTimeMillis();
            
            GenerateContentResponse response = client.models.generateContent(
                    "gemini-2.0-flash-exp",
                    prompt,
                    null
            );
            
            long duration = System.currentTimeMillis() - startTime;
            String result = response.text();

            System.out.println("✅ 分析成功！");
            System.out.println("响应时间: " + duration + " ms");
            System.out.println("响应长度: " + result.length() + " 字符");
            System.out.println("\n响应内容：");
            System.out.println("------------------------------------------");
            System.out.println(result);
            System.out.println("------------------------------------------\n");

            // 验证 JSON 格式
            validateJson(result);

        } catch (Exception e) {
            System.err.println("❌ 分析失败！");
            System.err.println("错误: " + e.getMessage());
            e.printStackTrace();
            System.out.println();
        }
    }

    /**
     * 构建八字分析提示词
     */
    private static String buildPrompt() {
        return "请作为一位专业的命理大师，分析以下八字：\n\n" +
                "八字信息：\n" +
                "年柱: 甲子（木水）\n" +
                "月柱: 乙丑（木土）\n" +
                "日柱: 丙寅（火木）\n" +
                "时柱: 丁卯（火木）\n\n" +
                "请严格按照以下 JSON 格式返回分析结果：\n" +
                "{\n" +
                "  \"格局分析\": \"分析格局情况\",\n" +
                "  \"学历情况\": \"分析学历情况\",\n" +
                "  \"用神喜忌\": \"确定用神和忌神\",\n" +
                "  \"性格特点\": \"推断性格特征\",\n" +
                "  \"事业财运\": \"分析事业和财运\",\n" +
                "  \"健康建议\": \"给出健康注意事项\",\n" +
                "  \"职业情况\": \"可能从事的职业\",\n" +
                "  \"综合评价\": \"总体命理评价\"\n" +
                "}\n\n" +
                "请用专业、通俗易懂的语言，字数控制在800字以内。";
    }

    /**
     * 验证 JSON 格式
     */
    private static void validateJson(String text) {
        try {
            String json = extractJson(text);
            
            // 简单验证 JSON 格式
            if (json.startsWith("{") && json.endsWith("}")) {
                System.out.println("✅ JSON 格式验证通过");
                
                // 检查是否包含所有必需字段
                String[] requiredFields = {
                    "格局分析", "学历情况", "用神喜忌", "性格特点",
                    "事业财运", "健康建议", "职业情况", "综合评价"
                };
                
                int foundCount = 0;
                for (String field : requiredFields) {
                    if (json.contains("\"" + field + "\"")) {
                        foundCount++;
                    }
                }
                
                System.out.println("✅ 包含必需字段: " + foundCount + "/" + requiredFields.length);
                
            } else {
                System.err.println("⚠️ 响应不是标准 JSON 格式");
            }
            
        } catch (Exception e) {
            System.err.println("⚠️ JSON 验证失败: " + e.getMessage());
        }
    }

    /**
     * 提取 JSON（去除 markdown 标记）
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
}
