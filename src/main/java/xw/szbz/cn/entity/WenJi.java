package xw.szbz.cn.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 问吉表实体类（六壬预测）
 */
@Entity
@Table(name = "wen_ji")
public class WenJi {
    
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String openId;
    
    @Column(nullable = false, length = 500)
    private String question;  // 占问事项
    
    @Column(length = 2000)
    private String background;  // 占问背景
    
    @Column(nullable = false)
    private Integer birthYear;  // 出生年份
    
    @Column(nullable = false, length = 10)
    private String gender;  // 性别
    
    @Column(columnDefinition = "TEXT")
    private String result;  // AI分析结果
    
    @Column(nullable = false)
    private Long createTime;  // 13位时间戳
    
    public WenJi() {
    }
    
    public WenJi(String openId, String question, String background, Integer birthYear, 
                 String gender, String result, Long createTime) {
        this.openId = openId;
        this.question = question;
        this.background = background;
        this.birthYear = birthYear;
        this.gender = gender;
        this.result = result;
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
    
    public String getQuestion() {
        return question;
    }
    
    public void setQuestion(String question) {
        this.question = question;
    }
    
    public String getBackground() {
        return background;
    }
    
    public void setBackground(String background) {
        this.background = background;
    }
    
    public Integer getBirthYear() {
        return birthYear;
    }
    
    public void setBirthYear(Integer birthYear) {
        this.birthYear = birthYear;
    }
    
    public String getGender() {
        return gender;
    }
    
    public void setGender(String gender) {
        this.gender = gender;
    }
    
    public String getResult() {
        return result;
    }
    
    public void setResult(String result) {
        this.result = result;
    }
    
    public Long getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }
}
