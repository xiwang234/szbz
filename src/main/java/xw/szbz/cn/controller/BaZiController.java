package xw.szbz.cn.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xw.szbz.cn.model.BaZiRequest;
import xw.szbz.cn.model.BaZiResult;
import xw.szbz.cn.service.BaZiService;
import xw.szbz.cn.service.GeminiService;

import java.util.HashMap;
import java.util.Map;

/**
 * 四柱八字API控制器
 */
@RestController
@RequestMapping("/api/bazi")
public class BaZiController {

    private final BaZiService baZiService;
    private final GeminiService geminiService;

    @Autowired
    public BaZiController(BaZiService baZiService, GeminiService geminiService) {
        this.baZiService = baZiService;
        this.geminiService = geminiService;
    }

    /**
     * 生成四柱八字
     *
     * @param request 请求参数（性别、出生年月日时）
     * @return 四柱八字结果
     */
    @PostMapping("/generate")
    public ResponseEntity<BaZiResult> generateBaZi(@RequestBody BaZiRequest request) {
        validateRequest(request);
        BaZiResult result = baZiService.calculate(request);
        return ResponseEntity.ok(result);
    }

    /**
     * GET方式生成四柱八字
     *
     * @param gender 性别
     * @param year   出生年
     * @param month  出生月
     * @param day    出生日
     * @param hour   出生时
     * @return 四柱八字结果
     */
    @GetMapping("/generate")
    public ResponseEntity<BaZiResult> generateBaZiGet(
            @RequestParam String gender,
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam int day,
            @RequestParam int hour) {
        BaZiRequest request = new BaZiRequest(gender, year, month, day, hour);
        validateRequest(request);
        BaZiResult result = baZiService.calculate(request);
        return ResponseEntity.ok(result);
    }

    private void validateRequest(BaZiRequest request) {
        if (request.getGender() == null || request.getGender().isEmpty()) {
            throw new IllegalArgumentException("性别不能为空");
        }
        if (request.getYear() < 1900 || request.getYear() > 2100) {
            throw new IllegalArgumentException("年份必须在1900-2100之间");
        }
        if (request.getMonth() < 1 || request.getMonth() > 12) {
            throw new IllegalArgumentException("月份必须在1-12之间");
        }
        if (request.getDay() < 1 || request.getDay() > 31) {
            throw new IllegalArgumentException("日期必须在1-31之间");
        }
        if (request.getHour() < 0 || request.getHour() > 23) {
            throw new IllegalArgumentException("小时必须在0-23之间");
        }
    }

    /**
     * 生成八字并使用 Gemini AI 进行分析
     *
     * @param request 请求参数（性别、出生年月日时）
     * @return 包含八字结果和 AI 分析的响应
     */
    @PostMapping("/analyze")
    public ResponseEntity<Map<String, Object>> analyzeBaZiWithAI(@RequestBody BaZiRequest request) {
        validateRequest(request);

        // 1. 计算八字
        BaZiResult baZiResult = baZiService.calculate(request);

        // 2. 使用 Gemini AI 分析
        String aiAnalysis = geminiService.analyzeBaZi(baZiResult);

        // 3. 构建返回结果
        Map<String, Object> response = new HashMap<>();
        response.put("baziResult", baZiResult);
        response.put("aiAnalysis", aiAnalysis);

        return ResponseEntity.ok(response);
    }

    /**
     * GET方式生成八字并使用 Gemini AI 进行分析
     *
     * @param gender 性别
     * @param year   出生年
     * @param month  出生月
     * @param day    出生日
     * @param hour   出生时
     * @return 包含八字结果和 AI 分析的响应
     */
    @GetMapping("/analyze")
    public ResponseEntity<Map<String, Object>> analyzeBaZiWithAIGet(
            @RequestParam String gender,
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam int day,
            @RequestParam int hour) {
        BaZiRequest request = new BaZiRequest(gender, year, month, day, hour);
        return analyzeBaZiWithAI(request);
    }
}
