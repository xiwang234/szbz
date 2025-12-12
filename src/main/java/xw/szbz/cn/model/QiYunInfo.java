package xw.szbz.cn.model;

/**
 * 起运信息
 */
public class QiYunInfo {
    private int years;          // 起运年数
    private int months;         // 起运月数
    private int days;           // 起运天数
    private int xuSuiAge;       // 起运虚岁
    private int zhouSuiAge;     // 起运周岁
    private String qiYunDate;   // 起运日期（格式：yyyy-MM-dd）
    private int qiYunYear;      // 起运年份
    private String description; // 起运描述（如：4年5个月零4日）

    public QiYunInfo() {
    }

    public QiYunInfo(int years, int months, int days, int xuSuiAge, int zhouSuiAge,
                     String qiYunDate, int qiYunYear, String description) {
        this.years = years;
        this.months = months;
        this.days = days;
        this.xuSuiAge = xuSuiAge;
        this.zhouSuiAge = zhouSuiAge;
        this.qiYunDate = qiYunDate;
        this.qiYunYear = qiYunYear;
        this.description = description;
    }

    public int getYears() {
        return years;
    }

    public void setYears(int years) {
        this.years = years;
    }

    public int getMonths() {
        return months;
    }

    public void setMonths(int months) {
        this.months = months;
    }

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }

    public int getXuSuiAge() {
        return xuSuiAge;
    }

    public void setXuSuiAge(int xuSuiAge) {
        this.xuSuiAge = xuSuiAge;
    }

    public int getZhouSuiAge() {
        return zhouSuiAge;
    }

    public void setZhouSuiAge(int zhouSuiAge) {
        this.zhouSuiAge = zhouSuiAge;
    }

    public String getQiYunDate() {
        return qiYunDate;
    }

    public void setQiYunDate(String qiYunDate) {
        this.qiYunDate = qiYunDate;
    }

    public int getQiYunYear() {
        return qiYunYear;
    }

    public void setQiYunYear(int qiYunYear) {
        this.qiYunYear = qiYunYear;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "{" +
                "\"years\":" + years + "," +
                "\"months\":" + months + "," +
                "\"days\":" + days + "," +
                "\"xuSuiAge\":" + xuSuiAge + "," +
                "\"zhouSuiAge\":" + zhouSuiAge + "," +
                "\"qiYunDate\":\"" + qiYunDate + "\"," +
                "\"qiYunYear\":" + qiYunYear + "," +
                "\"description\":\"" + description + "\"" +
                "}";
    }
}
