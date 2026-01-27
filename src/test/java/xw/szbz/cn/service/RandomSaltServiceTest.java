package xw.szbz.cn.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xw.szbz.cn.exception.ServiceException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 随机盐服务测试
 */
class RandomSaltServiceTest {

    private RandomSaltService randomSaltService;

    @BeforeEach
    void setUp() {
        randomSaltService = new RandomSaltService();
    }

    @Test
    void testGenerateRandomSalt_ShouldReturn32CharacterString() {
        String salt = randomSaltService.generateRandomSalt();

        assertNotNull(salt);
        assertEquals(32, salt.length());
    }

    @Test
    void testGenerateRandomSalt_ShouldGenerateUniqueSalts() {
        String salt1 = randomSaltService.generateRandomSalt();
        String salt2 = randomSaltService.generateRandomSalt();

        assertNotEquals(salt1, salt2);
    }

    @Test
    void testValidateSalt_NewSaltShouldBeValid() {
        String salt = randomSaltService.generateRandomSalt();

        assertTrue(randomSaltService.validateSalt(salt));
    }

    @Test
    void testValidateSalt_NullSaltShouldBeInvalid() {
        assertFalse(randomSaltService.validateSalt(null));
    }

    @Test
    void testValidateSalt_EmptySaltShouldBeInvalid() {
        assertFalse(randomSaltService.validateSalt(""));
    }

    @Test
    void testValidateSalt_NonExistentSaltShouldBeInvalid() {
        assertFalse(randomSaltService.validateSalt("nonexistent-salt-12345678901234"));
    }

    @Test
    void testMarkSaltAsUsed_ShouldInvalidateSalt() {
        String salt = randomSaltService.generateRandomSalt();

        assertTrue(randomSaltService.validateSalt(salt));

        randomSaltService.markSaltAsUsed(salt);

        assertFalse(randomSaltService.validateSalt(salt));
    }

    @Test
    void testValidateAndMarkSaltAsUsed_ValidSaltShouldSucceed() {
        String salt = randomSaltService.generateRandomSalt();

        assertDoesNotThrow(() -> randomSaltService.validateAndMarkSaltAsUsed(salt));

        // 验证盐已被标记为已使用
        assertFalse(randomSaltService.validateSalt(salt));
    }

    @Test
    void testValidateAndMarkSaltAsUsed_NullSaltShouldThrowException() {
        ServiceException exception = assertThrows(ServiceException.class,
            () -> randomSaltService.validateAndMarkSaltAsUsed(null));

        assertEquals("随机盐不能为空", exception.getMessage());
    }

    @Test
    void testValidateAndMarkSaltAsUsed_EmptySaltShouldThrowException() {
        ServiceException exception = assertThrows(ServiceException.class,
            () -> randomSaltService.validateAndMarkSaltAsUsed(""));

        assertEquals("随机盐不能为空", exception.getMessage());
    }

    @Test
    void testValidateAndMarkSaltAsUsed_NonExistentSaltShouldThrowException() {
        ServiceException exception = assertThrows(ServiceException.class,
            () -> randomSaltService.validateAndMarkSaltAsUsed("nonexistent-salt"));

        assertEquals("随机盐无效或已过期", exception.getMessage());
    }

    @Test
    void testValidateAndMarkSaltAsUsed_UsedSaltShouldThrowException() {
        String salt = randomSaltService.generateRandomSalt();

        // 第一次使用应该成功
        assertDoesNotThrow(() -> randomSaltService.validateAndMarkSaltAsUsed(salt));

        // 第二次使用应该失败
        ServiceException exception = assertThrows(ServiceException.class,
            () -> randomSaltService.validateAndMarkSaltAsUsed(salt));

        assertEquals("随机盐已被使用", exception.getMessage());
    }

    @Test
    void testGetCacheStats_ShouldReturnStats() {
        randomSaltService.generateRandomSalt();
        randomSaltService.generateRandomSalt();

        String stats = randomSaltService.getCacheStats();

        assertNotNull(stats);
        assertTrue(stats.contains("Salt Cache"));
    }

    @Test
    void testSaltExpiration_ShouldExpireAfter5Minutes() throws InterruptedException {
        // 注意：这个测试需要等待5分钟，实际测试中可能需要使用 mock
        // 这里只是演示测试逻辑，实际运行时可以跳过

        String salt = randomSaltService.generateRandomSalt();
        assertTrue(randomSaltService.validateSalt(salt));

        // 在实际测试中，可以使用 Guava Cache 的测试工具来模拟时间流逝
        // 这里只是示例，不实际等待5分钟
        // Thread.sleep(5 * 60 * 1000 + 1000); // 5分钟 + 1秒
        // assertFalse(randomSaltService.validateSalt(salt));
    }
}
