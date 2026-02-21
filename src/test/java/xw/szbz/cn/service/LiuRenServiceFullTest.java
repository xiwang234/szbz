package xw.szbz.cn.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * LiuRenService完整测试类 - 测试天地盘、四课、三传
 */
@SpringBootTest
class LiuRenServiceFullTest {

    @Autowired
    private LiuRenService liuRenService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 测试生成完整大六壬信息
     * 测试时间：2025年12月20日14时30分
     */
    @Test
    void testGenerateCourseInfo_Full_20251220() throws Exception {
        LocalDateTime testTime = LocalDateTime.of(2025, 9, 17, 17, 9);

        String result = liuRenService.generateCourseInfo(testTime);

        assertNotNull(result, "大六壬结果不应为null");
        assertFalse(result.isEmpty(), "大六壬结果不应为空");

        // 解析JSON
        JsonNode jsonNode = objectMapper.readTree(result);

        // 验证基本信息
        assertTrue(jsonNode.has("basicInfo"), "应包含基本信息");
        JsonNode basicInfo = jsonNode.get("basicInfo");
        assertTrue(basicInfo.has("year"), "应包含年份");
        assertTrue(basicInfo.has("yearPillar"), "应包含年柱");
        assertTrue(basicInfo.has("monthPillar"), "应包含月柱");
        assertTrue(basicInfo.has("dayPillar"), "应包含日柱");
        assertTrue(basicInfo.has("hourPillar"), "应包含时柱");
        assertTrue(basicInfo.has("yueJiang"), "应包含月将");

        // 验证天地盘
        assertTrue(jsonNode.has("tianDiPan"), "应包含天地盘");
        JsonNode tianDiPan = jsonNode.get("tianDiPan");
        assertTrue(tianDiPan.has("positions"), "天地盘应包含盘位");
        JsonNode positions = tianDiPan.get("positions");
        assertEquals(12, positions.size(), "天地盘应有12个盘位");

        // 验证每个盘位的结构
        for (int i = 0; i < 12; i++) {
            JsonNode position = positions.get(i);
            assertTrue(position.has("diPan"), "盘位应包含地盘");
            assertTrue(position.has("tianPan"), "盘位应包含天盘");
            assertTrue(position.has("tianJiang"), "盘位应包含天将");

            String diPan = position.get("diPan").asText();
            String tianPan = position.get("tianPan").asText();
            String tianJiang = position.get("tianJiang").asText();

            assertNotNull(diPan, "地盘不应为null");
            assertNotNull(tianPan, "天盘不应为null");
            assertNotNull(tianJiang, "天将不应为null");
        }

        // 验证四课
        assertTrue(jsonNode.has("siKe"), "应包含四课");
        JsonNode siKe = jsonNode.get("siKe");
        assertTrue(siKe.has("diYiKe"), "应包含第一课");
        assertTrue(siKe.has("diErKe"), "应包含第二课");
        assertTrue(siKe.has("diSanKe"), "应包含第三课");
        assertTrue(siKe.has("diSiKe"), "应包含第四课");

        // 验证每课的结构
        validateKe(siKe.get("diYiKe"), "第一课");
        validateKe(siKe.get("diErKe"), "第二课");
        validateKe(siKe.get("diSanKe"), "第三课");
        validateKe(siKe.get("diSiKe"), "第四课");

        // 验证三传
        assertTrue(jsonNode.has("sanChuan"), "应包含三传");
        JsonNode sanChuan = jsonNode.get("sanChuan");
        assertTrue(sanChuan.has("chuChuan"), "应包含初传");
        assertTrue(sanChuan.has("zhongChuan"), "应包含中传");
        assertTrue(sanChuan.has("moChuan"), "应包含末传");

        // 验证每传的结构
        validateChuan(sanChuan.get("chuChuan"), "初传");
        validateChuan(sanChuan.get("zhongChuan"), "中传");
        validateChuan(sanChuan.get("moChuan"), "末传");

        // 打印完整结果
        System.out.println("\n=== 2025年12月20日14时30分 大六壬完整信息 ===");
        System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode));
    }

    /**
     * 测试当前时间生成大六壬信息
     */
    @Test
    void testGenerateCourseInfo_Current() throws Exception {
        String result = liuRenService.generateCourseInfo();

        assertNotNull(result, "大六壬结果不应为null");
        assertFalse(result.isEmpty(), "大六壬结果不应为空");

        // 解析JSON验证结构
        JsonNode jsonNode = objectMapper.readTree(result);
        assertTrue(jsonNode.has("basicInfo"), "应包含基本信息");
        assertTrue(jsonNode.has("tianDiPan"), "应包含天地盘");
        assertTrue(jsonNode.has("siKe"), "应包含四课");
        assertTrue(jsonNode.has("sanChuan"), "应包含三传");

        System.out.println("\n=== 当前时间 大六壬完整信息 ===");
        System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode));
    }

    /**
     * 测试不同时辰的大六壬信息
     */
    @Test
    void testGenerateCourseInfo_DifferentHours() throws Exception {
        System.out.println("\n=== 测试不同时辰 ===");

        // 测试12个时辰
        for (int hour = 0; hour < 24; hour += 2) {
            LocalDateTime testTime = LocalDateTime.of(2025, 6, 15, hour, 0);
            String result = liuRenService.generateCourseInfo(testTime);

            assertNotNull(result);
            JsonNode jsonNode = objectMapper.readTree(result);

            String hourPillar = jsonNode.get("basicInfo").get("hourPillar").asText();
            String yueJiang = jsonNode.get("basicInfo").get("yueJiang").asText();

            System.out.println(String.format("时辰: %02d:00, 时柱: %s, 月将: %s",
                hour, hourPillar, yueJiang));
        }
    }

    /**
     * 测试不同月份的大六壬信息
     */
    @Test
    void testGenerateCourseInfo_DifferentMonths() throws Exception {
        System.out.println("\n=== 测试不同月份 ===");

        for (int month = 1; month <= 12; month++) {
            LocalDateTime testTime = LocalDateTime.of(2025, month, 15, 12, 0);
            String result = liuRenService.generateCourseInfo(testTime);

            assertNotNull(result);
            JsonNode jsonNode = objectMapper.readTree(result);

            String monthPillar = jsonNode.get("basicInfo").get("monthPillar").asText();
            String yueJiang = jsonNode.get("basicInfo").get("yueJiang").asText();

            System.out.println(String.format("%d月: 月柱: %s, 月将: %s",
                month, monthPillar, yueJiang));
        }
    }

    /**
     * 测试天地盘正确性
     */
    @Test
    void testTianDiPan() throws Exception {
        LocalDateTime testTime = LocalDateTime.of(2025, 6, 15, 14, 0);
        String result = liuRenService.generateCourseInfo(testTime);

        JsonNode jsonNode = objectMapper.readTree(result);
        JsonNode tianDiPan = jsonNode.get("tianDiPan");
        JsonNode positions = tianDiPan.get("positions");

        System.out.println("\n=== 天地盘详细信息 ===");
        System.out.println("地盘\t天盘\t天将");
        System.out.println("------------------------");

        for (int i = 0; i < 12; i++) {
            JsonNode position = positions.get(i);
            String diPan = position.get("diPan").asText();
            String tianPan = position.get("tianPan").asText();
            String tianJiang = position.get("tianJiang").asText();

            System.out.println(String.format("%s\t%s\t%s", diPan, tianPan, tianJiang));

            // 验证地盘顺序固定
            String[] expectedDiPan = {"子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥"};
            assertEquals(expectedDiPan[i], diPan, "地盘顺序应固定");
        }
    }

    /**
     * 测试四课正确性
     */
    @Test
    void testSiKe() throws Exception {
        LocalDateTime testTime = LocalDateTime.of(2025, 6, 15, 14, 0);
        String result = liuRenService.generateCourseInfo(testTime);

        JsonNode jsonNode = objectMapper.readTree(result);
        JsonNode siKe = jsonNode.get("siKe");

        System.out.println("\n=== 四课详细信息 ===");

        JsonNode diYiKe = siKe.get("diYiKe");
        System.out.println("第一课: " + diYiKe.get("ganZhi").asText());

        JsonNode diErKe = siKe.get("diErKe");
        System.out.println("第二课: " + diErKe.get("ganZhi").asText());

        JsonNode diSanKe = siKe.get("diSanKe");
        System.out.println("第三课: " + diSanKe.get("ganZhi").asText());

        JsonNode diSiKe = siKe.get("diSiKe");
        System.out.println("第四课: " + diSiKe.get("ganZhi").asText());
    }

    /**
     * 测试三传正确性
     */
    @Test
    void testSanChuan() throws Exception {
        LocalDateTime testTime = LocalDateTime.of(2025, 6, 15, 14, 0);
        String result = liuRenService.generateCourseInfo(testTime);

        JsonNode jsonNode = objectMapper.readTree(result);
        JsonNode sanChuan = jsonNode.get("sanChuan");

        System.out.println("\n=== 三传详细信息 ===");

        JsonNode chuChuan = sanChuan.get("chuChuan");
        System.out.println(String.format("初传: %s-%s-%s-%s",
            chuChuan.get("liuQin").asText(),
            chuChuan.get("ganZhi").asText(),
            chuChuan.get("tianJiang").asText(),
            chuChuan.get("shenSha").asText()));

        JsonNode zhongChuan = sanChuan.get("zhongChuan");
        System.out.println(String.format("中传: %s-%s-%s-%s",
            zhongChuan.get("liuQin").asText(),
            zhongChuan.get("ganZhi").asText(),
            zhongChuan.get("tianJiang").asText(),
            zhongChuan.get("shenSha").asText()));

        JsonNode moChuan = sanChuan.get("moChuan");
        System.out.println(String.format("末传: %s-%s-%s-%s",
            moChuan.get("liuQin").asText(),
            moChuan.get("ganZhi").asText(),
            moChuan.get("tianJiang").asText(),
            moChuan.get("shenSha").asText()));
    }

    /**
     * 测试特定案例：2026年2月7日
     */
    @Test
    void testSpecificCase_20260207() throws Exception {
        LocalDateTime testTime = LocalDateTime.of(2026, 2, 7, 15, 30);

        String result = liuRenService.generateCourseInfo(testTime);

        assertNotNull(result);
        JsonNode jsonNode = objectMapper.readTree(result);

        System.out.println("\n=== 2026年2月7日15时30分 大六壬信息 ===");
        System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode));

        // 验证结构完整性
        assertTrue(jsonNode.has("basicInfo"));
        assertTrue(jsonNode.has("tianDiPan"));
        assertTrue(jsonNode.has("siKe"));
        assertTrue(jsonNode.has("sanChuan"));
    }

    /**
     * 验证课的结构
     */
    private void validateKe(JsonNode ke, String keName) {
        assertTrue(ke.has("ganZhi"), keName + "应包含干支");
        assertTrue(ke.has("tianJiang"), keName + "应包含天将");
        assertTrue(ke.has("description"), keName + "应包含描述");

        String ganZhi = ke.get("ganZhi").asText();
        String tianJiang = ke.get("tianJiang").asText();
        String description = ke.get("description").asText();

        assertNotNull(ganZhi, keName + "干支不应为null");
        assertNotNull(tianJiang, keName + "天将不应为null");
        assertNotNull(description, keName + "描述不应为null");
        // ganZhi现在只包含干支部分，不再包含"呈XX"
        assertEquals(2, ganZhi.length(), keName + "干支应为两个字符");
    }

    /**
     * 验证传的结构
     */
    private void validateChuan(JsonNode chuan, String chuanName) {
        assertTrue(chuan.has("liuQin"), chuanName + "应包含六亲");
        assertTrue(chuan.has("ganZhi"), chuanName + "应包含干支");
        assertTrue(chuan.has("tianJiang"), chuanName + "应包含天将");
        assertTrue(chuan.has("shenSha"), chuanName + "应包含神煞");

        String liuQin = chuan.get("liuQin").asText();
        String ganZhi = chuan.get("ganZhi").asText();
        String tianJiang = chuan.get("tianJiang").asText();
        String shenSha = chuan.get("shenSha").asText();

        assertNotNull(liuQin, chuanName + "六亲不应为null");
        assertNotNull(ganZhi, chuanName + "干支不应为null");
        assertNotNull(tianJiang, chuanName + "天将不应为null");
        assertNotNull(shenSha, chuanName + "神煞不应为null");

        // 验证六亲是否在有效值范围内
        String[] validLiuQin = {"父母", "兄弟", "子孙", "妻财", "官鬼"};
        boolean validLiuQinFound = false;
        for (String valid : validLiuQin) {
            if (liuQin.equals(valid)) {
                validLiuQinFound = true;
                break;
            }
        }
        assertTrue(validLiuQinFound, chuanName + "六亲应为有效值: " + liuQin);
    }
}
