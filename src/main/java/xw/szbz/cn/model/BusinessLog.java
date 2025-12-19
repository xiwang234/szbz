package xw.szbz.cn.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

/**
 * 业务日志模型
 * 用于记录八字分析业务数据，便于后续导入 MySQL
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BusinessLog {
    
    // 基本信息
    private Long id;                        // 日志ID（时间戳 + 随机数）
    private String openId;                  // 用户OpenId
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime requestTime;      // 请求时间
    
    private String requestIp;               // 请求IP
    private String userAgent;               // 用户代理
    
    // 请求参数
    private String gender;                  // 性别
    private Integer year;                   // 出生年
    private Integer month;                  // 出生月
    private Integer day;                    // 出生日
    private Integer hour;                   // 出生时
    
    // 八字结果（JSON字符串）
    private String baziResult;              // 八字、大运、流年数据（BaZiResult JSON）
    
    // AI分析结果（JSON字符串）
    private String aiAnalysis;              // AI分析结果（JSON）
    
    // 缓存状态
    private Boolean cacheHit;               // 是否命中缓存
    
    // 响应信息
    private Integer responseCode;           // 响应码
    private String responseMessage;         // 响应消息
    private Long processingTime;            // 处理时长（毫秒）
    
    public BusinessLog() {
        this.requestTime = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    public LocalDateTime getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(LocalDateTime requestTime) {
        this.requestTime = requestTime;
    }

    public String getRequestIp() {
        return requestIp;
    }

    public void setRequestIp(String requestIp) {
        this.requestIp = requestIp;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Integer getDay() {
        return day;
    }

    public void setDay(Integer day) {
        this.day = day;
    }

    public Integer getHour() {
        return hour;
    }

    public void setHour(Integer hour) {
        this.hour = hour;
    }

    public String getBaziResult() {
        return baziResult;
    }

    public void setBaziResult(String baziResult) {
        this.baziResult = baziResult;
    }

    public String getAiAnalysis() {
        return aiAnalysis;
    }

    public void setAiAnalysis(String aiAnalysis) {
        this.aiAnalysis = aiAnalysis;
    }

    public Boolean getCacheHit() {
        return cacheHit;
    }

    public void setCacheHit(Boolean cacheHit) {
        this.cacheHit = cacheHit;
    }

    public Integer getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(Integer responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    public Long getProcessingTime() {
        return processingTime;
    }

    public void setProcessingTime(Long processingTime) {
        this.processingTime = processingTime;
    }
}
