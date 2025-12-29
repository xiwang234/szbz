package xw.szbz.cn.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xw.szbz.cn.enums.DiZhi;
import xw.szbz.cn.model.Pillar;

import java.time.LocalDateTime;

/**
 * 大六壬计算服务
 */
@Service
public class LiuRenService {

    @Autowired
    private BaZiService baZiService;

    /**
     * 根据当前时间生成大六壬课传信息
     * 格式：2025年 乙巳年 丁亥月 辛卯日 丁酉时 卯将
     *
     * @return 课传信息字符串
     */
    public String generateCourseInfo() {
        LocalDateTime now = LocalDateTime.now();
        return generateCourseInfo(now);
    }

    /**
     * 根据指定时间生成大六壬课传信息
     *
     * @param dateTime 指定时间
     * @return 课传信息字符串
     */
    public String generateCourseInfo(LocalDateTime dateTime) {
        int year = dateTime.getYear();
        int month = dateTime.getMonthValue();
        int day = dateTime.getDayOfMonth();
        int hour = dateTime.getHour();

        // 计算四柱
        Pillar yearPillar = baZiService.calculateYearPillar(year);
        Pillar monthPillar = baZiService.calculateMonthPillar(year, month, day);
        Pillar dayPillar = baZiService.calculateDayPillar(year, month, day);
        Pillar hourPillar = baZiService.calculateHourPillar(dayPillar, hour);

        // 计算月将（卯将）
        String yueJiang = calculateYueJiang(month);

        // 构建课传信息字符串
        StringBuilder courseInfo = new StringBuilder();
        courseInfo.append(year).append("年 ");
        courseInfo.append(yearPillar.getFullName()).append("年 ");
        courseInfo.append(monthPillar.getFullName()).append("月 ");
        courseInfo.append(dayPillar.getFullName()).append("日 ");
        courseInfo.append(hourPillar.getFullName()).append("时 ");
        courseInfo.append(yueJiang).append("将");

        return courseInfo.toString();
    }

    /**
     * 根据公历月份计算月将
     * 月将是大六壬中的重要概念，按照农历月份确定
     * 
     * 月将对应表（按节气月）：
     * 正月（寅月）- 登明（亥将）
     * 二月（卯月）- 河魁（戌将）
     * 三月（辰月）- 从魁（酉将）
     * 四月（巳月）- 传送（申将）
     * 五月（午月）- 小吉（未将）
     * 六月（未月）- 胜光（午将）
     * 七月（申月）- 太乙（巳将）
     * 八月（酉月）- 天罡（辰将）
     * 九月（戌月）- 太冲（卯将）
     * 十月（亥月）- 功曹（寅将）
     * 十一月（子月）- 大吉（丑将）
     * 十二月（丑月）- 神后（子将）
     *
     * @param month 公历月份
     * @return 月将名称
     */
    private String calculateYueJiang(int month) {
        // 简化处理：直接使用公历月份对应节气月
        // 实际应该根据节气精确计算，这里使用近似值
        switch (month) {
            case 1:  // 丑月 -> 神后（子将）
                return "子";
            case 2:  // 寅月 -> 登明（亥将）
                return "亥";
            case 3:  // 卯月 -> 河魁（戌将）
                return "戌";
            case 4:  // 辰月 -> 从魁（酉将）
                return "酉";
            case 5:  // 巳月 -> 传送（申将）
                return "申";
            case 6:  // 午月 -> 小吉（未将）
                return "未";
            case 7:  // 未月 -> 胜光（午将）
                return "午";
            case 8:  // 申月 -> 太乙（巳将）
                return "巳";
            case 9:  // 酉月 -> 天罡（辰将）
                return "辰";
            case 10: // 戌月 -> 太冲（卯将）
                return "卯";
            case 11: // 亥月 -> 功曹（寅将）
                return "寅";
            case 12: // 子月 -> 大吉（丑将）
                return "丑";
            default:
                return "卯"; // 默认卯将
        }
    }

    /**
     * 将出生年份转换为干支年份信息
     * 格式：2025年乙巳年
     *
     * @param birthYear 出生年份
     * @return 干支年份字符串
     */
    public String convertBirthYearToGanZhi(int birthYear) {
        Pillar yearPillar = baZiService.calculateYearPillar(birthYear);
        return birthYear + "年" + yearPillar.getFullName() + "年";
    }

    /**
     * 生成出生信息（包含性别）
     * 格式：2025年乙巳年男命
     *
     * @param birthYear 出生年份
     * @param gender 性别（男/女）
     * @return 出生信息字符串
     */
    public String generateBirthInfo(int birthYear, String gender) {
        String ganZhiYear = convertBirthYearToGanZhi(birthYear);
        return ganZhiYear + gender + "命";
    }
}
