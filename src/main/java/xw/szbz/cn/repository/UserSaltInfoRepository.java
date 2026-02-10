package xw.szbz.cn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import xw.szbz.cn.entity.UserSaltInfo;

import java.util.Optional;

/**
 * 用户随机盐信息数据访问接口
 */
@Repository
public interface UserSaltInfoRepository extends JpaRepository<UserSaltInfo, Long> {

    /**
     * 根据邮箱和随机盐查询记录
     */
    Optional<UserSaltInfo> findByEmailAndSalt(String email, String salt);

    /**
     * 根据邮箱和状态查询最新的未使用随机盐
     */
    Optional<UserSaltInfo> findFirstByEmailAndStatusOrderByCreateTimeDesc(String email, Integer status);
}
