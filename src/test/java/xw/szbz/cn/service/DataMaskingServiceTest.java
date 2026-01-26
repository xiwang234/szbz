package xw.szbz.cn.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 数据脱敏服务测试
 */
class DataMaskingServiceTest {
    
    private DataMaskingService maskingService;
    
    @BeforeEach
    void setUp() {
        maskingService = new DataMaskingService();
    }
    
    @Test
    void testMaskEmail() {
        assertEquals("u***@example.com", maskingService.maskEmail("user@example.com"));
        assertEquals("j***@gmail.com", maskingService.maskEmail("john@gmail.com"));
        assertEquals("a***@company.co.uk", maskingService.maskEmail("admin@company.co.uk"));
        assertEquals("v***@test.org", maskingService.maskEmail("verylongemailaddress@test.org"));
        
        System.out.println("Email masking tests:");
        System.out.println("user@example.com -> " + maskingService.maskEmail("user@example.com"));
        System.out.println("john@gmail.com -> " + maskingService.maskEmail("john@gmail.com"));
    }
    
    @Test
    void testMaskShortEmail() {
        assertEquals("a*@example.com", maskingService.maskEmail("ab@example.com"));
        assertEquals("x***@test.com", maskingService.maskEmail("x@test.com"));
    }
    
    @Test
    void testMaskPhone() {
        assertEquals("138****5678", maskingService.maskPhone("13812345678"));
        assertEquals("186****1234", maskingService.maskPhone("18612341234"));
        assertEquals("123****5678", maskingService.maskPhone("12345678901"));
        
        System.out.println("\nPhone masking tests:");
        System.out.println("13812345678 -> " + maskingService.maskPhone("13812345678"));
        System.out.println("18612341234 -> " + maskingService.maskPhone("18612341234"));
    }
    
    @Test
    void testMaskPhoneWithNonDigits() {
        assertEquals("138****5678", maskingService.maskPhone("+86 138-1234-5678"));
        assertEquals("138****5678", maskingService.maskPhone("138 1234 5678"));
    }
    
    @Test
    void testMaskIdCard() {
        assertEquals("110101********1234", maskingService.maskIdCard("110101199001011234"));
        assertEquals("320101*****2345", maskingService.maskIdCard("320101900012345"));
        
        System.out.println("\nID card masking tests:");
        System.out.println("110101199001011234 -> " + maskingService.maskIdCard("110101199001011234"));
    }
    
    @Test
    void testMaskName() {
        assertEquals("张*", maskingService.maskName("张三"));
        assertEquals("张**", maskingService.maskName("张三丰"));
        assertEquals("王****", maskingService.maskName("王二小三"));
        assertEquals("李", maskingService.maskName("李"));
        
        System.out.println("\nName masking tests:");
        System.out.println("张三 -> " + maskingService.maskName("张三"));
        System.out.println("张三丰 -> " + maskingService.maskName("张三丰"));
    }
    
    @Test
    void testMaskBankCard() {
        assertEquals("6222****0123", maskingService.maskBankCard("6222021234567890123"));
        assertEquals("6228****5678", maskingService.maskBankCard("6228481234565678"));
        
        System.out.println("\nBank card masking tests:");
        System.out.println("6222021234567890123 -> " + maskingService.maskBankCard("6222021234567890123"));
    }
    
    @Test
    void testMaskBankCardWithSpaces() {
        assertEquals("6222****0123", maskingService.maskBankCard("6222 0212 3456 7890 123"));
        assertEquals("6228****5678", maskingService.maskBankCard("6228-4812-3456-5678"));
    }
    
    @Test
    void testMaskGeneric() {
        assertEquals("a***e", maskingService.maskGeneric("abcde"));
        assertEquals("te***ng", maskingService.maskGeneric("testing"));
        assertEquals("***", maskingService.maskGeneric("abc"));
        
        System.out.println("\nGeneric masking tests:");
        System.out.println("abcde -> " + maskingService.maskGeneric("abcde"));
        System.out.println("testing -> " + maskingService.maskGeneric("testing"));
    }
    
    @Test
    void testMaskNullAndEmpty() {
        assertNull(maskingService.maskEmail(null));
        assertEquals("", maskingService.maskEmail(""));
        
        assertNull(maskingService.maskPhone(null));
        assertEquals("", maskingService.maskPhone(""));
        
        assertNull(maskingService.maskName(null));
        assertEquals("", maskingService.maskName(""));
    }
}
