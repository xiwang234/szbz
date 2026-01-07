package xw.szbz.cn.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import xw.szbz.cn.enums.DiZhi;
import xw.szbz.cn.enums.Gender;
import xw.szbz.cn.enums.TianGan;
import xw.szbz.cn.model.BaZiRequest;
import xw.szbz.cn.model.BaZiResult;
import xw.szbz.cn.model.BasicInfo;
import xw.szbz.cn.model.DaYun;
import xw.szbz.cn.model.DaYunLiuNian;
import xw.szbz.cn.model.ExtendedInfo;
import xw.szbz.cn.model.LiuNian;
import xw.szbz.cn.model.Pillar;
import xw.szbz.cn.model.QiYunInfo;

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
        result.setBackground(request.getBackground());
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

        // 设置基本信息（性别和四柱完整名称）
        BasicInfo basicInfo = new BasicInfo(gender.getName());
        basicInfo.setYearPillar(yearPillar.getFullName());
        basicInfo.setMonthPillar(monthPillar.getFullName());
        basicInfo.setDayPillar(dayPillar.getFullName());
        basicInfo.setHourPillar(hourPillar.getFullName());
        result.setBasicInfo(basicInfo);

        // 计算起运年龄和顺逆
        boolean isYangYear = isYangTianGan(yearPillar.getTianGan());
        boolean isMale = gender == Gender.MALE;
        boolean shunPai = (isYangYear && isMale) || (!isYangYear && !isMale);

        // 精确计算起运信息
        QiYunInfo qiYunInfo = calculateQiYunInfo(year, month, day, hour, shunPai);

        // 1. 创建扩展信息（覆盖到当前年份的大运和流年）
        ExtendedInfo extendedInfo = createExtendedInfo(
                basicInfo, gender, yearPillar, monthPillar, year,
                qiYunInfo, shunPai, true
        );

        // 2. 创建完整扩展信息（固定10步大运和100年流年）
        ExtendedInfo fullExtendedInfo = createExtendedInfo(
                basicInfo, gender, yearPillar, monthPillar, year,
                qiYunInfo, shunPai, false
        );

        // 3. 设置大运和流年的字符串列表（从 extendedInfo 提取）
        // daYunStringList 包含大运和其对应的流年，每个大运是一个 DaYunLiuNian 对象
        List<DaYunLiuNian> daYunStringList = new ArrayList<>();
        if (extendedInfo.getDaYunList() != null && extendedInfo.getLiuNianList() != null) {
            List<DaYun> daYunList = extendedInfo.getDaYunList();
            List<LiuNian> liuNianList = extendedInfo.getLiuNianList();

            for (DaYun daYun : daYunList) {
                // 创建大运对象
                String daYunFullName = daYun.getPillar().getFullName();

                // 收集该大运时期的流年（startYear 到 endYear）
                List<String> liuNianForDaYun = new ArrayList<>();
                for (LiuNian liuNian : liuNianList) {
                    if (liuNian.getYear() >= daYun.getStartYear() &&
                        liuNian.getYear() <= daYun.getEndYear()) {
                        liuNianForDaYun.add(liuNian.getPillar().getFullName());
                    }
                }

                // 添加到列表
                daYunStringList.add(new DaYunLiuNian(daYunFullName, liuNianForDaYun));
            }
        }
        result.setDaYunStringList(daYunStringList);

        // 4. 设置完整的10个大运和流年列表（从 fullExtendedInfo 提取）
        List<DaYunLiuNian> daYunALLStringList = new ArrayList<>();
        if (fullExtendedInfo.getDaYunList() != null && fullExtendedInfo.getLiuNianList() != null) {
            List<DaYun> fullDaYunList = fullExtendedInfo.getDaYunList();
            List<LiuNian> fullLiuNianList = fullExtendedInfo.getLiuNianList();

            for (DaYun daYun : fullDaYunList) {
                // 创建大运对象
                String daYunFullName = daYun.getPillar().getFullName();

                // 收集该大运时期的流年（startYear 到 endYear）
                List<String> liuNianForDaYun = new ArrayList<>();
                for (LiuNian liuNian : fullLiuNianList) {
                    if (liuNian.getYear() >= daYun.getStartYear() &&
                        liuNian.getYear() <= daYun.getEndYear()) {
                        liuNianForDaYun.add(liuNian.getPillar().getFullName());
                    }
                }

                // 添加到列表
                daYunALLStringList.add(new DaYunLiuNian(daYunFullName, liuNianForDaYun));
            }
        }
        result.setDaYunALLStringList(daYunALLStringList);

        return result;
    }

    /**
     * 创建扩展信息
     * @param basicInfo 基本信息
     * @param gender 性别
     * @param yearPillar 年柱
     * @param monthPillar 月柱
     * @param birthYear 出生年份
     * @param qiYunInfo 起运信息
     * @param shunPai 是否顺排
     * @param coverCurrentYear 是否只覆盖到当前年份（true=动态计算，false=固定10步）
     * @return 扩展信息
     */
    private ExtendedInfo createExtendedInfo(
            BasicInfo basicInfo,
            Gender gender,
            Pillar yearPillar,
            Pillar monthPillar,
            int birthYear,
            QiYunInfo qiYunInfo,
            boolean shunPai,
            boolean coverCurrentYear
    ) {
        ExtendedInfo info = new ExtendedInfo();
        info.setBasicInfo(basicInfo);
        info.setQiYunInfo(qiYunInfo);
        info.setQiYunAge(qiYunInfo.getZhouSuiAge()); // 保留原有的 qiYunAge 字段，使用周岁
        info.setShunPai(shunPai);

        int daYunSteps;
        int liuNianYears;
        int qiYunAge = qiYunInfo.getZhouSuiAge(); // 使用周岁
        int qiYunYear = qiYunInfo.getQiYunYear(); // 使用精确的起运年份

        if (coverCurrentYear) {
            // 动态计算：确保覆盖到当前年份
            int currentYear = java.time.Year.now().getValue();

            if (currentYear < qiYunYear) {
                // 当前年份还未起运，至少计算1步大运
                daYunSteps = 1;
            } else {
                // 计算需要多少步大运才能覆盖到当前年份
                int yearsPassed = currentYear - qiYunYear;
                daYunSteps = (yearsPassed / 10) + 1; // 向上取整
                // 最多10步
                daYunSteps = Math.min(daYunSteps, 10);
            }

            // 流年计算到当前年份
            int currentAge = currentYear - birthYear;
            liuNianYears = currentAge + 1; // 包含当前年
        } else {
            // 固定计算：10步大运，100年流年
            daYunSteps = 10;
            liuNianYears = 100;
        }

        // 计算大运
        List<DaYun> daYunList = calculateDaYun(gender, yearPillar, monthPillar, qiYunYear, qiYunAge, daYunSteps);
        info.setDaYunList(daYunList);

        // 计算流年
        List<LiuNian> liuNianList = calculateLiuNian(birthYear, 0, liuNianYears);
        info.setLiuNianList(liuNianList);

        return info;
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
        TianGan dayGan = TianGan.fromName(dayGanName);

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

    /**
     * 计算大运
     * @param gender 性别
     * @param yearPillar 年柱
     * @param monthPillar 月柱
     * @param qiYunYear 起运年份
     * @param qiYunAge 起运年龄（周岁）
     * @param count 计算多少步大运（默认10步，即100年）
     * @return 大运列表
     */
    public List<DaYun> calculateDaYun(Gender gender, Pillar yearPillar, Pillar monthPillar, int qiYunYear, int qiYunAge, int count) {
        List<DaYun> daYunList = new ArrayList<>();

        // 1. 确定顺逆：阳男阴女顺排，阴男阳女逆排
        boolean isYangYear = isYangTianGan(yearPillar.getTianGan());
        boolean isMale = gender == Gender.MALE;
        boolean shunPai = (isYangYear && isMale) || (!isYangYear && !isMale);

        // 3. 获取月柱的干支索引
        TianGan monthGan = TianGan.fromName(monthPillar.getTianGan());
        DiZhi monthZhi = DiZhi.fromName(monthPillar.getDiZhi());

        // 4. 计算每步大运
        for (int i = 0; i < count; i++) {
            int startAge = qiYunAge + i * 10;
            int endAge = startAge + 9;
            int startYear = qiYunYear + i * 10;
            int endYear = qiYunYear + i * 10 + 9;

            // 计算大运干支
            int ganIndex, zhiIndex;
            if (shunPai) {
                // 顺排：从月柱往后推
                ganIndex = (monthGan.getIndex() + i + 1) % 10;
                zhiIndex = (monthZhi.getIndex() + i + 1) % 12;
            } else {
                // 逆排：从月柱往前推
                ganIndex = (monthGan.getIndex() - i - 1 + 10) % 10;
                zhiIndex = (monthZhi.getIndex() - i - 1 + 12) % 12;
            }

            TianGan daYunGan = TianGan.fromIndex(ganIndex);
            DiZhi daYunZhi = DiZhi.fromIndex(zhiIndex);
            Pillar daYunPillar = new Pillar(daYunGan.getName(), daYunZhi.getName());

            DaYun daYun = new DaYun(startAge, endAge, startYear, endYear, daYunPillar);
            daYunList.add(daYun);
        }

        return daYunList;
    }

    /**
     * 计算流年
     * @param birthYear 出生年份
     * @param startAge 起始年龄
     * @param count 计算多少年
     * @return 流年列表
     */
    public List<LiuNian> calculateLiuNian(int birthYear, int startAge, int count) {
        List<LiuNian> liuNianList = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            int age = startAge + i;
            int year = birthYear + age;

            // 使用年柱计算公式计算流年干支
            Pillar yearPillar = calculateYearPillar(year);

            LiuNian liuNian = new LiuNian(year, age, yearPillar);
            liuNianList.add(liuNian);
        }

        return liuNianList;
    }

    /**
     * 判断天干是否为阳
     */
    private boolean isYangTianGan(String tianGanName) {
        TianGan gan = TianGan.fromName(tianGanName);
        // 甲丙戊庚壬为阳，乙丁己辛癸为阴
        return gan.getIndex() % 2 == 0;
    }

    /**
     * 计算精确的起运信息
     * 传统规则：
     * - 阳男阴女顺排：从生日到下一个节气
     * - 阴男阳女逆排：从生日到上一个节气
     * - 3天 = 1年（虚岁）
     * - 1天 = 4个月
     * - 1个时辰（2小时）= 10天
     *
     * @param birthYear 出生年份
     * @param birthMonth 出生月份
     * @param birthDay 出生日期
     * @param birthHour 出生小时
     * @param shunPai 是否顺排
     * @return 起运信息
     */
    private QiYunInfo calculateQiYunInfo(int birthYear, int birthMonth, int birthDay, int birthHour, boolean shunPai) {
        LocalDate birthDate = LocalDate.of(birthYear, birthMonth, birthDay);

        // 获取最近的节气日期（这里使用简化算法）
        LocalDate nearestJieQi = getNearestJieQi(birthYear, birthMonth, birthDay, shunPai);

        // 计算天数差和小时差
        long daysBetween;
        int hoursDiff = 0;

        if (shunPai) {
            // 顺排：到下一个节气的天数
            daysBetween = java.time.temporal.ChronoUnit.DAYS.between(birthDate, nearestJieQi);
            // 简化处理：考虑出生时辰和节气时刻的差异
            // 如果出生在深夜（如23点），到节气还需要加上额外的时辰
            if (birthHour >= 22) {
                // 深夜（22-23点）出生，节气一般在白天，增加约3-4小时的偏移
                hoursDiff = 4; // 约4小时
            }
        } else {
            // 逆排：到上一个节气的天数
            daysBetween = java.time.temporal.ChronoUnit.DAYS.between(nearestJieQi, birthDate);
        }

        // 确保天数为正数
        daysBetween = Math.abs(daysBetween);

        // 精确计算：3天 = 1年，1天 = 4个月，1个时辰 = 10天
        int years = (int) (daysBetween / 3);
        int remainingDays = (int) (daysBetween % 3);

        int months = remainingDays * 4;
        int days = 0;

        // 如果考虑小时（简化处理）
        if (hoursDiff > 0) {
            int shiChen = hoursDiff / 2; // 转换为时辰
            days = shiChen * 10; // 1个时辰 = 10天
            // 如果天数超过30，转换为月
            if (days >= 30) {
                months += days / 30;
                days = days % 30;
            }
        }

        // 计算周岁（起运所需的年数）
        int zhouSuiAge = years;

        // 计算起运日期
        LocalDate qiYunDate = birthDate.plusYears(years).plusMonths(months).plusDays(days);
        int qiYunYear = qiYunDate.getYear();

        // 计算虚岁（起运年份 - 出生年份 + 1）
        // 这样可以正确反映中国传统虚岁的概念：出生算1岁，每过春节加1岁
        int xuSuiAge = qiYunYear - birthYear + 1;

        // 生成描述
        String description = years + "年";
        if (months > 0) {
            description += months + "个月";
        }
        if (days > 0) {
            description += "零" + days + "日";
        }

        return new QiYunInfo(years, months, days, xuSuiAge, zhouSuiAge,
                qiYunDate.toString(), qiYunYear, description);
    }

    /**
     * 获取最近的节气日期
     * 简化算法：使用固定日期近似
     *
     * @param year 年份
     * @param month 月份
     * @param day 日期
     * @param shunPai 是否顺排（true=找下一个节气，false=找上一个节气）
     * @return 节气日期
     */
    private LocalDate getNearestJieQi(int year, int month, int day, boolean shunPai) {
        // 24节气的近似日期（简化算法）
        // 格式：[月份, 节气1日期, 节气2日期]
        // 注：这里使用的是简化日期，实际节气时刻会有几个小时的偏差
        int[][] jieQiDates = {
            {1, 6, 20},   // 小寒(1/6)、大寒(1/20)
            {2, 4, 19},   // 立春(2/4)、雨水(2/19)
            {3, 6, 21},   // 惊蛰(3/6)、春分(3/21)
            {4, 5, 20},   // 清明(4/5)、谷雨(4/20)
            {5, 6, 21},   // 立夏(5/6)、小满(5/21)
            {6, 6, 21},   // 芒种(6/6)、夏至(6/21)
            {7, 7, 23},   // 小暑(7/7)、大暑(7/23)
            {8, 8, 23},   // 立秋(8/8)、处暑(8/23)
            {9, 8, 23},   // 白露(9/8)、秋分(9/23)
            {10, 8, 23},  // 寒露(10/8)、霜降(10/23)
            {11, 7, 22},  // 立冬(11/7)、小雪(11/22)
            {12, 7, 22}   // 大雪(12/7)、冬至(12/22) - 恢复为7日，通过时辰调整
        };

        LocalDate targetJieQi;

        if (shunPai) {
            // 顺排：找下一个节气
            int[] currentMonth = jieQiDates[month - 1];

            if (day < currentMonth[1]) {
                // 当月第一个节气之前，下一个节气是当月第一个
                targetJieQi = LocalDate.of(year, month, currentMonth[1]);
            } else if (day < currentMonth[2]) {
                // 当月第一个节气之后、第二个节气之前，下一个节气是当月第二个
                targetJieQi = LocalDate.of(year, month, currentMonth[2]);
            } else {
                // 当月第二个节气之后，下一个节气是下月第一个
                int nextMonth = month + 1;
                int nextYear = year;
                if (nextMonth > 12) {
                    nextMonth = 1;
                    nextYear = year + 1;
                }
                int[] nextMonthData = jieQiDates[nextMonth - 1];
                targetJieQi = LocalDate.of(nextYear, nextMonth, nextMonthData[1]);
            }
        } else {
            // 逆排：找上一个节气
            int[] currentMonth = jieQiDates[month - 1];

            if (day > currentMonth[2]) {
                // 当月第二个节气之后，上一个节气是当月第二个
                targetJieQi = LocalDate.of(year, month, currentMonth[2]);
            } else if (day > currentMonth[1]) {
                // 当月第一个节气之后、第二个节气之前，上一个节气是当月第一个
                targetJieQi = LocalDate.of(year, month, currentMonth[1]);
            } else {
                // 当月第一个节气之前，上一个节气是上月第二个
                int prevMonth = month - 1;
                int prevYear = year;
                if (prevMonth < 1) {
                    prevMonth = 12;
                    prevYear = year - 1;
                }
                int[] prevMonthData = jieQiDates[prevMonth - 1];
                targetJieQi = LocalDate.of(prevYear, prevMonth, prevMonthData[2]);
            }
        }

        return targetJieQi;
    }
}
