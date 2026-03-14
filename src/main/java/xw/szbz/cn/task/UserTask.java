package xw.szbz.cn.task;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import xw.szbz.cn.model.UserFreeCountDto;
import xw.szbz.cn.repository.WebUserRepository;
import xw.szbz.cn.service.EmailService;

/**
 * User scheduled tasks
 */
@Component
public class UserTask {

    private static final Logger logger = LoggerFactory.getLogger(UserTask.class);

    @Autowired
    private WebUserRepository webUserRepository;

    @Autowired
    private EmailService emailService;

    @Value("${email.scheduled.recipient}")
    private String recipient;

    /**
     * Send user statistics email every 3 hours
     * cron expression: 0 0 0/3 * * ?
     * runs at: 0:00, 3:00, 6:00, 9:00, 12:00, 15:00, 18:00, 21:00
     */
    @Scheduled(cron = "0 0 18 * * ?")
    public void sendUserStatisticsEmail() {
        logger.info("Start scheduled task: send user statistics email");

        try {
            // 1. Query total user count
            long totalUserCount = webUserRepository.count();

            // 2. Query all active users' ID and free count
            List<UserFreeCountDto> activeUsers = webUserRepository.findActiveUsersFreeCount();

            // 3. Build email content
            String emailContent = buildEmailContent(totalUserCount, activeUsers);
            //已使用次数
            int activeCount = buildActiveCount(activeUsers);
            // 4. Send email
            emailService.sendHtmlEmail(recipient, "lifeai用户" + totalUserCount + "个,已使用" + activeCount + "次", emailContent);

        } catch (Exception e) {
            logger.error("Scheduled task failed", e);
        }
    }
    private int buildActiveCount(List<UserFreeCountDto> activeUsers) {
        int freeCount = 0;
        int allCount = activeUsers.size() * 5;
        for (UserFreeCountDto user : activeUsers) {
            freeCount += user.getFreeCount();
        }
        return allCount - freeCount;
    }
    /**
     * Build HTML email content
     */
    private String buildEmailContent(long totalUserCount, List<UserFreeCountDto> activeUsers) {
        StringBuilder html = new StringBuilder();

        // Get current time
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        html.append("<!DOCTYPE html>");
        html.append("<html>");
        html.append("<head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }");
        html.append("h2 { color: #2c3e50; border-bottom: 2px solid #3498db; padding-bottom: 10px; }");
        html.append("p { margin: 10px 0; }");
        html.append(".info { background-color: #ecf0f1; padding: 15px; border-radius: 5px; margin: 20px 0; }");
        html.append("table { border-collapse: collapse; width: 100%; margin-top: 20px; }");
        html.append("th { background-color: #3498db; color: white; padding: 12px; text-align: left; }");
        html.append("td { border: 1px solid #ddd; padding: 10px; }");
        html.append("tr:nth-child(even) { background-color: #f2f2f2; }");
        html.append("tr:hover { background-color: #e8f4f8; }");
        html.append(".footer { margin-top: 30px; font-size: 12px; color: #7f8c8d; }");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");

        // Title
        html.append("<h2>LifeAI 用户信息统计报告</h2>");

        // Basic info
        html.append("<div class='info'>");
        html.append("<p><strong>统计时间:</strong>").append(currentTime).append("</p>");
        html.append("<p><strong>用户总数:</strong>").append(totalUserCount).append("</p>");
        html.append("<p><strong>激活用户数:</strong>").append(activeUsers.size()).append("</p>");
        html.append("</div>");

        // User free count list
        html.append("<h3>每个用户免费剩余使用次数</h3>");

        if (activeUsers.isEmpty()) {
            html.append("<p>暂无激活用户</p>");
        } else {
            html.append("<table>");
            html.append("<thead>");
            html.append("<tr>");
            html.append("<th>用户ID</th>");
            html.append("<th>免费剩余次数</th>");
            html.append("</tr>");
            html.append("</thead>");
            html.append("<tbody>");

            for (UserFreeCountDto user : activeUsers) {
                html.append("<tr>");
                html.append("<td>").append(user.getId()).append("</td>");
                html.append("<td>").append(user.getFreeCount() != null ? user.getFreeCount() : 0).append("</td>");
                html.append("</tr>");
            }

            html.append("</tbody>");
            html.append("</table>");
        }

        // Footer
        html.append("<div class='footer'>");
        html.append("<p>此邮件由 LifeAI 系统自动发送, 每3小时发送一次 (整点执行)。</p>");
        html.append("</div>");

        html.append("</body>");
        html.append("</html>");

        return html.toString();
    }
}
