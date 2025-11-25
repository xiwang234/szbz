package xw.szbz.cn.model;

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
    }
}
