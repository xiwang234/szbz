package xw.szbz.cn;

import xw.szbz.cn.model.BaZiRequest;
import xw.szbz.cn.model.BaZiResult;
import xw.szbz.cn.service.BaZiService;

/**
 * 手动测试计算八字
 */
public class BaZiCalculator {
    public static void main(String[] args) {
        BaZiService service = new BaZiService();

        // 1984年11月23日23点25分
        BaZiRequest request = new BaZiRequest("男", 1984, 11, 23, 23,"");
        BaZiResult result = service.calculate(request);

        System.out.println("========== 四柱八字排盘结果 ==========");
        System.out.println("输入时间: 1984年11月23日 23:25");
        System.out.println("排盘时间: " + result.getBirthInfo().getYear() + "年"
                + result.getBirthInfo().getMonth() + "月"
                + result.getBirthInfo().getDay() + "日 "
                + result.getBirthInfo().getShiChen());
        System.out.println("是否跨日调整: " + result.getBirthInfo().isAdjusted());
        System.out.println();
        System.out.println("年柱: " + result.getYearPillar().getFullName());
        System.out.println("月柱: " + result.getMonthPillar().getFullName());
        System.out.println("日柱: " + result.getDayPillar().getFullName());
        System.out.println("时柱: " + result.getHourPillar().getFullName());
        System.out.println();
        System.out.println("完整八字: " + result.getFullBaZi());
        System.out.println("性别: " + result.getGender());
    }
}
