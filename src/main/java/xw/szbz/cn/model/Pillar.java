package xw.szbz.cn.model;

/**
 * 柱（天干+地支）
 */
public class Pillar {
    private String tianGan;
    private String diZhi;
    private String fullName;

    public Pillar() {
    }

    public Pillar(String tianGan, String diZhi) {
        this.tianGan = tianGan;
        this.diZhi = diZhi;
        this.fullName = tianGan + diZhi;
    }

    public String getTianGan() {
        return tianGan;
    }

    public void setTianGan(String tianGan) {
        this.tianGan = tianGan;
        this.fullName = this.tianGan + this.diZhi;
    }

    public String getDiZhi() {
        return diZhi;
    }

    public void setDiZhi(String diZhi) {
        this.diZhi = diZhi;
        this.fullName = this.tianGan + this.diZhi;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    @Override
    public String toString() {
        return "{" +
                "\"tianGan\":\"" + tianGan + "\"," +
                "\"diZhi\":\"" + diZhi + "\"," +
                "\"fullName\":\"" + fullName + "\"" +
                "}";
    }
}
