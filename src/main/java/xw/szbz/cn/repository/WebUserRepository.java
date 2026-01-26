package xw.szbz.cn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import xw.szbz.cn.entity.WebUser;

import java.util.Optional;

/**
 * Web用户数据访问接口
 */
@Repository
public interface WebUserRepository extends JpaRepository<WebUser, Long> {
    
    /**
     * 根据用户名查找用户
     */
    Optional<WebUser> findByUsername(String username);
    
    /**
     * 根据加密邮箱查找用户
     */
    Optional<WebUser> findByEmail(String encryptedEmail);
    
    /**
     * 根据邮箱验证令牌查找用户
     */
    Optional<WebUser> findByEmailVerificationToken(String token);
    
    /**
     * 根据密码重置令牌查找用户
     */
    Optional<WebUser> findByPasswordResetToken(String token);
    
    /**
     * 检查用户名是否已存在
     */
    boolean existsByUsername(String username);
    
    /**
     * 检查邮箱是否已存在（加密后的邮箱）
     */
    boolean existsByEmail(String encryptedEmail);
}
