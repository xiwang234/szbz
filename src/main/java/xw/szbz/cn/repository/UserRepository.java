package xw.szbz.cn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import xw.szbz.cn.entity.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * 根据openId查询用户
     */
    Optional<User> findByOpenId(String openId);
    
    /**
     * 检查openId是否已存在
     */
    boolean existsByOpenId(String openId);
}
