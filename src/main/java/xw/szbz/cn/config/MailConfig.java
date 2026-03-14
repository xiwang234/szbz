package xw.szbz.cn.config;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/**
 * 邮件发送配置
 * 配置两个 JavaMailSender：163（内部定时任务）和 Gmail（网站对外邮件）
 */
@Configuration
public class MailConfig {

    @Value("${spring.mail.username}")
    private String gmailUsername;

    @Value("${spring.mail.password}")
    private String gmailPassword;

    /**
     * Gmail 邮件发送器（网站对外邮件，如密码重置）
     * 使用 Gmail SMTP + App Password
     */
    @Bean(name = "gmailSender")
    public JavaMailSender gmailSender() {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost("smtp.gmail.com");
        sender.setPort(587);
        sender.setUsername(gmailUsername);
        sender.setPassword(gmailPassword);
        sender.setDefaultEncoding("UTF-8");

        Properties props = sender.getJavaMailProperties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");

        return sender;
    }
}
