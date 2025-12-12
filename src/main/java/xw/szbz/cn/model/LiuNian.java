package xw.szbz.cn.model;

/**
 * 流年信息
 * 流年：每年的运势，按照天干地支纪年
 */
public class LiuNian {
    private int year;          // 年份
    private int age;           // 年龄
    private Pillar pillar;     // 流年干支

    public LiuNian() {
    }

    public LiuNian(int year, int age, Pillar pillar) {
        this.year = year;
        this.age = age;
        this.pillar = pillar;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
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
                "\"year\":" + year + "," +
                "\"age\":" + age + "," +
                "\"pillar\":{" +
                "\"tianGan\":\"" + pillar.getTianGan() + "\"," +
                "\"diZhi\":\"" + pillar.getDiZhi() + "\"," +
                "\"fullName\":\"" + pillar.getFullName() + "\"" +
                "}" +
                "}";
    }
}
