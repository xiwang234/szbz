package xw.szbz.cn.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 吉途表实体类（八字分析）
 */
@Entity
@Table(name = "ji_tu")
public class JiTu {
    
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String openId;
    
    @Column(nullable = false, length = 10)
    private String gender;  // 性别
    
    @Column(nullable = false)
    private Integer year;  // 出生年
    
    @Column(nullable = false)
    private Integer month;  // 出生月
    
    @Column(nullable = false)
    private Integer day;  // 出生日
    
    @Column(nullable = false)
    private Integer hour;  // 出生时
    
    @Column(columnDefinition = "TEXT")
    private String defaultResult;  // AI分析结果
    
    @Column(nullable = false)
    private Long createTime;  // 13位时间戳
    
    public JiTu() {
    }
    
    public JiTu(String openId, String gender, Integer year, Integer month, Integer day, 
                Integer hour, String defaultResult, Long createTime) {
        this.openId = openId;
        this.gender = gender;
        this.year = year;
        this.month = month;
        this.day = day;
        this.hour = hour;
        this.defaultResult = defaultResult;
        this.createTime = createTime;
    }
    
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
    
    public String getDefaultResult() {
        return defaultResult;
    }
    
    public void setDefaultResult(String defaultResult) {
        this.defaultResult = defaultResult;
    }
    
    public Long getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }
}
