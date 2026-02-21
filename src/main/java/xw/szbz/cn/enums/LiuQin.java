package xw.szbz.cn.enums;

/**
 * 六亲枚举
 */
public enum LiuQin {
    FU_MU("父母"),
    XIONG_DI("兄弟"),
    ZI_SUN("子孙"),
    QI_CAI("妻财"),
    GUAN_GUI("官鬼");

    private final String name;

    LiuQin(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static LiuQin fromName(String name) {
        for (LiuQin lq : values()) {
            if (lq.name.equals(name)) {
                return lq;
            }
        }
        throw new IllegalArgumentException("Invalid LiuQin name: " + name);
    }
}
