package xw.szbz.cn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import xw.szbz.cn.entity.PaymentRecord;

import java.util.List;
import java.util.Optional;

/**
 * 支付记录数据访问接口
 */
@Repository
public interface PaymentRecordRepository extends JpaRepository<PaymentRecord, Long> {
    
    /**
     * 根据Session ID查找支付记录
     */
    Optional<PaymentRecord> findBySessionId(String sessionId);
    
    /**
     * 根据Payment Intent ID查找支付记录
     */
    Optional<PaymentRecord> findByPaymentIntentId(String paymentIntentId);
    
    /**
     * 查找用户的所有支付记录
     */
    List<PaymentRecord> findByUserIdOrderByCreateTimeDesc(Long userId);
    
    /**
     * 查找用户指定状态的支付记录
     */
    List<PaymentRecord> findByUserIdAndStatus(Long userId, String status);
}
