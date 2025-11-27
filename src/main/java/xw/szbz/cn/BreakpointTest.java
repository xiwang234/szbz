package xw.szbz.cn;

/**
 * 断点测试类
 * 用于验证调试功能是否正常
 */
public class BreakpointTest {
    public static void main(String[] args) {
        System.out.println("开始测试断点功能");

        // 在下面这行设置断点（点击行号11左侧，或光标放这里按F9）
        String message = "这是第一个断点"; // ← 在这行设置断点
        System.out.println(message);

        // 在下面这行设置第二个断点
        String message2 = "这是第二个断点"; // ← 在这行设置断点
        System.out.println(message2);

        System.out.println("测试完成");
    }
}
