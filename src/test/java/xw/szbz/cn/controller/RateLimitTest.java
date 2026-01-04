package xw.szbz.cn.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import xw.szbz.cn.entity.JiTu;
import xw.szbz.cn.entity.WenJi;
import xw.szbz.cn.repository.JiTuRepository;
import xw.szbz.cn.repository.WenJiRepository;

import java.time.LocalDate;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 限流功能测试
 */
@SpringBootTest
public class RateLimitTest {

    @Autowired
    private WenJiRepository wenJiRepository;

    @Autowired
    private JiTuRepository jiTuRepository;

    @Test
    public void testWenJiRateLimit() {
        String testOpenId = "test-rate-limit-wenji-" + System.currentTimeMillis();
        
        // 获取今日时间范围
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        long startOfDay = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long endOfDay = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1;
        
        // 插入3条今日记录
        for (int i = 0; i < 3; i++) {
            WenJi wenJi = new WenJi(
                testOpenId,
                "测试问题" + i,
                "测试背景",
                1990,
                "male",
                "测试结果",
                System.currentTimeMillis()
            );
            wenJiRepository.save(wenJi);
        }
        
        // 查询今日提交次数
        long todayCount = wenJiRepository.countByOpenIdAndCreateTimeBetween(testOpenId, startOfDay, endOfDay);
        
        System.out.println("问吉今日提交次数: " + todayCount);
        assertEquals(3, todayCount, "应该有3条今日记录");
        
        // 清理测试数据
        wenJiRepository.deleteAll(wenJiRepository.findByOpenId(testOpenId));
    }

    @Test
    public void testJiTuRateLimit() {
        String testOpenId = "test-rate-limit-jitu-" + System.currentTimeMillis();
        
        // 获取今日时间范围
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        long startOfDay = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long endOfDay = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1;
        
        // 插入5条今日记录
        for (int i = 0; i < 5; i++) {
            JiTu jiTu = new JiTu(
                testOpenId,
                "male",
                1990,
                1,
                1 + i,
                10,
                "{\"test\": \"result\"}",
                System.currentTimeMillis()
            );
            jiTuRepository.save(jiTu);
        }
        
        // 查询今日提交次数
        long todayCount = jiTuRepository.countByOpenIdAndCreateTimeBetween(testOpenId, startOfDay, endOfDay);
        
        System.out.println("吉途今日提交次数: " + todayCount);
        assertEquals(5, todayCount, "应该有5条今日记录");
        
        // 清理测试数据
        jiTuRepository.deleteAll(jiTuRepository.findByOpenId(testOpenId));
    }

    @Test
    public void testCrossDayRateLimit() {
        String testOpenId = "test-cross-day-" + System.currentTimeMillis();
        
        // 获取今日和昨日时间范围
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        LocalDate yesterday = today.minusDays(1);
        
        long startOfToday = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long endOfToday = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1;
        long yesterdayTime = yesterday.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        
        // 插入2条今日记录
        for (int i = 0; i < 2; i++) {
            WenJi wenJi = new WenJi(
                testOpenId,
                "今日问题" + i,
                "测试背景",
                1990,
                "male",
                "测试结果",
                System.currentTimeMillis()
            );
            wenJiRepository.save(wenJi);
        }
        
        // 插入1条昨日记录
        WenJi yesterdayWenJi = new WenJi(
            testOpenId,
            "昨日问题",
            "测试背景",
            1990,
            "male",
            "测试结果",
            yesterdayTime
        );
        wenJiRepository.save(yesterdayWenJi);
        
        // 查询今日提交次数（应该只统计今日的2条）
        long todayCount = wenJiRepository.countByOpenIdAndCreateTimeBetween(testOpenId, startOfToday, endOfToday);
        
        System.out.println("跨天测试 - 今日提交次数: " + todayCount);
        assertEquals(2, todayCount, "应该只统计今日的2条记录");
        
        // 清理测试数据
        wenJiRepository.deleteAll(wenJiRepository.findByOpenId(testOpenId));
    }
}
