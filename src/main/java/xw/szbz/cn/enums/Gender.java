package xw.szbz.cn.enums;

/**
 * 性别枚举
 */
public enum Gender {
    MALE("男"),
    FEMALE("女");

    private final String name;

    Gender(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static Gender fromString(String gender) {
        if ("男".equals(gender) || "male".equalsIgnoreCase(gender) || "m".equalsIgnoreCase(gender)) {
            return MALE;
        } else if ("女".equals(gender) || "female".equalsIgnoreCase(gender) || "f".equalsIgnoreCase(gender)) {
            return FEMALE;
        }
        throw new IllegalArgumentException("Invalid gender: " + gender);
    }
}
