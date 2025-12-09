package xw.szbz.cn.service;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;

public class QuickGeminiTest {
    public static void main(String[] args) {
        String apiKey = "AIzaSyA9aKhNqwaYN0bsDqzqi9cmHL84WpM-xX8";
        
        try {
            System.out.println("开始测试 Gemini API...");
            
            Client client = Client.builder()
                    .apiKey(apiKey)
                    .build();
            
            System.out.println("客户端创建成功");
            
            String prompt = "你好，请简单介绍一下你自己，不超过50字。";
            
            System.out.println("发送请求...");
            GenerateContentResponse response = client.models.generateContent(
                    "gemini-2.0-flash-exp",
                    prompt,
                    null
            );
            
            System.out.println("======== Gemini 响应 ========");
            System.out.println(response.text());
            System.out.println("============================");
            
        } catch (Exception e) {
            System.err.println("错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
