package xw.szbz.cn.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 密码哈希工具测试
 */
class PasswordHashUtilTest {

    private PasswordHashUtil passwordHashUtil;

    @BeforeEach
    void setUp() {
        passwordHashUtil = new PasswordHashUtil();
    }

    @Test
    void testSha256Hash_ShouldReturnConsistentHash() {
        String password = "testPassword123";

        String hash1 = passwordHashUtil.sha256Hash(password);
        String hash2 = passwordHashUtil.sha256Hash(password);

        assertEquals(hash1, hash2);
    }

    @Test
    void testSha256Hash_ShouldReturn64CharacterHexString() {
        String password = "testPassword123";

        String hash = passwordHashUtil.sha256Hash(password);

        assertNotNull(hash);
        assertEquals(64, hash.length()); // SHA-256 produces 64 hex characters
        assertTrue(hash.matches("^[a-f0-9]{64}$")); // Only lowercase hex characters
    }

    @Test
    void testSha256Hash_DifferentPasswordsShouldProduceDifferentHashes() {
        String password1 = "password1";
        String password2 = "password2";

        String hash1 = passwordHashUtil.sha256Hash(password1);
        String hash2 = passwordHashUtil.sha256Hash(password2);

        assertNotEquals(hash1, hash2);
    }

    @Test
    void testSha256Hash_EmptyStringShouldProduceValidHash() {
        String password = "";

        String hash = passwordHashUtil.sha256Hash(password);

        assertNotNull(hash);
        assertEquals(64, hash.length());
        // SHA-256 of empty string is a known value
        assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", hash);
    }

    @Test
    void testSha256Hash_WithSalt() {
        String password = "myPassword";
        String salt = "mySalt";

        String hash = passwordHashUtil.sha256Hash(password + salt);

        assertNotNull(hash);
        assertEquals(64, hash.length());
    }

    @Test
    void testMatches_CorrectPasswordShouldMatch() {
        String rawPassword = "testPassword123";
        String hashedPassword = passwordHashUtil.sha256Hash(rawPassword);

        assertTrue(passwordHashUtil.matches(rawPassword, hashedPassword));
    }

    @Test
    void testMatches_IncorrectPasswordShouldNotMatch() {
        String rawPassword = "testPassword123";
        String wrongPassword = "wrongPassword";
        String hashedPassword = passwordHashUtil.sha256Hash(rawPassword);

        assertFalse(passwordHashUtil.matches(wrongPassword, hashedPassword));
    }

    @Test
    void testMatches_CaseSensitive() {
        String rawPassword = "TestPassword";
        String hashedPassword = passwordHashUtil.sha256Hash(rawPassword);

        assertFalse(passwordHashUtil.matches("testpassword", hashedPassword));
    }

    @Test
    void testSha256Hash_KnownTestVector() {
        // Test with a known SHA-256 test vector
        String input = "abc";
        String expectedHash = "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad";

        String actualHash = passwordHashUtil.sha256Hash(input);

        assertEquals(expectedHash, actualHash);
    }

    @Test
    void testSha256Hash_WithFixedAndRandomSalt() {
        String rawPassword = "myPassword";
        String fixedSalt = "fixed-salt-123";
        String randomSalt = "random-salt-456";

        // Simulate registration: SHA256(password + fixedSalt)
        String registrationHash = passwordHashUtil.sha256Hash(rawPassword + fixedSalt);

        // Simulate login: SHA256(registrationHash + randomSalt)
        String loginHash = passwordHashUtil.sha256Hash(registrationHash + randomSalt);

        // Verify the login hash can be reproduced
        String verificationHash = passwordHashUtil.sha256Hash(registrationHash + randomSalt);
        assertEquals(loginHash, verificationHash);
    }

    @Test
    void testSha256Hash_UnicodeCharacters() {
        String password = "密码测试123";

        String hash = passwordHashUtil.sha256Hash(password);

        assertNotNull(hash);
        assertEquals(64, hash.length());

        // Verify consistency
        String hash2 = passwordHashUtil.sha256Hash(password);
        assertEquals(hash, hash2);
    }

    @Test
    void testSha256Hash_SpecialCharacters() {
        String password = "p@ssw0rd!#$%^&*()";

        String hash = passwordHashUtil.sha256Hash(password);

        assertNotNull(hash);
        assertEquals(64, hash.length());
        assertTrue(passwordHashUtil.matches(password, hash));
    }

    @Test
    void testSha256Hash_LongPassword() {
        String password = "a".repeat(1000); // 1000 character password

        String hash = passwordHashUtil.sha256Hash(password);

        assertNotNull(hash);
        assertEquals(64, hash.length());
        assertTrue(passwordHashUtil.matches(password, hash));
    }
}
