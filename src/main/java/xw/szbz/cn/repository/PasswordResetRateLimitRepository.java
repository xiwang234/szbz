package xw.szbz.cn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import xw.szbz.cn.entity.PasswordResetRateLimit;

import java.util.Optional;

@Repository
public interface PasswordResetRateLimitRepository extends JpaRepository<PasswordResetRateLimit, Long> {

    Optional<PasswordResetRateLimit> findByEmailAndDateKey(String email, String dateKey);
}
