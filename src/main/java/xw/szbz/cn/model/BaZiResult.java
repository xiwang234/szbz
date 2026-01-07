package xw.szbz.cn.model;

import java.util.List;

/**
 * 四柱八字返回结果
 */
public class BaZiResult {
    private String gender;
    private Pillar yearPillar;
    private Pillar monthPillar;
    private Pillar dayPillar;
    private Pillar hourPillar;
    private String fullBaZi;
    private BirthInfo birthInfo;
    private String background;

    public String getBackground() {
        return background;
    }

    public void setBackground(String background) {
        this.background = background;
    }


    // 新增属性
    private BasicInfo basicInfo;                // 基本信息（性别、年、月、日、时）
    private List<DaYunLiuNian> daYunStringList; // 大运及其对应流年列表（覆盖到当前年份）
    private List<DaYunLiuNian> daYunALLStringList; // 完整的10个大运及其对应流年列表

    public BaZiResult() {
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Pillar getYearPillar() {
        return yearPillar;
    }

    public void setYearPillar(Pillar yearPillar) {
        this.yearPillar = yearPillar;
    }

    public Pillar getMonthPillar() {
        return monthPillar;
    }

    public void setMonthPillar(Pillar monthPillar) {
        this.monthPillar = monthPillar;
    }

    public Pillar getDayPillar() {
        return dayPillar;
    }

    public void setDayPillar(Pillar dayPillar) {
        this.dayPillar = dayPillar;
    }

    public Pillar getHourPillar() {
        return hourPillar;
    }

    public void setHourPillar(Pillar hourPillar) {
        this.hourPillar = hourPillar;
    }

    public String getFullBaZi() {
        return fullBaZi;
    }

    public void setFullBaZi(String fullBaZi) {
        this.fullBaZi = fullBaZi;
    }

    public BirthInfo getBirthInfo() {
        return birthInfo;
    }

    public void setBirthInfo(BirthInfo birthInfo) {
        this.birthInfo = birthInfo;
    }

    public BasicInfo getBasicInfo() {
        return basicInfo;
    }

    public void setBasicInfo(BasicInfo basicInfo) {
        this.basicInfo = basicInfo;
    }

    public List<DaYunLiuNian> getDaYunStringList() {
        return daYunStringList;
    }

    public void setDaYunStringList(List<DaYunLiuNian> daYunStringList) {
        this.daYunStringList = daYunStringList;
    }

    public List<DaYunLiuNian> getDaYunALLStringList() {
        return daYunALLStringList;
    }

    public void setDaYunALLStringList(List<DaYunLiuNian> daYunALLStringList) {
        this.daYunALLStringList = daYunALLStringList;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"gender\":\"").append(gender).append("\",");
        sb.append("\"yearPillar\":").append(yearPillar != null ? yearPillar.toString() : "null").append(",");
        sb.append("\"monthPillar\":").append(monthPillar != null ? monthPillar.toString() : "null").append(",");
        sb.append("\"dayPillar\":").append(dayPillar != null ? dayPillar.toString() : "null").append(",");
        sb.append("\"hourPillar\":").append(hourPillar != null ? hourPillar.toString() : "null").append(",");
        sb.append("\"fullBaZi\":\"").append(fullBaZi).append("\",");
        sb.append("\"birthInfo\":").append(birthInfo != null ? birthInfo.toString() : "null").append(",");
        sb.append("\"basicInfo\":").append(basicInfo != null ? basicInfo.toString() : "null").append(",");
        sb.append("\"background\":\"").append(background).append("\",");
        // 大运及其对应流年列表（覆盖到当前年份）
        sb.append("\"daYunStringList\":[");
        if (daYunStringList != null && !daYunStringList.isEmpty()) {
            for (int i = 0; i < daYunStringList.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(daYunStringList.get(i).toString());
            }
        }
        sb.append("],");

        // 完整的10个大运及其对应流年列表
        sb.append("\"daYunALLStringList\":[");
        if (daYunALLStringList != null && !daYunALLStringList.isEmpty()) {
            for (int i = 0; i < daYunALLStringList.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(daYunALLStringList.get(i).toString());
            }
        }
        sb.append("]");

        sb.append("}");
        return sb.toString();
    }

    /**
     * 出生信息（用于排盘的实际日期）
     */
    public static class BirthInfo {
        private int year;
        private int month;
        private int day;
        private int hour;
        private String shiChen;
        private boolean adjusted;
        private String dayPillarDate;

        public BirthInfo() {
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

        public String getShiChen() {
            return shiChen;
        }

        public void setShiChen(String shiChen) {
            this.shiChen = shiChen;
        }

        public boolean isAdjusted() {
            return adjusted;
        }

        public void setAdjusted(boolean adjusted) {
            this.adjusted = adjusted;
        }

        public String getDayPillarDate() {
            return dayPillarDate;
        }

        public void setDayPillarDate(String dayPillarDate) {
            this.dayPillarDate = dayPillarDate;
        }

        @Override
        public String toString() {
            return "{" +
                    "\"year\":" + year + "," +
                    "\"month\":" + month + "," +
                    "\"day\":" + day + "," +
                    "\"hour\":" + hour + "," +
                    "\"shiChen\":\"" + shiChen + "\"," +
                    "\"adjusted\":" + adjusted + "," +
                    "\"dayPillarDate\":\"" + dayPillarDate + "\"" +
                    "}";
        }
    }
}
