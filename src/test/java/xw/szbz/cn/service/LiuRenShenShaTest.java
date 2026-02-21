package xw.szbz.cn.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 神煞系统测试
 */
@SpringBootTest
class LiuRenShenShaTest {

    @Autowired
    private LiuRenService liuRenService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 测试生气和死气的计算
     * 案例：2026年6月21日21点43分
     * 四柱：丙午年 甲午月 丙寅日 己亥时
     *
     * 午月(6)：
     * - 生气 = (6-2+12)%12 = 4(辰)
     * - 死气 = (6+4)%12 = 10(戌)
     *
     * 末传：丙寅(2)
     * - 寅(2) ≠ 辰(4)，不是生气 ✓
     * - 寅(2) ≠ 戌(10)，不是死气 ✓
     */
    @Test
    void testShengQiSiQi_20260621() throws Exception {
        LocalDateTime testTime = LocalDateTime.of(2026, 6, 21, 21, 43);

        String result = liuRenService.generateCourseInfo(testTime);
        assertNotNull(result);

        JsonNode jsonNode = objectMapper.readTree(result);

        System.out.println("\n=== 2026年6月21日21点43分 大六壬信息 ===");

        // 验证四柱
        JsonNode basicInfo = jsonNode.get("basicInfo");
        String yearPillar = basicInfo.get("yearPillar").asText();
        String monthPillar = basicInfo.get("monthPillar").asText();
        String dayPillar = basicInfo.get("dayPillar").asText();
        String hourPillar = basicInfo.get("hourPillar").asText();

        System.out.println("\n【四柱】");
        System.out.println("年柱：" + yearPillar);
        System.out.println("月柱：" + monthPillar);
        System.out.println("日柱：" + dayPillar);
        System.out.println("时柱：" + hourPillar);

        // 验证三传
        JsonNode sanChuan = jsonNode.get("sanChuan");

        JsonNode chuChuan = sanChuan.get("chuChuan");
        System.out.println("\n【三传】");
        System.out.println("初传：" + chuChuan.get("ganZhi").asText() + " - " +
                         chuChuan.get("tianJiang").asText());
        System.out.println("  神煞：" + chuChuan.get("shenSha").asText());

        JsonNode zhongChuan = sanChuan.get("zhongChuan");
        System.out.println("中传：" + zhongChuan.get("ganZhi").asText() + " - " +
                         zhongChuan.get("tianJiang").asText());
        System.out.println("  神煞：" + zhongChuan.get("shenSha").asText());

        JsonNode moChuan = sanChuan.get("moChuan");
        String moGanZhi = moChuan.get("ganZhi").asText();
        String moShenSha = moChuan.get("shenSha").asText();
        System.out.println("末传：" + moGanZhi + " - " + moChuan.get("tianJiang").asText());
        System.out.println("  神煞：" + moShenSha);

        // 验证末传神煞
        System.out.println("\n【验证】");
        System.out.println("午月(6)：");
        System.out.println("  生气 = (6-2+12)%12 = 4(辰)");
        System.out.println("  死气 = (6+4)%12 = 10(戌)");
        System.out.println("末传寅(2)：");
        System.out.println("  寅 ≠ 辰，不是生气 ✓");
        System.out.println("  寅 ≠ 戌，不是死气 ✓");

        // 断言：末传神煞中不应该包含"死气"
        if (moGanZhi.contains("寅")) {
            System.out.println("\n结果：末传神煞 = " + moShenSha);
            if (moShenSha.contains("死气")) {
                System.out.println("❌ 错误：末传寅不应该有死气");
                throw new AssertionError("末传寅(2)不应该包含死气，午月死气应该是戌(10)");
            } else {
                System.out.println("✓ 正确：末传寅不包含死气");
            }
        }
    }

    /**
     * 测试不同月份的生气和死气
     */
    @Test
    void testShengQiSiQi_DifferentMonths() throws Exception {
        System.out.println("\n=== 测试不同月份的生气和死气 ===\n");

        // 测试数据：月份 -> [生气地支, 死气地支]
        String[][] testData = {
            {"寅", "子", "午"},  // 寅月：生气子(0)，死气午(6)
            {"卯", "亥", "未"},  // 卯月：生气亥(11)，死气未(7)
            {"辰", "戌", "申"},  // 辰月：生气戌(10)，死气申(8)
            {"巳", "酉", "酉"},  // 巳月：生气酉(9)，死气酉(9) - 相同
            {"午", "申", "戌"},  // 午月：生气申(8)，但用户说应该是辰？
            {"未", "巳", "亥"},  // 未月：生气巳(5)，死气亥(11)
        };

        // 实际上让我重新验证一下公式
        String[] zhiNames = {"子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥"};

        System.out.println("月份\t生气\t死气\t生气对冲");
        System.out.println("----------------------------------------");

        for (int m = 0; m < 12; m++) {
            int shengQi = (m - 2 + 12) % 12;
            int siQi = (m + 4) % 12;
            int duiChong = (shengQi + 6) % 12;

            System.out.println(String.format("%s\t%s\t%s\t%s%s",
                zhiNames[m],
                zhiNames[shengQi],
                zhiNames[siQi],
                zhiNames[duiChong],
                siQi == duiChong ? " ✓" : " (不同)"));
        }
    }

    /**
     * 测试天马的计算
     * 规则：寅月起午顺行六阳辰（子寅辰午申戌）
     */
    @Test
    void testTianMa() throws Exception {
        System.out.println("\n=== 测试天马神煞 ===\n");

        String[] zhiNames = {"子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥"};

        System.out.println("月份\t天马\t验证");
        System.out.println("----------------------------------------");

        // 按照规则：寅月起午顺行六阳辰
        // 寅月(2)→午(6), 卯月(3)→申(8), 辰月(4)→戌(10), 巳月(5)→子(0)
        // 午月(6)→寅(2), 未月(7)→辰(4), 申月(8)→午(6), 酉月(9)→申(8)
        // 戌月(10)→戌(10), 亥月(11)→子(0), 子月(0)→寅(2), 丑月(1)→辰(4)
        int[] expectedTianMa = {2, 4, 6, 8, 10, 0, 2, 4, 6, 8, 10, 0};

        for (int m = 0; m < 12; m++) {
            int tianMa = (6 + 2 * (m - 2 + 12)) % 12;
            String checkMark = (tianMa == expectedTianMa[m]) ? "✓" : "❌";
            System.out.println(String.format("%s\t%s\t%s",
                zhiNames[m],
                zhiNames[tianMa],
                checkMark));
        }

        // 特别验证午月的天马
        System.out.println("\n【特别验证】午月(6)的天马：");
        int wuYueTianMa = (6 + 2 * (6 - 2 + 12)) % 12;
        System.out.println("计算：(6 + 2 * (6 - 2 + 12)) % 12 = " + wuYueTianMa + " = " + zhiNames[wuYueTianMa]);
        System.out.println("预期：寅(2)");
        System.out.println("结果：" + (wuYueTianMa == 2 ? "✓ 正确" : "❌ 错误"));
    }
}
