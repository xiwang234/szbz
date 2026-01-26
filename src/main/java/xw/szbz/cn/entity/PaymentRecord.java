package xw.szbz.cn.entity;

import jakarta.persistence.*;

/**
 * 支付记录实体
 */
@Entity
@Table(name = "payment_record")
public class PaymentRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 关联的WebUser ID
     */
    @Column(nullable = false)
    private Long userId;
    
    /**
     * Stripe Checkout Session ID
     */
    @Column(nullable = false, unique = true, length = 200)
    private String sessionId;
    
    /**
     * Stripe Payment Intent ID
     */
    @Column(length = 200)
    private String paymentIntentId;
    
    /**
     * 产品名称
     */
    @Column(nullable = false, length = 200)
    private String productName;
    
    /**
     * 金额（分）
     */
    @Column(nullable = false)
    private Long amount;
    
    /**
     * 币种（如USD, EUR, CNY）
     */
    @Column(nullable = false, length = 3)
    private String currency;
    
    /**
     * 支付状态：pending, paid, failed, refunded
     */
    @Column(nullable = false, length = 20)
    private String status;
    
    /**
     * 创建时间（13位时间戳）
     */
    @Column(nullable = false)
    private Long createTime;
    
    /**
     * 支付完成时间（13位时间戳）
     */
    private Long paidTime;
    
    /**
     * 退款时间（13位时间戳）
     */
    private Long refundTime;
    
    /**
     * 备注
     */
    @Column(length = 500)
    private String note;
    
    // 构造函数
    public PaymentRecord() {
        this.createTime = System.currentTimeMillis();
        this.status = "pending";
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public String getPaymentIntentId() {
        return paymentIntentId;
    }
    
    public void setPaymentIntentId(String paymentIntentId) {
        this.paymentIntentId = paymentIntentId;
    }
    
    public String getProductName() {
        return productName;
    }
    
    public void setProductName(String productName) {
        this.productName = productName;
    }
    
    public Long getAmount() {
        return amount;
    }
    
    public void setAmount(Long amount) {
        this.amount = amount;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Long getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }
    
    public Long getPaidTime() {
        return paidTime;
    }
    
    public void setPaidTime(Long paidTime) {
        this.paidTime = paidTime;
    }
    
    public Long getRefundTime() {
        return refundTime;
    }
    
    public void setRefundTime(Long refundTime) {
        this.refundTime = refundTime;
    }
    
    public String getNote() {
        return note;
    }
    
    public void setNote(String note) {
        this.note = note;
    }
}
