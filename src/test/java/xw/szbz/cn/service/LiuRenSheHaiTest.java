package xw.szbz.cn.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 涉害法测试
 */
@SpringBootTest
class LiuRenSheHaiTest {

    @Autowired
    private LiuRenService liuRenService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 测试涉害法三传顺序
     * 案例：2025年11月26日12点45分
     *
     * 预期结果：
     * - 三传应该是：乙未（初传）、癸卯（中传）、己亥（末传）
     * - 方法：涉害法
     */
    @Test
    void testSheHai_20251126() throws Exception {
        LocalDateTime testTime = LocalDateTime.of(2025, 11, 26, 12, 45);

        String result = liuRenService.generateCourseInfo(testTime);
        assertNotNull(result);

        JsonNode jsonNode = objectMapper.readTree(result);

        System.out.println("\n=== 2025年11月26日12点45分 大六壬信息 ===");

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

        // 验证四课
        JsonNode siKe = jsonNode.get("siKe");
        System.out.println("\n【四课】");
        System.out.println("第一课：" + siKe.get("diYiKe").get("ganZhi").asText());
        System.out.println("第二课：" + siKe.get("diErKe").get("ganZhi").asText());
        System.out.println("第三课：" + siKe.get("diSanKe").get("ganZhi").asText());
        System.out.println("第四课：" + siKe.get("diSiKe").get("ganZhi").asText());

        // 验证三传
        JsonNode sanChuan = jsonNode.get("sanChuan");
        String chuChuanGanZhi = sanChuan.get("chuChuan").get("ganZhi").asText();
        String zhongChuanGanZhi = sanChuan.get("zhongChuan").get("ganZhi").asText();
        String moChuanGanZhi = sanChuan.get("moChuan").get("ganZhi").asText();

        System.out.println("\n【三传】");
        System.out.println("初传：" + chuChuanGanZhi);
        System.out.println("中传：" + zhongChuanGanZhi);
        System.out.println("末传：" + moChuanGanZhi);

        System.out.println("\n【验证】");
        System.out.println("预期三传顺序：乙未（初传）、癸卯（中传）、己亥（末传）");
        System.out.println("实际三传顺序：" + chuChuanGanZhi + "（初传）、" +
                         zhongChuanGanZhi + "（中传）、" + moChuanGanZhi + "（末传）");

        // 验证旬空和坐空
        String xunKong = basicInfo.get("xunKong").asText();
        String zuoKong = basicInfo.get("zuoKong").asText();

        System.out.println("\n【旬空和坐空】");
        System.out.println("日柱：" + dayPillar);
        System.out.println("旬空：" + xunKong);
        System.out.println("坐空：" + zuoKong);
        System.out.println("\n旬空计算验证：");
        System.out.println("  日柱己亥：己(5) 亥(11)");
        System.out.println("  旬首 = (11 - 5 + 12) % 12 = 6（午）→ 甲午旬");
        System.out.println("  旬空1 = (6 - 2 + 12) % 12 = 4（辰）");
        System.out.println("  旬空2 = (6 - 1 + 12) % 12 = 5（巳）");
        System.out.println("  预期旬空：辰巳");
        System.out.println("  实际旬空：" + xunKong);
        System.out.println("\n坐空计算验证：");
        System.out.println("  在天地盘中，地盘辰巳上的天盘地支即为坐空");

        // 断言三传顺序
        assertEquals("乙未", chuChuanGanZhi, "初传应该是乙未");
        assertEquals("癸卯", zhongChuanGanZhi, "中传应该是癸卯");
        assertEquals("己亥", moChuanGanZhi, "末传应该是己亥");

        // 断言旬空
        assertEquals("辰巳", xunKong, "旬空应该是辰巳");
    }
}
