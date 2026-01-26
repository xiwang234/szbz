package xw.szbz.cn.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.checkout.Session;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.param.RefundCreateParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xw.szbz.cn.entity.PaymentRecord;
import xw.szbz.cn.exception.ServiceException;
import xw.szbz.cn.repository.PaymentRecordRepository;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;

/**
 * Stripe支付服务
 * 提供Stripe Checkout集成、Webhook处理、退款等功能
 */
@Service
public class StripePaymentService {
    
    private static final Logger logger = LoggerFactory.getLogger(StripePaymentService.class);
    
    @Value("${stripe.api.secret-key:}")
    private String stripeSecretKey;
    
    @Value("${stripe.webhook.secret:}")
    private String webhookSecret;
    
    @Value("${stripe.checkout.success-url:http://localhost:3000/payment/success}")
    private String successUrl;
    
    @Value("${stripe.checkout.cancel-url:http://localhost:3000/payment/cancel}")
    private String cancelUrl;
    
    @Autowired
    private PaymentRecordRepository paymentRecordRepository;
    
    @PostConstruct
    public void init() {
        if (stripeSecretKey != null && !stripeSecretKey.isEmpty()) {
            Stripe.apiKey = stripeSecretKey;
        }
    }
    
    /**
     * 创建Stripe Checkout Session
     * 
     * @param userId 用户ID
     * @param productName 产品名称
     * @param amount 金额（分）
     * @param currency 币种（如usd）
     * @return Checkout Session URL
     */
    @Transactional
    public String createCheckoutSession(Long userId, String productName, long amount, String currency) {
        try {
            // 1. 创建Stripe Checkout Session参数
            SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl + "?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(cancelUrl)
                .addLineItem(
                    SessionCreateParams.LineItem.builder()
                        .setPriceData(
                            SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency(currency)
                                .setUnitAmount(amount)
                                .setProductData(
                                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName(productName)
                                        .build()
                                )
                                .build()
                        )
                        .setQuantity(1L)
                        .build()
                )
                .putMetadata("user_id", userId.toString())
                .build();
            
            // 2. 创建Session
            Session session = Session.create(params);
            
            // 3. 保存支付记录
            PaymentRecord record = new PaymentRecord();
            record.setUserId(userId);
            record.setSessionId(session.getId());
            record.setProductName(productName);
            record.setAmount(amount);
            record.setCurrency(currency);
            record.setStatus("pending");
            paymentRecordRepository.save(record);
            
            logger.info("Created Stripe checkout session for user {}: {}", userId, session.getId());
            
            // 4. 返回Checkout URL
            return session.getUrl();
            
        } catch (StripeException e) {
            logger.error("Failed to create Stripe checkout session", e);
            throw new ServiceException("Failed to create payment session: " + e.getMessage());
        }
    }
    
    /**
     * 更新支付状态
     */
    @Transactional
    public void updatePaymentStatus(String sessionId, String status, String paymentIntentId) {
        Optional<PaymentRecord> recordOpt = paymentRecordRepository.findBySessionId(sessionId);
        
        if (!recordOpt.isPresent()) {
            logger.warn("Payment record not found for session: {}", sessionId);
            return;
        }
        
        PaymentRecord record = recordOpt.get();
        record.setStatus(status);
        record.setPaymentIntentId(paymentIntentId);
        
        if ("paid".equals(status)) {
            record.setPaidTime(System.currentTimeMillis());
        }
        
        paymentRecordRepository.save(record);
        logger.info("Updated payment status for session {}: {}", sessionId, status);
    }
    
    /**
     * 处理Stripe Webhook
     */
    @Transactional
    public void handleWebhook(String payload, String signatureHeader) {
        Event event;
        
        try {
            // 1. 验证Webhook签名
            event = Webhook.constructEvent(payload, signatureHeader, webhookSecret);
        } catch (Exception e) {
            logger.error("Webhook signature verification failed", e);
            throw new ServiceException("Invalid webhook signature");
        }
        
        // 2. 处理不同类型的事件
        String eventType = event.getType();
        logger.info("Received Stripe webhook event: {}", eventType);
        
        switch (eventType) {
            case "checkout.session.completed":
                handleCheckoutSessionCompleted(event);
                break;
            
            case "payment_intent.succeeded":
                handlePaymentIntentSucceeded(event);
                break;
            
            case "payment_intent.payment_failed":
                handlePaymentIntentFailed(event);
                break;
            
            case "charge.refunded":
                handleChargeRefunded(event);
                break;
            
            default:
                logger.info("Unhandled webhook event type: {}", eventType);
        }
    }
    
    /**
     * 处理退款
     */
    @Transactional
    public void processRefund(String sessionId) {
        Optional<PaymentRecord> recordOpt = paymentRecordRepository.findBySessionId(sessionId);
        
        if (!recordOpt.isPresent()) {
            throw new ServiceException("Payment record not found");
        }
        
        PaymentRecord record = recordOpt.get();
        
        if (!"paid".equals(record.getStatus())) {
            throw new ServiceException("Cannot refund a payment that is not paid");
        }
        
        if (record.getPaymentIntentId() == null) {
            throw new ServiceException("Payment intent ID not found");
        }
        
        try {
            // 创建退款
            RefundCreateParams params = RefundCreateParams.builder()
                .setPaymentIntent(record.getPaymentIntentId())
                .build();
            
            Refund refund = Refund.create(params);
            
            // 更新支付记录
            record.setStatus("refunded");
            record.setRefundTime(System.currentTimeMillis());
            paymentRecordRepository.save(record);
            
            logger.info("Processed refund for session {}: {}", sessionId, refund.getId());
            
        } catch (StripeException e) {
            logger.error("Failed to process refund", e);
            throw new ServiceException("Failed to process refund: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户的支付记录
     */
    public List<PaymentRecord> getUserPayments(Long userId) {
        return paymentRecordRepository.findByUserIdOrderByCreateTimeDesc(userId);
    }
    
    /**
     * 根据Session ID获取支付记录
     */
    public PaymentRecord getPaymentBySessionId(String sessionId) {
        return paymentRecordRepository.findBySessionId(sessionId)
            .orElseThrow(() -> new ServiceException("Payment record not found"));
    }
    
    // ========== 私有方法：处理Webhook事件 ==========
    
    private void handleCheckoutSessionCompleted(Event event) {
        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
        if (deserializer.getObject().isPresent()) {
            Session session = (Session) deserializer.getObject().get();
            String sessionId = session.getId();
            String paymentIntentId = session.getPaymentIntent();
            
            updatePaymentStatus(sessionId, "paid", paymentIntentId);
            logger.info("Checkout session completed: {}", sessionId);
        }
    }
    
    private void handlePaymentIntentSucceeded(Event event) {
        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
        if (deserializer.getObject().isPresent()) {
            PaymentIntent paymentIntent = (PaymentIntent) deserializer.getObject().get();
            String paymentIntentId = paymentIntent.getId();
            
            Optional<PaymentRecord> recordOpt = paymentRecordRepository.findByPaymentIntentId(paymentIntentId);
            if (recordOpt.isPresent()) {
                PaymentRecord record = recordOpt.get();
                record.setStatus("paid");
                record.setPaidTime(System.currentTimeMillis());
                paymentRecordRepository.save(record);
                logger.info("Payment intent succeeded: {}", paymentIntentId);
            }
        }
    }
    
    private void handlePaymentIntentFailed(Event event) {
        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
        if (deserializer.getObject().isPresent()) {
            PaymentIntent paymentIntent = (PaymentIntent) deserializer.getObject().get();
            String paymentIntentId = paymentIntent.getId();
            
            Optional<PaymentRecord> recordOpt = paymentRecordRepository.findByPaymentIntentId(paymentIntentId);
            if (recordOpt.isPresent()) {
                PaymentRecord record = recordOpt.get();
                record.setStatus("failed");
                paymentRecordRepository.save(record);
                logger.info("Payment intent failed: {}", paymentIntentId);
            }
        }
    }
    
    private void handleChargeRefunded(Event event) {
        logger.info("Charge refunded event received");
        // 退款已在processRefund方法中处理
    }
}
