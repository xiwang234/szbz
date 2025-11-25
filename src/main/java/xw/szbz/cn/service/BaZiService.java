package xw.szbz.cn.service;

import org.springframework.stereotype.Service;
import xw.szbz.cn.enums.DiZhi;
import xw.szbz.cn.enums.Gender;
import xw.szbz.cn.enums.TianGan;
import xw.szbz.cn.model.BaZiRequest;
import xw.szbz.cn.model.BaZiResult;
import xw.szbz.cn.model.Pillar;

import java.time.LocalDate;

/**
 * 四柱八字计算服务
 */
@Service
public class BaZiService {

    /**
     * 计算四柱八字
     */
    public BaZiResult calculate(BaZiRequest request) {
        Gender gender = Gender.fromString(request.getGender());

        int year = request.getYear();
        int month = request.getMonth();
        int day = request.getDay();
        int hour = request.getHour();

        // 处理子时跨日的情况：23点属于次日子时
        // 注意：只有日柱需要按次日计算，年柱和月柱仍按原日期
        boolean adjusted = false;
        int dayForDayPillar = day;
        int monthForDayPillar = month;
        int yearForDayPillar = year;

        if (hour == 23) {
            // 23点属于次日子时，日柱需要按次日计算
            LocalDate nextDay = LocalDate.of(year, month, day).plusDays(1);
            yearForDayPillar = nextDay.getYear();
            monthForDayPillar = nextDay.getMonthValue();
            dayForDayPillar = nextDay.getDayOfMonth();
            adjusted = true;
        }

        // 计算四柱
        // 年柱和月柱使用原始日期
        Pillar yearPillar = calculateYearPillar(year);
        Pillar monthPillar = calculateMonthPillar(year, month, day);
        // 日柱使用调整后的日期（如果是23点）
        Pillar dayPillar = calculateDayPillar(yearForDayPillar, monthForDayPillar, dayForDayPillar);
        Pillar hourPillar = calculateHourPillar(dayPillar, hour);

        // 构建返回结果
        BaZiResult result = new BaZiResult();
        result.setGender(gender.getName());
        result.setYearPillar(yearPillar);
        result.setMonthPillar(monthPillar);
        result.setDayPillar(dayPillar);
        result.setHourPillar(hourPillar);
        result.setFullBaZi(yearPillar.getFullName() + " " + monthPillar.getFullName() + " "
                + dayPillar.getFullName() + " " + hourPillar.getFullName());

        // 设置出生信息
        BaZiResult.BirthInfo birthInfo = new BaZiResult.BirthInfo();
        birthInfo.setYear(year);
        birthInfo.setMonth(month);
        birthInfo.setDay(day);
        birthInfo.setHour(hour);
        birthInfo.setShiChen(DiZhi.fromHour(hour).getName() + "时");
        birthInfo.setAdjusted(adjusted);
        // 设置日柱计算使用的日期
        birthInfo.setDayPillarDate(yearForDayPillar + "-" + monthForDayPillar + "-" + dayForDayPillar);
        result.setBirthInfo(birthInfo);

        return result;
    }

    /**
     * 计算年柱
     * 年干支计算公式：
     * 年干 = (年份 - 4) % 10
     * 年支 = (年份 - 4) % 12
     */
    public Pillar calculateYearPillar(int year) {
        int ganIndex = (year - 4) % 10;
        int zhiIndex = (year - 4) % 12;

        TianGan gan = TianGan.fromIndex(ganIndex);
        DiZhi zhi = DiZhi.fromIndex(zhiIndex);

        return new Pillar(gan.getName(), zhi.getName());
    }

    /**
     * 计算月柱
     * 注意：这里使用的是节气月，不是农历月或公历月
     * 月支固定：寅月(立春-惊蛰)、卯月(惊蛰-清明)...
     * 简化处理：根据公历日期近似推算节气月
     *
     * 节气月对应关系（大约日期）：
     * 寅月：2月4日-3月5日（立春-惊蛰）
     * 卯月：3月6日-4月4日（惊蛰-清明）
     * 辰月：4月5日-5月5日（清明-立夏）
     * 巳月：5月6日-6月5日（立夏-芒种）
     * 午月：6月6日-7月6日（芒种-小暑）
     * 未月：7月7日-8月7日（小暑-立秋）
     * 申月：8月8日-9月7日（立秋-白露）
     * 酉月：9月8日-10月7日（白露-寒露）
     * 戌月：10月8日-11月7日（寒露-立冬）
     * 亥月：11月8日-12月6日（立冬-大雪）
     * 子月：12月7日-1月5日（大雪-小寒）
     * 丑月：1月6日-2月3日（小寒-立春）
     */
    public Pillar calculateMonthPillar(int year, int month, int day) {
        // 根据公历日期推算节气月
        int jieQiMonth = getJieQiMonth(month, day);

        // 月支：寅月为index=2
        DiZhi zhi = DiZhi.fromIndex(jieQiMonth);

        // 计算年干（注意：立春前属于上一年）
        int effectiveYear = year;
        if (month == 1 || (month == 2 && day < 4)) {
            effectiveYear = year - 1;
        }

        int yearGanIndex = (effectiveYear - 4) % 10;
        TianGan yearGan = TianGan.fromIndex(yearGanIndex);

        // 月干：根据年干推算（五虎遁月）
        // 甲己之年丙作首（寅月丙寅）
        // 乙庚之年戊为头（寅月戊寅）
        // 丙辛之年寻庚上（寅月庚寅）
        // 丁壬壬位顺行流（寅月壬寅）
        // 戊癸之年甲寅始（寅月甲寅）
        int monthGanBase;
        switch (yearGan) {
            case JIA:
            case JI:
                monthGanBase = 2; // 丙（寅月丙寅）
                break;
            case YI:
            case GENG:
                monthGanBase = 4; // 戊（寅月戊寅）
                break;
            case BING:
            case XIN:
                monthGanBase = 6; // 庚（寅月庚寅）
                break;
            case DING:
            case REN:
                monthGanBase = 8; // 壬（寅月壬寅）
                break;
            case WU:
            case GUI:
            default:
                monthGanBase = 0; // 甲（寅月甲寅）
                break;
        }

        // 月干 = 基数 + (节气月 - 寅月)
        // 寅月index=2，所以偏移量为 jieQiMonth - 2
        int monthOffset = (jieQiMonth - 2 + 12) % 12;
        int monthGanIndex = (monthGanBase + monthOffset) % 10;
        TianGan gan = TianGan.fromIndex(monthGanIndex);

        return new Pillar(gan.getName(), zhi.getName());
    }

    /**
     * 根据公历月日获取节气月（返回地支索引）
     * 这是简化算法，使用固定日期近似
     */
    private int getJieQiMonth(int month, int day) {
        // 节气月对应地支索引
        // 寅=2, 卯=3, 辰=4, 巳=5, 午=6, 未=7, 申=8, 酉=9, 戌=10, 亥=11, 子=0, 丑=1
        switch (month) {
            case 1:
                return day < 6 ? 0 : 1;   // 子月或丑月
            case 2:
                return day < 4 ? 1 : 2;   // 丑月或寅月
            case 3:
                return day < 6 ? 2 : 3;   // 寅月或卯月
            case 4:
                return day < 5 ? 3 : 4;   // 卯月或辰月
            case 5:
                return day < 6 ? 4 : 5;   // 辰月或巳月
            case 6:
                return day < 6 ? 5 : 6;   // 巳月或午月
            case 7:
                return day < 7 ? 6 : 7;   // 午月或未月
            case 8:
                return day < 8 ? 7 : 8;   // 未月或申月
            case 9:
                return day < 8 ? 8 : 9;   // 申月或酉月
            case 10:
                return day < 8 ? 9 : 10;  // 酉月或戌月
            case 11:
                return day < 7 ? 10 : 11; // 戌月或亥月
            case 12:
                return day < 7 ? 11 : 0;  // 亥月或子月
            default:
                return 2; // 默认寅月
        }
    }

    /**
     * 计算日柱
     * 使用已知基准日期推算
     */
    public Pillar calculateDayPillar(int year, int month, int day) {
        // 使用已知基准日期：1985年5月14日是癸丑日
        // 癸=9, 丑=1
        LocalDate baseDate = LocalDate.of(1985, 5, 14);
        LocalDate targetDate = LocalDate.of(year, month, day);

        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(baseDate, targetDate);

        // 1985年5月14日是癸丑日
        int baseGanIndex = 9;  // 癸
        int baseZhiIndex = 1;  // 丑

        // 计算目标日期的干支
        int ganIndex = (int) (((baseGanIndex + daysBetween) % 10 + 10) % 10);
        int zhiIndex = (int) (((baseZhiIndex + daysBetween) % 12 + 12) % 12);

        TianGan gan = TianGan.fromIndex(ganIndex);
        DiZhi zhi = DiZhi.fromIndex(zhiIndex);

        return new Pillar(gan.getName(), zhi.getName());
    }

    /**
     * 计算时柱
     * 时支根据出生时辰确定
     * 时干根据日干推算（五鼠遁时）
     */
    public Pillar calculateHourPillar(Pillar dayPillar, int hour) {
        // 时支
        DiZhi zhi = DiZhi.fromHour(hour);

        // 日干
        String dayGanName = dayPillar.getTianGan();
        TianGan dayGan = null;
        for (TianGan tg : TianGan.values()) {
            if (tg.getName().equals(dayGanName)) {
                dayGan = tg;
                break;
            }
        }

        // 时干：根据日干推算（五鼠遁时）
        // 甲己还加甲（子时甲子）
        // 乙庚丙作初（子时丙子）
        // 丙辛从戊起（子时戊子）
        // 丁壬庚子居（子时庚子）
        // 戊癸何方发，壬子是真途（子时壬子）
        int hourGanBase;
        switch (dayGan) {
            case JIA:
            case JI:
                hourGanBase = 0; // 甲（子时甲子）
                break;
            case YI:
            case GENG:
                hourGanBase = 2; // 丙（子时丙子）
                break;
            case BING:
            case XIN:
                hourGanBase = 4; // 戊（子时戊子）
                break;
            case DING:
            case REN:
                hourGanBase = 6; // 庚（子时庚子）
                break;
            case WU:
            case GUI:
            default:
                hourGanBase = 8; // 壬（子时壬子）
                break;
        }

        // 时干 = 基数 + 时支序号
        int hourGanIndex = (hourGanBase + zhi.getIndex()) % 10;
        TianGan gan = TianGan.fromIndex(hourGanIndex);

        return new Pillar(gan.getName(), zhi.getName());
    }
}
