package xw.szbz.cn.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import xw.szbz.cn.model.BaZiRequest;
import xw.szbz.cn.model.BaZiResult;

/**
 * Ph8Service 单元测试
 * 注意：这些测试需要有效的 Ph8 API Key 才能运行
 */
@SpringBootTest
@TestPropertySource(properties = {
        "ph8.api.key=sk-37abe08dab1f43059ab9ed1988a7b9e9",
        "ph8.model=gemini-3-pro-preview:share"
})
class Ph8ServiceTest {

    @Autowired
    private Ph8Service ph8Service;

    @Autowired
    private BaZiService baZiService;

    @Test
    @DisplayName("测试分析八字 - 完整流程")
    void testAnalyzeBaZi_FullFlow() {
        // 检查是否配置了 API Key
        String apiKey = "sk-37abe08dab1f43059ab9ed1988a7b9e9";
        if (apiKey == null || apiKey.isEmpty()) {
            System.out.println("跳过测试：未配置 PH8_API_KEY 环境变量");
            return;
        }

        // 1. 计算八字
        BaZiRequest request = new BaZiRequest("男", 1984, 11, 23, 23);
        BaZiResult baZiResult = baZiService.calculate(request);

        // 2. 使用 Ph8 分析
        String analysis = ph8Service.analyzeBaZi(baZiResult);

        // 3. 验证结果
        assertNotNull(analysis, "分析结果不应为空");
        assertFalse(analysis.isEmpty(), "分析结果不应为空字符串");
        assertTrue(analysis.length() > 100, "分析结果应该包含足够的内容");

        // 输出结果以便查看
        System.out.println("========== Ph8 AI 八字分析结果 ==========");
        System.out.println("八字：" + baZiResult.getFullBaZi());
        System.out.println("\n分析结果：");
        System.out.println(analysis);
        System.out.println("==========================================");
    }

    @Test
    @DisplayName("测试分析八字 - 女性八字")
    void testAnalyzeBaZi_Female() {
        // 检查是否配置了 API Key
        String apiKey = System.getenv("PH8_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            System.out.println("跳过测试：未配置 PH8_API_KEY 环境变量");
            return;
        }

        // 1. 计算八字
        BaZiRequest request = new BaZiRequest("女", 1989, 11, 23, 20);
        BaZiResult baZiResult = baZiService.calculate(request);

        // 2. 使用 Ph8 分析
        String analysis = ph8Service.analyzeBaZi(baZiResult);

        // 3. 验证结果
        assertNotNull(analysis);
        assertFalse(analysis.isEmpty());

        System.out.println("========== Ph8 AI 八字分析结果（女性）==========");
        System.out.println("八字：" + baZiResult.getFullBaZi());
        System.out.println("\n分析结果：");
        System.out.println(analysis);
        System.out.println("==========================================");
    }

    @Test
    @DisplayName("测试 API Key 未配置的情况")
    void testAnalyzeBaZi_NoApiKey() {
        // 如果环境变量中有 API Key，跳过此测试
        String apiKey = System.getenv("PH8_API_KEY");
        if (apiKey != null && !apiKey.isEmpty()) {
            System.out.println("跳过测试：已配置 PH8_API_KEY");
            return;
        }

        BaZiRequest request = new BaZiRequest("男", 1984, 11, 23, 23);
        BaZiResult baZiResult = baZiService.calculate(request);

        // 应该抛出 IllegalStateException
        assertThrows(IllegalStateException.class, () -> {
            ph8Service.analyzeBaZi(baZiResult);
        });
    }

    @Test
    @DisplayName("测试分析八字 - 子时跨日场景")
    void testAnalyzeBaZi_MidnightCase() {
        // 检查是否配置了 API Key
        String apiKey = System.getenv("PH8_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            System.out.println("跳过测试：未配置 PH8_API_KEY 环境变量");
            return;
        }

        // 测试子时（23点）的特殊情况
        BaZiRequest request = new BaZiRequest("男", 2025, 11, 24, 23);
        BaZiResult baZiResult = baZiService.calculate(request);

        // 使用 Ph8 分析
        String analysis = ph8Service.analyzeBaZi(baZiResult);

        // 验证结果
        assertNotNull(analysis);
        assertFalse(analysis.isEmpty());

        System.out.println("========== Ph8 AI 八字分析结果（子时跨日）==========");
        System.out.println("八字：" + baZiResult.getFullBaZi());
        System.out.println("\n分析结果：");
        System.out.println(analysis);
        System.out.println("==========================================");
    }
}
