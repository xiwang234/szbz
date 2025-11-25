package xw.szbz.cn.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import xw.szbz.cn.model.BaZiRequest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * BaZiController 集成测试
 */
@SpringBootTest
@AutoConfigureMockMvc
class BaZiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/bazi/generate - 核心测试 1984年11月23日23点")
    void testGenerateBaZi_19841123_2325() throws Exception {
        BaZiRequest request = new BaZiRequest("男", 1984, 11, 23, 23);

        mockMvc.perform(post("/api/bazi/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gender").value("男"))
                .andExpect(jsonPath("$.yearPillar.fullName").value("甲子"))
                .andExpect(jsonPath("$.monthPillar.fullName").value("乙亥"))
                .andExpect(jsonPath("$.dayPillar.fullName").value("壬戌"))
                .andExpect(jsonPath("$.hourPillar.fullName").value("庚子"))
                .andExpect(jsonPath("$.fullBaZi").value("甲子 乙亥 壬戌 庚子"))
                .andExpect(jsonPath("$.birthInfo.adjusted").value(true))
                .andExpect(jsonPath("$.birthInfo.shiChen").value("子时"))
                .andExpect(jsonPath("$.birthInfo.dayPillarDate").value("1984-11-24"));
    }

    @Test
    @DisplayName("POST /api/bazi/generate - 成功生成八字")
    void testGenerateBaZi_Success() throws Exception {
        BaZiRequest request = new BaZiRequest("男", 2024, 6, 15, 10);

        mockMvc.perform(post("/api/bazi/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gender").value("男"))
                .andExpect(jsonPath("$.yearPillar").exists())
                .andExpect(jsonPath("$.monthPillar").exists())
                .andExpect(jsonPath("$.dayPillar").exists())
                .andExpect(jsonPath("$.hourPillar").exists())
                .andExpect(jsonPath("$.fullBaZi").exists());
    }

    @Test
    @DisplayName("GET /api/bazi/generate - 成功生成八字")
    void testGenerateBaZiGet_Success() throws Exception {
        mockMvc.perform(get("/api/bazi/generate")
                        .param("gender", "女")
                        .param("year", "2024")
                        .param("month", "8")
                        .param("day", "20")
                        .param("hour", "14"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gender").value("女"))
                .andExpect(jsonPath("$.yearPillar.fullName").exists())
                .andExpect(jsonPath("$.monthPillar.fullName").exists())
                .andExpect(jsonPath("$.dayPillar.fullName").exists())
                .andExpect(jsonPath("$.hourPillar.fullName").exists());
    }

    @Test
    @DisplayName("GET /api/bazi/generate - 1984年11月23日23点")
    void testGenerateBaZiGet_19841123_2325() throws Exception {
        mockMvc.perform(get("/api/bazi/generate")
                        .param("gender", "男")
                        .param("year", "1984")
                        .param("month", "11")
                        .param("day", "23")
                        .param("hour", "23"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullBaZi").value("甲子 乙亥 壬戌 庚子"))
                .andExpect(jsonPath("$.birthInfo.adjusted").value(true));
    }

    @Test
    @DisplayName("POST /api/bazi/generate - 无效性别")
    void testGenerateBaZi_InvalidGender() throws Exception {
        BaZiRequest request = new BaZiRequest("", 2024, 6, 15, 10);

        mockMvc.perform(post("/api/bazi/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("性别不能为空"));
    }

    @Test
    @DisplayName("POST /api/bazi/generate - 无效年份")
    void testGenerateBaZi_InvalidYear() throws Exception {
        BaZiRequest request = new BaZiRequest("男", 1800, 6, 15, 10);

        mockMvc.perform(post("/api/bazi/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("年份必须在1900-2100之间"));
    }

    @Test
    @DisplayName("POST /api/bazi/generate - 无效月份")
    void testGenerateBaZi_InvalidMonth() throws Exception {
        BaZiRequest request = new BaZiRequest("男", 2024, 13, 15, 10);

        mockMvc.perform(post("/api/bazi/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("月份必须在1-12之间"));
    }

    @Test
    @DisplayName("POST /api/bazi/generate - 无效日期")
    void testGenerateBaZi_InvalidDay() throws Exception {
        BaZiRequest request = new BaZiRequest("男", 2024, 6, 32, 10);

        mockMvc.perform(post("/api/bazi/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("日期必须在1-31之间"));
    }

    @Test
    @DisplayName("POST /api/bazi/generate - 无效小时")
    void testGenerateBaZi_InvalidHour() throws Exception {
        BaZiRequest request = new BaZiRequest("男", 2024, 6, 15, 25);

        mockMvc.perform(post("/api/bazi/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("小时必须在0-23之间"));
    }

    @Test
    @DisplayName("POST /api/bazi/generate - 英文性别male")
    void testGenerateBaZi_GenderMaleEnglish() throws Exception {
        BaZiRequest request = new BaZiRequest("male", 2024, 6, 15, 10);

        mockMvc.perform(post("/api/bazi/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gender").value("男"));
    }

    @Test
    @DisplayName("POST /api/bazi/generate - 英文性别female")
    void testGenerateBaZi_GenderFemaleEnglish() throws Exception {
        BaZiRequest request = new BaZiRequest("female", 2024, 6, 15, 10);

        mockMvc.perform(post("/api/bazi/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gender").value("女"));
    }

    @Test
    @DisplayName("GET /api/bazi/generate - 年末跨日测试")
    void testGenerateBaZiGet_EndOfYear() throws Exception {
        mockMvc.perform(get("/api/bazi/generate")
                        .param("gender", "男")
                        .param("year", "1984")
                        .param("month", "12")
                        .param("day", "31")
                        .param("hour", "23"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.birthInfo.adjusted").value(true))
                .andExpect(jsonPath("$.yearPillar.fullName").value("甲子"))
                .andExpect(jsonPath("$.birthInfo.dayPillarDate").value("1985-1-1"));
    }

    @Test
    @DisplayName("POST /api/bazi/generate - 0点不跨日")
    void testGenerateBaZi_0Hour_NoAdjust() throws Exception {
        BaZiRequest request = new BaZiRequest("男", 1984, 11, 24, 0);

        mockMvc.perform(post("/api/bazi/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.birthInfo.adjusted").value(false))
                .andExpect(jsonPath("$.birthInfo.shiChen").value("子时"))
                .andExpect(jsonPath("$.dayPillar.fullName").value("壬戌"));
    }
}
