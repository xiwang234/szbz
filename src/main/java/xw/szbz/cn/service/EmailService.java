package xw.szbz.cn.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

/**
 * 邮件发送服务
 */
@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    @Qualifier("gmailSender")
    private JavaMailSender gmailMailSender;

    @Value("${spring.mail.username}")
    private String gmailSender;

    /**
     * 发送HTML邮件
     *
     * @param to 收件人
     * @param subject 主题
     * @param htmlContent HTML内容
     */
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = gmailMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(gmailSender);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true表示HTML格式

            gmailMailSender.send(message);
            logger.info("邮件发送成功，收件人: {}, 主题: {}", to, subject);

        } catch (Exception e) {
            logger.error("邮件发送失败，收件人: {}, 主题: {}", to, subject, e);
            throw new RuntimeException("邮件发送失败: " + e.getMessage(), e);
        }
    }

    /**
     * 发送纯文本邮件
     *
     * @param to 收件人
     * @param subject 主题
     * @param textContent 文本内容
     */
    public void sendTextEmail(String to, String subject, String textContent) {
        try {
            MimeMessage message = gmailMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(gmailSender);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(textContent, false); // false表示纯文本格式

            gmailMailSender.send(message);
            logger.info("邮件发送成功，收件人: {}, 主题: {}", to, subject);

        } catch (Exception e) {
            logger.error("邮件发送失败，收件人: {}, 主题: {}", to, subject, e);
            throw new RuntimeException("邮件发送失败: " + e.getMessage(), e);
        }
    }

    /**
     * 使用 Gmail 发送 HTML 邮件（网站对外邮件，如密码重置）
     * 发件人：main.lifeai@gmail.com
     *
     * @param to 收件人
     * @param subject 主题
     * @param htmlContent HTML内容
     */
    public void sendHtmlEmailViaGmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = gmailMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(gmailSender);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            gmailMailSender.send(message);
            logger.info("Gmail邮件发送成功，收件人: {}, 主题: {}", to, subject);

        } catch (Exception e) {
            logger.error("Gmail邮件发送失败，收件人: {}, 主题: {}", to, subject, e);
            throw new RuntimeException("Gmail邮件发送失败: " + e.getMessage(), e);
        }
    }
}

