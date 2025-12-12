package xw.szbz.cn.model;

/**
 * 基本信息
 * 包含性别和对应的四柱
 */
public class BasicInfo {
    private String gender;      // 性别
    private String yearPillar;  // 年柱（完整名称，如"甲子"）
    private String monthPillar; // 月柱（完整名称）
    private String dayPillar;   // 日柱（完整名称）
    private String hourPillar;  // 时柱（完整名称）

    public BasicInfo() {
    }

    public BasicInfo(String gender) {
        this.gender = gender;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getYearPillar() {
        return yearPillar;
    }

    public void setYearPillar(String yearPillar) {
        this.yearPillar = yearPillar;
    }

    public String getMonthPillar() {
        return monthPillar;
    }

    public void setMonthPillar(String monthPillar) {
        this.monthPillar = monthPillar;
    }

    public String getDayPillar() {
        return dayPillar;
    }

    public void setDayPillar(String dayPillar) {
        this.dayPillar = dayPillar;
    }

    public String getHourPillar() {
        return hourPillar;
    }

    public void setHourPillar(String hourPillar) {
        this.hourPillar = hourPillar;
    }

    @Override
    public String toString() {
        return "{" +
                "\"gender\":\"" + gender + "\"," +
                "\"yearPillar\":\"" + yearPillar + "\"," +
                "\"monthPillar\":\"" + monthPillar + "\"," +
                "\"dayPillar\":\"" + dayPillar + "\"," +
                "\"hourPillar\":\"" + hourPillar + "\"" +
                "}";
    }
}
