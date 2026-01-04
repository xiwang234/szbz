package xw.szbz.cn;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Gemini API HTTP 直接调用测试
 * 使用 HttpURLConnection 直接调用 REST API
 */
public class GeminiHttpTest {

    public static void main(String[] args) {
        // 配置信息
        String apiKey = "AIzaSyAOexD_9JQNMMSKX698GjKXlU-pVBKo-mA";
        String model = "gemini-2.5-flash";
        String prompt = "请用一句话介绍什么是八字命理。";

        System.out.println("==========================================");
        System.out.println("Gemini API HTTP 直接调用测试");
        System.out.println("==========================================");
        System.out.println("API Key: " + apiKey.substring(0, 20) + "...");
        System.out.println("模型: " + model);
        System.out.println("提示词: " + prompt);
        System.out.println("------------------------------------------\n");

        try {
            // 构建请求 URL
            String urlString = String.format(
                "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s",
                model, apiKey
            );
            System.out.println("请求 URL: " + urlString.replace(apiKey, "***"));
            
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            
            // 设置请求方法和头部
            System.out.println("\n正在配置 HTTP 连接...");
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);
            System.out.println("✅ HTTP 连接配置完成");

            // 构建请求体
            String jsonInputString = String.format(
                "{\"contents\": [{\"parts\": [{\"text\": \"%s\"}]}]}",
                prompt.replace("\"", "\\\"")
            );
            
            System.out.println("\n正在发送请求...");
            System.out.println("请求体: " + jsonInputString);
            
            long startTime = System.currentTimeMillis();
            
            // 发送请求
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // 获取响应
            int responseCode = conn.getResponseCode();
            long endTime = System.currentTimeMillis();
            
            System.out.println("\n✅ 请求已发送");
            System.out.println("响应代码: " + responseCode);
            System.out.println("响应时间: " + (endTime - startTime) + " ms");

            if (responseCode == HttpURLConnection.HTTP_OK) {
                // 读取响应内容
                BufferedReader in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)
                );
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                String responseBody = response.toString();
                System.out.println("\n------------------------------------------");
                System.out.println("完整响应: ");
                System.out.println(formatJson(responseBody));
                System.out.println("------------------------------------------");

                // 解析响应中的文本内容
                String text = extractTextFromResponse(responseBody);
                if (text != null && !text.isEmpty()) {
                    System.out.println("\n提取的文本内容: ");
                    System.out.println(text);
                    System.out.println("\n🎉 测试完成！Gemini API (HTTP) 可以正常调用。");
                } else {
                    System.out.println("\n⚠️ 响应解析失败，无法提取文本内容");
                }

            } else {
                // 读取错误信息
                BufferedReader errorReader = new BufferedReader(
                    new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8)
                );
                String inputLine;
                StringBuilder errorResponse = new StringBuilder();

                while ((inputLine = errorReader.readLine()) != null) {
                    errorResponse.append(inputLine);
                }
                errorReader.close();

                System.err.println("\n❌ API 调用失败！");
                System.err.println("错误响应: " + formatJson(errorResponse.toString()));
            }

            conn.disconnect();

        } catch (Exception e) {
            System.err.println("\n❌ 测试失败！");
            System.err.println("错误类型: " + e.getClass().getName());
            System.err.println("错误信息: " + e.getMessage());
            System.err.println("\n详细堆栈信息：");
            e.printStackTrace();
            
            System.err.println("\n可能的原因：");
            System.err.println("1. API Key 无效或已过期");
            System.err.println("2. 网络连接问题（需要访问 Google 服务）");
            System.err.println("3. 模型名称错误");
            System.err.println("4. 配额已用完");
            System.err.println("5. 需要配置代理");
            System.err.println("6. 防火墙拦截");
        }
    }

    /**
     * 从响应 JSON 中提取文本内容
     */
    private static String extractTextFromResponse(String jsonResponse) {
        try {
            // 简单的 JSON 解析（避免引入额外依赖）
            // 查找 "text": "..." 模式
            String searchKey = "\"text\":";
            int textIndex = jsonResponse.indexOf(searchKey);
            if (textIndex == -1) {
                return null;
            }
            
            int startQuote = jsonResponse.indexOf("\"", textIndex + searchKey.length());
            if (startQuote == -1) {
                return null;
            }
            
            int endQuote = jsonResponse.indexOf("\"", startQuote + 1);
            if (endQuote == -1) {
                return null;
            }
            
            return jsonResponse.substring(startQuote + 1, endQuote)
                    .replace("\\n", "\n")
                    .replace("\\\"", "\"")
                    .replace("\\\\", "\\");
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 简单格式化 JSON（仅用于显示）
     */
    private static String formatJson(String json) {
        if (json == null || json.length() < 100) {
            return json;
        }
        // 如果 JSON 太长，只显示前面部分
        return json.substring(0, Math.min(500, json.length())) + "\n... (内容过长，已截断)";
    }
}
