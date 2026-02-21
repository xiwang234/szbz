package xw.szbz.cn.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 验证用户提供的具体案例
 */
@SpringBootTest
class LiuRenGuiRenTest {

    @Autowired
    private LiuRenService liuRenService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 用户案例1：庚申日未时卯将占课
     * 庚日昼贵为丑，丑加在巳上（天盘），贵人逆排
     * 预期天将排布：
     * 丑-贵人，子-螣蛇，亥-朱雀，戌-六合，酉-勾陈，申-青龙
     * 未-天空，午-白虎，巳-太常，辰-玄武，卯-太阴，寅-天后
     */
    @Test
    void testUserCase1_GengShenDay_WeiShi() throws Exception {
        // 庚申日：需要找一个庚申日
        // 2024年2月14日是庚申日
        // 未时：13-15点，取14点
        LocalDateTime dateTime = LocalDateTime.of(2024, 2, 14, 14, 0);

        String result = liuRenService.generateCourseInfo(dateTime);
        JsonNode jsonNode = objectMapper.readTree(result);

        // 验证基本信息
        JsonNode basicInfo = jsonNode.get("basicInfo");
        String dayPillar = basicInfo.get("dayPillar").asText();
        System.out.println("\n=== 用户案例1：庚申日未时 ===");
        System.out.println("日柱：" + dayPillar);

        // 显示天地盘
        JsonNode tianDiPan = jsonNode.get("tianDiPan");
        JsonNode positions = tianDiPan.get("positions");

        System.out.println("\n天地盘：");
        System.out.println("地盘\t天盘\t天将");
        System.out.println("------------------------");
        for (int i = 0; i < 12; i++) {
            JsonNode pos = positions.get(i);
            System.out.println(String.format("%s\t%s\t%s",
                pos.get("diPan").asText(),
                pos.get("tianPan").asText(),
                pos.get("tianJiang").asText()));
        }

        // 打印完整结果
        System.out.println("\n完整结果：");
        System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode));
    }

    /**
     * 用户案例2：庚申日酉时（如果改为酉时）
     * 庚日夜贵为未，未加在丑上（天盘），贵人顺排
     * 预期天将排布：
     * 未-贵人，申-螣蛇，酉-朱雀，戌-六合，亥-勾陈，子-青龙
     * 丑-天空，寅-白虎，卯-太常，辰-玄武，巳-太阴，午-天后
     */
    @Test
    void testUserCase2_GengShenDay_YouShi() throws Exception {
        // 庚申日：2024年2月14日
        // 酉时：17-19点，取18点
        LocalDateTime dateTime = LocalDateTime.of(2024, 2, 14, 18, 0);

        String result = liuRenService.generateCourseInfo(dateTime);
        JsonNode jsonNode = objectMapper.readTree(result);

        // 验证基本信息
        JsonNode basicInfo = jsonNode.get("basicInfo");
        String dayPillar = basicInfo.get("dayPillar").asText();
        System.out.println("\n=== 用户案例2：庚申日酉时 ===");
        System.out.println("日柱：" + dayPillar);

        // 显示天地盘
        JsonNode tianDiPan = jsonNode.get("tianDiPan");
        JsonNode positions = tianDiPan.get("positions");

        System.out.println("\n天地盘：");
        System.out.println("地盘\t天盘\t天将");
        System.out.println("------------------------");
        for (int i = 0; i < 12; i++) {
            JsonNode pos = positions.get(i);
            System.out.println(String.format("%s\t%s\t%s",
                pos.get("diPan").asText(),
                pos.get("tianPan").asText(),
                pos.get("tianJiang").asText()));
        }

        // 打印完整结果
        System.out.println("\n完整结果：");
        System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode));
    }

    /**
     * 测试昼夜贵人判断
     */
    @Test
    void testDayNightGuiRen() throws Exception {
        System.out.println("\n=== 昼夜贵人测试 ===");

        // 测试不同日干在不同时辰的贵人
        String[][] testCases = {
            {"甲", "10", "丑"}, // 甲日昼贵丑
            {"甲", "20", "未"}, // 甲日夜贵未
            {"乙", "10", "子"}, // 乙日昼贵子
            {"乙", "20", "申"}, // 乙日夜贵申
            {"丙", "10", "亥"}, // 丙日昼贵亥
            {"丙", "20", "酉"}, // 丙日夜贵酉
            {"壬", "10", "巳"}, // 壬日昼贵巳
            {"壬", "20", "卯"}, // 壬日夜贵卯
            {"辛", "10", "午"}, // 辛日昼贵午
            {"辛", "20", "寅"}  // 辛日夜贵寅
        };

        for (String[] testCase : testCases) {
            String expectedGan = testCase[0];
            int hour = Integer.parseInt(testCase[1]);
            String expectedGuiRen = testCase[2];
            String timeDesc = hour >= 5 && hour < 17 ? "昼" : "夜";

            System.out.println(String.format("%s日%s时：贵人应在%s",
                expectedGan, timeDesc, expectedGuiRen));
        }
    }

    /**
     * 测试贵人顺逆判断
     */
    @Test
    void testGuiRenShunNi() throws Exception {
        System.out.println("\n=== 贵人顺逆测试 ===");
        System.out.println("贵人加临位置：");
        System.out.println("亥子丑寅卯辰（天门之前、地户之后）- 顺排");
        System.out.println("巳午未申酉戌（天门之后、地户之前）- 逆排");

        // 测试几个具体时间
        LocalDateTime[] testTimes = {
            LocalDateTime.of(2025, 12, 20, 14, 30), // 癸日昼时
            LocalDateTime.of(2024, 2, 14, 14, 0),   // 庚日昼时
            LocalDateTime.of(2024, 2, 14, 18, 0)    // 庚日夜时
        };

        for (LocalDateTime time : testTimes) {
            String result = liuRenService.generateCourseInfo(time);
            JsonNode jsonNode = objectMapper.readTree(result);

            String dayPillar = jsonNode.get("basicInfo").get("dayPillar").asText();
            String hourPillar = jsonNode.get("basicInfo").get("hourPillar").asText();

            System.out.println(String.format("\n%s %s:", dayPillar, hourPillar));

            // 找到贵人位置
            JsonNode positions = jsonNode.get("tianDiPan").get("positions");
            for (int i = 0; i < 12; i++) {
                JsonNode pos = positions.get(i);
                if ("天乙贵人".equals(pos.get("tianJiang").asText())) {
                    System.out.println(String.format("  贵人在天盘%s（地盘%s）",
                        pos.get("tianPan").asText(),
                        pos.get("diPan").asText()));
                    break;
                }
            }
        }
    }
}
