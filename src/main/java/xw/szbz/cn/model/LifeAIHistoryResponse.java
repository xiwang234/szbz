package xw.szbz.cn.model;

/**
 * LifeAI 历史记录响应对象
 * 不包含 user_id，避免暴露真实用户ID
 */
public class LifeAIHistoryResponse {

    private Long id;
    private String question;
    private String background;
    private String result;
    private Integer birthdayYear;
    private String gender;
    private String category;
    private Long createTime;

    // 构造函数
    public LifeAIHistoryResponse() {
    }

    public LifeAIHistoryResponse(Long id, String question, String background, String result,
                                  Integer birthdayYear, String gender, String category, Long createTime) {
        this.id = id;
        this.question = question;
        this.background = background;
        this.result = result;
        this.birthdayYear = birthdayYear;
        this.gender = gender;
        this.category = category;
        this.createTime = createTime;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
