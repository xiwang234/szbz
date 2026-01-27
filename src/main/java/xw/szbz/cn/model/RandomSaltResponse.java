package xw.szbz.cn.model;

/**
 * 随机盐响应
 */
public class RandomSaltResponse {

    private String randomSalt;
    private Long expiresAt;

    public RandomSaltResponse() {
    }

    public RandomSaltResponse(String randomSalt, Long expiresAt) {
        this.randomSalt = randomSalt;
        this.expiresAt = expiresAt;
    }

    public String getRandomSalt() {
        return randomSalt;
    }

    public void setRandomSalt(String randomSalt) {
        this.randomSalt = randomSalt;
    }

    public Long getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Long expiresAt) {
        this.expiresAt = expiresAt;
    }
}
