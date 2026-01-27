package xw.szbz.cn.model;

/**
 * LifeAI 请求
 */
public class LifeAIRequest {

    private String background;      // 背景描述
    private String question;        // 问题
    private Integer birthYear;      // 出生年份
    private String gender;          // 性别（男/女/male/female）
    private String category;        // 分类

    public LifeAIRequest() {
    }

    public LifeAIRequest(String background, String question, Integer birthYear, String gender, String category) {
        this.background = background;
        this.question = question;
        this.birthYear = birthYear;
        this.gender = gender;
        this.category = category;
    }

    public String getBackground() {
        return background;
    }

    public void setBackground(String background) {
        this.background = background;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return "LifeAIRequest{" +
                "background='" + background + '\'' +
                ", question='" + question + '\'' +
                ", birthYear=" + birthYear +
                ", gender='" + gender + '\'' +
                ", category='" + category + '\'' +
                '}';
    }
}
