package xw.szbz.cn.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

/**
 * 验证天地盘构建方法：月将加临占时
 */
@SpringBootTest
class LiuRenTianDiPanTest {

    @Autowired
    private LiuRenService liuRenService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 验证天盘构建：月将加临占时
     * 例如：月将卯加临地盘巳
     * 预期结果：
     * 地盘巳 → 天盘卯
     * 地盘午 → 天盘辰
     * 地盘未 → 天盘巳
     * 地盘申 → 天盘午
     * 地盘酉 → 天盘未
     * 地盘戌 → 天盘申
     * 地盘亥 → 天盘酉
     * 地盘子 → 天盘戌
     * 地盘丑 → 天盘亥
     * 地盘寅 → 天盘子
     * 地盘卯 → 天盘丑
     * 地盘辰 → 天盘寅
     */
    @Test
    void testTianPanConstruction() throws Exception {
        // 2021年11月4日巳时
        // 注意：实际月将要根据节气确定，这里假设月将是卯
        LocalDateTime dateTime = LocalDateTime.of(2021, 11, 4, 10, 0); // 巳时：9-11点

        String result = liuRenService.generateCourseInfo(dateTime);
        JsonNode jsonNode = objectMapper.readTree(result);

        System.out.println("\n=== 天盘构建验证：月将加临占时 ===");
        System.out.println("时间：2021年11月4日巳时");

        JsonNode basicInfo = jsonNode.get("basicInfo");
        String yueJiang = basicInfo.get("yueJiang").asText();
        String hourPillar = basicInfo.get("hourPillar").asText();

        System.out.println("月将：" + yueJiang);
        System.out.println("时柱：" + hourPillar);

        // 显示天地盘
        JsonNode tianDiPan = jsonNode.get("tianDiPan");
        JsonNode positions = tianDiPan.get("positions");

        System.out.println("\n天地盘对应关系：");
        System.out.println("地盘\t→\t天盘\t天将");
        System.out.println("----------------------------------------");

        for (int i = 0; i < 12; i++) {
            JsonNode pos = positions.get(i);
            System.out.println(String.format("%s\t→\t%s\t%s",
                pos.get("diPan").asText(),
                pos.get("tianPan").asText(),
                pos.get("tianJiang").asText()));
        }

        // 如果月将是卯，占时是巳，验证关键对应关系
        if ("卯".equals(yueJiang)) {
            String tianPanAtSi = positions.get(5).get("tianPan").asText(); // 地盘巳是index 5
            System.out.println("\n关键验证：地盘巳 → 天盘" + tianPanAtSi + " (预期：卯)");

            String tianPanAtWu = positions.get(6).get("tianPan").asText(); // 地盘午是index 6
            System.out.println("关键验证：地盘午 → 天盘" + tianPanAtWu + " (预期：辰)");
        }
    }

    /**
     * 测试不同月将和占时的组合
     */
    @Test
    void testDifferentYueJiangAndTime() throws Exception {
        System.out.println("\n=== 测试不同月将和占时组合 ===");

        // 测试几个不同的时间
        LocalDateTime[] testTimes = {
            LocalDateTime.of(2025, 12, 20, 14, 30), // 未时
            LocalDateTime.of(2025, 6, 15, 10, 0),   // 巳时
            LocalDateTime.of(2025, 3, 20, 18, 0),   // 酉时
            LocalDateTime.of(2025, 9, 10, 2, 0)     // 丑时
        };

        for (LocalDateTime time : testTimes) {
            String result = liuRenService.generateCourseInfo(time);
            JsonNode jsonNode = objectMapper.readTree(result);

            JsonNode basicInfo = jsonNode.get("basicInfo");
            String yueJiang = basicInfo.get("yueJiang").asText();
            String hourPillar = basicInfo.get("hourPillar").asText();
            String hourZhi = hourPillar.substring(1); // 取时支

            System.out.println(String.format("\n时间：%s, 月将：%s, 占时：%s",
                time, yueJiang, hourZhi));

            // 显示月将加临占时的结果
            JsonNode positions = jsonNode.get("tianDiPan").get("positions");
            int hourIndex = getZhiIndex(hourZhi);
            if (hourIndex >= 0) {
                JsonNode pos = positions.get(hourIndex);
                System.out.println(String.format("  地盘%s → 天盘%s (应等于月将%s)",
                    pos.get("diPan").asText(),
                    pos.get("tianPan").asText(),
                    yueJiang));
            }
        }
    }

    /**
     * 测试天将排布的顺逆
     */
    @Test
    void testTianJiangShunNi() throws Exception {
        System.out.println("\n=== 测试天将顺逆排布 ===");

        // 测试昼时和夜时
        LocalDateTime dayTime = LocalDateTime.of(2025, 12, 20, 14, 30);  // 未时，白天
        LocalDateTime nightTime = LocalDateTime.of(2025, 12, 20, 20, 0); // 戌时，夜晚

        String[] times = {"昼时", "夜时"};
        LocalDateTime[] dateTimes = {dayTime, nightTime};

        for (int i = 0; i < 2; i++) {
            String result = liuRenService.generateCourseInfo(dateTimes[i]);
            JsonNode jsonNode = objectMapper.readTree(result);

            JsonNode basicInfo = jsonNode.get("basicInfo");
            String dayPillar = basicInfo.get("dayPillar").asText();
            String dayGan = dayPillar.substring(0, 1);

            System.out.println(String.format("\n%s，日干：%s", times[i], dayGan));

            // 找到贵人位置
            JsonNode positions = jsonNode.get("tianDiPan").get("positions");
            String guiRenTianPan = null;
            String guiRenDiPan = null;

            for (int j = 0; j < 12; j++) {
                JsonNode pos = positions.get(j);
                if ("天乙贵人".equals(pos.get("tianJiang").asText())) {
                    guiRenTianPan = pos.get("tianPan").asText();
                    guiRenDiPan = pos.get("diPan").asText();
                    break;
                }
            }

            System.out.println(String.format("  贵人在天盘%s（地盘%s）", guiRenTianPan, guiRenDiPan));

            // 判断顺逆
            int guiRenIndex = getZhiIndex(guiRenTianPan);
            boolean isShun = (guiRenIndex >= 11 || guiRenIndex <= 4);
            System.out.println(String.format("  贵人在天盘%s，%s排",
                guiRenTianPan, isShun ? "顺" : "逆"));

            // 显示天将序列
            System.out.println("  天将序列：");
            for (int j = 0; j < 12; j++) {
                JsonNode pos = positions.get(j);
                System.out.println(String.format("    天盘%s → %s",
                    pos.get("tianPan").asText(),
                    pos.get("tianJiang").asText()));
            }
        }
    }

    /**
     * 完整示例：显示一个具体时间的完整天地盘信息
     */
    @Test
    void testCompleteExample() throws Exception {
        LocalDateTime dateTime = LocalDateTime.of(2026, 2, 21, 18, 39);

        String result = liuRenService.generateCourseInfo(dateTime);
        JsonNode jsonNode = objectMapper.readTree(result);

        System.out.println("\n" + "=".repeat(60));
        System.out.println("完整示例：2026年2月21日18时39分");
        System.out.println("=".repeat(60));

        // 基本信息
        JsonNode basicInfo = jsonNode.get("basicInfo");
        System.out.println("\n【基本信息】");
        System.out.println("年柱：" + basicInfo.get("yearPillar").asText());
        System.out.println("月柱：" + basicInfo.get("monthPillar").asText());
        System.out.println("日柱：" + basicInfo.get("dayPillar").asText());
        System.out.println("时柱：" + basicInfo.get("hourPillar").asText());
        System.out.println("月将：" + basicInfo.get("yueJiang").asText());

        // 天地盘
        System.out.println("\n【天地盘】");
        System.out.println("地盘\t天盘\t天将");
        System.out.println("-".repeat(50));
        JsonNode positions = jsonNode.get("tianDiPan").get("positions");
        for (int i = 0; i < 12; i++) {
            JsonNode pos = positions.get(i);
            System.out.println(String.format("%s\t%s\t%s",
                pos.get("diPan").asText(),
                pos.get("tianPan").asText(),
                pos.get("tianJiang").asText()));
        }

        // 四课
        System.out.println("\n【四课】");
        JsonNode siKe = jsonNode.get("siKe");
        System.out.println("第一课：" + siKe.get("diYiKe").get("ganZhi").asText());
        System.out.println("第二课：" + siKe.get("diErKe").get("ganZhi").asText());
        System.out.println("第三课：" + siKe.get("diSanKe").get("ganZhi").asText());
        System.out.println("第四课：" + siKe.get("diSiKe").get("ganZhi").asText());

        // 三传
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

        System.out.println("\n" + "=".repeat(60));
    }

    /**
     * 辅助方法：获取地支的index
     */
    private int getZhiIndex(String zhi) {
        String[] zhiArray = {"子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥"};
        for (int i = 0; i < 12; i++) {
            if (zhiArray[i].equals(zhi)) {
                return i;
            }
        }
        return -1;
    }
}
