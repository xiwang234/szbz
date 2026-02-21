package xw.szbz.cn.model;

/**
 * 大六壬完整信息模型
 */
public class LiuRenResult {
    private BasicInfo basicInfo;          // 基本信息
    private TianDiPan tianDiPan;         // 天地盘
    private SiKe siKe;                   // 四课
    private SanChuan sanChuan;           // 三传

    public BasicInfo getBasicInfo() {
        return basicInfo;
    }

    public void setBasicInfo(BasicInfo basicInfo) {
        this.basicInfo = basicInfo;
    }

    public TianDiPan getTianDiPan() {
        return tianDiPan;
    }

    public void setTianDiPan(TianDiPan tianDiPan) {
        this.tianDiPan = tianDiPan;
    }

    public SiKe getSiKe() {
        return siKe;
    }

    public void setSiKe(SiKe siKe) {
        this.siKe = siKe;
    }

    public SanChuan getSanChuan() {
        return sanChuan;
    }

    public void setSanChuan(SanChuan sanChuan) {
        this.sanChuan = sanChuan;
    }

    /**
     * 基本信息
     */
    public static class BasicInfo {
        private int year;
        private String yearPillar;
        private String monthPillar;
        private String dayPillar;
        private String hourPillar;
        private String yueJiang;

        public BasicInfo() {}

        public BasicInfo(int year, String yearPillar, String monthPillar, String dayPillar, String hourPillar, String yueJiang) {
            this.year = year;
            this.yearPillar = yearPillar;
            this.monthPillar = monthPillar;
            this.dayPillar = dayPillar;
            this.hourPillar = hourPillar;
            this.yueJiang = yueJiang;
        }

        public int getYear() {
            return year;
        }

        public void setYear(int year) {
            this.year = year;
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

        public String getYueJiang() {
            return yueJiang;
        }

        public void setYueJiang(String yueJiang) {
            this.yueJiang = yueJiang;
        }
    }

    /**
     * 天地盘
     */
    public static class TianDiPan {
        private PanPosition[] positions = new PanPosition[12];

        public PanPosition[] getPositions() {
            return positions;
        }

        public void setPositions(PanPosition[] positions) {
            this.positions = positions;
        }

        public void setPosition(int index, PanPosition position) {
            this.positions[index] = position;
        }
    }

    /**
     * 盘位信息
     */
    public static class PanPosition {
        private String diPan;      // 地盘
        private String tianPan;    // 天盘
        private String tianJiang;  // 天将

        public PanPosition() {}

        public PanPosition(String diPan, String tianPan, String tianJiang) {
            this.diPan = diPan;
            this.tianPan = tianPan;
            this.tianJiang = tianJiang;
        }

        public String getDiPan() {
            return diPan;
        }

        public void setDiPan(String diPan) {
            this.diPan = diPan;
        }

        public String getTianPan() {
            return tianPan;
        }

        public void setTianPan(String tianPan) {
            this.tianPan = tianPan;
        }

        public String getTianJiang() {
            return tianJiang;
        }

        public void setTianJiang(String tianJiang) {
            this.tianJiang = tianJiang;
        }
    }

    /**
     * 四课
     */
    public static class SiKe {
        private Ke diYiKe;   // 第一课
        private Ke diErKe;   // 第二课
        private Ke diSanKe;  // 第三课
        private Ke diSiKe;   // 第四课

        public Ke getDiYiKe() {
            return diYiKe;
        }

        public void setDiYiKe(Ke diYiKe) {
            this.diYiKe = diYiKe;
        }

        public Ke getDiErKe() {
            return diErKe;
        }

        public void setDiErKe(Ke diErKe) {
            this.diErKe = diErKe;
        }

        public Ke getDiSanKe() {
            return diSanKe;
        }

        public void setDiSanKe(Ke diSanKe) {
            this.diSanKe = diSanKe;
        }

        public Ke getDiSiKe() {
            return diSiKe;
        }

        public void setDiSiKe(Ke diSiKe) {
            this.diSiKe = diSiKe;
        }
    }

    /**
     * 课信息
     */
    public static class Ke {
        private String ganZhi;     // 干支
        private String tianJiang;  // 天将
        private String description; // 描述

        public Ke() {}

        public Ke(String ganZhi, String tianJiang, String description) {
            this.ganZhi = ganZhi;
            this.tianJiang = tianJiang;
            this.description = description;
        }

        public String getGanZhi() {
            return ganZhi;
        }

        public void setGanZhi(String ganZhi) {
            this.ganZhi = ganZhi;
        }

        public String getTianJiang() {
            return tianJiang;
        }

        public void setTianJiang(String tianJiang) {
            this.tianJiang = tianJiang;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    /**
     * 三传
     */
    public static class SanChuan {
        private Chuan chuChuan;  // 初传
        private Chuan zhongChuan; // 中传
        private Chuan moChuan;   // 末传

        public Chuan getChuChuan() {
            return chuChuan;
        }

        public void setChuChuan(Chuan chuChuan) {
            this.chuChuan = chuChuan;
        }

        public Chuan getZhongChuan() {
            return zhongChuan;
        }

        public void setZhongChuan(Chuan zhongChuan) {
            this.zhongChuan = zhongChuan;
        }

        public Chuan getMoChuan() {
            return moChuan;
        }

        public void setMoChuan(Chuan moChuan) {
            this.moChuan = moChuan;
        }
    }

    /**
     * 传信息
     */
    public static class Chuan {
        private String liuQin;     // 六亲
        private String ganZhi;     // 干支
        private String tianJiang;  // 天将
        private String shenSha;    // 神煞

        public Chuan() {}

        public Chuan(String liuQin, String ganZhi, String tianJiang, String shenSha) {
            this.liuQin = liuQin;
            this.ganZhi = ganZhi;
            this.tianJiang = tianJiang;
            this.shenSha = shenSha;
        }

        public String getLiuQin() {
            return liuQin;
        }

        public void setLiuQin(String liuQin) {
            this.liuQin = liuQin;
        }

        public String getGanZhi() {
            return ganZhi;
        }

        public void setGanZhi(String ganZhi) {
            this.ganZhi = ganZhi;
        }

        public String getTianJiang() {
            return tianJiang;
        }

        public void setTianJiang(String tianJiang) {
            this.tianJiang = tianJiang;
        }

        public String getShenSha() {
            return shenSha;
        }

        public void setShenSha(String shenSha) {
            this.shenSha = shenSha;
        }
    }
}
