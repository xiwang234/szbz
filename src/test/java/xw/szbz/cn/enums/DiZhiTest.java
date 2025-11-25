package xw.szbz.cn.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DiZhi 枚举单元测试
 */
class DiZhiTest {

    @Test
    @DisplayName("测试地支数量")
    void testDiZhiCount() {
        assertEquals(12, DiZhi.values().length);
    }

    @ParameterizedTest
    @DisplayName("测试根据小时获取地支")
    @CsvSource({
            "0, 子",
            "1, 丑",
            "2, 丑",
            "3, 寅",
            "4, 寅",
            "5, 卯",
            "6, 卯",
            "7, 辰",
            "8, 辰",
            "9, 巳",
            "10, 巳",
            "11, 午",
            "12, 午",
            "13, 未",
            "14, 未",
            "15, 申",
            "16, 申",
            "17, 酉",
            "18, 酉",
            "19, 戌",
            "20, 戌",
            "21, 亥",
            "22, 亥",
            "23, 子"
    })
    void testFromHour(int hour, String expectedName) {
        assertEquals(expectedName, DiZhi.fromHour(hour).getName());
    }

    @Test
    @DisplayName("测试23点应返回子时")
    void testFromHour_23_ReturnsZi() {
        assertEquals(DiZhi.ZI, DiZhi.fromHour(23));
    }

    @Test
    @DisplayName("测试0点应返回子时")
    void testFromHour_0_ReturnsZi() {
        assertEquals(DiZhi.ZI, DiZhi.fromHour(0));
    }

    @Test
    @DisplayName("测试fromIndex正常范围")
    void testFromIndex_NormalRange() {
        assertEquals(DiZhi.ZI, DiZhi.fromIndex(0));
        assertEquals(DiZhi.CHOU, DiZhi.fromIndex(1));
        assertEquals(DiZhi.HAI, DiZhi.fromIndex(11));
    }

    @Test
    @DisplayName("测试fromIndex负数处理")
    void testFromIndex_NegativeNumber() {
        assertEquals(DiZhi.HAI, DiZhi.fromIndex(-1));
        assertEquals(DiZhi.XU, DiZhi.fromIndex(-2));
    }

    @Test
    @DisplayName("测试fromIndex超出范围")
    void testFromIndex_OutOfRange() {
        assertEquals(DiZhi.ZI, DiZhi.fromIndex(12));
        assertEquals(DiZhi.CHOU, DiZhi.fromIndex(13));
    }
}
