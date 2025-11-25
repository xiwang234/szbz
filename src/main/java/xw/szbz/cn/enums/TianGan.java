package xw.szbz.cn.enums;

/**
 * 天干枚举
 */
public enum TianGan {
    JIA("甲", 0),
    YI("乙", 1),
    BING("丙", 2),
    DING("丁", 3),
    WU("戊", 4),
    JI("己", 5),
    GENG("庚", 6),
    XIN("辛", 7),
    REN("壬", 8),
    GUI("癸", 9);

    private final String name;
    private final int index;

    TianGan(String name, int index) {
        this.name = name;
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public int getIndex() {
        return index;
    }

    public static TianGan fromIndex(int index) {
        int normalizedIndex = ((index % 10) + 10) % 10;
        for (TianGan tg : values()) {
            if (tg.index == normalizedIndex) {
                return tg;
            }
        }
        throw new IllegalArgumentException("Invalid TianGan index: " + index);
    }
}
