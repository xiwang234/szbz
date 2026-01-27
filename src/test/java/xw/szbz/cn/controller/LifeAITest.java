package xw.szbz.cn.controller;

import org.junit.jupiter.api.Test;
import xw.szbz.cn.model.LifeAIRequest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * LifeAI 接口测试
 */
class LifeAITest {

    @Test
    void testLifeAIRequest_AllFieldsSet() {
        LifeAIRequest request = new LifeAIRequest();
        request.setBackground("我是一名软件工程师");
        request.setQuestion("如何提升职业发展？");
        request.setBirthYear(1990);
        request.setGender("男");
        request.setCategory("职业规划");

        assertNotNull(request.getBackground());
        assertNotNull(request.getQuestion());
        assertNotNull(request.getBirthYear());
        assertNotNull(request.getGender());
        assertNotNull(request.getCategory());

        assertEquals("我是一名软件工程师", request.getBackground());
        assertEquals("如何提升职业发展？", request.getQuestion());
        assertEquals(1990, request.getBirthYear());
        assertEquals("男", request.getGender());
        assertEquals("职业规划", request.getCategory());
    }

    @Test
    void testLifeAIRequest_Constructor() {
        LifeAIRequest request = new LifeAIRequest(
            "背景描述",
            "问题内容",
            1995,
            "女",
            "情感咨询"
        );

        assertEquals("背景描述", request.getBackground());
        assertEquals("问题内容", request.getQuestion());
        assertEquals(1995, request.getBirthYear());
        assertEquals("女", request.getGender());
        assertEquals("情感咨询", request.getCategory());
    }

    @Test
    void testLifeAIRequest_ToString() {
        LifeAIRequest request = new LifeAIRequest(
            "测试背景",
            "测试问题",
            2000,
            "male",
            "测试分类"
        );

        String str = request.toString();
        assertTrue(str.contains("测试背景"));
        assertTrue(str.contains("测试问题"));
        assertTrue(str.contains("2000"));
        assertTrue(str.contains("male"));
        assertTrue(str.contains("测试分类"));
    }

    @Test
    void testLifeAIRequest_DifferentGenderFormats() {
        // 测试支持的所有性别格式
        String[] validGenders = {"男", "女", "male", "female"};

        for (String gender : validGenders) {
            LifeAIRequest request = new LifeAIRequest();
            request.setGender(gender);
            assertEquals(gender, request.getGender());
        }
    }

    @Test
    void testLifeAIRequest_BoundaryYears() {
        // 测试边界年份
        LifeAIRequest request1 = new LifeAIRequest();
        request1.setBirthYear(1900);
        assertEquals(1900, request1.getBirthYear());

        LifeAIRequest request2 = new LifeAIRequest();
        request2.setBirthYear(java.time.Year.now().getValue());
        assertEquals(java.time.Year.now().getValue(), request2.getBirthYear());
    }
}
