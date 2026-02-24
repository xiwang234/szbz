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
 * 别责法测试
 */
@SpringBootTest
class LiuRenBieZeTest {

    @Autowired
    private LiuRenService liuRenService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 测试别责法三传顺序
     * 案例：2026年2月23日20点45分
     *
     * 预期结果：
     * - 三传应该是：丙寅（初传）、庚午（中传）、庚午（末传）
     * - 方法：别责法
     */
    @Test
    void testBieZe_20260223() throws Exception {
        LocalDateTime testTime = LocalDateTime.of(2026, 2, 23, 20, 45);

        String result = liuRenService.generateCourseInfo(testTime);
        assertNotNull(result);

        JsonNode jsonNode = objectMapper.readTree(result);

        System.out.println("\n=== 2026年2月23日20点45分 大六壬信息 ===");

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
        System.out.println("预期三传顺序：丙寅（初传）、庚午（中传）、庚午（末传）");
        System.out.println("实际三传顺序：" + chuChuanGanZhi + "（初传）、" +
                         zhongChuanGanZhi + "（中传）、" + moChuanGanZhi + "（末传）");

        System.out.println("\n【别责法规则】");
        System.out.println("四课之中出现其中两组上下神字面皆相同，且无相克遥克之神");
        System.out.println("五阳日取干合上神作初传，五阴日以地支三合前辰为初传");
        System.out.println("两者中末传俱归干上神");

        // 断言三传顺序
        assertEquals("丙寅", chuChuanGanZhi, "初传应该是丙寅");
        assertEquals("庚午", zhongChuanGanZhi, "中传应该是庚午");
        assertEquals("庚午", moChuanGanZhi, "末传应该是庚午");
    }
}
