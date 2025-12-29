package xw.szbz.cn.model;

/**
 * 六壬预测请求模型
 */
public class LiuRenRequest {
    
    /**
     * 占问事项（必填）
     */
    private String question;
    
    /**
     * 占问背景（可选）
     */
    private String background;
    
    /**
     * 出生年份（必填）
     */
    private Integer birthYear;
    
    /**
     * 性别：男/女（必填）
     */
    private String gender;

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
}

