package xw.szbz.cn.model;

/**
 * 大运信息
 * 大运：每10年一个大运周期
 */
public class DaYun {
    private int startAge;      // 起始年龄
    private int endAge;        // 结束年龄
    private int startYear;     // 起始年份
    private int endYear;       // 结束年份
    private Pillar pillar;     // 大运干支

    public DaYun() {
    }

    public DaYun(int startAge, int endAge, int startYear, int endYear, Pillar pillar) {
        this.startAge = startAge;
        this.endAge = endAge;
        this.startYear = startYear;
        this.endYear = endYear;
        this.pillar = pillar;
    }

    public int getStartAge() {
        return startAge;
    }

    public void setStartAge(int startAge) {
        this.startAge = startAge;
    }

    public int getEndAge() {
        return endAge;
    }

    public void setEndAge(int endAge) {
        this.endAge = endAge;
    }

    public int getStartYear() {
        return startYear;
    }

    public void setStartYear(int startYear) {
        this.startYear = startYear;
    }

    public int getEndYear() {
        return endYear;
    }

    public void setEndYear(int endYear) {
        this.endYear = endYear;
    }

    public Pillar getPillar() {
        return pillar;
    }

    public void setPillar(Pillar pillar) {
        this.pillar = pillar;
    }

    @Override
    public String toString() {
        return "{" +
                "\"startAge\":" + startAge + "," +
                "\"endAge\":" + endAge + "," +
                "\"startYear\":" + startYear + "," +
                "\"endYear\":" + endYear + "," +
                "\"pillar\":{" +
                "\"tianGan\":\"" + pillar.getTianGan() + "\"," +
                "\"diZhi\":\"" + pillar.getDiZhi() + "\"," +
                "\"fullName\":\"" + pillar.getFullName() + "\"" +
                "}" +
                "}";
    }
}
