package xw.szbz.cn.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * LiuRenService测试类
 */
@SpringBootTest
class LiuRenServiceTest {

    @Autowired
    private LiuRenService liuRenService;

    /**
     * 测试生成课传信息
     */
    @Test
    void testGenerateCourseInfo() {
        // 使用固定时间测试
        LocalDateTime testTime = LocalDateTime.of(2025, 12, 20, 14, 30);
        
        String courseInfo = liuRenService.generateCourseInfo(testTime);
        
        assertNotNull(courseInfo, "课传信息不应为null");
        assertFalse(courseInfo.isEmpty(), "课传信息不应为空");
        
        // 验证格式
        assertTrue(courseInfo.contains("年"), "应包含'年'");
        assertTrue(courseInfo.contains("月"), "应包含'月'");
        assertTrue(courseInfo.contains("日"), "应包含'日'");
        assertTrue(courseInfo.contains("时"), "应包含'时'");
        assertTrue(courseInfo.contains("将"), "应包含'将'");
        
        System.out.println("生成的课传信息: " + courseInfo);
    }

    /**
     * 测试生成当前课传信息
     */
    @Test
    void testGenerateCourseInfoCurrent() {
        String courseInfo = liuRenService.generateCourseInfo();
        
        assertNotNull(courseInfo, "课传信息不应为null");
        assertFalse(courseInfo.isEmpty(), "课传信息不应为空");
        
        System.out.println("当前课传信息: " + courseInfo);
    }

    /**
     * 测试转换出生年份为干支
     */
    @Test
    void testConvertBirthYearToGanZhi() {
        // 测试几个已知的年份
        String ganZhi1984 = liuRenService.convertBirthYearToGanZhi(1984);
        assertEquals("1984年甲子年", ganZhi1984, "1984年应该是甲子年");
        
        String ganZhi2025 = liuRenService.convertBirthYearToGanZhi(2025);
        assertEquals("2025年乙巳年", ganZhi2025, "2025年应该是乙巳年");
        
        String ganZhi2000 = liuRenService.convertBirthYearToGanZhi(2000);
        assertEquals("2000年庚辰年", ganZhi2000, "2000年应该是庚辰年");
        
        System.out.println("1984年: " + ganZhi1984);
        System.out.println("2025年: " + ganZhi2025);
        System.out.println("2000年: " + ganZhi2000);
    }

    /**
     * 测试生成出生信息
     */
    @Test
    void testGenerateBirthInfo() {
        String birthInfoMale = liuRenService.generateBirthInfo(1984, "男");
        assertEquals("1984年甲子年男命", birthInfoMale, "男命格式应正确");
        
        String birthInfoFemale = liuRenService.generateBirthInfo(1984, "女");
        assertEquals("1984年甲子年女命", birthInfoFemale, "女命格式应正确");
        
        System.out.println("男命: " + birthInfoMale);
        System.out.println("女命: " + birthInfoFemale);
    }

    /**
     * 测试不同月份的月将计算
     */
    @Test
    void testMonthJiang() {
        // 测试一年12个月的课传信息
        for (int month = 1; month <= 12; month++) {
            LocalDateTime testTime = LocalDateTime.of(2025, month, 15, 12, 0);
            String courseInfo = liuRenService.generateCourseInfo(testTime);
            System.out.println(month + "月课传: " + courseInfo);
            assertNotNull(courseInfo);
        }
    }
}
