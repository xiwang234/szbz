package xw.szbz.cn.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Gender 枚举单元测试
 */
class GenderTest {

    @Test
    @DisplayName("测试中文性别 - 男")
    void testFromString_ChineseMale() {
        assertEquals(Gender.MALE, Gender.fromString("男"));
    }

    @Test
    @DisplayName("测试中文性别 - 女")
    void testFromString_ChineseFemale() {
        assertEquals(Gender.FEMALE, Gender.fromString("女"));
    }

    @Test
    @DisplayName("测试英文性别 - male")
    void testFromString_EnglishMale() {
        assertEquals(Gender.MALE, Gender.fromString("male"));
        assertEquals(Gender.MALE, Gender.fromString("MALE"));
        assertEquals(Gender.MALE, Gender.fromString("Male"));
    }

    @Test
    @DisplayName("测试英文性别 - female")
    void testFromString_EnglishFemale() {
        assertEquals(Gender.FEMALE, Gender.fromString("female"));
        assertEquals(Gender.FEMALE, Gender.fromString("FEMALE"));
        assertEquals(Gender.FEMALE, Gender.fromString("Female"));
    }

    @Test
    @DisplayName("测试简写性别 - m/f")
    void testFromString_ShortForm() {
        assertEquals(Gender.MALE, Gender.fromString("m"));
        assertEquals(Gender.MALE, Gender.fromString("M"));
        assertEquals(Gender.FEMALE, Gender.fromString("f"));
        assertEquals(Gender.FEMALE, Gender.fromString("F"));
    }

    @Test
    @DisplayName("测试无效性别抛出异常")
    void testFromString_InvalidGender() {
        assertThrows(IllegalArgumentException.class, () -> Gender.fromString("unknown"));
        assertThrows(IllegalArgumentException.class, () -> Gender.fromString(""));
        assertThrows(IllegalArgumentException.class, () -> Gender.fromString("x"));
    }

    @Test
    @DisplayName("测试getName方法")
    void testGetName() {
        assertEquals("男", Gender.MALE.getName());
        assertEquals("女", Gender.FEMALE.getName());
    }
}
