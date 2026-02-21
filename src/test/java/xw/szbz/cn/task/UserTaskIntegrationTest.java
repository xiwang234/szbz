package xw.szbz.cn.task;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * UserTask integration test
 * This test will send REAL email to txw250@163.com
 * Make sure the database has some test data before running
 */
@SpringBootTest
@ActiveProfiles("test")
class UserTaskIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(UserTaskIntegrationTest.class);

    @Autowired
    private UserTask userTask;

    /**
     * Test sending real email
     * This will:
     * 1. Query real database for user count
     * 2. Query real database for active users
     * 3. Build real email content
     * 4. Send REAL email to txw250@163.com
     *
     * Check your email inbox after running this test!
     */
    @Test
    void testSendRealEmail() {
        logger.info("=================================================");
        logger.info("Starting REAL email test");
        logger.info("This will send a REAL email to txw250@163.com");
        logger.info("=================================================");

        try {
            // Call the real method
            userTask.sendUserStatisticsEmail();

            logger.info("=================================================");
            logger.info("Email sent successfully!");
            logger.info("Please check your email inbox: txw250@163.com");
            logger.info("=================================================");

        } catch (Exception e) {
            logger.error("=================================================");
            logger.error("Failed to send email!");
            logger.error("Error: {}", e.getMessage(), e);
            logger.error("=================================================");
            throw e;
        }
    }
}
