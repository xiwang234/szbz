package xw.szbz.cn.enums;

/**
 * 五行枚举
 */
public enum WuXing {
    MU("木"),
    HUO("火"),
    TU("土"),
    JIN("金"),
    SHUI("水");

    private final String name;

    WuXing(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    /**
     * 根据天干获取五行
     * 甲乙-木，丙丁-火，戊己-土，庚辛-金，壬癸-水
     */
    public static WuXing fromTianGan(String tianGan) {
        switch (tianGan) {
            case "甲":
            case "乙":
                return MU;
            case "丙":
            case "丁":
                return HUO;
            case "戊":
            case "己":
                return TU;
            case "庚":
            case "辛":
                return JIN;
            case "壬":
            case "癸":
                return SHUI;
            default:
                throw new IllegalArgumentException("Invalid TianGan: " + tianGan);
        }
    }

    /**
     * 根据地支获取五行（地支藏干取本气）
     * 寅卯-木，巳午-火，辰戌丑未-土，申酉-金，亥子-水
     */
    public static WuXing fromDiZhi(String diZhi) {
        switch (diZhi) {
            case "寅":
            case "卯":
                return MU;
            case "巳":
            case "午":
                return HUO;
            case "辰":
            case "戌":
            case "丑":
            case "未":
                return TU;
            case "申":
            case "酉":
                return JIN;
            case "亥":
            case "子":
                return SHUI;
            default:
                throw new IllegalArgumentException("Invalid DiZhi: " + diZhi);
        }
    }

    /**
     * 判断五行生克关系
     * 木生火，火生土，土生金，金生水，水生木（我生者为子孙）
     * 木克土，土克水，水克火，火克金，金克木（我克者为妻财）
     * 木被金克（克我者为官鬼）
     * 木被水生（生我者为父母）
     * 同五行为兄弟
     */
    public LiuQin getLiuQin(WuXing target) {
        if (this == target) {
            return LiuQin.XIONG_DI;
        }

        // 我生者为子孙
        if ((this == MU && target == HUO) ||
            (this == HUO && target == TU) ||
            (this == TU && target == JIN) ||
            (this == JIN && target == SHUI) ||
            (this == SHUI && target == MU)) {
            return LiuQin.ZI_SUN;
        }

        // 我克者为妻财
        if ((this == MU && target == TU) ||
            (this == TU && target == SHUI) ||
            (this == SHUI && target == HUO) ||
            (this == HUO && target == JIN) ||
            (this == JIN && target == MU)) {
            return LiuQin.QI_CAI;
        }

        // 克我者为官鬼
        if ((this == MU && target == JIN) ||
            (this == JIN && target == HUO) ||
            (this == HUO && target == SHUI) ||
            (this == SHUI && target == TU) ||
            (this == TU && target == MU)) {
            return LiuQin.GUAN_GUI;
        }

        // 生我者为父母
        return LiuQin.FU_MU;
    }
}
