# API安全功能使用指南

## 功能概述

本项目已实现完整的API安全防护机制，包括：
1. **JWT令牌认证** - 基于OpenId生成和验证JWT令牌
2. **Redis缓存** - 3天过期时间，避免重复AI分析
3. **签名验证** - MD5签名防止参数篡改
4. **时间戳验证** - 2秒有效期防止重放攻击
5. **JSON响应** - 统一的API响应格式
6. **AI结果JSON化** - Gemini分析结果以JSON格式返回

## 接口说明

### POST /api/bazi/analyze

**请求头 (Headers):**
```
Content-Type: application/json
X-Timestamp: 1702825200000  (当前时间戳，毫秒)
X-Sign: 32位MD5签名
```

**请求体 (Request Body):**
```json
{
  "openId": "oABCD1234567890",
  "gender": "男",
  "year": 1984,
  "month": 11,
  "day": 27,
  "hour": 0
}
```

**响应格式 (Response):**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "baziResult": {
      "gender": "男",
      "yearPillar": {...},
      "monthPillar": {...},
      "dayPillar": {...},
      "hourPillar": {...},
      "fullBaZi": "甲子 乙亥 壬戌 庚子",
      "basicInfo": {...},
      "daYunStringList": [...]
    },
    "aiAnalysis": {
      "格局分析": "...",
      "学历情况": "...",
      "用神喜忌": "...",
      "性格特点": "...",
      "事业财运": "...",
      "健康建议": "...",
      "职业情况": "...",
      "综合评价": "..."
    }
  },
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "timestamp": 1702825200000
}
```

## 签名生成算法

### 1. 参数准备
将请求参数按key排序，加上timestamp和密钥：
```
day=27&gender=男&hour=0&month=11&openId=oABCD1234567890&timestamp=1702825200000&year=1984&key=szbz-api-sign-key-2024
```

### 2. MD5加密
对上述字符串进行MD5加密，得到32位小写签名

### 3. Java示例代码

```java
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.TreeMap;
import java.util.Map;

public class SignatureExample {
    
    private static final String SIGN_KEY = "szbz-api-sign-key-2024";
    
    public static String generateSignature(Map<String, Object> params, long timestamp) throws Exception {
        // 使用TreeMap自动排序
        TreeMap<String, Object> sortedParams = new TreeMap<>(params);
        sortedParams.put("timestamp", timestamp);
        
        // 拼接参数
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> entry : sortedParams.entrySet()) {
            if (entry.getValue() != null) {
                sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
        }
        sb.append("key=").append(SIGN_KEY);
        
        // MD5加密
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] messageDigest = md.digest(sb.toString().getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : messageDigest) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
    
    public static void main(String[] args) throws Exception {
        Map<String, Object> params = new TreeMap<>();
        params.put("openId", "oABCD1234567890");
        params.put("gender", "男");
        params.put("year", 1984);
        params.put("month", 11);
        params.put("day", 27);
        params.put("hour", 0);
        
        long timestamp = System.currentTimeMillis();
        String sign = generateSignature(params, timestamp);
        
        System.out.println("Timestamp: " + timestamp);
        System.out.println("Signature: " + sign);
    }
}
```

### 4. JavaScript示例代码（微信小程序）

```javascript
const crypto = require('crypto');

function generateSignature(params, timestamp) {
  const SIGN_KEY = 'szbz-api-sign-key-2024';
  
  // 添加时间戳
  params.timestamp = timestamp;
  
  // 按key排序
  const sortedKeys = Object.keys(params).sort();
  
  // 拼接参数
  let str = '';
  sortedKeys.forEach(key => {
    if (params[key] !== null && params[key] !== undefined) {
      str += `${key}=${params[key]}&`;
    }
  });
  str += `key=${SIGN_KEY}`;
  
  // MD5加密
  return crypto.createHash('md5').update(str).digest('hex');
}

// 使用示例
const params = {
  openId: 'oABCD1234567890',
  gender: '男',
  year: 1984,
  month: 11,
  day: 27,
  hour: 0
};

const timestamp = Date.now();
const sign = generateSignature(params, timestamp);

console.log('Timestamp:', timestamp);
console.log('Signature:', sign);

// 发起请求
wx.request({
  url: 'http://localhost:8080/api/bazi/analyze',
  method: 'POST',
  header: {
    'Content-Type': 'application/json',
    'X-Timestamp': timestamp.toString(),
    'X-Sign': sign
  },
  data: params,
  success: (res) => {
    console.log('Response:', res.data);
    if (res.data.code === 200) {
      const token = res.data.token;
      const analysis = res.data.data.aiAnalysis;
      // 保存token供后续使用
      wx.setStorageSync('jwt_token', token);
    }
  }
});
```

## 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未授权/Token无效 |
| 500 | 服务器内部错误 |

## 常见错误

1. **"openId不能为空"**
   - 确保请求体中包含openId字段

2. **"缺少时间戳参数"**
   - 确保请求头中包含X-Timestamp

3. **"请求已过期，时间戳超过2秒"**
   - 客户端和服务器时间差超过2秒
   - 检查客户端系统时间是否准确
   - 确保及时发送请求

4. **"签名验证失败，参数可能被篡改"**
   - 检查签名生成算法是否正确
   - 确保参数值与签名计算时一致
   - 检查密钥是否匹配

## 缓存机制

- **缓存Key格式**: `bazi:openId:gender:year:month:day:hour`
- **过期时间**: 3天（259200秒）
- **缓存命中**: 控制台输出 "缓存命中: xxx"
- **缓存写入**: 控制台输出 "结果已缓存: xxx"

## Redis配置

确保Redis已启动并配置正确（参见application.properties）：
```properties
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.password=
spring.data.redis.database=0
```

## JWT配置

JWT令牌有效期为24小时，可在配置文件中修改：
```properties
jwt.secret=szbz-api-secret-key-for-wechat-miniprogram-authentication-2024
jwt.expiration=86400000
```

## 测试建议

1. **启动Redis服务**
   ```bash
   # Windows
   redis-server.exe
   
   # Linux/Mac
   redis-server
   ```

2. **启动Spring Boot应用**
   ```bash
   mvn spring-boot:run
   ```

3. **使用Postman测试**
   - 设置Headers
   - 准备请求Body
   - 计算签名
   - 发送请求

4. **查看日志**
   - 检查缓存命中情况
   - 查看AI分析提示词
   - 确认签名验证过程

## 安全建议

1. **生产环境**必须修改以下密钥：
   - `jwt.secret`
   - `SIGN_KEY`（SignatureUtil.java中）

2. **Redis密码**应在生产环境中设置

3. **HTTPS**生产环境应使用HTTPS协议

4. **OpenId管理**确保OpenId来自可信的微信小程序登录接口
