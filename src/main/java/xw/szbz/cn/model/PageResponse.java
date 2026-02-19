package xw.szbz.cn.model;

import java.util.List;

/**
 * 分页响应对象
 */
public class PageResponse<T> {

    private List<T> content;        // 当前页数据
    private Integer pageNo;         // 当前页码（从1开始）
    private Integer pageSize;       // 每页大小
    private Long totalElements;     // 总记录数
    private Integer totalPages;     // 总页数
    private Boolean hasNext;        // 是否有下一页
    private Boolean hasPrevious;    // 是否有上一页

    public PageResponse() {
    }

    public PageResponse(List<T> content, Integer pageNo, Integer pageSize,
                       Long totalElements, Integer totalPages,
                       Boolean hasNext, Boolean hasPrevious) {
        this.content = content;
        this.pageNo = pageNo;
        this.pageSize = pageSize;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.hasNext = hasNext;
        this.hasPrevious = hasPrevious;
    }

    // Getters and Setters
    public List<T> getContent() {
        return content;
    }

    public void setContent(List<T> content) {
        this.content = content;
    }

    public Integer getPageNo() {
        return pageNo;
    }

    public void setPageNo(Integer pageNo) {
        this.pageNo = pageNo;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(Long totalElements) {
        this.totalElements = totalElements;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }

    public Boolean getHasNext() {
        return hasNext;
    }

    public void setHasNext(Boolean hasNext) {
        this.hasNext = hasNext;
    }

    public Boolean getHasPrevious() {
        return hasPrevious;
    }

    public void setHasPrevious(Boolean hasPrevious) {
        this.hasPrevious = hasPrevious;
    }
}
