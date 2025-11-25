package xw.szbz.cn.enums;

/**
 * 地支枚举
 */
public enum DiZhi {
    ZI("子", 0),
    CHOU("丑", 1),
    YIN("寅", 2),
    MAO("卯", 3),
    CHEN("辰", 4),
    SI("巳", 5),
    WU("午", 6),
    WEI("未", 7),
    SHEN("申", 8),
    YOU("酉", 9),
    XU("戌", 10),
    HAI("亥", 11);

    private final String name;
    private final int index;

    DiZhi(String name, int index) {
        this.name = name;
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public int getIndex() {
        return index;
    }

    public static DiZhi fromIndex(int index) {
        int normalizedIndex = ((index % 12) + 12) % 12;
        for (DiZhi dz : values()) {
            if (dz.index == normalizedIndex) {
                return dz;
            }
        }
        throw new IllegalArgumentException("Invalid DiZhi index: " + index);
    }

    /**
     * 根据时辰获取地支
     * 子时：23:00-01:00
     * 丑时：01:00-03:00
     * 寅时：03:00-05:00
     * 卯时：05:00-07:00
     * 辰时：07:00-09:00
     * 巳时：09:00-11:00
     * 午时：11:00-13:00
     * 未时：13:00-15:00
     * 申时：15:00-17:00
     * 酉时：17:00-19:00
     * 戌时：19:00-21:00
     * 亥时：21:00-23:00
     */
    public static DiZhi fromHour(int hour) {
        if (hour == 23 || hour == 0) {
            return ZI;
        }
        return fromIndex((hour + 1) / 2);
    }
}
