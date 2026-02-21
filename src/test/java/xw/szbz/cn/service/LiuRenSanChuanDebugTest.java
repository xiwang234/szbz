package xw.szbz.cn.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 三传计算Debug测试 - 专门测试2023年4月11日11点5分案例
 */
@SpringBootTest
class LiuRenSanChuanDebugTest {

    @Autowired
    private LiuRenService liuRenService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 测试案例4：2023年4月11日11点5分（己亥日午时）
     *
     * 预期结果：
     * - 日柱：己亥
     * - 占时：午时
     * - 月将：酉
     * - 昼夜：昼贵
     * - 贵人：子（己日昼贵子）
     *
     * 天地盘：
     * - 地盘申 → 天盘子 → 天将天乙贵人
     *
     * 四课（根据用户反馈）：
     * - 第一课：午己（日干己，日干寄宫午，天盘午）
     * - 第二课：未午（日柱地支亥的天盘是未，地盘是午）
     * - 第三课：亥卯（日支亥的天盘是未，但这里应该显示亥卯？需要确认）
     * - 第四课：未卯（第三课天盘的天盘）
     *
     * 三传（预期）：
     * - 初传：兄弟-乙未-青龙（下克上，卯克未，取未）
     * - 中传：妻财-己亥-螣蛇
     * - 末传：官鬼-癸卯-玄武
     *
     * 问题：第四课是"未卯"，卯(木)克未(土)，这是下克上，应该取未为初传
     */
    @Test
    void testCase4_20230411_1105() throws Exception {
        LocalDateTime testTime = LocalDateTime.of(2023, 4, 11, 11, 5);

        String result = liuRenService.generateCourseInfo(testTime);

        JsonNode jsonNode = objectMapper.readTree(result);

        // 打印完整结果
        System.out.println("\n=== 2023年4月11日11点5分 大六壬信息 ===");
        System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode));

        // 验证基本信息
        JsonNode basicInfo = jsonNode.get("basicInfo");
        System.out.println("\n=== 基本信息验证 ===");
        System.out.println("日柱: " + basicInfo.get("dayPillar").asText());
        System.out.println("时柱: " + basicInfo.get("hourPillar").asText());
        System.out.println("月将: " + basicInfo.get("yueJiang").asText());

        // 验证天地盘中的关键位置
        JsonNode tianDiPan = jsonNode.get("tianDiPan");
        JsonNode positions = tianDiPan.get("positions");

        System.out.println("\n=== 天地盘验证 ===");
        System.out.println("地盘\t天盘\t天将");
        System.out.println("------------------------");
        for (int i = 0; i < 12; i++) {
            JsonNode position = positions.get(i);
            String diPan = position.get("diPan").asText();
            String tianPan = position.get("tianPan").asText();
            String tianJiang = position.get("tianJiang").asText();

            System.out.println(String.format("%s\t%s\t%s", diPan, tianPan, tianJiang));

            // 验证关键位置：地盘申 → 天盘子 → 天将天乙贵人
            if ("申".equals(diPan)) {
                System.out.println(">>> 地盘申: 天盘=" + tianPan + " (预期: 子), 天将=" + tianJiang + " (预期: 天乙贵人)");
                assertEquals("子", tianPan, "地盘申上天盘应该是子");
                assertEquals("天乙贵人", tianJiang, "地盘申上天将应该是天乙贵人");
            }
        }

        // 验证四课
        JsonNode siKe = jsonNode.get("siKe");
        System.out.println("\n=== 四课验证 ===");

        JsonNode diYiKe = siKe.get("diYiKe");
        System.out.println("第一课: " + diYiKe.get("ganZhi").asText() + " - " + diYiKe.get("tianJiang").asText());

        JsonNode diErKe = siKe.get("diErKe");
        System.out.println("第二课: " + diErKe.get("ganZhi").asText() + " - " + diErKe.get("tianJiang").asText());

        JsonNode diSanKe = siKe.get("diSanKe");
        System.out.println("第三课: " + diSanKe.get("ganZhi").asText() + " - " + diSanKe.get("tianJiang").asText());

        JsonNode diSiKe = siKe.get("diSiKe");
        System.out.println("第四课: " + diSiKe.get("ganZhi").asText() + " - " + diSiKe.get("tianJiang").asText());

        // 验证三传
        JsonNode sanChuan = jsonNode.get("sanChuan");
        System.out.println("\n=== 三传验证 ===");

        JsonNode chuChuan = sanChuan.get("chuChuan");
        String chuChuanStr = String.format("%s-%s-%s-%s",
            chuChuan.get("liuQin").asText(),
            chuChuan.get("ganZhi").asText(),
            chuChuan.get("tianJiang").asText(),
            chuChuan.get("shenSha").asText());
        System.out.println("初传: " + chuChuanStr);
        System.out.println("     (预期: 兄弟-乙未-青龙)");

        JsonNode zhongChuan = sanChuan.get("zhongChuan");
        String zhongChuanStr = String.format("%s-%s-%s-%s",
            zhongChuan.get("liuQin").asText(),
            zhongChuan.get("ganZhi").asText(),
            zhongChuan.get("tianJiang").asText(),
            zhongChuan.get("shenSha").asText());
        System.out.println("中传: " + zhongChuanStr);
        System.out.println("     (预期: 妻财-己亥-螣蛇)");

        JsonNode moChuan = sanChuan.get("moChuan");
        String moChuanStr = String.format("%s-%s-%s-%s",
            moChuan.get("liuQin").asText(),
            moChuan.get("ganZhi").asText(),
            moChuan.get("tianJiang").asText(),
            moChuan.get("shenSha").asText());
        System.out.println("末传: " + moChuanStr);
        System.out.println("     (预期: 官鬼-癸卯-玄武)");

        // 验证三传是否正确
        System.out.println("\n=== 三传结果检查 ===");

        // 初传应该是乙未
        String chuChuanGanZhi = chuChuan.get("ganZhi").asText();
        System.out.println("初传干支: " + chuChuanGanZhi + " (预期: 乙未)");
        assertEquals("乙未", chuChuanGanZhi, "初传应该是乙未（第四课下克上，卯克未，取未）");

        // 中传应该是己亥
        String zhongChuanGanZhi = zhongChuan.get("ganZhi").asText();
        System.out.println("中传干支: " + zhongChuanGanZhi + " (预期: 己亥)");
        assertEquals("己亥", zhongChuanGanZhi, "中传应该是己亥");

        // 末传应该是癸卯
        String moChuanGanZhi = moChuan.get("ganZhi").asText();
        System.out.println("末传干支: " + moChuanGanZhi + " (预期: 癸卯)");
        assertEquals("癸卯", moChuanGanZhi, "末传应该是癸卯");
    }
}
