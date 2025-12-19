package xw.szbz.cn.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import xw.szbz.cn.model.BaZiRequest;
import xw.szbz.cn.model.BaZiResult;

/**
 * GeminiService 单元测试
 * 注意：这些测试需要有效的 Gemini API Key 才能运行
 */
@SpringBootTest
@TestPropertySource(properties = {
        "gemini.api.key=${GEMINI_API_KEY:}",
        "gemini.model=gemini-2.0-flash-exp"
})
class GeminiServiceTest {

    @Autowired
    private GeminiService geminiService;

    @Autowired
    private BaZiService baZiService;

    @Test
    @DisplayName("测试分析八字 - 完整流程")
    void testAnalyzeBaZi_FullFlow() throws Exception {
        // 检查是否配置了 API Key
        String apiKey = "cr_eb5f1a47c692841a0f5408e48514c2b8d1e98f8024b6d6af14ffd60767195bf2";
        if (apiKey == null || apiKey.isEmpty()) {
            System.out.println("跳过测试：未配置 GEMINI_API_KEY 环境变量");
            return;
        }

        // 1. 计算八字
        BaZiRequest request = new BaZiRequest("男", 1984, 11, 23, 23);
        BaZiResult baZiResult = baZiService.calculate(request);

        // 2. 使用 Gemini 分析（返回Object类型）
        Object analysisResult = geminiService.analyzeBaZi(baZiResult);

        // 3. 验证结果
        assertNotNull(analysisResult, "分析结果不应为空");
        
        // 4. 格式化输出
        String analysisText;
        if (analysisResult instanceof String) {
            analysisText = (String) analysisResult;
            assertFalse(analysisText.isEmpty(), "分析结果不应为空字符串");
            assertTrue(analysisText.length() > 100, "分析结果应该包含足够的内容");
        } else {
            // JSON对象，格式化输出
            ObjectMapper mapper = new ObjectMapper();
            analysisText = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(analysisResult);
        }

        // 输出结果以便查看
        System.out.println("========== Gemini AI 八字分析结果 ==========");
        System.out.println("八字：" + baZiResult.getFullBaZi());
        System.out.println("\n分析结果：");
        System.out.println(analysisText);
        System.out.println("==========================================");
    }

    @Test
    @DisplayName("测试分析八字 - 女性八字")
    void testAnalyzeBaZi_Female() throws Exception {
        // 检查是否配置了 API Key
        String apiKey = System.getenv("GEMINI_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            System.out.println("跳过测试：未配置 GEMINI_API_KEY 环境变量");
            return;
        }

        // 1. 计算八字
        BaZiRequest request = new BaZiRequest("女", 1989, 11, 23, 20);
        BaZiResult baZiResult = baZiService.calculate(request);

        // 2. 使用 Gemini 分析（返回Object类型）
        Object analysisResult = geminiService.analyzeBaZi(baZiResult);

        // 3. 验证结果
        assertNotNull(analysisResult);

        // 4. 格式化输出
        String analysisText;
        if (analysisResult instanceof String) {
            analysisText = (String) analysisResult;
            assertFalse(analysisText.isEmpty());
        } else {
            // JSON对象，格式化输出
            ObjectMapper mapper = new ObjectMapper();
            analysisText = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(analysisResult);
        }

        System.out.println("========== Gemini AI 八字分析结果（女性）==========");
        System.out.println("八字：" + baZiResult.getFullBaZi());
        System.out.println("\n分析结果：");
        System.out.println(analysisText);
        System.out.println("==========================================");
    }

    @Test
    @DisplayName("测试 API Key 未配置的情况")
    void testAnalyzeBaZi_NoApiKey() {
        // 如果环境变量中有 API Key，跳过此测试
        String apiKey = System.getenv("GEMINI_API_KEY");
        if (apiKey != null && !apiKey.isEmpty()) {
            System.out.println("跳过测试：已配置 GEMINI_API_KEY");
            return;
        }

        BaZiRequest request = new BaZiRequest("男", 1984, 11, 23, 23);
        BaZiResult baZiResult = baZiService.calculate(request);

        // 应该抛出 IllegalStateException
        assertThrows(IllegalStateException.class, () -> {
            geminiService.analyzeBaZi(baZiResult);
        });
    }
}
