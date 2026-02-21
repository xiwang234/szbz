package xw.szbz.cn.service;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 验证用户提供的具体案例：2026年2月21日19点16分
 */
@SpringBootTest
class LiuRenUserCaseTest {

    @Autowired
    private LiuRenService liuRenService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 用户案例：2026年2月21日19点16分
     * 日柱：丙寅日
     * 时辰：戌时（19-21点）
     * 日干丙，根据"丙丁猪鸡位"，夜贵酉
     * 酉在巳午未申酉戌范围，逆排
     * 预期：酉-贵人、申-螣蛇、未-朱雀、午-六合、巳-勾陈、辰-青龙、卯-天空、寅-白虎、丑-太常、子-玄武、亥-太阴、戌-天后
     */
    @Test
    void testUserCase_20260221_1916() throws Exception {
        // LocalDateTime dateTime = LocalDateTime.of(2016, 6, 21, 17, 37);
        // LocalDateTime dateTime = LocalDateTime.of(2025, 9, 17, 17, 9);
        // LocalDateTime dateTime = LocalDateTime.of(2026, 2, 21, 19, 16);
        // LocalDateTime dateTime = LocalDateTime.of(2023, 4, 11, 11, 5);
        LocalDateTime dateTime = LocalDateTime.of(2026, 6, 21, 21, 43);
        
        String result = liuRenService.generateCourseInfo(dateTime);
        JsonNode jsonNode = objectMapper.readTree(result);

        System.out.println("\n" + "=".repeat(70));
        System.out.println("用户案例验证：2026年2月21日19点16分");
        System.out.println("=".repeat(70));

        // 基本信息
        JsonNode basicInfo = jsonNode.get("basicInfo");
        String yearPillar = basicInfo.get("yearPillar").asText();
        String monthPillar = basicInfo.get("monthPillar").asText();
        String dayPillar = basicInfo.get("dayPillar").asText();
        String hourPillar = basicInfo.get("hourPillar").asText();
        String yueJiang = basicInfo.get("yueJiang").asText();

        System.out.println("\n【基本信息】");
        System.out.println("年柱：" + yearPillar);
        System.out.println("月柱：" + monthPillar);
        System.out.println("日柱：" + dayPillar + " (日干：" + dayPillar.substring(0, 1) + ")");
        System.out.println("时柱：" + hourPillar);
        System.out.println("月将：" + yueJiang);

        String dayGan = dayPillar.substring(0, 1);
        System.out.println("\n【贵人判断】");
        System.out.println("日干：" + dayGan);
        System.out.println("时辰：19点（夜晚）");

        String expectedGuiRen = "酉";
        if ("丙".equals(dayGan) || "丁".equals(dayGan)) {
            System.out.println("口诀：丙丁猪鸡位");
            System.out.println("夜贵：酉");
        }

        System.out.println("贵人地支：" + expectedGuiRen);
        System.out.println("酉在巳午未申酉戌范围，应逆排");

        // 天地盘
        System.out.println("\n【天地盘】");
        System.out.println("地盘\t天盘\t天将");
        System.out.println("-".repeat(60));

        JsonNode positions = jsonNode.get("tianDiPan").get("positions");

        // 验证天将排列
        System.out.println("\n【天将验证】");
        String[] expectedTianJiang = {
            "玄武",    // 子
            "太常",    // 丑
            "白虎",    // 寅
            "天空",    // 卯
            "青龙",    // 辰
            "勾陈",    // 巳
            "六合",    // 午
            "朱雀",    // 未
            "螣蛇",    // 申
            "天乙贵人", // 酉
            "天后",    // 戌
            "太阴"     // 亥
        };

        String[] zhiArray = {"子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥"};

        System.out.println("预期天将排列（从贵人酉逆排）：");
        System.out.println("酉-天乙贵人、申-螣蛇、未-朱雀、午-六合、巳-勾陈、辰-青龙");
        System.out.println("卯-天空、寅-白虎、丑-太常、子-玄武、亥-太阴、戌-天后");

        System.out.println("\n实际结果对比：");
        System.out.println("地支\t预期天将\t\t实际天将\t\t匹配");
        System.out.println("-".repeat(70));

        boolean allMatch = true;
        for (int i = 0; i < 12; i++) {
            // 找到天盘为该地支的位置
            String targetZhi = zhiArray[i];
            String actualTianJiang = null;

            for (int j = 0; j < 12; j++) {
                JsonNode pos = positions.get(j);
                if (targetZhi.equals(pos.get("tianPan").asText())) {
                    actualTianJiang = pos.get("tianJiang").asText();
                    break;
                }
            }

            boolean match = expectedTianJiang[i].equals(actualTianJiang);
            allMatch = allMatch && match;

            System.out.println(String.format("%s\t%s\t\t%s\t\t%s",
                targetZhi,
                expectedTianJiang[i],
                actualTianJiang,
                match ? "✓" : "✗"));
        }

        System.out.println("\n整体验证：" + (allMatch ? "✓ 全部匹配" : "✗ 存在不匹配"));

        // 显示完整天地盘
        System.out.println("\n【完整天地盘】");
        System.out.println("地盘\t天盘\t天将");
        System.out.println("-".repeat(60));
        for (int i = 0; i < 12; i++) {
            JsonNode pos = positions.get(i);
            System.out.println(String.format("%s\t%s\t%s",
                pos.get("diPan").asText(),
                pos.get("tianPan").asText(),
                pos.get("tianJiang").asText()));
        }

        // 四课三传
        System.out.println("\n【四课】");
        JsonNode siKe = jsonNode.get("siKe");
        System.out.println("第一课：" + siKe.get("diYiKe").get("ganZhi").asText());
        System.out.println("第二课：" + siKe.get("diErKe").get("ganZhi").asText());
        System.out.println("第三课：" + siKe.get("diSanKe").get("ganZhi").asText());
        System.out.println("第四课：" + siKe.get("diSiKe").get("ganZhi").asText());

        System.out.println("\n【三传】");
        JsonNode sanChuan = jsonNode.get("sanChuan");

        JsonNode chuChuan = sanChuan.get("chuChuan");
        System.out.println(String.format("初传：%s - %s - %s - %s",
            chuChuan.get("liuQin").asText(),
            chuChuan.get("ganZhi").asText(),
            chuChuan.get("tianJiang").asText(),
            chuChuan.get("shenSha").asText()));

        JsonNode zhongChuan = sanChuan.get("zhongChuan");
        System.out.println(String.format("中传：%s - %s - %s - %s",
            zhongChuan.get("liuQin").asText(),
            zhongChuan.get("ganZhi").asText(),
            zhongChuan.get("tianJiang").asText(),
            zhongChuan.get("shenSha").asText()));

        JsonNode moChuan = sanChuan.get("moChuan");
        System.out.println(String.format("末传：%s - %s - %s - %s",
            moChuan.get("liuQin").asText(),
            moChuan.get("ganZhi").asText(),
            moChuan.get("tianJiang").asText(),
            moChuan.get("shenSha").asText()));

        System.out.println("\n" + "=".repeat(70));

        // 完整JSON
        System.out.println("\n【完整JSON】");
        System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode));
    }
}
