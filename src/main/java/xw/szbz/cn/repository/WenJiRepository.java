package xw.szbz.cn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import xw.szbz.cn.entity.WenJi;

import java.util.List;

@Repository
public interface WenJiRepository extends JpaRepository<WenJi, Long> {
    
    /**
     * 根据openId查询所有问吉记录
     */
    List<WenJi> findByOpenId(String openId);
    
    /**
     * 根据openId查询最新的N条记录
     */
    List<WenJi> findTop10ByOpenIdOrderByCreateTimeDesc(String openId);
    
    /**
     * 查询指定openId在指定时间范围内的提交次数
     * @param openId 用户openId
     * @param startTime 开始时间（13位时间戳）
     * @param endTime 结束时间（13位时间戳）
     * @return 提交次数
     */
    @Query("SELECT COUNT(w) FROM WenJi w WHERE w.openId = :openId AND w.createTime >= :startTime AND w.createTime <= :endTime")
    long countByOpenIdAndCreateTimeBetween(@Param("openId") String openId, 
                                           @Param("startTime") Long startTime, 
                                           @Param("endTime") Long endTime);
}
