package xw.szbz.cn.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TianGan 枚举单元测试
 */
class TianGanTest {

    @Test
    @DisplayName("测试天干数量")
    void testTianGanCount() {
        assertEquals(10, TianGan.values().length);
    }

    @Test
    @DisplayName("测试天干顺序")
    void testTianGanOrder() {
        assertEquals("甲", TianGan.JIA.getName());
        assertEquals("乙", TianGan.YI.getName());
        assertEquals("丙", TianGan.BING.getName());
        assertEquals("丁", TianGan.DING.getName());
        assertEquals("戊", TianGan.WU.getName());
        assertEquals("己", TianGan.JI.getName());
        assertEquals("庚", TianGan.GENG.getName());
        assertEquals("辛", TianGan.XIN.getName());
        assertEquals("壬", TianGan.REN.getName());
        assertEquals("癸", TianGan.GUI.getName());
    }

    @Test
    @DisplayName("测试fromIndex正常范围")
    void testFromIndex_NormalRange() {
        assertEquals(TianGan.JIA, TianGan.fromIndex(0));
        assertEquals(TianGan.YI, TianGan.fromIndex(1));
        assertEquals(TianGan.GUI, TianGan.fromIndex(9));
    }

    @Test
    @DisplayName("测试fromIndex负数处理")
    void testFromIndex_NegativeNumber() {
        assertEquals(TianGan.GUI, TianGan.fromIndex(-1));
        assertEquals(TianGan.REN, TianGan.fromIndex(-2));
    }

    @Test
    @DisplayName("测试fromIndex超出范围")
    void testFromIndex_OutOfRange() {
        assertEquals(TianGan.JIA, TianGan.fromIndex(10));
        assertEquals(TianGan.YI, TianGan.fromIndex(11));
    }
}
