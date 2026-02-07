package xw.szbz.cn.entity;

import jakarta.persistence.*;

/**
 * LifeAI 分析结果实体
 * 保存用户的问题、背景和AI分析结果
 */
@Entity
@Table(name = "lifeai_result")
public class LifeAIResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户ID（关联 web_user 表）
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 问题
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String question;

    /**
     * 背景描述
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String background;

    /**
     * AI分析结果
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String result;

    /**
     * 出生年份
     */
    @Column(name = "birthday_year", nullable = false)
    private Integer birthdayYear;

    /**
     * 性别
     */
    @Column(nullable = false, length = 10)
    private String gender;

    /**
     * 分类
     */
    @Column(nullable = false, length = 50)
    private String category;

    /**
     * 创建时间（13位毫秒时间戳）
     */
    @Column(name = "create_time", nullable = false)
    private Long createTime;

    // 构造函数
    public LifeAIResult() {
        this.createTime = System.currentTimeMillis();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public Integer getBirthdayYear() {
        return birthdayYear;
    }

    public void setBirthdayYear(Integer birthdayYear) {
        this.birthdayYear = birthdayYear;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }
}
