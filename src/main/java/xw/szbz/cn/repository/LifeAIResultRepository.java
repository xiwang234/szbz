package xw.szbz.cn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import xw.szbz.cn.entity.LifeAIResult;

import java.util.List;

/**
 * LifeAI 分析结果数据访问接口
 */
@Repository
public interface LifeAIResultRepository extends JpaRepository<LifeAIResult, Long> {

    /**
     * 根据用户ID查询所有分析记录
     */
    List<LifeAIResult> findByUserIdOrderByCreateTimeDesc(Long userId);

    /**
     * 根据用户ID和分类查询分析记录
     */
    List<LifeAIResult> findByUserIdAndCategoryOrderByCreateTimeDesc(Long userId, String category);
}
