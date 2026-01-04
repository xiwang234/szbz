package xw.szbz.cn.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * GeminiService 单元测试
 * 重点测试 generateContent 方法（HTTP 方式调用）
 * 不使用 Spring 上下文，直接测试服务类
 */
@DisplayName("GeminiService.generateContent 方法测试")
class GeminiServiceTest {

    private GeminiService geminiService;
    private String apiKey;
    private String modelName;

    @BeforeEach
    void setUp() {
        // 创建服务实例
        geminiService = new GeminiService();
        
        // 从环境变量读取 API Key，如果没有则使用默认值
        apiKey = System.getenv("GEMINI_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            apiKey = "AIzaSyA9aKhNqwaYN0bsDqzqi9cmHL84WpM-xX8";
        }
        
        modelName = "gemini-2.0-flash-exp";
        
        // 使用反射设置私有字段
        ReflectionTestUtils.setField(geminiService, "apiKey", apiKey);
        ReflectionTestUtils.setField(geminiService, "modelName", modelName);
    }

    @Test
    @DisplayName("测试简单文本生成")
    void testGenerateContent_SimplePrompt() {
        // Given
        String prompt = "请用一句话解释什么是人工智能。";

        // When
        String result = geminiService.generateContent(prompt);

        // Then
        assertNotNull(result, "返回结果不应为空");
        assertFalse(result.isEmpty(), "返回结果不应为空字符串");
        assertTrue(result.length() > 10, "返回结果应该有实质内容");
        
        System.out.println("测试提示词: " + prompt);
        System.out.println("返回结果: " + result);
    }

    @Test
    @DisplayName("测试六壬预测场景")
    void testGenerateContent_LiuRenDivination() {
        // Given
        String prompt = "请根据以下六壬排盘结果进行预测分析：\n" +
                "日干：甲\n" +
                "地支：子丑寅卯\n" +
                "请给出简要的吉凶判断。";

        // When
        String result = geminiService.generateContent(prompt);

        // Then
        assertNotNull(result, "返回结果不应为空");
        assertFalse(result.isEmpty(), "返回结果不应为空字符串");
        
        System.out.println("测试提示词: " + prompt);
        System.out.println("返回结果: " + result);
    }

    @Test
    @DisplayName("测试长文本提示词")
    void testGenerateContent_LongPrompt() {
        // Given
        StringBuilder longPrompt = new StringBuilder();
        longPrompt.append("请详细分析以下八字命盘：\n");
        longPrompt.append("年柱：甲子（天干甲木，地支子水）\n");
        longPrompt.append("月柱：乙丑（天干乙木，地支丑土）\n");
        longPrompt.append("日柱：丙寅（天干丙火，地支寅木）\n");
        longPrompt.append("时柱：丁卯（天干丁火，地支卯木）\n");
        longPrompt.append("请从格局、用神、事业、财运等方面进行分析，字数控制在200字以内。");

        // When
        String result = geminiService.generateContent(longPrompt.toString());

        // Then
        assertNotNull(result, "返回结果不应为空");
        assertFalse(result.isEmpty(), "返回结果不应为空字符串");
        assertTrue(result.length() > 50, "长提示词应该返回较长的内容");
        
        System.out.println("测试提示词长度: " + longPrompt.length());
        System.out.println("返回结果长度: " + result.length());
        System.out.println("返回结果: " + result);
    }

    @Test
    @DisplayName("测试包含特殊字符的提示词")
    void testGenerateContent_SpecialCharacters() {
        // Given
        String prompt = "请解释以下符号的含义：\n" +
                "1. 引号：\"测试\"\n" +
                "2. 换行符：\\n\n" +
                "3. 反斜杠：\\\n" +
                "简要回答即可。";

        // When
        String result = geminiService.generateContent(prompt);

        // Then
        assertNotNull(result, "返回结果不应为空");
        assertFalse(result.isEmpty(), "返回结果不应为空字符串");
        
        System.out.println("测试提示词: " + prompt);
        System.out.println("返回结果: " + result);
    }

    @Test
    @DisplayName("测试 API Key 未配置的情况")
    void testGenerateContent_NoApiKey() {
        // Given
        GeminiService serviceWithoutKey = new GeminiService();
        ReflectionTestUtils.setField(serviceWithoutKey, "apiKey", "");
        ReflectionTestUtils.setField(serviceWithoutKey, "modelName", "gemini-2.0-flash-exp");
        
        String prompt = "测试提示词";

        // When & Then
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> serviceWithoutKey.generateContent(prompt),
            "应该抛出 IllegalStateException"
        );
        
        assertTrue(
            exception.getMessage().contains("未配置"),
            "异常信息应该提示 API key 未配置"
        );
        
        System.out.println("预期的异常信息: " + exception.getMessage());
    }

    @Test
    @DisplayName("测试响应时间")
    void testGenerateContent_ResponseTime() {
        // Given
        String prompt = "请说一个字。";

        // When
        long startTime = System.currentTimeMillis();
        String result = geminiService.generateContent(prompt);
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // Then
        assertNotNull(result, "返回结果不应为空");
        System.out.println("请求耗时: " + duration + " ms");
        System.out.println("返回结果: " + result);
        
        // 正常情况下应该在 30 秒内返回（设置的超时时间）
        assertTrue(duration < 30000, "响应时间应该在超时时间内");
    }

    @Test
    @DisplayName("测试空提示词")
    void testGenerateContent_EmptyPrompt() {
        // Given
        String prompt = "";

        // When & Then
        // 空提示词可能返回空结果或抛出异常，取决于 API 行为
        assertDoesNotThrow(() -> {
            String result = geminiService.generateContent(prompt);
            System.out.println("空提示词返回: " + result);
        });
    }

    @Test
    @DisplayName("测试中文提示词")
    void testGenerateContent_ChinesePrompt() {
        // Given
        String prompt = "请用中文回答：什么是八字命理学？用一句话概括。";

        // When
        String result = geminiService.generateContent(prompt);

        // Then
        assertNotNull(result, "返回结果不应为空");
        assertFalse(result.isEmpty(), "返回结果不应为空字符串");
        // 检查返回结果中是否包含中文字符
        assertTrue(result.matches(".*[\\u4e00-\\u9fa5]+.*"), "返回结果应该包含中文");
        
        System.out.println("中文提示词测试结果: " + result);
    }

    @Test
    @DisplayName("测试连续多次调用")
    void testGenerateContent_MultipleCalls() {
        // Given
        String[] prompts = {
            "说一个数字",
            "说一个颜色",
            "说一个动物"
        };

        // When & Then
        for (String prompt : prompts) {
            String result = geminiService.generateContent(prompt);
            assertNotNull(result, "每次调用都应该返回结果");
            assertFalse(result.isEmpty(), "返回结果不应为空");
            System.out.println("提示词: " + prompt + " -> 结果: " + result);
        }
    }

    @Test
    @DisplayName("测试 JSON 格式提示词")
    void testGenerateContent_JsonFormatRequest() {
        // Given
        String prompt = "请用 JSON 格式返回一个人的基本信息，包含姓名、年龄、职业三个字段。仅返回 JSON，不要其他说明。";

        // When
        String result = geminiService.generateContent(prompt);

        // Then
        assertNotNull(result, "返回结果不应为空");
        System.out.println("JSON 格式测试返回: " + result);
        
        // 可选：验证返回的是否为有效 JSON
        assertTrue(
            result.contains("{") && result.contains("}"),
            "返回结果应该包含 JSON 结构"
        );
    }
}
