package xw.szbz.cn;

import xw.szbz.cn.model.BaZiRequest;
import xw.szbz.cn.model.BaZiResult;
import xw.szbz.cn.service.BaZiService;
import xw.szbz.cn.service.GeminiService;

/**
 * Gemini AI 八字分析示例
 *
 * 使用前请确保：
 * 1. 已在 application.properties 中配置 gemini.api.key
 * 2. 或设置环境变量 GEMINI_API_KEY
 */
public class GeminiAnalysisExample {
    public static void main(String[] args) {
        // 注意：这是一个独立示例类，不依赖 Spring 容器
        // 通过反射设置 GeminiService 的配置

        BaZiService baZiService = new BaZiService();
        GeminiService geminiService = createGeminiService();

        // 1. 创建八字请求
        BaZiRequest request = new BaZiRequest("男", 1984, 11, 23, 23);

        // 2. 计算八字
        BaZiResult baZiResult = baZiService.calculate(request);

        // 3. 显示八字结果
        System.out.println("========== 八字排盘结果 ==========");
        System.out.println("性别: " + baZiResult.getGender());
        System.out.println("出生时间: " + baZiResult.getBirthInfo().getYear() + "年"
                + baZiResult.getBirthInfo().getMonth() + "月"
                + baZiResult.getBirthInfo().getDay() + "日 "
                + baZiResult.getBirthInfo().getShiChen());
        System.out.println("完整八字: " + baZiResult.getFullBaZi());
        System.out.println();

        // 4. 使用 Gemini AI 分析八字
        try {
            System.out.println("========== Gemini AI 分析 ==========");
            System.out.println("正在调用 Gemini AI 进行分析...");
            String analysis = geminiService.analyzeBaZi(baZiResult);
            System.out.println(analysis);
            System.out.println("====================================");
        } catch (IllegalStateException e) {
            System.err.println("错误: " + e.getMessage());
            System.err.println("请配置 Gemini API Key 后重试");
        } catch (Exception e) {
            System.err.println("调用 Gemini API 失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 创建并配置 GeminiService（不依赖 Spring）
     */
    private static GeminiService createGeminiService() {
        GeminiService service = new GeminiService();
        try {
            // 从环境变量或配置文件读取 API Key
            String apiKey = System.getenv("GEMINI_API_KEY");
            if (apiKey == null || apiKey.isEmpty()) {
                apiKey = "AIzaSyA9aKhNqwaYN0bsDqzqi9cmHL84WpM-xX8"; // 默认值
            }
            String model = "gemini-2.0-flash-exp"; // 默认模型

            // 使用反射设置私有字段
            java.lang.reflect.Field apiKeyField = GeminiService.class.getDeclaredField("apiKey");
            apiKeyField.setAccessible(true);
            apiKeyField.set(service, apiKey);

            java.lang.reflect.Field modelField = GeminiService.class.getDeclaredField("modelName");
            modelField.setAccessible(true);
            modelField.set(service, model);

            System.out.println("✓ GeminiService 配置成功");
            System.out.println("  API Key: " + apiKey.substring(0, 10) + "...");
            System.out.println("  Model: " + model);
            System.out.println();
        } catch (Exception e) {
            System.err.println("配置 GeminiService 失败: " + e.getMessage());
            e.printStackTrace();
        }
        return service;
    }
}
