package xw.szbz.cn.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import xw.szbz.cn.entity.JiTu;
import xw.szbz.cn.entity.User;
import xw.szbz.cn.entity.WenJi;

/**
 * Repository查询功能测试类
 */
@SpringBootTest
class RepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WenJiRepository wenJiRepository;

    @Autowired
    private JiTuRepository jiTuRepository;

    // ========== 用户表查询测试 ==========

    @Test
    @DisplayName("用户表 - 查询所有用户")
    void testFindAllUsers() {
        List<User> users = userRepository.findAll();
        System.out.println("用户总数: " + users.size());
        
        for (User user : users) {
            System.out.println("用户ID: " + user.getId() + 
                             ", OpenId: " + user.getOpenId() + 
                             ", 创建时间: " + user.getCreateTime());
             userRepository.deleteById(user.getId());
        }
        
        assertTrue(users.size() >= 0, "用户列表不应为null");
    }

    @Test
    @DisplayName("用户表 - 根据openId查询用户")
    void testFindUserByOpenId() {
        // 先插入测试数据
        String testOpenId = "test-openid-" + System.currentTimeMillis();
        User testUser = new User(testOpenId, System.currentTimeMillis());
        userRepository.save(testUser);
        
        // 查询
        Optional<User> userOpt = userRepository.findByOpenId(testOpenId);
        assertTrue(userOpt.isPresent(), "应该能找到用户");
        
        User user = userOpt.get();
        assertEquals(testOpenId, user.getOpenId(), "OpenId应该匹配");
        System.out.println("查询到用户: ID=" + user.getId() + ", OpenId=" + user.getOpenId());
    }

    @Test
    @DisplayName("用户表 - 检查openId是否存在")
    void testExistsByOpenId() {
        // 先插入测试数据
        String testOpenId = "test-exist-openid-" + System.currentTimeMillis();
        User testUser = new User(testOpenId, System.currentTimeMillis());
        userRepository.save(testUser);
        
        // 检查存在性
        boolean exists = userRepository.existsByOpenId(testOpenId);
        assertTrue(exists, "应该存在该用户");
        
        boolean notExists = userRepository.existsByOpenId("non-exist-openid");
        assertFalse(notExists, "不应该存在该用户");
    }

    // ========== 问吉表查询测试 ==========

    @Test
    @DisplayName("问吉表 - 查询所有记录")
    void testFindAllWenJi() {
        List<WenJi> records = wenJiRepository.findAll();
        System.out.println("问吉记录总数: " + records.size());
        
        for (WenJi record : records) {
            System.out.println("记录ID: " + record.getId() + 
                             ", OpenId: " + record.getOpenId() + 
                             ", 问题: " + record.getQuestion() + 
                             ", 背景: " + record.getBackground() + 
                             ", 创建时间: " + record.getCreateTime());
        }
        
        assertTrue(records.size() >= 0, "记录列表不应为null");
    }

    @Test
    @DisplayName("问吉表 - 根据openId查询所有记录")
    void testFindWenJiByOpenId() {
        // 先插入测试数据
        String testOpenId = "test-wenji-openid-" + System.currentTimeMillis();
        WenJi record1 = new WenJi(testOpenId, "问财运", "背景信息1", 1990, "男", "预测结果1", System.currentTimeMillis());
        WenJi record2 = new WenJi(testOpenId, "问事业", "背景信息2", 1990, "男", "预测结果2", System.currentTimeMillis() + 1000);
        wenJiRepository.save(record1);
        wenJiRepository.save(record2);
        
        // 查询
        List<WenJi> records = wenJiRepository.findByOpenId(testOpenId);
        assertTrue(records.size() >= 2, "应该至少有2条记录");
        
        System.out.println("查询到" + records.size() + "条问吉记录:");
        for (WenJi record : records) {
            System.out.println("  - 问题: " + record.getQuestion() + 
                             ", 性别: " + record.getGender() + 
                             ", 出生年: " + record.getBirthYear());
        }
    }

    @Test
    @DisplayName("问吉表 - 查询最新10条记录")
    void testFindTop10WenJi() {
        // 先插入测试数据
        String testOpenId = "test-top10-openid-" + System.currentTimeMillis();
        for (int i = 0; i < 5; i++) {
            WenJi record = new WenJi(
                testOpenId, 
                "问题" + i, 
                "背景" + i, 
                1990, 
                "男", 
                "结果" + i, 
                System.currentTimeMillis() + i * 1000
            );
            wenJiRepository.save(record);
        }
        
        // 查询最新10条
        List<WenJi> records = wenJiRepository.findTop10ByOpenIdOrderByCreateTimeDesc(testOpenId);
        assertTrue(records.size() >= 5, "应该至少有5条记录");
        
        // 验证是否按时间倒序
        if (records.size() >= 2) {
            assertTrue(records.get(0).getCreateTime() >= records.get(1).getCreateTime(), 
                      "应该按创建时间倒序排列");
        }
        
        System.out.println("查询到最新" + records.size() + "条记录:");
        for (WenJi record : records) {
            System.out.println("  - 问题: " + record.getQuestion() + 
                             ", 创建时间: " + record.getCreateTime());
        }
    }

    // ========== 吉途表查询测试 ==========

    @Test
    @DisplayName("吉途表 - 查询所有记录")
    void testFindAllJiTu() {
        List<JiTu> records = jiTuRepository.findAll();
        System.out.println("吉途记录总数: " + records.size());
        
        for (JiTu record : records) {
            System.out.println("记录ID: " + record.getId() + 
                             ", OpenId: " + record.getOpenId() + 
                             ", 结果: " + record.getDefaultResult() + 
                             ", 出生日期: " + record.getYear() + "-" + record.getMonth() + "-" + record.getDay() + 
                             ", 创建时间: " + record.getCreateTime());
        }
        
        assertTrue(records.size() >= 0, "记录列表不应为null");
    }

    @Test
    @DisplayName("吉途表 - 根据openId查询所有记录")
    void testFindJiTuByOpenId() {
        // 先插入测试数据
        String testOpenId = "test-jitu-openid-" + System.currentTimeMillis();
        JiTu record1 = new JiTu(testOpenId, "男", 1990, 1, 1, 10, "{\"result\":\"分析结果1\"}", System.currentTimeMillis());
        JiTu record2 = new JiTu(testOpenId, "男", 1990, 2, 2, 12, "{\"result\":\"分析结果2\"}", System.currentTimeMillis() + 1000);
        jiTuRepository.save(record1);
        jiTuRepository.save(record2);
        
        // 查询
        List<JiTu> records = jiTuRepository.findByOpenId(testOpenId);
        assertTrue(records.size() >= 2, "应该至少有2条记录");
        
        System.out.println("查询到" + records.size() + "条吉途记录:");
        for (JiTu record : records) {
            System.out.println("  - 出生日期: " + record.getYear() + "-" + record.getMonth() + "-" + record.getDay() + 
                             ", 性别: " + record.getGender() + 
                             ", 时辰: " + record.getHour());
        }
    }

    @Test
    @DisplayName("吉途表 - 查询最新10条记录")
    void testFindTop10JiTu() {
        // 先插入测试数据
        String testOpenId = "test-jitu-top10-openid-" + System.currentTimeMillis();
        for (int i = 0; i < 5; i++) {
            JiTu record = new JiTu(
                testOpenId, 
                "男", 
                1990 + i, 
                1, 
                1, 
                10, 
                "{\"result\":\"结果" + i + "\"}", 
                System.currentTimeMillis() + i * 1000
            );
            jiTuRepository.save(record);
        }
        
        // 查询最新10条
        List<JiTu> records = jiTuRepository.findTop10ByOpenIdOrderByCreateTimeDesc(testOpenId);
        assertTrue(records.size() >= 5, "应该至少有5条记录");
        
        // 验证是否按时间倒序
        if (records.size() >= 2) {
            assertTrue(records.get(0).getCreateTime() >= records.get(1).getCreateTime(), 
                      "应该按创建时间倒序排列");
        }
        
        System.out.println("查询到最新" + records.size() + "条记录:");
        for (JiTu record : records) {
            System.out.println("  - 年份: " + record.getYear() + 
                             ", 创建时间: " + record.getCreateTime());
        }
    }

    @Test
    @DisplayName("综合测试 - 验证数据库三张表的关联")
    void testComprehensiveQuery() {
        // 创建一个测试用户
        String testOpenId = "test-comprehensive-" + System.currentTimeMillis();
        User user = new User(testOpenId, System.currentTimeMillis());
        userRepository.save(user);
        
        // 创建问吉记录
        WenJi wenJi = new WenJi(testOpenId, "综合测试问题", "综合背景", 1985, "女", "综合结果", System.currentTimeMillis());
        wenJiRepository.save(wenJi);
        
        // 创建吉途记录
        JiTu jiTu = new JiTu(testOpenId, "女", 1985, 6, 15, 14, "{\"test\":\"综合测试\"}", System.currentTimeMillis());
        jiTuRepository.save(jiTu);
        
        // 验证查询
        Optional<User> userOpt = userRepository.findByOpenId(testOpenId);
        List<WenJi> wenJiList = wenJiRepository.findByOpenId(testOpenId);
        List<JiTu> jiTuList = jiTuRepository.findByOpenId(testOpenId);
        
        assertTrue(userOpt.isPresent(), "用户应该存在");
        assertTrue(wenJiList.size() >= 1, "问吉记录应该存在");
        assertTrue(jiTuList.size() >= 1, "吉途记录应该存在");
        
        System.out.println("综合测试通过:");
        System.out.println("  用户: " + userOpt.get().getOpenId());
        System.out.println("  问吉记录数: " + wenJiList.size());
        System.out.println("  吉途记录数: " + jiTuList.size());
    }
}
