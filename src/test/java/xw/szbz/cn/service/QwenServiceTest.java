package xw.szbz.cn.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import xw.szbz.cn.model.BaZiRequest;
import xw.szbz.cn.model.BaZiResult;

/**
 * QwenService 单元测试（异步版本）
 * 注意：这些测试需要有效的阿里云千问 API Key 才能运行
 */
@SpringBootTest
@TestPropertySource(properties = {
        "qwen.api.key=sk-9babe99c7cd34a22963c2a2e13cb717b",
        "qwen.model=qwen-turbo"
})
class QwenServiceTest {

    @Autowired
    private QwenService qwenService;

    @Autowired
    private BaZiService baZiService;

    @Test
    @DisplayName("测试异步分析八字 - 完整流程")
    void testAnalyzeBaZi_FullFlow() throws ExecutionException, InterruptedException {
        // 检查是否配置了 API Key
        String apiKey = "sk-9babe99c7cd34a22963c2a2e13cb717b";
        if (apiKey == null || apiKey.isEmpty()) {
            System.out.println("跳过测试：未配置 QWEN_API_KEY 环境变量");
            return;
        }

        // 1. 计算八字
        BaZiRequest request = new BaZiRequest("男", 1984, 11, 23, 23);
        BaZiResult baZiResult = baZiService.calculate(request);

        // 2. 使用千问异步分析
        CompletableFuture<String> future = qwenService.analyzeBaZi(baZiResult);

        // 3. 等待异步结果
        String analysis = future.get();

        // 4. 验证结果
        assertNotNull(analysis, "分析结果不应为空");
        assertFalse(analysis.isEmpty(), "分析结果不应为空字符串");
        assertTrue(analysis.length() > 100, "分析结果应该包含足够的内容");

        // 输出结果以便查看
        System.out.println("========== 通义千问 八字分析结果（异步）==========");
        System.out.println("八字：" + baZiResult.getFullBaZi());
        System.out.println("\n分析结果：");
        System.out.println(analysis);
        System.out.println("==========================================");
    }

    @Test
    @DisplayName("测试异步分析八字 - 女性八字")
    void testAnalyzeBaZi_Female() throws ExecutionException, InterruptedException {
        // 检查是否配置了 API Key
        String apiKey = System.getenv("QWEN_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            System.out.println("跳过测试：未配置 QWEN_API_KEY 环境变量");
            return;
        }

        // 1. 计算八字
        BaZiRequest request = new BaZiRequest("女", 1989, 11, 23, 20);
        BaZiResult baZiResult = baZiService.calculate(request);

        // 2. 使用千问异步分析
        CompletableFuture<String> future = qwenService.analyzeBaZi(baZiResult);

        // 3. 等待异步结果
        String analysis = future.get();

        // 4. 验证结果
        assertNotNull(analysis);
        assertFalse(analysis.isEmpty());

        System.out.println("========== 通义千问 八字分析结果（女性，异步）==========");
        System.out.println("八字：" + baZiResult.getFullBaZi());
        System.out.println("\n分析结果：");
        System.out.println(analysis);
        System.out.println("==========================================");
    }

    @Test
    @DisplayName("测试异步分析八字 - 子时跨日案例")
    void testAnalyzeBaZi_ZiShiCrossDay() throws ExecutionException, InterruptedException {
        // 检查是否配置了 API Key
        String apiKey = System.getenv("QWEN_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            System.out.println("跳过测试：未配置 QWEN_API_KEY 环境变量");
            return;
        }

        // 1. 计算八字 - 23点属于次日子时
        BaZiRequest request = new BaZiRequest("男", 2025, 11, 24, 23);
        BaZiResult baZiResult = baZiService.calculate(request);

        // 2. 使用千问异步分析
        CompletableFuture<String> future = qwenService.analyzeBaZi(baZiResult);

        // 3. 等待异步结果
        String analysis = future.get();

        // 4. 验证结果
        assertNotNull(analysis);
        assertFalse(analysis.isEmpty());

        System.out.println("========== 通义千问 八字分析结果（子时跨日，异步）==========");
        System.out.println("八字：" + baZiResult.getFullBaZi());
        System.out.println("\n分析结果：");
        System.out.println(analysis);
        System.out.println("==========================================");
    }

    @Test
    @DisplayName("测试并发异步分析多个八字")
    void testAnalyzeBaZi_Concurrent() throws ExecutionException, InterruptedException {
        // 检查是否配置了 API Key
        String apiKey = System.getenv("QWEN_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            System.out.println("跳过测试：未配置 QWEN_API_KEY 环境变量");
            return;
        }

        // 准备多个八字计算结果
        BaZiResult bazi1 = baZiService.calculate(new BaZiRequest("男", 1984, 11, 23, 23));
        BaZiResult bazi2 = baZiService.calculate(new BaZiRequest("女", 1989, 11, 23, 20));
        BaZiResult bazi3 = baZiService.calculate(new BaZiRequest("男", 2000, 1, 1, 12));

        // 并发发送三个异步请求
        CompletableFuture<String> future1 = qwenService.analyzeBaZi(bazi1);
        CompletableFuture<String> future2 = qwenService.analyzeBaZi(bazi2);
        CompletableFuture<String> future3 = qwenService.analyzeBaZi(bazi3);

        // 等待所有异步操作完成
        CompletableFuture.allOf(future1, future2, future3).get();

        // 验证所有结果
        String analysis1 = future1.get();
        String analysis2 = future2.get();
        String analysis3 = future3.get();

        assertNotNull(analysis1);
        assertNotNull(analysis2);
        assertNotNull(analysis3);
        assertFalse(analysis1.isEmpty());
        assertFalse(analysis2.isEmpty());
        assertFalse(analysis3.isEmpty());

        System.out.println("========== 并发异步测试通过 ==========");
        System.out.println("成功并发分析了 3 个八字");
        System.out.println("八字1: " + bazi1.getFullBaZi() + " - 分析长度: " + analysis1.length());
        System.out.println("八字2: " + bazi2.getFullBaZi() + " - 分析长度: " + analysis2.length());
        System.out.println("八字3: " + bazi3.getFullBaZi() + " - 分析长度: " + analysis3.length());
        System.out.println("==========================================");
    }

    @Test
    @DisplayName("测试 API Key 未配置的情况")
    void testAnalyzeBaZi_NoApiKey() {
        // 如果环境变量中有 API Key，跳过此测试
        String apiKey = System.getenv("QWEN_API_KEY");
        if (apiKey != null && !apiKey.isEmpty()) {
            System.out.println("跳过测试：已配置 QWEN_API_KEY");
            return;
        }

        BaZiRequest request = new BaZiRequest("男", 1984, 11, 23, 23);
        BaZiResult baZiResult = baZiService.calculate(request);

        // 异步调用应该返回失败的 Future
        CompletableFuture<String> future = qwenService.analyzeBaZi(baZiResult);

        // 应该抛出 ExecutionException，内部原因是 IllegalStateException
        ExecutionException exception = assertThrows(ExecutionException.class, future::get);
        assertTrue(exception.getCause() instanceof IllegalStateException);
    }

    @Test
    @DisplayName("测试提示词构建逻辑")
    void testPromptBuilding() {
        // 这个测试不需要 API Key，仅测试提示词构建
        BaZiRequest request = new BaZiRequest("男", 1984, 11, 23, 23);
        BaZiResult baZiResult = baZiService.calculate(request);

        // 验证 BaZiResult 对象完整性
        assertNotNull(baZiResult);
        assertNotNull(baZiResult.getGender());
        assertNotNull(baZiResult.getFullBaZi());
        assertNotNull(baZiResult.getYearPillar());
        assertNotNull(baZiResult.getMonthPillar());
        assertNotNull(baZiResult.getDayPillar());
        assertNotNull(baZiResult.getHourPillar());
        assertNotNull(baZiResult.getBirthInfo());

        System.out.println("八字计算结果验证通过，可用于构建提示词");
    }
}
