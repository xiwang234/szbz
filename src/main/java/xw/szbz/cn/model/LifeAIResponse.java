package xw.szbz.cn.model;

/**
 * LifeAI 响应
 */
public class LifeAIResponse {

    private String requestId;       // 请求ID
    private String background;      // 背景描述
    private String question;        // 问题
    private Integer birthYear;      // 出生年份
    private String gender;          // 性别
    private String category;        // 分类
    private String answer;          // AI回答（业务逻辑待定）
    private Long timestamp;         // 时间戳

    public LifeAIResponse() {
    }

    public LifeAIResponse(String requestId, String background, String question, Integer birthYear,
                         String gender, String category, String answer, Long timestamp) {
        this.requestId = requestId;
        this.background = background;
        this.question = question;
        this.birthYear = birthYear;
        this.gender = gender;
        this.category = category;
        this.answer = answer;
        this.timestamp = timestamp;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
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

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "LifeAIResponse{" +
                "requestId='" + requestId + '\'' +
                ", background='" + background + '\'' +
                ", question='" + question + '\'' +
                ", birthYear=" + birthYear +
                ", gender='" + gender + '\'' +
                ", category='" + category + '\'' +
                ", answer='" + answer + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
