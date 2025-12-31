package xw.szbz.cn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
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
}
