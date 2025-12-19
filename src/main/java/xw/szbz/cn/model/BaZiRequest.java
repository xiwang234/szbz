package xw.szbz.cn.model;

/**
 * 四柱八字请求参数
 */
public class BaZiRequest {
    /**
     * 微信小程序登录凭证 code
     * 通过小程序端 wx.login() 获取
     */
    private String code;
    
    private String gender;
    private int year;
    private int month;
    private int day;
    private int hour;

    public BaZiRequest() {
    }

    public BaZiRequest(String gender, int year, int month, int day, int hour) {
        this.gender = gender;
        this.year = year;
        this.month = month;
        this.day = day;
        this.hour = hour;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }
}
