package xw.szbz.cn.util;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PromptTemplateUtil测试类
 * 演示如何使用提示词模板工具
 */
@SpringBootTest
class PromptTemplateUtilTest {

    @Autowired
    private PromptTemplateUtil promptTemplateUtil;

    /**
     * 测试六壬预测模板渲染
     */
    @Test
    void testRenderLiuRenTemplate() {
        // 准备测试数据
        String courseInfo = "2025年 乙巳年 丁亥月 辛卯日 丁酉时 卯将";
        
        String question = "问近期投资项目能否成功？";
        
        String background = "最近有朋友邀请我一起投资一个互联网项目，需要投入50万，" +
                "承诺半年回本，一年翻倍。项目看起来很好，但心里没底。";
        
        String birthInfo = "1984年甲子年男命";

        // 执行渲染
        String prompt = promptTemplateUtil.renderLiuRenTemplate(
            courseInfo, question, background, birthInfo
        );

        // 验证结果
        assertNotNull(prompt, "渲染结果不应为null");
        assertFalse(prompt.isEmpty(), "渲染结果不应为空");
        
        // 验证变量替换
        assertTrue(prompt.contains(courseInfo), "应包含课传信息");
        assertTrue(prompt.contains(question), "应包含占问事项");
        assertTrue(prompt.contains(background), "应包含占问背景");
        assertTrue(prompt.contains(birthInfo), "应包含出生信息");
        
        // 验证不应包含未替换的占位符
        assertFalse(prompt.contains("${courseInfo}"), "不应包含未替换的占位符");
        assertFalse(prompt.contains("${question}"), "不应包含未替换的占位符");
        assertFalse(prompt.contains("${background}"), "不应包含未替换的占位符");
        assertFalse(prompt.contains("${birthInfo}"), "不应包含未替换的占位符");
        
        // 验证关键内容
        assertTrue(prompt.contains("# Role"), "应包含Role部分");
        assertTrue(prompt.contains("# Task"), "应包含Task部分");
        assertTrue(prompt.contains("# Input Data"), "应包含Input Data部分");
        assertTrue(prompt.contains("# Analysis Protocol"), "应包含Analysis Protocol部分");
        assertTrue(prompt.contains("# Output Guidelines"), "应包含Output Guidelines部分");
        assertTrue(prompt.contains("# Response Template"), "应包含Response Template部分");

        // 打印渲染结果（仅用于调试）
        System.out.println("=== 渲染后的提示词 ===");
        System.out.println(prompt);
        System.out.println("======================");
    }

    /**
     * 测试空值处理
     */
    @Test
    void testRenderLiuRenTemplateWithNullValues() {
        // 使用null值
        String prompt = promptTemplateUtil.renderLiuRenTemplate(
            null, null, null, null
        );

        // 验证结果
        assertNotNull(prompt, "渲染结果不应为null");
        
        // 验证null值被替换为空字符串
        assertFalse(prompt.contains("${courseInfo}"), "null值应被替换");
        assertTrue(prompt.contains("（）"), "null值应显示为空");
    }

    /**
     * 测试特殊字符处理
     */
    @Test
    void testRenderLiuRenTemplateWithSpecialCharacters() {
        // 包含特殊字符的数据
        String courseInfo = "2025年 乙巳年 丁亥月 辛卯日 丁酉时 卯将";
        String question = "问题<包含>标签";
        String background = "背景{包含}大括号";
        String birthInfo = "1984年甲子年男命";

        // 执行渲染
        String prompt = promptTemplateUtil.renderLiuRenTemplate(
            courseInfo, question, background, birthInfo
        );

        // 验证特殊字符被正确处理
        assertTrue(prompt.contains(courseInfo), "应正确处理特殊字符");
        assertTrue(prompt.contains(question), "应正确处理标签字符");
        assertTrue(prompt.contains(background), "应正确处理大括号");
    }

    /**
     * 测试长文本处理
     */
    @Test
    void testRenderLiuRenTemplateWithLongText() {
        // 准备长文本
        StringBuilder longQuestion = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            longQuestion.append("这是第").append(i + 1).append("段占问信息。");
        }

        // 执行渲染
        String prompt = promptTemplateUtil.renderLiuRenTemplate(
            "2025年 乙巳年 丁亥月 辛卯日 丁酉时 卯将",
            longQuestion.toString(),
            "测试背景",
            "1984年甲子年男命"
        );

        // 验证长文本被正确处理
        assertTrue(prompt.contains(longQuestion.toString()), "应正确处理长文本");
        assertTrue(prompt.length() > 1000, "渲染结果应包含完整内容");
    }
}
