package xw.szbz.cn.model;

/**
 * 用户免费次数统计对象
 */
public class UserFreeCountDto {

    private Long id;
    private Integer freeCount;

    public UserFreeCountDto() {
    }

    public UserFreeCountDto(Long id, Integer freeCount) {
        this.id = id;
        this.freeCount = freeCount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getFreeCount() {
        return freeCount;
    }

    public void setFreeCount(Integer freeCount) {
        this.freeCount = freeCount;
    }
}
