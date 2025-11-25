package xw.szbz.cn.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import xw.szbz.cn.model.BaZiRequest;
import xw.szbz.cn.model.BaZiResult;
import xw.szbz.cn.model.Pillar;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BaZiService 单元测试
 */
class BaZiServiceTest {

    private BaZiService baZiService;

    @BeforeEach
    void setUp() {
        baZiService = new BaZiService();
    }

    // ==================== 年柱测试 ====================

    @Test
    @DisplayName("测试年柱计算 - 1984年（甲子年）")
    void testCalculateYearPillar_1984() {
        Pillar pillar = baZiService.calculateYearPillar(1984);
        assertEquals("甲", pillar.getTianGan());
        assertEquals("子", pillar.getDiZhi());
        assertEquals("甲子", pillar.getFullName());
    }

    @Test
    @DisplayName("测试年柱计算 - 1989年（己巳年）")
    void testCalculateYearPillar_1989() {
        Pillar pillar = baZiService.calculateYearPillar(1989);
        assertEquals("己", pillar.getTianGan());
        assertEquals("巳", pillar.getDiZhi());
        assertEquals("己巳", pillar.getFullName());
    }

    @Test
    @DisplayName("测试年柱计算 - 2024年（甲辰年）")
    void testCalculateYearPillar_2024() {
        Pillar pillar = baZiService.calculateYearPillar(2024);
        assertEquals("甲", pillar.getTianGan());
        assertEquals("辰", pillar.getDiZhi());
        assertEquals("甲辰", pillar.getFullName());
    }

    // ==================== 月柱测试（节气月）====================

    @Test
    @DisplayName("测试月柱计算 - 1984年11月24日（甲子年乙亥月）")
    void testCalculateMonthPillar_1984_11_24() {
        // 11月24日在立冬(11月7日)之后，属于亥月
        // 甲子年，甲己之年丙作首，寅月丙寅，亥月=寅月+9=乙亥
        Pillar pillar = baZiService.calculateMonthPillar(1984, 11, 24);
        assertEquals("乙", pillar.getTianGan());
        assertEquals("亥", pillar.getDiZhi());
        assertEquals("乙亥", pillar.getFullName());
    }

    @Test
    @DisplayName("测试月柱计算 - 1989年11月23日（己巳年乙亥月）")
    void testCalculateMonthPillar_1989_11_23() {
        // 11月23日在立冬之后，属于亥月
        // 己巳年，甲己之年丙作首，亥月应为乙亥
        Pillar pillar = baZiService.calculateMonthPillar(1989, 11, 23);
        assertEquals("乙", pillar.getTianGan());
        assertEquals("亥", pillar.getDiZhi());
        assertEquals("乙亥", pillar.getFullName());
    }

    @Test
    @DisplayName("测试月柱计算 - 11月5日（戌月）")
    void testCalculateMonthPillar_November5() {
        // 11月5日在立冬(约11月7日)之前，属于戌月
        Pillar pillar = baZiService.calculateMonthPillar(1989, 11, 5);
        assertEquals("戌", pillar.getDiZhi());
    }

    @Test
    @DisplayName("测试月柱计算 - 11月8日（亥月）")
    void testCalculateMonthPillar_November8() {
        // 11月8日在立冬之后，属于亥月
        Pillar pillar = baZiService.calculateMonthPillar(1989, 11, 8);
        assertEquals("亥", pillar.getDiZhi());
    }

    // ==================== 日柱测试 ====================

    @Test
    @DisplayName("测试日柱计算 - 1985年5月14日（癸丑日）- 基准日期")
    void testCalculateDayPillar_19850514() {
        Pillar pillar = baZiService.calculateDayPillar(1985, 5, 14);
        assertEquals("癸", pillar.getTianGan());
        assertEquals("丑", pillar.getDiZhi());
        assertEquals("癸丑", pillar.getFullName());
    }

    @Test
    @DisplayName("测试日柱计算 - 1989年11月23日（丁亥日）")
    void testCalculateDayPillar_19891123() {
        Pillar pillar = baZiService.calculateDayPillar(1989, 11, 23);
        assertEquals("丁", pillar.getTianGan());
        assertEquals("亥", pillar.getDiZhi());
        assertEquals("丁亥", pillar.getFullName());
    }

    @Test
    @DisplayName("测试日柱计算 - 1984年11月24日（壬戌日）")
    void testCalculateDayPillar_19841124() {
        Pillar pillar = baZiService.calculateDayPillar(1984, 11, 24);
        assertEquals("壬", pillar.getTianGan());
        assertEquals("戌", pillar.getDiZhi());
        assertEquals("壬戌", pillar.getFullName());
    }

    @Test
    @DisplayName("测试日柱计算 - 1981年6月17日（丙寅日）")
    void testCalculateDayPillar_19810617() {
        Pillar pillar = baZiService.calculateDayPillar(1981, 6, 17);
        assertEquals("丙", pillar.getTianGan());
        assertEquals("寅", pillar.getDiZhi());
        assertEquals("丙寅", pillar.getFullName());
    }

    // ==================== 时柱测试 ====================

    @Test
    @DisplayName("测试时柱计算 - 丁日戌时（庚戌）")
    void testCalculateHourPillar_DingDay_XuShi() {
        // 丁日戌时应为庚戌（丁壬庚子居，子时庚子，戌时=子+10=庚戌）
        Pillar dayPillar = new Pillar("丁", "亥");
        Pillar hourPillar = baZiService.calculateHourPillar(dayPillar, 20);
        assertEquals("庚", hourPillar.getTianGan());
        assertEquals("戌", hourPillar.getDiZhi());
        assertEquals("庚戌", hourPillar.getFullName());
    }

    @Test
    @DisplayName("测试时柱计算 - 壬日子时（庚子）")
    void testCalculateHourPillar_RenDay_ZiShi() {
        Pillar dayPillar = new Pillar("壬", "戌");
        Pillar hourPillar = baZiService.calculateHourPillar(dayPillar, 0);
        assertEquals("庚", hourPillar.getTianGan());
        assertEquals("子", hourPillar.getDiZhi());
        assertEquals("庚子", hourPillar.getFullName());
    }

    // ==================== 完整八字测试 ====================

    @Test
    @DisplayName("核心测试 - 2025年11月24日16点48分 = 乙巳年丁亥月丁酉日戊申时")
    void testCalculate_20251124_1648() {
        BaZiRequest request = new BaZiRequest("男", 2025, 11, 24, 16);
        BaZiResult result = baZiService.calculate(request);

        // 验证四柱
        assertEquals("乙巳", result.getYearPillar().getFullName());
        assertEquals("丁亥", result.getMonthPillar().getFullName());
        assertEquals("丁酉", result.getDayPillar().getFullName());
        assertEquals("戊申", result.getHourPillar().getFullName());

        // 验证完整八字
        assertEquals("乙巳 丁亥 丁酉 戊申", result.getFullBaZi());
        assertEquals("男", result.getGender());
        assertFalse(result.getBirthInfo().isAdjusted());
    }

    @Test
    @DisplayName("核心测试 - 1996年6月2日5点43分 = 丙子年癸巳月庚午日己卯时")
    void testCalculate_19960602_0543() {
        BaZiRequest request = new BaZiRequest("女", 1996, 6, 2, 5);
        BaZiResult result = baZiService.calculate(request);

        // 验证四柱
        assertEquals("丙子", result.getYearPillar().getFullName());
        assertEquals("癸巳", result.getMonthPillar().getFullName());
        assertEquals("庚午", result.getDayPillar().getFullName());
        assertEquals("己卯", result.getHourPillar().getFullName());

        // 验证完整八字
        assertEquals("丙子 癸巳 庚午 己卯", result.getFullBaZi());
        assertEquals("女", result.getGender());
        assertFalse(result.getBirthInfo().isAdjusted());
    }

    @Test
    @DisplayName("核心测试 - 1985年5月14日10点47分 = 乙丑年辛巳月癸丑日丁巳时")
    void testCalculate_19850514_1047() {
        BaZiRequest request = new BaZiRequest("男", 1985, 5, 14, 10);
        BaZiResult result = baZiService.calculate(request);

        // 验证四柱
        assertEquals("乙丑", result.getYearPillar().getFullName());
        assertEquals("辛巳", result.getMonthPillar().getFullName());
        assertEquals("癸丑", result.getDayPillar().getFullName());
        assertEquals("丁巳", result.getHourPillar().getFullName());

        // 验证完整八字
        assertEquals("乙丑 辛巳 癸丑 丁巳", result.getFullBaZi());
        assertFalse(result.getBirthInfo().isAdjusted());
    }

    @Test
    @DisplayName("核心测试 - 1981年6月17日14点49分 = 辛酉年甲午月丙寅日乙未时")
    void testCalculate_19810617_1449() {
        BaZiRequest request = new BaZiRequest("女", 1981, 6, 17, 14);
        BaZiResult result = baZiService.calculate(request);

        // 验证四柱
        assertEquals("辛酉", result.getYearPillar().getFullName());
        assertEquals("甲午", result.getMonthPillar().getFullName());
        assertEquals("丙寅", result.getDayPillar().getFullName());
        assertEquals("乙未", result.getHourPillar().getFullName());

        // 验证完整八字
        assertEquals("辛酉 甲午 丙寅 乙未", result.getFullBaZi());
        assertEquals("女", result.getGender());
        assertFalse(result.getBirthInfo().isAdjusted());
    }

    @Test
    @DisplayName("核心测试 - 1989年11月23日20点36分 = 己巳年乙亥月丁亥日庚戌时")
    void testCalculate_19891123_2036() {
        BaZiRequest request = new BaZiRequest("女", 1989, 11, 23, 20);
        BaZiResult result = baZiService.calculate(request);

        // 验证四柱
        assertEquals("己巳", result.getYearPillar().getFullName());
        assertEquals("乙亥", result.getMonthPillar().getFullName());
        assertEquals("丁亥", result.getDayPillar().getFullName());
        assertEquals("庚戌", result.getHourPillar().getFullName());

        // 验证完整八字
        assertEquals("己巳 乙亥 丁亥 庚戌", result.getFullBaZi());
        assertEquals("女", result.getGender());
        assertFalse(result.getBirthInfo().isAdjusted());
    }

    @Test
    @DisplayName("核心测试 - 1984年11月23日23点25分 = 甲子年乙亥月壬戌日庚子时")
    void testCalculate_19841123_2325() {
        BaZiRequest request = new BaZiRequest("男", 1984, 11, 23, 23);
        BaZiResult result = baZiService.calculate(request);

        // 验证四柱
        assertEquals("甲子", result.getYearPillar().getFullName());
        assertEquals("乙亥", result.getMonthPillar().getFullName());
        assertEquals("壬戌", result.getDayPillar().getFullName());
        assertEquals("庚子", result.getHourPillar().getFullName());

        // 验证完整八字
        assertEquals("甲子 乙亥 壬戌 庚子", result.getFullBaZi());
        assertTrue(result.getBirthInfo().isAdjusted());
        assertEquals("子时", result.getBirthInfo().getShiChen());
    }

    @Test
    @DisplayName("核心测试 - 2014年5月24日16点48分 = 甲午年己巳月乙未日甲申时")
    void testCalculate_20140524_1648() {
        BaZiRequest request = new BaZiRequest("男", 2014, 5, 24, 16);
        BaZiResult result = baZiService.calculate(request);

        // 验证四柱
        assertEquals("甲午", result.getYearPillar().getFullName());
        assertEquals("己巳", result.getMonthPillar().getFullName());
        assertEquals("乙未", result.getDayPillar().getFullName());
        assertEquals("甲申", result.getHourPillar().getFullName());

        // 验证完整八字
        assertEquals("甲午 己巳 乙未 甲申", result.getFullBaZi());
        assertEquals("男", result.getGender());
        assertFalse(result.getBirthInfo().isAdjusted());
    }

    // ==================== 子时跨日测试 ====================

    @Test
    @DisplayName("测试子时跨日处理 - 23点只调整日柱")
    void testCalculate_23Hour_OnlyDayPillarAdjusted() {
        BaZiRequest request = new BaZiRequest("男", 1984, 11, 23, 23);
        BaZiResult result = baZiService.calculate(request);

        // 年柱月柱仍然是原日期
        assertEquals("甲子", result.getYearPillar().getFullName());
        assertEquals("乙亥", result.getMonthPillar().getFullName());
        // 日柱按11月24日计算
        assertEquals("壬戌", result.getDayPillar().getFullName());
        assertTrue(result.getBirthInfo().isAdjusted());
    }

    @Test
    @DisplayName("测试0点不需要跨日调整")
    void testCalculate_0Hour_ShouldNotAdjust() {
        BaZiRequest request = new BaZiRequest("男", 1984, 11, 24, 0);
        BaZiResult result = baZiService.calculate(request);

        assertFalse(result.getBirthInfo().isAdjusted());
        assertEquals("子时", result.getBirthInfo().getShiChen());
        assertEquals("壬戌", result.getDayPillar().getFullName());
    }

    @Test
    @DisplayName("测试年末跨日 - 12月31日23点")
    void testCalculate_23Hour_EndOfYear() {
        BaZiRequest request = new BaZiRequest("男", 1984, 12, 31, 23);
        BaZiResult result = baZiService.calculate(request);

        assertTrue(result.getBirthInfo().isAdjusted());
        // 年柱仍然是甲子年
        assertEquals("甲子", result.getYearPillar().getFullName());
        // 日柱按1985年1月1日计算
        assertEquals("1985-1-1", result.getBirthInfo().getDayPillarDate());
    }

    // ==================== 性别测试 ====================

    @Test
    @DisplayName("测试性别 - 男")
    void testCalculate_GenderMale() {
        BaZiRequest request = new BaZiRequest("男", 1989, 11, 23, 20);
        BaZiResult result = baZiService.calculate(request);
        assertEquals("男", result.getGender());
    }

    @Test
    @DisplayName("测试性别 - 女")
    void testCalculate_GenderFemale() {
        BaZiRequest request = new BaZiRequest("女", 1989, 11, 23, 20);
        BaZiResult result = baZiService.calculate(request);
        assertEquals("女", result.getGender());
    }

    @Test
    @DisplayName("测试性别 - male")
    void testCalculate_GenderMaleEnglish() {
        BaZiRequest request = new BaZiRequest("male", 1989, 11, 23, 20);
        BaZiResult result = baZiService.calculate(request);
        assertEquals("男", result.getGender());
    }

    @Test
    @DisplayName("测试性别 - female")
    void testCalculate_GenderFemaleEnglish() {
        BaZiRequest request = new BaZiRequest("female", 1989, 11, 23, 20);
        BaZiResult result = baZiService.calculate(request);
        assertEquals("女", result.getGender());
    }
}
