package xw.szbz.cn.enums;

/**
 * 天将枚举 - 十二天将
 * 正确顺序：天乙贵人、螣蛇、朱雀、六合、勾陈、青龙、天空、白虎、太常、玄武、太阴、天后
 */
public enum TianJiang {
    TIAN_YI("天乙贵人", 0),
    TENG_SHE("螣蛇", 1),
    ZHU_QUE("朱雀", 2),
    LIU_HE("六合", 3),
    GOU_CHEN("勾陈", 4),
    QING_LONG("青龙", 5),
    TIAN_KONG("天空", 6),
    BAI_HU("白虎", 7),
    TAI_CHANG("太常", 8),
    XUAN_WU("玄武", 9),
    TAI_YIN("太阴", 10),
    TIAN_HOU("天后", 11);

    private final String name;
    private final int index;

    TianJiang(String name, int index) {
        this.name = name;
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public int getIndex() {
        return index;
    }

    public static TianJiang fromIndex(int index) {
        int normalizedIndex = ((index % 12) + 12) % 12;
        for (TianJiang tj : values()) {
            if (tj.index == normalizedIndex) {
                return tj;
            }
        }
        throw new IllegalArgumentException("Invalid TianJiang index: " + index);
    }

    public static TianJiang fromName(String name) {
        for (TianJiang tj : values()) {
            if (tj.name.equals(name)) {
                return tj;
            }
        }
        throw new IllegalArgumentException("Invalid TianJiang name: " + name);
    }
}
