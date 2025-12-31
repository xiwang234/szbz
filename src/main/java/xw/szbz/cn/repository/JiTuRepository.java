package xw.szbz.cn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import xw.szbz.cn.entity.JiTu;

import java.util.List;
import java.util.Optional;

@Repository
public interface JiTuRepository extends JpaRepository<JiTu, Long> {
    
    /**
     * 根据openId查询所有吉途记录
     */
    List<JiTu> findByOpenId(String openId);
    
    /**
     * 根据openId查询最新的N条记录
     */
    List<JiTu> findTop10ByOpenIdOrderByCreateTimeDesc(String openId);
    
    /**
     * 根据性别、年月日时查询吉途记录（用于缓存查询）
     * 返回第一条匹配的记录（按创建时间倒序）
     */
    Optional<JiTu> findFirstByGenderAndYearAndMonthAndDayAndHourOrderByCreateTimeDesc(
        String gender, Integer year, Integer month, Integer day, Integer hour
    );
}
