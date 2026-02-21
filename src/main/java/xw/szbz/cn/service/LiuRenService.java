package xw.szbz.cn.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import xw.szbz.cn.enums.DiZhi;
import xw.szbz.cn.enums.LiuQin;
import xw.szbz.cn.enums.TianGan;
import xw.szbz.cn.enums.WuXing;
import xw.szbz.cn.model.LiuRenResult;
import xw.szbz.cn.model.LiuRenResult.BasicInfo;
import xw.szbz.cn.model.LiuRenResult.Chuan;
import xw.szbz.cn.model.LiuRenResult.Ke;
import xw.szbz.cn.model.LiuRenResult.PanPosition;
import xw.szbz.cn.model.LiuRenResult.SanChuan;
import xw.szbz.cn.model.LiuRenResult.SiKe;
import xw.szbz.cn.model.LiuRenResult.TianDiPan;
import xw.szbz.cn.model.Pillar;

/**
 * 大六壬计算服务
 */
@Service
public class LiuRenService {

    @Autowired
    private BaZiService baZiService;

    private final ObjectMapper objectMapper = new ObjectMapper();

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
     * @return 课传信息JSON字符串
     */
    public String generateCourseInfo(LocalDateTime dateTime) {
        try {
            int year = dateTime.getYear();
            int month = dateTime.getMonthValue();
            int day = dateTime.getDayOfMonth();
            int hour = dateTime.getHour();
            int minute = dateTime.getMinute();

            // 计算四柱
            Pillar monthPillar = baZiService.calculateMonthPillar(year, month, day);
            Pillar dayPillar = baZiService.calculateDayPillar(year, month, day);
            Pillar hourPillar = baZiService.calculateHourPillar(dayPillar, hour);

            // 判断是否过了立春，如果没过立春，年份应该使用上一年
            int lunarYear = year;
            if (!hasPassedLiChun(year, month, day)) {
                lunarYear = year - 1;
            }

            // 使用农历年份计算年柱
            Pillar yearPillar = baZiService.calculateYearPillar(lunarYear);

            // 根据月柱的地支计算月将
            // 月将的确立基于中气，而不是月建
            String yueJiang = calculateYueJiangByZhongQi(year, month, day);

            // 大六壬的占时：使用更精确的时刻判断（时辰中点前后）
            // 如17点15分前算申时，17点15分后算酉时
            String hourZhi = getLiuRenHourZhi(hour, minute);

            // 创建大六壬结果对象
            LiuRenResult result = new LiuRenResult();

            // 1. 基本信息
            BasicInfo basicInfo = new BasicInfo(
                lunarYear,
                yearPillar.getFullName(),
                monthPillar.getFullName(),
                dayPillar.getFullName(),
                hourPillar.getFullName(),
                yueJiang
            );
            result.setBasicInfo(basicInfo);

            // 2. 天地盘（需要传入日柱和小时来判断昼夜和贵人）
            TianDiPan tianDiPan = calculateTianDiPan(yueJiang, hourZhi, dayPillar, hour);
            result.setTianDiPan(tianDiPan);

            // 3. 四课
            SiKe siKe = calculateSiKe(dayPillar, tianDiPan);
            result.setSiKe(siKe);

            // 4. 三传（需要传入月将和占时来判断伏吟反吟）
            SanChuan sanChuan = calculateSanChuan(yearPillar, monthPillar, dayPillar, hourPillar,
                                                   siKe, tianDiPan, yueJiang, hourZhi);
            result.setSanChuan(sanChuan);

            // 转换为JSON字符串
            return objectMapper.writeValueAsString(result);
        } catch (Exception e) {
            throw new RuntimeException("生成大六壬信息失败: " + e.getMessage(), e);
        }
    }

    /**
     * 计算天地盘
     * 天地盘由天盘、地盘和天将三部分组成
     *
     * 构建方法：
     * 1. 地盘固定为子丑寅卯辰巳午未申酉戌亥
     * 2. 天盘：将月将加临到占时的地盘位置上，顺时针转一圈
     *    例如：月将卯加临地盘巳，则地盘巳对应天盘卯，地盘午对应天盘辰，依此类推
     * 3. 天将：根据日干和昼夜确定贵人地支，再根据贵人地支判断顺逆，从贵人地支开始排列天将
     *    例如：贵人为酉，酉在巳午未申酉戌范围内，逆排
     *    则：酉-贵人、申-螣蛇、未-朱雀、午-六合、巳-勾陈、辰-青龙、卯-天空、寅-白虎、丑-太常、子-玄武、亥-太阴、戌-天后
     *
     * @param yueJiang 月将
     * @param hourZhi 占时地支
     * @param dayPillar 日柱（需要日干判断贵人）
     * @param hour 小时（判断昼夜）
     * @return 天地盘
     */
    private TianDiPan calculateTianDiPan(String yueJiang, String hourZhi, Pillar dayPillar, int hour) {
        TianDiPan tianDiPan = new TianDiPan();

        // 地盘固定顺序
        String[] diPanArray = {"子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥"};

        // 1. 计算天盘：月将加临占时
        int yueJiangIndex = DiZhi.fromName(yueJiang).getIndex();
        int hourZhiIndex = DiZhi.fromName(hourZhi).getIndex();

        // 2. 根据日干和昼夜确定贵人地支
        String dayGan = dayPillar.getTianGan();
        // 昼夜判断基于占时地支：卯辰巳午未申用昼贵，酉戌亥子丑寅用夜贵
        // 特殊：申时17点后用夜贵
        boolean isDaytime = isDaytimeByShiChen(hourZhi, hour);
        String guiRenZhi = getGuiRenZhi(dayGan, isDaytime);
        int guiRenZhiIndex = DiZhi.fromName(guiRenZhi).getIndex();

        // 3. 找到贵人地支作为天盘时，对应的地盘位置
        // 使用公式反推：天盘 = (月将 + (地盘 - 占时) + 12) % 12
        // 当天盘 = 贵人地支时，求地盘
        // (月将 + (地盘 - 占时) + 12) % 12 = 贵人
        // 地盘 = (贵人 - 月将 + 占时 + 12) % 12
        int guiRenDiPanIndex = (guiRenZhiIndex - yueJiangIndex + hourZhiIndex + 12) % 12;

        // 4. 根据贵人加临的地盘位置判断顺逆
        // 贵人加临于地盘亥子丑寅卯辰(11,0,1,2,3,4)：顺排（递增）
        // 贵人加临于地盘巳午未申酉戌(5,6,7,8,9,10)：逆排（递减）
        boolean isShunPai = (guiRenDiPanIndex == 11 || guiRenDiPanIndex <= 4);

        // 5. 天将固定顺序
        String[] tianJiangOrder = {
            "天乙贵人", "螣蛇", "朱雀", "六合", "勾陈", "青龙",
            "天空", "白虎", "太常", "玄武", "太阴", "天后"
        };

        // 6. 从贵人地支开始，按顺逆方向排列天将
        // 创建一个映射：天盘地支 -> 天将
        String[] zhiToTianJiang = new String[12];
        for (int i = 0; i < 12; i++) {
            int zhiIndex;
            if (isShunPai) {
                // 顺排：从贵人地支开始递增（顺时针）
                zhiIndex = (guiRenZhiIndex + i) % 12;
            } else {
                // 逆排：从贵人地支开始递减（逆时针）
                zhiIndex = (guiRenZhiIndex - i + 12) % 12;
            }
            zhiToTianJiang[zhiIndex] = tianJiangOrder[i];
        }

        // 7. 生成12个盘位（地盘、天盘、天将）
        for (int i = 0; i < 12; i++) {
            String diPan = diPanArray[i];

            // 计算该地盘位置对应的天盘地支
            // 标准公式：天盘 = (月将 + (地盘 - 占时) + 12) % 12
            int tianPanIndex = (yueJiangIndex + (i - hourZhiIndex) + 12) % 12;
            String tianPan = diPanArray[tianPanIndex];

            // 根据天盘地支获取对应的天将
            String tianJiang = zhiToTianJiang[tianPanIndex];

            PanPosition position = new PanPosition(diPan, tianPan, tianJiang);
            tianDiPan.setPosition(i, position);
        }

        return tianDiPan;
    }

    /**
     * 获取大六壬的占时地支
     * 大六壬使用传统时辰划分，每个时辰2小时
     *
     * 时辰对应关系：
     * 子时：23:00-00:59
     * 丑时：01:00-02:59
     * 寅时：03:00-04:59
     * 卯时：05:00-06:59
     * 辰时：07:00-08:59
     * 巳时：09:00-10:59
     * 午时：11:00-12:59
     * 未时：13:00-14:59
     * 申时：15:00-16:59
     * 酉时：17:00-18:59
     * 戌时：19:00-20:59
     * 亥时：21:00-22:59
     *
     * @param hour 小时（0-23）
     * @param minute 分钟（0-59）
     * @return 占时地支
     */
    private String getLiuRenHourZhi(int hour, int minute) {
        // 子时特殊处理：23:00-23:59算次日子时，0:00-0:59算当日子时
        if (hour == 23 || hour == 0) {
            return "子";
        }

        // 标准时辰划分：每2小时一个时辰
        // 1-2点丑，3-4点寅，5-6点卯，7-8点辰，9-10点巳，11-12点午
        // 13-14点未，15-16点申，17-18点酉，19-20点戌，21-22点亥
        int index = (hour + 1) / 2;
        String[] zhiArray = {"子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥"};
        return zhiArray[index % 12];
    }

    /**
     * 根据占时地支和具体时刻判断昼夜
     * 卯辰巳午未申用昼贵，酉戌亥子丑寅用夜贵
     * 特殊规则：申时如果是17点及以后（接近酉时），用夜贵
     *
     * @param shiChen 占时地支
     * @param hour 小时数
     * @return 是否为白天
     */
    private boolean isDaytimeByShiChen(String shiChen, int hour) {
        int index = DiZhi.fromName(shiChen).getIndex();

        // 申时(8)特殊处理：17点及以后用夜贵
        if (index == 8 && hour >= 17) {
            return false;
        }

        // 卯(3)辰(4)巳(5)午(6)未(7)申(8)用昼贵
        return index >= 3 && index <= 8;
    }

    /**
     * 根据日干和昼夜获取贵人地支
     * 昼夜贵人口诀：
     * 甲戊庚牛羊（昼贵丑，夜贵未）
     * 乙己鼠猴乡（昼贵子，夜贵申）
     * 丙丁猪鸡位（昼贵亥，夜贵酉）
     * 壬癸蛇兔藏（昼贵巳，夜贵卯）
     * 六辛逢马虎（昼贵午，夜贵寅）
     *
     * @param dayGan 日干
     * @param isDaytime 是否白天
     * @return 贵人地支
     */
    private String getGuiRenZhi(String dayGan, boolean isDaytime) {
        switch (dayGan) {
            case "甲":
            case "戊":
            case "庚":
                return isDaytime ? "丑" : "未";
            case "乙":
            case "己":
                return isDaytime ? "子" : "申";
            case "丙":
            case "丁":
                return isDaytime ? "亥" : "酉";
            case "壬":
            case "癸":
                return isDaytime ? "巳" : "卯";
            case "辛":
                return isDaytime ? "午" : "寅";
            default:
                return "丑"; // 默认值
        }
    }

    /**
     * 计算四课
     * 四课是以日辰干支为基础，根据天地盘局的位置关系推演出的课体形式
     *
     * 排法：
     * 1. 将日干化为地支（寄宫）
     * 2. 第一课：日干寄宫地盘位置的天盘地支 加 日干
     * 3. 第二课：第一课天盘地支的地盘位置的天盘地支 加 第一课天盘地支
     * 4. 第三课：日支地盘位置的天盘地支 加 日支
     * 5. 第四课：第三课天盘地支的地盘位置的天盘地支 加 第三课天盘地支
     *
     * 例如：丁巳日卯将未时
     * - 日干丁寄未，未的天盘是卯，第一课：卯加丁
     * - 卯的天盘是亥，第二课：亥加卯
     * - 日支巳的天盘是丑，第三课：丑加巳
     * - 丑的天盘是酉，第四课：酉加丑
     *
     * @param dayPillar 日柱
     * @param tianDiPan 天地盘
     * @return 四课
     */
    private SiKe calculateSiKe(Pillar dayPillar, TianDiPan tianDiPan) {
        SiKe siKe = new SiKe();

        String dayGan = dayPillar.getTianGan();
        String dayZhi = dayPillar.getDiZhi();

        // 1. 天干寄宫
        String ganJiGong = getGanZhiMapping(dayGan);

        // 2. 第一课：日干寄宫的天盘地支
        String ke1TianPan = getTianPanByDiPan(ganJiGong, tianDiPan);
        String ke1TianJiang = getTianJiangByTianPan(ke1TianPan, tianDiPan);
        Ke diYiKe = new Ke(
            ke1TianPan + dayGan,  // 只保留干支部分
            ke1TianJiang,
            "第一课"
        );
        siKe.setDiYiKe(diYiKe);

        // 3. 第二课：第一课天盘的天盘地支
        String ke2TianPan = getTianPanByDiPan(ke1TianPan, tianDiPan);
        String ke2TianJiang = getTianJiangByTianPan(ke2TianPan, tianDiPan);
        Ke diErKe = new Ke(
            ke2TianPan + ke1TianPan,  // 只保留干支部分
            ke2TianJiang,
            "第二课"
        );
        siKe.setDiErKe(diErKe);

        // 4. 第三课：日支的天盘地支
        String ke3TianPan = getTianPanByDiPan(dayZhi, tianDiPan);
        String ke3TianJiang = getTianJiangByTianPan(ke3TianPan, tianDiPan);
        Ke diSanKe = new Ke(
            ke3TianPan + dayZhi,  // 只保留干支部分
            ke3TianJiang,
            "第三课"
        );
        siKe.setDiSanKe(diSanKe);

        // 5. 第四课：第三课天盘的天盘地支
        String ke4TianPan = getTianPanByDiPan(ke3TianPan, tianDiPan);
        String ke4TianJiang = getTianJiangByTianPan(ke4TianPan, tianDiPan);
        Ke diSiKe = new Ke(
            ke4TianPan + ke3TianPan,  // 只保留干支部分
            ke4TianJiang,
            "第四课"
        );
        siKe.setDiSiKe(diSiKe);

        return siKe;
    }

    /**
     * 计算三传
     * 三传取法有九种（九宗门）：贼克、比用、涉害、遥克、昴星、别责、八专、伏吟、反吟
     *
     * 判断顺序：
     * 1. 伏吟（月将占时相同）
     * 2. 反吟（天地六冲）
     * 3. 八专（甲寅、庚申、丁未、己未四日）
     * 4. 贼克法（只有一组克）
     * 5. 多克时下克上优先（多组克中如果只有一个下克上，按贼克法取下克上）
     * 6. 比用法（多组克，取阴阳）
     * 7. 涉害法（多组克，阴阳相同）
     * 8. 遥克法（四课无克，日干与四课相克）
     * 9. 别责法（四课不备，有两课相同）
     * 10. 昴星法（最后的方法）
     *
     * @param dayPillar 日柱
     * @param siKe 四课
     * @param tianDiPan 天地盘
     * @param yueJiang 月将
     * @param hourZhi 占时
     * @return 三传
     */
    private SanChuan calculateSanChuan(Pillar yearPillar, Pillar monthPillar, Pillar dayPillar, Pillar hourPillar,
                                       SiKe siKe, TianDiPan tianDiPan,
                                       String yueJiang, String hourZhi) {
        String dayGan = dayPillar.getTianGan();
        String dayZhi = dayPillar.getDiZhi();
        boolean isYangDay = isYangGan(dayGan);

        // 提取四课的完整干支（用于克关系分析）
        String ke1 = siKe.getDiYiKe().getGanZhi();
        String ke2 = siKe.getDiErKe().getGanZhi();
        String ke3 = siKe.getDiSanKe().getGanZhi();
        String ke4 = siKe.getDiSiKe().getGanZhi();

        // 分析四课的克关系
        KeInfo keInfo = analyzeKeRelations(ke1, ke2, ke3, ke4, dayGan, dayZhi);

        // 初传、中传、末传的地支
        String chuChuanZhi;
        String zhongChuanZhi;
        String moChuanZhi;

        // 1. 判断伏吟
        if (yueJiang.equals(hourZhi)) {
            String[] chuan = calculateFuYin(dayGan, dayZhi, isYangDay, ke1, ke4);
            chuChuanZhi = chuan[0];
            zhongChuanZhi = chuan[1];
            moChuanZhi = chuan[2];
        }
        // 2. 判断反吟（天地六冲）
        else if (isReverse(yueJiang, hourZhi)) {
            String[] chuan = calculateFanYin(dayGan, dayZhi, keInfo, ke1, ke4);
            chuChuanZhi = chuan[0];
            zhongChuanZhi = chuan[1];
            moChuanZhi = chuan[2];
        }
        // 3. 判断八专
        else if (isBaZhuan(dayPillar.getFullName())) {
            String[] chuan = calculateBaZhuan(ke1, isYangDay);
            chuChuanZhi = chuan[0];
            zhongChuanZhi = chuan[1];
            moChuanZhi = chuan[2];
        }
        // 4. 贼克法（只有一组克）
        else if (keInfo.keCount == 1) {
            String[] chuan = calculateZeiKe(keInfo, ke1, ke2, ke3, ke4, tianDiPan);
            chuChuanZhi = chuan[0];
            zhongChuanZhi = chuan[1];
            moChuanZhi = chuan[2];
        }
        // 5. 多组克时，下克上优先级最高
        else if (keInfo.keCount > 1) {
            // 统计下克上的数量
            List<KeRelation> xiaKeShangRelations = new ArrayList<>();
            for (KeRelation rel : keInfo.keRelations) {
                if (!rel.isShangKeXia) {  // 下克上
                    xiaKeShangRelations.add(rel);
                }
            }

            // 如果有下克上，按贼克法处理（下克上优先级最高）
            // 有多个下克上时，取课序最后的那个
            if (!xiaKeShangRelations.isEmpty()) {
                // 按课序排序，取最后一个
                xiaKeShangRelations.sort((a, b) -> Integer.compare(b.keIndex, a.keIndex));
                KeRelation selectedRelation = xiaKeShangRelations.get(0);

                // 创建只包含选中下克上关系的KeInfo
                KeInfo xiaKeShangInfo = new KeInfo();
                xiaKeShangInfo.keCount = 1;
                xiaKeShangInfo.keRelations.add(selectedRelation);
                xiaKeShangInfo.xiaKeShangZhi = selectedRelation.keZhi;

                String[] chuan = calculateZeiKe(xiaKeShangInfo, ke1, ke2, ke3, ke4, tianDiPan);
                chuChuanZhi = chuan[0];
                zhongChuanZhi = chuan[1];
                moChuanZhi = chuan[2];
            }
            // 比用法（多组克，取阴阳）
            else if (keInfo.hasYinYangDiff) {
                String[] chuan = calculateBiYong(keInfo, isYangDay, tianDiPan);
                chuChuanZhi = chuan[0];
                zhongChuanZhi = chuan[1];
                moChuanZhi = chuan[2];
            }
            // 涉害法（多组克，阴阳相同）
            else {
                String[] chuan = calculateSheHai(keInfo, tianDiPan);
                chuChuanZhi = chuan[0];
                zhongChuanZhi = chuan[1];
                moChuanZhi = chuan[2];
            }
        }
        // 6. 遥克法
        else if (keInfo.hasYaoKe) {
            String[] chuan = calculateYaoKe(keInfo, isYangDay, tianDiPan);
            chuChuanZhi = chuan[0];
            zhongChuanZhi = chuan[1];
            moChuanZhi = chuan[2];
        }
        // 7. 别责法（四课不备）
        else if (isSiKeBuBei(ke1, ke2, ke3, ke4)) {
            String[] chuan = calculateBieZe(dayGan, dayZhi, isYangDay, ke1, tianDiPan);
            chuChuanZhi = chuan[0];
            zhongChuanZhi = chuan[1];
            moChuanZhi = chuan[2];
        }
        // 8. 昴星法（最后的方法）
        else {
            String[] chuan = calculateMaoXing(isYangDay, ke1, ke4, tianDiPan);
            chuChuanZhi = chuan[0];
            zhongChuanZhi = chuan[1];
            moChuanZhi = chuan[2];
        }

        // 构建三传结果
        SanChuan sanChuan = new SanChuan();

        // 为三传配上天干、六亲、天将、神煞
        WuXing dayGanWuXing = WuXing.fromTianGan(dayGan);

        // 初传
        String chuChuanGan = getGanForZhi(chuChuanZhi, dayPillar);
        String chuChuanGanZhi = chuChuanGan.isEmpty() ? chuChuanZhi : chuChuanGan + chuChuanZhi;
        String chuChuanTianJiang = getTianJiangByTianPan(chuChuanZhi, tianDiPan);
        WuXing chuChuanWuXing = WuXing.fromDiZhi(chuChuanZhi);
        LiuQin chuChuanLiuQin = dayGanWuXing.getLiuQin(chuChuanWuXing);
        String chuChuanShenSha = getShenSha(chuChuanZhi, yearPillar, monthPillar, dayPillar, hourPillar);

        Chuan chuChuan = new Chuan(
            chuChuanLiuQin.getName(),
            chuChuanGanZhi,
            chuChuanTianJiang,
            chuChuanShenSha
        );
        sanChuan.setChuChuan(chuChuan);

        // 中传
        String zhongChuanGan = getGanForZhi(zhongChuanZhi, dayPillar);
        String zhongChuanGanZhi = zhongChuanGan.isEmpty() ? zhongChuanZhi : zhongChuanGan + zhongChuanZhi;
        String zhongChuanTianJiang = getTianJiangByTianPan(zhongChuanZhi, tianDiPan);
        WuXing zhongChuanWuXing = WuXing.fromDiZhi(zhongChuanZhi);
        LiuQin zhongChuanLiuQin = dayGanWuXing.getLiuQin(zhongChuanWuXing);
        String zhongChuanShenSha = getShenSha(zhongChuanZhi, yearPillar, monthPillar, dayPillar, hourPillar);

        Chuan zhongChuan = new Chuan(
            zhongChuanLiuQin.getName(),
            zhongChuanGanZhi,
            zhongChuanTianJiang,
            zhongChuanShenSha
        );
        sanChuan.setZhongChuan(zhongChuan);

        // 末传
        String moChuanGan = getGanForZhi(moChuanZhi, dayPillar);
        String moChuanGanZhi = moChuanGan.isEmpty() ? moChuanZhi : moChuanGan + moChuanZhi;
        String moChuanTianJiang = getTianJiangByTianPan(moChuanZhi, tianDiPan);
        WuXing moChuanWuXing = WuXing.fromDiZhi(moChuanZhi);
        LiuQin moChuanLiuQin = dayGanWuXing.getLiuQin(moChuanWuXing);
        String moChuanShenSha = getShenSha(moChuanZhi, yearPillar, monthPillar, dayPillar, hourPillar);

        Chuan moChuan = new Chuan(
            moChuanLiuQin.getName(),
            moChuanGanZhi,
            moChuanTianJiang,
            moChuanShenSha
        );
        sanChuan.setMoChuan(moChuan);

        return sanChuan;
    }

    /**
     * 判断是否阳干
     */
    private boolean isYangGan(String gan) {
        return "甲".equals(gan) || "丙".equals(gan) || "戊".equals(gan) ||
               "庚".equals(gan) || "壬".equals(gan);
    }

    /**
     * 判断是否阳支
     */
    private boolean isYangZhi(String zhi) {
        return "子".equals(zhi) || "寅".equals(zhi) || "辰".equals(zhi) ||
               "午".equals(zhi) || "申".equals(zhi) || "戌".equals(zhi);
    }

    /**
     * 克关系信息
     */
    private static class KeInfo {
        int keCount = 0;              // 克的数量
        boolean hasYinYangDiff = false; // 是否有阴阳差异
        boolean hasYaoKe = false;      // 是否有遥克
        List<KeRelation> keRelations = new ArrayList<>(); // 所有克关系
        String shangKeXiaZhi;          // 上克下的地支（如果只有一个上克下）
        String xiaKeShangZhi;          // 下克上的地支（如果只有一个下克上）
    }

    /**
     * 单个克关系
     */
    private static class KeRelation {
        String keZhi;           // 克的地支（初传候选）
        boolean isShangKeXia;   // true=上克下（元首），false=下克上（重审）
        String tianPanZhi;      // 天盘地支
        String diPanZhi;        // 地盘地支
        int keIndex;            // 课的序号（1-4）

        KeRelation(String keZhi, boolean isShangKeXia, String tianPanZhi, String diPanZhi, int keIndex) {
            this.keZhi = keZhi;
            this.isShangKeXia = isShangKeXia;
            this.tianPanZhi = tianPanZhi;
            this.diPanZhi = diPanZhi;
            this.keIndex = keIndex;
        }
    }

    /**
     * 分析四课的克关系
     * 四课格式：午丙呈六合（天盘地支+地盘地支或天干+呈+天将）
     */
    private KeInfo analyzeKeRelations(String ke1, String ke2, String ke3, String ke4, String dayGan, String dayZhi) {
        KeInfo info = new KeInfo();

        // 提取四课的天盘和地盘
        // 第一课和第三课是天盘地支+干（日干或日支对应的天干），需要转换为五行
        // 第二课和第四课是天盘地支+地支，可以直接比较

        // 第一课：午丙 -> 午 vs 丙寄宫（巳）
        String ke1TianPan = extractTianPanFromKe(ke1);
        String ke1DiZhi = getGanZhiMapping(dayGan); // 日干的寄宫
        checkKeRelation(ke1TianPan, ke1DiZhi, 1, info);

        // 第二课：未午 -> 未 vs 午
        String[] ke2Parts = extractTianPanDiPan(ke2);
        checkKeRelation(ke2Parts[0], ke2Parts[1], 2, info);

        // 第三课：卯寅 -> 卯 vs 寅
        checkKeRelation(extractTianPanFromKe(ke3), dayZhi, 3, info);

        // 第四课：辰卯 -> 辰 vs 卯（第三课的天盘）
        String[] ke4Parts = extractTianPanDiPan(ke4);
        checkKeRelation(ke4Parts[0], ke4Parts[1], 4, info);

        // 统计克的数量
        info.keCount = info.keRelations.size();

        // 判断是否有阴阳差异
        if (info.keCount > 1) {
            boolean hasYang = false;
            boolean hasYin = false;
            for (KeRelation rel : info.keRelations) {
                if (isYangZhi(rel.keZhi)) {
                    hasYang = true;
                } else {
                    hasYin = true;
                }
            }
            info.hasYinYangDiff = hasYang && hasYin;
        }

        // 如果只有一个克，记录是上克下还是下克上
        if (info.keCount == 1) {
            KeRelation rel = info.keRelations.get(0);
            if (rel.isShangKeXia) {
                info.shangKeXiaZhi = rel.keZhi;
            } else {
                info.xiaKeShangZhi = rel.keZhi;
            }
        }

        return info;
    }

    /**
     * 提取课的天盘地支（第一个字）
     */
    private String extractTianPanFromKe(String ke) {
        // 新格式不再包含"呈XX"，直接提取第一个字符
        if (ke.length() >= 1) {
            return ke.substring(0, 1);
        }
        return "子";
    }

    /**
     * 提取天盘和地盘地支
     * 格式：午丙 -> [午, 丙]
     * 格式：未午 -> [未, 午]
     */
    private String[] extractTianPanDiPan(String ke) {
        // 新格式不再包含"呈XX"，直接提取前两个字符
        if (ke.length() >= 2) {
            String tianPan = ke.substring(0, 1);
            String diPan = ke.substring(1, 2);
            return new String[]{tianPan, diPan};
        }
        return new String[]{"子", "子"};
    }

    /**
     * 检查单个课的克关系
     */
    private void checkKeRelation(String tianPanZhi, String diPanZhi, int keIndex, KeInfo info) {
        WuXing tianPanWuXing = WuXing.fromDiZhi(tianPanZhi);
        WuXing diPanWuXing = WuXing.fromDiZhi(diPanZhi);

        // 判断是否相克
        boolean tianKeRen = isKe(tianPanWuXing, diPanWuXing); // 天盘克地盘（上克下）
        boolean diKeRen = isKe(diPanWuXing, tianPanWuXing);   // 地盘克天盘（下克上）

        if (tianKeRen) {
            // 上克下（元首）：取天盘地支作为初传候选
            info.keRelations.add(new KeRelation(tianPanZhi, true, tianPanZhi, diPanZhi, keIndex));
        } else if (diKeRen) {
            // 下克上（重审）：取天盘地支作为初传候选
            info.keRelations.add(new KeRelation(tianPanZhi, false, tianPanZhi, diPanZhi, keIndex));
        }
    }

    /**
     * 判断五行是否相克（source克target）
     */
    private boolean isKe(WuXing source, WuXing target) {
        return (source == WuXing.MU && target == WuXing.TU) ||
               (source == WuXing.TU && target == WuXing.SHUI) ||
               (source == WuXing.SHUI && target == WuXing.HUO) ||
               (source == WuXing.HUO && target == WuXing.JIN) ||
               (source == WuXing.JIN && target == WuXing.MU);
    }

    /**
     * 贼克法：只有一组克
     * - 只有一组下克上：重审课，取受克的上神（天盘地支）为初传
     * - 只有一组上克下：元首课，取克下的上神（天盘地支）为初传
     * - 中传：取初传的本位上神（阴神）
     * - 末传：取中传的本位上神
     */
    private String[] calculateZeiKe(KeInfo keInfo, String ke1, String ke2, String ke3, String ke4, TianDiPan tianDiPan) {
        // 取唯一的克关系
        KeRelation keRelation = keInfo.keRelations.get(0);

        // 初传就是克的地支（天盘地支）
        String chuChuan = keRelation.keZhi;

        // 中传：取初传的本位上神（即以初传为地盘，找其天盘）
        String zhongChuan = getTianPanByDiPan(chuChuan, tianDiPan);

        // 末传：取中传的本位上神
        String moChuan = getTianPanByDiPan(zhongChuan, tianDiPan);

        return new String[]{chuChuan, zhongChuan, moChuan};
    }

    /**
     * 比用法：多组克，且有阴阳差异
     * - 阳日取阳神，阴日取阴神
     * - 多个符合条件时，取课序靠前者
     * - 中传：取初传的本位上神
     * - 末传：取中传的本位上神
     */
    private String[] calculateBiYong(KeInfo keInfo, boolean isYangDay, TianDiPan tianDiPan) {
        // 根据阴阳选择初传
        String chuChuan = null;

        for (KeRelation rel : keInfo.keRelations) {
            boolean isYangZhi = isYangZhi(rel.keZhi);
            if ((isYangDay && isYangZhi) || (!isYangDay && !isYangZhi)) {
                // 取第一个符合阴阳条件的
                chuChuan = rel.keZhi;
                break;
            }
        }

        // 如果没有找到符合阴阳的，取第一个（兜底）
        if (chuChuan == null) {
            chuChuan = keInfo.keRelations.get(0).keZhi;
        }

        // 中传：取初传的本位上神
        String zhongChuan = getTianPanByDiPan(chuChuan, tianDiPan);

        // 末传：取中传的本位上神
        String moChuan = getTianPanByDiPan(zhongChuan, tianDiPan);

        return new String[]{chuChuan, zhongChuan, moChuan};
    }

    /**
     * 涉害法：多组克，且阴阳相同
     * - 先取四孟(寅申巳亥)，无则取四仲(子午卯酉)，无则取四季(辰戌丑未)
     * - 多个同类时取课序靠前者
     * - 中传：取初传的本位上神
     * - 末传：取中传的本位上神
     */
    private String[] calculateSheHai(KeInfo keInfo, TianDiPan tianDiPan) {
        String chuChuan = null;

        // 四孟：寅(2) 申(8) 巳(5) 亥(11)
        for (KeRelation rel : keInfo.keRelations) {
            int zhiIndex = DiZhi.fromName(rel.keZhi).getIndex();
            if (zhiIndex == 2 || zhiIndex == 8 || zhiIndex == 5 || zhiIndex == 11) {
                chuChuan = rel.keZhi;
                break;
            }
        }

        // 四仲：子(0) 午(6) 卯(3) 酉(9)
        if (chuChuan == null) {
            for (KeRelation rel : keInfo.keRelations) {
                int zhiIndex = DiZhi.fromName(rel.keZhi).getIndex();
                if (zhiIndex == 0 || zhiIndex == 6 || zhiIndex == 3 || zhiIndex == 9) {
                    chuChuan = rel.keZhi;
                    break;
                }
            }
        }

        // 四季：辰(4) 戌(10) 丑(1) 未(7)
        if (chuChuan == null) {
            for (KeRelation rel : keInfo.keRelations) {
                int zhiIndex = DiZhi.fromName(rel.keZhi).getIndex();
                if (zhiIndex == 4 || zhiIndex == 10 || zhiIndex == 1 || zhiIndex == 7) {
                    chuChuan = rel.keZhi;
                    break;
                }
            }
        }

        // 兜底：取第一个
        if (chuChuan == null) {
            chuChuan = keInfo.keRelations.get(0).keZhi;
        }

        // 中传：取初传的本位上神
        String zhongChuan = getTianPanByDiPan(chuChuan, tianDiPan);

        // 末传：取中传的本位上神
        String moChuan = getTianPanByDiPan(zhongChuan, tianDiPan);

        return new String[]{chuChuan, zhongChuan, moChuan};
    }
    private String[] calculateYaoKe(KeInfo keInfo, boolean isYangDay, TianDiPan tianDiPan) { return new String[]{"子", "丑", "寅"}; }
    private String[] calculateBieZe(String dayGan, String dayZhi, boolean isYangDay, String ke1, TianDiPan tianDiPan) { return new String[]{"子", "丑", "寅"}; }
    private String[] calculateMaoXing(boolean isYangDay, String ke1, String ke4, TianDiPan tianDiPan) { return new String[]{"子", "丑", "寅"}; }
    private String[] calculateFuYin(String dayGan, String dayZhi, boolean isYangDay, String ke1, String ke4) { return new String[]{"子", "丑", "寅"}; }
    private String[] calculateFanYin(String dayGan, String dayZhi, KeInfo keInfo, String ke1, String ke4) { return new String[]{"子", "丑", "寅"}; }
    private String[] calculateBaZhuan(String ke1, boolean isYangDay) { return new String[]{"子", "丑", "寅"}; }
    private boolean isReverse(String yueJiang, String hourZhi) { return false; }
    private boolean isBaZhuan(String dayPillar) { return false; }
    private boolean isSiKeBuBei(String ke1, String ke2, String ke3, String ke4) { return false; }

    /**
     * 为地支配上天干（根据日柱所在旬配置）
     * 如果地支在空亡，返回空字符串
     */
    private String getGanForZhi(String zhi, Pillar dayPillar) {
        // 1. 判断日柱所在的旬
        String dayGan = dayPillar.getTianGan();
        String dayZhi = dayPillar.getDiZhi();

        // 2. 计算旬首（找到该旬的甲X日）
        int dayGanIndex = TianGan.fromName(dayGan).getIndex();
        int dayZhiIndex = DiZhi.fromName(dayZhi).getIndex();

        // 旬首的地支 = (日支 - 日干) % 12
        int xunShouZhiIndex = (dayZhiIndex - dayGanIndex + 12) % 12;

        // 3. 判断空亡（每旬空两个地支）
        // 空亡的地支是旬首后的第10和第11个地支
        int kongWang1 = (xunShouZhiIndex + 10) % 12;
        int kongWang2 = (xunShouZhiIndex + 11) % 12;

        int zhiIndex = DiZhi.fromName(zhi).getIndex();
        if (zhiIndex == kongWang1 || zhiIndex == kongWang2) {
            // 空亡，不配天干
            return "";
        }

        // 4. 根据旬首配置天干
        // 从旬首开始：甲X、乙X+1、丙X+2...
        int ganOffset = (zhiIndex - xunShouZhiIndex + 12) % 12;
        if (ganOffset >= 10) {
            // 超过10个天干，说明在下一旬，返回空
            return "";
        }

        String[] ganArray = {"甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸"};
        return ganArray[ganOffset];
    }

    /**
     * 天干寄宫对应关系
     * 甲寄寅、乙寄辰、丙戊寄巳、丁己寄未、庚寄申、辛寄戌、壬寄亥、癸寄丑
     * 阳干寄宫是本身的禄位，阴干所寄是自身禄地的前一位
     */
    private String getGanZhiMapping(String gan) {
        switch (gan) {
            case "甲": return "寅";
            case "乙": return "辰";
            case "丙": return "巳";
            case "丁": return "未";
            case "戊": return "巳";
            case "己": return "未";
            case "庚": return "申";
            case "辛": return "戌";
            case "壬": return "亥";
            case "癸": return "丑";
            default: throw new IllegalArgumentException("Invalid gan: " + gan);
        }
    }

    /**
     * 根据地盘地支获取该位置的天盘地支
     */
    private String getTianPanByDiPan(String diPan, TianDiPan tianDiPan) {
        int index = DiZhi.fromName(diPan).getIndex();
        return tianDiPan.getPositions()[index].getTianPan();
    }

    /**
     * 根据天盘地支获取对应的天将
     * 需要遍历所有位置，找到天盘为指定地支的位置，返回该位置的天将
     */
    private String getTianJiangByTianPan(String tianPan, TianDiPan tianDiPan) {
        for (int i = 0; i < 12; i++) {
            PanPosition pos = tianDiPan.getPositions()[i];
            if (tianPan.equals(pos.getTianPan())) {
                return pos.getTianJiang();
            }
        }
        return "未知";
    }

    /**
     * 获取神煞（完整版）
     * 根据传支、年月日时四柱信息，判断所有相关神煞
     *
     * @param chuanZhi 传的地支
     * @param yearPillar 年柱
     * @param monthPillar 月柱
     * @param dayPillar 日柱
     * @param hourPillar 时柱
     * @return 所有匹配的神煞，用斜杠分隔
     */
    private String getShenSha(String chuanZhi, Pillar yearPillar, Pillar monthPillar,
                             Pillar dayPillar, Pillar hourPillar) {
        List<String> shenShaList = new ArrayList<>();

        int chuanIndex = DiZhi.fromName(chuanZhi).getIndex();
        String yearZhi = yearPillar.getDiZhi();
        String monthZhi = monthPillar.getDiZhi();
        String dayGan = dayPillar.getTianGan();
        String dayZhi = dayPillar.getDiZhi();

        int yearIndex = DiZhi.fromName(yearZhi).getIndex();
        int monthIndex = DiZhi.fromName(monthZhi).getIndex();
        int dayIndex = DiZhi.fromName(dayZhi).getIndex();

        // ========== 1. 岁煞（基于年支）==========

        // 太岁：年支本身
        if (chuanIndex == yearIndex) {
            shenShaList.add("太岁");
        }

        // 岁破：太岁对冲
        int suiPoIndex = (yearIndex + 6) % 12;
        if (chuanIndex == suiPoIndex) {
            shenShaList.add("岁破");
        }

        // 病符：旧年太岁（去年的太岁，即今年太岁的前一位）
        int bingFuIndex = (yearIndex - 1 + 12) % 12;
        if (chuanIndex == bingFuIndex) {
            shenShaList.add("病符");
        }

        // 丧门：太岁后两位
        int sangMenIndex = (yearIndex + 2) % 12;
        if (chuanIndex == sangMenIndex) {
            shenShaList.add("丧门");
        }

        // 吊客：太岁前两位
        int diaoKeIndex = (yearIndex - 2 + 12) % 12;
        if (chuanIndex == diaoKeIndex) {
            shenShaList.add("吊客");
        }

        // 官符：太岁三合前辰
        // 巳酉丑合，巳前辰酉；申子辰合，申前辰子；亥卯未合，亥前辰卯；寅午戌合，寅前辰午
        int guanFuIndex = getSanHeQianChen(yearIndex);
        if (guanFuIndex != -1 && chuanIndex == guanFuIndex) {
            shenShaList.add("官符");
        }

        // 白虎：太岁三合后辰
        int baiHuIndex = getSanHeHouChen(yearIndex);
        if (baiHuIndex != -1 && chuanIndex == baiHuIndex) {
            shenShaList.add("白虎");
        }

        // 岁墓：太岁后五辰
        int suiMuIndex = (yearIndex + 5) % 12;
        if (chuanIndex == suiMuIndex) {
            shenShaList.add("岁墓");
        }

        // ========== 2. 季煞（基于月份季节）==========

        // 判断季节：寅卯辰春，巳午未夏，申酉戌秋，亥子丑冬
        int season = getSeason(monthIndex);

        // 天喜：春戌、夏丑、秋辰、冬未
        int tianXiIndex = -1;
        switch (season) {
            case 0: tianXiIndex = 10; break; // 春戌
            case 1: tianXiIndex = 1; break;  // 夏丑
            case 2: tianXiIndex = 4; break;  // 秋辰
            case 3: tianXiIndex = 7; break;  // 冬未
        }
        if (chuanIndex == tianXiIndex) {
            shenShaList.add("天喜");
        }

        // 孤辰、寡宿：春巳/丑，夏申/辰，秋亥/未，冬寅/戌
        int guChenIndex = -1, guaSuIndex = -1;
        switch (season) {
            case 0: guChenIndex = 5; guaSuIndex = 1; break;  // 春巳/丑
            case 1: guChenIndex = 8; guaSuIndex = 4; break;  // 夏申/辰
            case 2: guChenIndex = 11; guaSuIndex = 7; break; // 秋亥/未
            case 3: guChenIndex = 2; guaSuIndex = 10; break; // 冬寅/戌
        }
        if (chuanIndex == guChenIndex) {
            shenShaList.add("孤辰");
        }
        if (chuanIndex == guaSuIndex) {
            shenShaList.add("寡宿");
            shenShaList.add("关神");
            shenShaList.add("三丘");
        }

        // 五墓：三丘对冲
        int wuMuIndex = (guaSuIndex + 6) % 12;
        if (chuanIndex == wuMuIndex) {
            shenShaList.add("五墓");
        }

        // 皇书/战雄：春寅、夏巳、秋申、冬亥（四孟）
        int huangShuIndex = -1;
        switch (season) {
            case 0: huangShuIndex = 2; break;  // 春寅
            case 1: huangShuIndex = 5; break;  // 夏巳
            case 2: huangShuIndex = 8; break;  // 秋申
            case 3: huangShuIndex = 11; break; // 冬亥
        }
        if (chuanIndex == huangShuIndex) {
            shenShaList.add("皇书");
            shenShaList.add("战雄");
        }
        // 战雌：皇书对冲
        int zhanCiIndex = (huangShuIndex + 6) % 12;
        if (chuanIndex == zhanCiIndex) {
            shenShaList.add("战雌");
        }

        // 浴盆/天目：春辰、夏未、秋戌、冬丑（四季）
        int yuPenIndex = -1;
        switch (season) {
            case 0: yuPenIndex = 4; break;  // 春辰
            case 1: yuPenIndex = 7; break;  // 夏未
            case 2: yuPenIndex = 10; break; // 秋戌
            case 3: yuPenIndex = 1; break;  // 冬丑
        }
        if (chuanIndex == yuPenIndex) {
            shenShaList.add("浴盆");
            shenShaList.add("天目");
        }

        // 火鬼：春午、夏酉、秋子、冬卯
        int huoGuiIndex = -1;
        switch (season) {
            case 0: huoGuiIndex = 6; break;  // 春午
            case 1: huoGuiIndex = 9; break;  // 夏酉
            case 2: huoGuiIndex = 0; break;  // 秋子
            case 3: huoGuiIndex = 3; break;  // 冬卯
        }
        if (chuanIndex == huoGuiIndex) {
            shenShaList.add("火鬼");
        }

        // 丧车：春酉、夏子、秋卯、冬午
        int sangCheIndex = -1;
        switch (season) {
            case 0: sangCheIndex = 9; break;  // 春酉
            case 1: sangCheIndex = 0; break;  // 夏子
            case 2: sangCheIndex = 3; break;  // 秋卯
            case 3: sangCheIndex = 6; break;  // 冬午
        }
        if (chuanIndex == sangCheIndex) {
            shenShaList.add("丧车");
        }

        // 天赦：春寅、夏午、秋申、冬子
        int tianSheIndex = -1;
        switch (season) {
            case 0: tianSheIndex = 2; break;  // 春寅
            case 1: tianSheIndex = 6; break;  // 夏午
            case 2: tianSheIndex = 8; break;  // 秋申
            case 3: tianSheIndex = 0; break;  // 冬子
        }
        if (chuanIndex == tianSheIndex) {
            shenShaList.add("天赦");
        }

        // 游神：春丑、夏子、秋亥、冬戌
        int youShenIndex = -1;
        switch (season) {
            case 0: youShenIndex = 1; break;  // 春丑
            case 1: youShenIndex = 0; break;  // 夏子
            case 2: youShenIndex = 11; break; // 秋亥
            case 3: youShenIndex = 10; break; // 冬戌
        }
        if (chuanIndex == youShenIndex) {
            shenShaList.add("游神");
        }

        // 戏神：春巳、夏子、秋酉、冬辰
        int xiShenIndex = -1;
        switch (season) {
            case 0: xiShenIndex = 5; break;  // 春巳
            case 1: xiShenIndex = 0; break;  // 夏子
            case 2: xiShenIndex = 9; break;  // 秋酉
            case 3: xiShenIndex = 4; break;  // 冬辰
        }
        if (chuanIndex == xiShenIndex) {
            shenShaList.add("戏神");
        }

        // 贼伏：春卯、夏巳、秋申、冬子
        int zeiFuIndex = -1;
        switch (season) {
            case 0: zeiFuIndex = 3; break;  // 春卯
            case 1: zeiFuIndex = 5; break;  // 夏巳
            case 2: zeiFuIndex = 8; break;  // 秋申
            case 3: zeiFuIndex = 0; break;  // 冬子
        }
        if (chuanIndex == zeiFuIndex) {
            shenShaList.add("贼伏");
        }

        // ========== 3. 月煞（基于月支）==========

        // 生气：月建后二辰（逆数两位）
        // 如寅月子为生气：寅(2) - 2 = 0(子)
        int shengQiIndex = (monthIndex - 2 + 12) % 12;
        if (chuanIndex == shengQiIndex) {
            shenShaList.add("生气");
        }

        // 死气：月建三合前一辰（顺数四位），等同于生气对冲
        // 如寅月午为死气：寅(2) + 4 = 6(午)，或子(0) + 6 = 6(午)
        int siQiIndex = (monthIndex + 4) % 12;  // 或 (shengQiIndex + 6) % 12
        if (chuanIndex == siQiIndex) {
            shenShaList.add("死气");
        }

        // 死神：死气后一位
        int siShenIndex = (siQiIndex + 1) % 12;
        if (chuanIndex == siShenIndex) {
            shenShaList.add("死神");
        }

        // 天医：寅月起辰顺行十二辰
        int tianYiIndex = (4 + (monthIndex - 2 + 12)) % 12; // 寅月(2)起辰(4)
        if (chuanIndex == tianYiIndex) {
            shenShaList.add("天医");
            shenShaList.add("天巫");
        }

        // 地医：天医对冲
        int diYiIndex = (tianYiIndex + 6) % 12;
        if (chuanIndex == diYiIndex) {
            shenShaList.add("地医");
        }

        // 飞魂：正月起亥顺行
        int feiHunIndex = (11 + (monthIndex - 2 + 12)) % 12; // 正月(寅月)起亥
        if (chuanIndex == feiHunIndex) {
            shenShaList.add("飞魂");
        }

        // 天诏：同飞魂
        if (chuanIndex == feiHunIndex) {
            shenShaList.add("天诏");
        }

        // 月厌：寅月起戌逆行
        int yueYanIndex = (10 - (monthIndex - 2 + 12)) % 12; // 寅月(2)起戌(10)
        if (chuanIndex == yueYanIndex) {
            shenShaList.add("月厌");
        }

        // 天马：寅月起午顺行六阳辰
        // 六阳辰 = 子寅辰午申戌(0,2,4,6,8,10)
        // 寅月(2)→午(6), 卯月(3)→申(8), 辰月(4)→戌(10), 巳月(5)→子(0), 午月(6)→寅(2), 未月(7)→辰(4)
        // 申月(8)→午(6), 酉月(9)→申(8), 戌月(10)→戌(10), 亥月(11)→子(0), 子月(0)→寅(2), 丑月(1)→辰(4)
        // 规律：从寅月起午(月2→地支6)开始，每月前进2位，只落在六阳辰上
        int tianMaIndex = (6 + 2 * (monthIndex - 2 + 12)) % 12;
        if (chuanIndex == tianMaIndex) {
            shenShaList.add("天马");
        }

        // 皇恩：寅月起未顺行六阴辰
        int huangEnIndex = -1;
        if (monthIndex % 2 == 1) { // 阴月
            huangEnIndex = (7 + (monthIndex - 3 + 12)) % 12; // 卯月起未
        } else { // 阳月
            huangEnIndex = (7 + (monthIndex - 2)) % 12; // 寅月起未
        }
        if (chuanIndex == huangEnIndex) {
            shenShaList.add("皇恩");
        }

        // 天鬼：正月起酉逆行四仲（子卯午酉）
        // 寅午戌月见酉，亥卯未月见午，申子辰月见卯，巳酉丑月见子
        int sanHeJu = getSanHeJu(monthIndex);
        int tianGuiIndex = -1;
        switch (sanHeJu) {
            case 0: tianGuiIndex = 9; break;  // 寅午戌见酉
            case 1: tianGuiIndex = 6; break;  // 亥卯未见午
            case 2: tianGuiIndex = 3; break;  // 申子辰见卯
            case 3: tianGuiIndex = 0; break;  // 巳酉丑见子
        }
        if (chuanIndex == tianGuiIndex) {
            shenShaList.add("天鬼");
            shenShaList.add("长绳");
        }

        // 丧魄：正月起戌逆行四季
        // 寅午戌见戌，亥卯未见未，申子辰见辰，巳酉丑见丑
        int sangPoIndex = -1;
        switch (sanHeJu) {
            case 0: sangPoIndex = 10; break; // 寅午戌见戌
            case 1: sangPoIndex = 7; break;  // 亥卯未见未
            case 2: sangPoIndex = 4; break;  // 申子辰见辰
            case 3: sangPoIndex = 1; break;  // 巳酉丑见丑
        }
        if (chuanIndex == sangPoIndex) {
            shenShaList.add("丧魄");
            shenShaList.add("迷惑");
        }

        // 天鸡：正月起酉逆行
        int tianJiIndex = (9 - (monthIndex - 2 + 12)) % 12; // 正月(寅月)起酉
        if (chuanIndex == tianJiIndex) {
            shenShaList.add("天鸡");
        }

        // 信神：正月起酉顺行
        int xinShenIndex = (9 + (monthIndex - 2 + 12)) % 12;
        if (chuanIndex == xinShenIndex) {
            shenShaList.add("信神");
        }

        // 天财：正月起辰顺行六阳辰
        int tianCaiIndex = (4 + (monthIndex - 2 + 12)) % 12;
        if (chuanIndex == tianCaiIndex) {
            shenShaList.add("天财");
        }

        // 血支：月建后一辰（月病符）
        int xueZhiIndex = (monthIndex + 1) % 12;
        if (chuanIndex == xueZhiIndex) {
            shenShaList.add("血支");
        }

        // 血忌：六阳月从丑至午，六阴月从未至子
        int xueJiIndex = -1;
        if (monthIndex % 2 == 0) { // 阳月：寅辰午申戌子
            // 正月丑、三月寅、五月卯、七月辰、九月巳、冬月午
            xueJiIndex = (1 + (monthIndex - 2) / 2) % 12;
        } else { // 阴月：卯巳未酉亥丑
            // 二月未、四月申、六月酉、八月戌、十月亥、腊月子
            xueJiIndex = (7 + (monthIndex - 3) / 2) % 12;
        }
        if (chuanIndex == xueJiIndex) {
            shenShaList.add("血忌");
        }

        // 悬锁：月桃花（同支煞桃花）
        int xuanSuoIndex = getTaoHua(monthIndex);
        if (chuanIndex == xuanSuoIndex) {
            shenShaList.add("悬锁");
        }

        // 奸门、阴奸
        int jianMenIndex = -1, yinJianIndex = -1;
        switch (sanHeJu) {
            case 0: jianMenIndex = 8; yinJianIndex = 7; break;  // 寅午戌：申/未
            case 1: jianMenIndex = 11; yinJianIndex = 4; break; // 亥卯未：亥/辰
            case 2: jianMenIndex = 2; yinJianIndex = 1; break;  // 申子辰：寅/丑
            case 3: jianMenIndex = 5; yinJianIndex = 10; break; // 巳酉丑：巳/戌
        }
        if (chuanIndex == jianMenIndex) {
            shenShaList.add("奸门");
        }
        if (chuanIndex == yinJianIndex) {
            shenShaList.add("阴奸");
        }

        // 谩语：同死气
        // 已在死气中添加

        // 成神
        int chengShenIndex = -1;
        switch (sanHeJu) {
            case 0: chengShenIndex = 5; break;  // 寅午戌月巳
            case 1: chengShenIndex = 8; break;  // 亥卯未月申
            case 2: chengShenIndex = 11; break; // 申子辰月亥
            case 3: chengShenIndex = 2; break;  // 巳酉丑月寅
        }
        if (chuanIndex == chengShenIndex) {
            shenShaList.add("成神");
        }

        // ========== 4. 旬煞（基于日干支的旬）==========

        // 计算旬首（日干支所在的旬）
        int dayGanIndex = TianGan.fromName(dayGan).getIndex();
        int xunShouIndex = getXunShou(dayGanIndex, dayIndex);

        // 旬空：旬中最后两个地支
        int kongWang1 = (xunShouIndex + 10) % 12;
        int kongWang2 = (xunShouIndex + 11) % 12;
        if (chuanIndex == kongWang1 || chuanIndex == kongWang2) {
            shenShaList.add("旬空");
            shenShaList.add("天中");
        }

        // 旬奇：甲子甲戌旬奇亥，甲申甲午旬奇子，甲辰甲寅旬奇丑
        int xunQiIndex = -1;
        switch (xunShouIndex) {
            case 0:  // 甲子旬
            case 10: // 甲戌旬
                xunQiIndex = 11; break; // 亥
            case 8:  // 甲申旬
            case 6:  // 甲午旬
                xunQiIndex = 0; break;  // 子
            case 4:  // 甲辰旬
            case 2:  // 甲寅旬
                xunQiIndex = 1; break;  // 丑
        }
        if (chuanIndex == xunQiIndex) {
            shenShaList.add("旬奇");
        }

        // 旬仪：六旬地支（旬首地支）
        if (chuanIndex == xunShouIndex) {
            shenShaList.add("旬仪");
        }

        // 丁马/六丁：每旬六丁之辰
        int dingMaIndex = getDingMa(xunShouIndex);
        if (dingMaIndex != -1 && chuanIndex == dingMaIndex) {
            shenShaList.add("丁马");
            shenShaList.add("六丁");
        }

        // 旬癸/闭口：每旬六癸之辰
        int xunGuiIndex = getXunGui(xunShouIndex);
        if (xunGuiIndex != -1 && chuanIndex == xunGuiIndex) {
            shenShaList.add("旬癸");
            shenShaList.add("闭口");
        }

        // 旬乙/盗神：每旬六乙之辰
        int xunYiIndex = getXunYi(xunShouIndex);
        if (xunYiIndex != -1 && chuanIndex == xunYiIndex) {
            shenShaList.add("旬乙");
            shenShaList.add("盗神");
        }

        // 旬辛：每旬六辛之辰
        int xunXinIndex = getXunXin(xunShouIndex);
        if (xunXinIndex != -1 && chuanIndex == xunXinIndex) {
            shenShaList.add("旬辛");
        }

        // ========== 5. 干煞（基于日干）==========

        // 干德
        int ganDeIndex = -1;
        switch (dayGan) {
            case "甲": case "己": ganDeIndex = 2; break;  // 寅
            case "乙": case "庚": ganDeIndex = 8; break;  // 申
            case "丙": case "辛": ganDeIndex = 5; break;  // 巳
            case "丁": case "壬": ganDeIndex = 11; break; // 亥
            case "戊": case "癸": ganDeIndex = 5; break;  // 巳
        }
        if (chuanIndex == ganDeIndex) {
            shenShaList.add("干德");
            shenShaList.add("日德");
        }

        // 游都
        int youDuIndex = -1;
        switch (dayGan) {
            case "甲": case "己": youDuIndex = 1; break;  // 丑
            case "乙": case "庚": youDuIndex = 0; break;  // 子
            case "丙": case "辛": youDuIndex = 2; break;  // 寅
            case "丁": case "壬": youDuIndex = 5; break;  // 巳
            case "戊": case "癸": youDuIndex = 8; break;  // 申
        }
        if (chuanIndex == youDuIndex) {
            shenShaList.add("游都");
        }

        // 鲁都：游都对冲
        int luDuIndex = (youDuIndex + 6) % 12;
        if (chuanIndex == luDuIndex) {
            shenShaList.add("鲁都");
        }

        // ========== 6. 支煞（基于日支）==========

        // 驿马：三合头冲
        int yiMaIndex = getYiMa(dayIndex);
        if (chuanIndex == yiMaIndex) {
            shenShaList.add("驿马");
        }

        // 劫煞
        int jieShaIndex = getJieSha(dayIndex);
        if (chuanIndex == jieShaIndex) {
            shenShaList.add("劫煞");
        }

        // 亡神
        int wangShenIndex = getWangShen(dayIndex);
        if (chuanIndex == wangShenIndex) {
            shenShaList.add("亡神");
        }

        // 破碎煞
        int poSuiIndex = getPoSui(dayIndex);
        if (poSuiIndex != -1 && chuanIndex == poSuiIndex) {
            shenShaList.add("破碎");
        }

        // 华盖
        int huaGaiIndex = getHuaGai(dayIndex);
        if (chuanIndex == huaGaiIndex) {
            shenShaList.add("华盖");
        }

        // 桃花煞
        int taoHuaIndex = getTaoHua(dayIndex);
        if (chuanIndex == taoHuaIndex) {
            shenShaList.add("桃花");
        }

        // 将星
        int jiangXingIndex = getJiangXing(dayIndex);
        if (chuanIndex == jiangXingIndex) {
            shenShaList.add("将星");
        }

        // 灾煞
        int zaiShaIndex = getZaiSha(dayIndex);
        if (chuanIndex == zaiShaIndex) {
            shenShaList.add("灾煞");
        }

        // ========== 返回结果 ==========

        if (shenShaList.isEmpty()) {
            return "无";
        }

        return String.join("/", shenShaList);
    }

    /**
     * 判断指定日期是否已过立春
     * 立春一般在公历2月3-5日之间
     * 简化处理：2月4日之前视为未过立春，2月4日及之后视为已过立春
     *
     * @param year 年份
     * @param month 月份
     * @param day 日期
     * @return 是否已过立春
     */
    private boolean hasPassedLiChun(int year, int month, int day) {
        // 如果是3月及之后，肯定过了立春
        if (month >= 3) {
            return true;
        }
        // 如果是1月，肯定没过立春
        if (month == 1) {
            return false;
        }
        // 如果是2月，判断日期是否在4日及之后
        // 简化处理：2月4日及之后视为已过立春
        return day >= 4;
    }

    /**
     * 根据月柱地支计算月将
     * 月将是大六壬中的重要概念，按照月支确定
     *
     * 月将对应表（按节气月的月支）：
     * 寅月 - 登明（亥将）
     * 卯月 - 河魁（戌将）
     * 辰月 - 从魁（酉将）
     * 巳月 - 传送（申将）
     * 午月 - 小吉（未将）
     * 未月 - 胜光（午将）
     * 申月 - 太乙（巳将）
     * 酉月 - 天罡（辰将）
     * 戌月 - 太冲（卯将）
     * 亥月 - 功曹（寅将）
     * 子月 - 大吉（丑将）
     * 丑月 - 神后（子将）
     *
     * @param monthZhi 月柱地支
     * @return 月将地支
     */
    /**
     * 根据中气计算月将
     * 月将的确立规则：
     * - 正月交雨水后 → 亥宫(登明)
     * - 二月交春分后 → 戌宫(河魁)
     * - 三月交谷雨后 → 酉宫(从魁)
     * - 四月交小满后 → 申宫(传送)
     * - 五月交夏至后 → 未宫(小吉)
     * - 六月交大暑后 → 午宫(圣光)
     * - 七月交处暑后 → 巳宫(太乙)
     * - 八月交秋分后 → 辰宫(天罡)
     * - 九月交霜降后 → 卯宫(太冲)
     * - 十月交小雪后 → 寅宫(功曹)
     * - 冬月交冬至后 → 丑宫(大吉)
     * - 腊月交大寒后 → 子宫(神后)
     *
     * 月将的交换更替都是根据中气的变化而开始的
     *
     * @param year 年份
     * @param month 月份
     * @param day 日期
     * @return 月将地支
     */
    private String calculateYueJiangByZhongQi(int year, int month, int day) {
        // 12个中气的近似日期（简化算法）
        // 格式：[月份, 日期, 对应月将]
        // 注：实际中气时刻会有几个小时的偏差，这里使用简化的日期
        Object[][] zhongQiList = {
            {1, 20, "子"},   // 大寒(1/20) - 腊月中气 → 子将(神后)
            {2, 19, "亥"},   // 雨水(2/19) - 正月中气 → 亥将(登明)
            {3, 21, "戌"},   // 春分(3/21) - 二月中气 → 戌将(河魁)
            {4, 20, "酉"},   // 谷雨(4/20) - 三月中气 → 酉将(从魁)
            {5, 21, "申"},   // 小满(5/21) - 四月中气 → 申将(传送)
            {6, 21, "未"},   // 夏至(6/21) - 五月中气 → 未将(小吉)
            {7, 23, "午"},   // 大暑(7/23) - 六月中气 → 午将(圣光)
            {8, 23, "巳"},   // 处暑(8/23) - 七月中气 → 巳将(太乙)
            {9, 23, "辰"},   // 秋分(9/23) - 八月中气 → 辰将(天罡)
            {10, 23, "卯"},  // 霜降(10/23) - 九月中气 → 卯将(太冲)
            {11, 22, "寅"},  // 小雪(11/22) - 十月中气 → 寅将(功曹)
            {12, 22, "丑"}   // 冬至(12/22) - 冬月中气 → 丑将(大吉)
        };

        // 从后往前查找，找到当前日期之前最近的中气
        String yueJiang = "丑"; // 默认丑将

        // 先查找当前年份的中气
        for (int i = zhongQiList.length - 1; i >= 0; i--) {
            int zhongQiMonth = (int) zhongQiList[i][0];
            int zhongQiDay = (int) zhongQiList[i][1];
            String jiang = (String) zhongQiList[i][2];

            // 如果当前日期在该中气之后（或当天）
            if (month > zhongQiMonth || (month == zhongQiMonth && day >= zhongQiDay)) {
                yueJiang = jiang;
                break;
            }
        }

        return yueJiang;
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
        if(gender.equals("male")) {
            return ganZhiYear + "男命";
        } else {
            return ganZhiYear + "女命";
        }
    }

    // ========== 神煞辅助方法 ==========

    /**
     * 获取季节：0春、1夏、2秋、3冬
     */
    private int getSeason(int monthIndex) {
        // 寅卯辰春(2,3,4)，巳午未夏(5,6,7)，申酉戌秋(8,9,10)，亥子丑冬(11,0,1)
        if (monthIndex >= 2 && monthIndex <= 4) return 0; // 春
        if (monthIndex >= 5 && monthIndex <= 7) return 1; // 夏
        if (monthIndex >= 8 && monthIndex <= 10) return 2; // 秋
        return 3; // 冬
    }

    /**
     * 获取三合局：0=寅午戌，1=亥卯未，2=申子辰，3=巳酉丑
     */
    private int getSanHeJu(int zhiIndex) {
        if (zhiIndex == 2 || zhiIndex == 6 || zhiIndex == 10) return 0; // 寅午戌
        if (zhiIndex == 11 || zhiIndex == 3 || zhiIndex == 7) return 1; // 亥卯未
        if (zhiIndex == 8 || zhiIndex == 0 || zhiIndex == 4) return 2; // 申子辰
        if (zhiIndex == 5 || zhiIndex == 9 || zhiIndex == 1) return 3; // 巳酉丑
        return 0;
    }

    /**
     * 获取三合前辰
     */
    private int getSanHeQianChen(int zhiIndex) {
        int ju = getSanHeJu(zhiIndex);
        switch (ju) {
            case 0: return 9;  // 寅午戌 → 酉
            case 1: return 6;  // 亥卯未 → 午
            case 2: return 3;  // 申子辰 → 卯
            case 3: return 0;  // 巳酉丑 → 子
        }
        return -1;
    }

    /**
     * 获取三合后辰
     */
    private int getSanHeHouChen(int zhiIndex) {
        int ju = getSanHeJu(zhiIndex);
        switch (ju) {
            case 0: return 5;  // 寅午戌 → 巳
            case 1: return 2;  // 亥卯未 → 寅
            case 2: return 11; // 申子辰 → 亥
            case 3: return 8;  // 巳酉丑 → 申
        }
        return -1;
    }

    /**
     * 获取桃花
     */
    private int getTaoHua(int zhiIndex) {
        int ju = getSanHeJu(zhiIndex);
        switch (ju) {
            case 0: return 3;  // 寅午戌 → 卯
            case 1: return 0;  // 亥卯未 → 子
            case 2: return 9;  // 申子辰 → 酉
            case 3: return 6;  // 巳酉丑 → 午
        }
        return -1;
    }

    /**
     * 获取驿马
     */
    private int getYiMa(int zhiIndex) {
        int ju = getSanHeJu(zhiIndex);
        switch (ju) {
            case 0: return 8;  // 寅午戌 → 申
            case 1: return 5;  // 亥卯未 → 巳
            case 2: return 2;  // 申子辰 → 寅
            case 3: return 11; // 巳酉丑 → 亥
        }
        return -1;
    }

    /**
     * 获取劫煞
     */
    private int getJieSha(int zhiIndex) {
        int ju = getSanHeJu(zhiIndex);
        switch (ju) {
            case 0: return 11; // 寅午戌 → 亥
            case 1: return 8;  // 亥卯未 → 申
            case 2: return 5;  // 申子辰 → 巳
            case 3: return 2;  // 巳酉丑 → 寅
        }
        return -1;
    }

    /**
     * 获取亡神
     */
    private int getWangShen(int zhiIndex) {
        int ju = getSanHeJu(zhiIndex);
        switch (ju) {
            case 0: return 5;  // 寅午戌 → 巳
            case 1: return 2;  // 亥卯未 → 寅
            case 2: return 11; // 申子辰 → 亥
            case 3: return 8;  // 巳酉丑 → 申
        }
        return -1;
    }

    /**
     * 获取破碎煞
     */
    private int getPoSui(int zhiIndex) {
        // 寅申巳亥日在酉，卯子酉午日在巳，丑戌辰未日在丑
        if (zhiIndex == 2 || zhiIndex == 8 || zhiIndex == 5 || zhiIndex == 11) {
            return 9; // 酉
        }
        if (zhiIndex == 3 || zhiIndex == 0 || zhiIndex == 9 || zhiIndex == 6) {
            return 5; // 巳
        }
        if (zhiIndex == 1 || zhiIndex == 10 || zhiIndex == 4 || zhiIndex == 7) {
            return 1; // 丑
        }
        return -1;
    }

    /**
     * 获取华盖
     */
    private int getHuaGai(int zhiIndex) {
        int ju = getSanHeJu(zhiIndex);
        switch (ju) {
            case 0: return 10; // 寅午戌 → 戌
            case 1: return 7;  // 亥卯未 → 未
            case 2: return 4;  // 申子辰 → 辰
            case 3: return 1;  // 巳酉丑 → 丑
        }
        return -1;
    }

    /**
     * 获取将星
     */
    private int getJiangXing(int zhiIndex) {
        int ju = getSanHeJu(zhiIndex);
        switch (ju) {
            case 0: return 6;  // 寅午戌 → 午
            case 1: return 3;  // 亥卯未 → 卯
            case 2: return 0;  // 申子辰 → 子
            case 3: return 9;  // 巳酉丑 → 酉
        }
        return -1;
    }

    /**
     * 获取灾煞
     */
    private int getZaiSha(int zhiIndex) {
        int ju = getSanHeJu(zhiIndex);
        switch (ju) {
            case 0: return 0;  // 寅午戌 → 子
            case 1: return 9;  // 亥卯未 → 酉
            case 2: return 6;  // 申子辰 → 午
            case 3: return 3;  // 巳酉丑 → 卯
        }
        return -1;
    }

    /**
     * 计算旬首（日干支所在旬的第一个干支的地支index）
     */
    private int getXunShou(int ganIndex, int zhiIndex) {
        // 旬首地支 = (地支 - 天干 + 12) % 12
        return (zhiIndex - ganIndex + 12) % 12;
    }

    /**
     * 获取六丁/丁马
     */
    private int getDingMa(int xunShouIndex) {
        // 甲子旬丁卯(3)，甲戌旬丁丑(1)，甲申旬丁亥(11)，
        // 甲午旬丁酉(9)，甲辰旬丁未(7)，甲寅旬丁巳(5)
        switch (xunShouIndex) {
            case 0:  return 3;  // 甲子旬 → 丁卯
            case 10: return 1;  // 甲戌旬 → 丁丑
            case 8:  return 11; // 甲申旬 → 丁亥
            case 6:  return 9;  // 甲午旬 → 丁酉
            case 4:  return 7;  // 甲辰旬 → 丁未
            case 2:  return 5;  // 甲寅旬 → 丁巳
        }
        return -1;
    }

    /**
     * 获取六癸/旬癸
     */
    private int getXunGui(int xunShouIndex) {
        // 甲子旬癸酉(9)，甲戌旬癸未(7)，甲申旬癸巳(5)，
        // 甲午旬癸卯(3)，甲辰旬癸丑(1)，甲寅旬癸亥(11)
        switch (xunShouIndex) {
            case 0:  return 9;  // 甲子旬 → 癸酉
            case 10: return 7;  // 甲戌旬 → 癸未
            case 8:  return 5;  // 甲申旬 → 癸巳
            case 6:  return 3;  // 甲午旬 → 癸卯
            case 4:  return 1;  // 甲辰旬 → 癸丑
            case 2:  return 11; // 甲寅旬 → 癸亥
        }
        return -1;
    }

    /**
     * 获取六乙/旬乙
     */
    private int getXunYi(int xunShouIndex) {
        // 甲子旬乙丑(1)，甲戌旬乙亥(11)，甲申旬乙酉(9)，
        // 甲午旬乙未(7)，甲辰旬乙巳(5)，甲寅旬乙卯(3)
        switch (xunShouIndex) {
            case 0:  return 1;  // 甲子旬 → 乙丑
            case 10: return 11; // 甲戌旬 → 乙亥
            case 8:  return 9;  // 甲申旬 → 乙酉
            case 6:  return 7;  // 甲午旬 → 乙未
            case 4:  return 5;  // 甲辰旬 → 乙巳
            case 2:  return 3;  // 甲寅旬 → 乙卯
        }
        return -1;
    }

    /**
     * 获取六辛/旬辛
     */
    private int getXunXin(int xunShouIndex) {
        // 甲子旬辛未(7)，甲戌旬辛巳(5)，甲申旬辛卯(3)，
        // 甲午旬辛丑(1)，甲辰旬辛亥(11)，甲寅旬辛酉(9)
        switch (xunShouIndex) {
            case 0:  return 7;  // 甲子旬 → 辛未
            case 10: return 5;  // 甲戌旬 → 辛巳
            case 8:  return 3;  // 甲申旬 → 辛卯
            case 6:  return 1;  // 甲午旬 → 辛丑
            case 4:  return 11; // 甲辰旬 → 辛亥
            case 2:  return 9;  // 甲寅旬 → 辛酉
        }
        return -1;
    }
}
