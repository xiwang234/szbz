# 微信小程序登录集成 - 迁移文档

## 📅 迁移日期
2025-12-19

## 🎯 迁移目标
将原有的 `openId` 直接传递方式改为**微信小程序标准登录流程**，通过 `code` 换取 `openId`。

---

## 📋 核心变更

### 1. 数据模型变更

#### `BaZiRequest.java` ✅
```java
// 修改前
private String openId;

// 修改后
private String code;  // 微信小程序登录凭证
```

**影响范围**：
- 所有使用 `BaZiRequest.openId` 的代码
- 所有测试用例
- API 文档和示例

---

### 2. 新增服务类

#### `WeChatConfig.java` ✅ （新增）
```java
@Configuration
@ConfigurationProperties(prefix = "wechat.miniapp")
public class WeChatConfig {
    private String appId;
    private String appSecret;
    // ... getters/setters
}
```

**配置文件**：`application.properties`
```properties
wechat.miniapp.app-id=your_wechat_appid_here
wechat.miniapp.app-secret=your_wechat_secret_here
```

**⚠️ 重要**：上线前必须配置真实的微信小程序 AppID 和 AppSecret！

---

#### `WeChatSessionResponse.java` ✅ （新增）
```java
public class WeChatSessionResponse {
    private String openId;
    private String sessionKey;
    private String unionId;
    private Integer errCode;
    private String errMsg;
    // ... 
}
```

**用途**：封装微信官方 `code2Session` 接口的响应数据。

---

#### `WeChatService.java` ✅ （新增）
```java
@Service
public class WeChatService {
    /**
     * 调用微信官方接口，通过 code 换取 openId
     */
    public String getOpenId(String code) {
        // 调用: https://api.weixin.qq.com/sns/jscode2session
        // 返回: openId, session_key, unionid
    }
}
```

**API文档**：[微信官方 code2Session](https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/user-login/code2Session.html)

---

### 3. Controller 逻辑变更

#### `BaZiController.analyzeBaZiWithAI()` ✅

**修改前流程**：
```
1. 验证 openId 参数
2. 验证时间戳
3. 验证签名
4. 检查缓存
5. 执行业务逻辑
```

**修改后流程**：
```
1. 验证 code 参数              ← 新增
2. 调用微信接口换取 openId      ← 新增
3. 验证时间戳
4. 验证签名
5. 检查缓存（使用 openId 作为 Key）
6. 执行业务逻辑
```

**关键代码**：
```java
// Step 1: 验证 code
if (request.getCode() == null || request.getCode().isEmpty()) {
    return ResponseEntity.ok(ApiResponse.error(400, "code不能为空"));
}

// Step 2: 调用微信接口
try {
    openId = weChatService.getOpenId(request.getCode());
    System.out.println("微信登录成功，OpenId: " + openId);
} catch (Exception e) {
    return ResponseEntity.ok(ApiResponse.error(401, "微信登录失败: " + e.getMessage()));
}

// Step 5: 使用 openId 生成缓存 Key
String cacheKey = generateCacheKey(openId, request);
```

---

### 4. 测试用例修复

#### `SecurityIntegrationTest.java` ✅

**修复内容**：

1. **testSignatureGeneration()** - 将 `openId` 改为 `code`
2. **testCompleteRequestFlow()** - 使用 `setCode()` 而非 `setOpenId()`
3. **testCurlCommandGeneration()** - 更新 CURL 命令参数
4. **testPostmanRequestExample()** - 更新 Postman 示例

**示例修改**：
```java
// 修改前
params.put("openId", "oABCD1234567890");
request.setOpenId("oABCD1234567890");

// 修改后
params.put("code", "081nBp0w3MqiWf27BQ2w3UWgRg1nBp0P");
request.setCode("081nBp0w3MqiWf27BQ2w3UWgRg1nBp0P");
```

---

## 📡 API 请求格式变化

### 修改前
```json
POST /api/bazi/analyze
Headers:
  X-Timestamp: 1702800000000
  X-Sign: abc123...

Body:
{
  "openId": "oABCD1234567890",
  "gender": "男",
  "year": 1984,
  "month": 11,
  "day": 27,
  "hour": 0
}
```

### 修改后
```json
POST /api/bazi/analyze
Headers:
  X-Timestamp: 1702800000000
  X-Sign: abc123...

Body:
{
  "code": "081nBp0w3MqiWf27BQ2w3UWgRg1nBp0P",  ← 修改点
  "gender": "男",
  "year": 1984,
  "month": 11,
  "day": 27,
  "hour": 0
}
```

**⚠️ 注意**：
- `code` 需要从小程序端通过 `wx.login()` 获取
- `code` 有效期仅 **5分钟**
- `code` 使用后**立即失效**（一次性）

---

## 🔐 微信小程序端集成

### 小程序登录流程
```javascript
// 1. 获取登录凭证
wx.login({
  success: (res) => {
    if (res.code) {
      const code = res.code;  // 这就是要传递给后端的 code
      
      // 2. 构建请求参数
      const params = {
        code: code,
        gender: "男",
        year: 1984,
        month: 11,
        day: 27,
        hour: 0
      };
      
      // 3. 生成时间戳和签名
      const timestamp = Date.now();
      const sign = generateSignature(params, timestamp);
      
      // 4. 调用后端接口
      wx.request({
        url: 'https://your-domain.com/api/bazi/analyze',
        method: 'POST',
        header: {
          'Content-Type': 'application/json',
          'X-Timestamp': timestamp,
          'X-Sign': sign
        },
        data: params,
        success: (response) => {
          console.log('OpenId:', response.data.openId);
          console.log('Token:', response.data.token);
        }
      });
    }
  }
});
```

---

## 🧪 测试验证

### 1. 编译测试
```bash
cd D:\project\szbz
mvn clean compile
```
**结果**：✅ BUILD SUCCESS

### 2. 测试编译
```bash
mvn test-compile
```
**结果**：✅ BUILD SUCCESS

### 3. 单元测试
```bash
mvn test -Dtest=SecurityIntegrationTest
```

### 4. 集成测试（需要真实 AppID）
```bash
# 1. 配置 application.properties
wechat.miniapp.app-id=wx1234567890abcdef
wechat.miniapp.app-secret=1234567890abcdef1234567890abcdef

# 2. 启动服务
mvn spring-boot:run

# 3. 从小程序获取真实 code
# 4. 使用 test-api.ps1 脚本测试
```

---

## ⚠️ 注意事项

### 1. 微信 code 的特性
- **有效期**：5分钟
- **使用次数**：一次性（使用后失效）
- **获取方式**：小程序端 `wx.login()`
- **作用**：换取 `openId` 和 `session_key`

### 2. 开发环境测试
由于 `code` 的特殊性，本地测试需要：
- 真实的小程序 AppID 和 AppSecret
- 从小程序端获取真实的 `code`
- 或者 Mock `WeChatService` 进行单元测试

### 3. Mock 测试建议
```java
@MockBean
private WeChatService weChatService;

@Test
public void testWithMockWeChatService() {
    // Mock 微信接口返回
    when(weChatService.getOpenId(anyString()))
        .thenReturn("oABCD1234567890");
    
    // 执行测试
    // ...
}
```

---

## 📊 迁移影响范围

| 组件 | 修改类型 | 状态 |
|------|---------|------|
| `BaZiRequest.java` | 属性修改 | ✅ 已完成 |
| `BaZiController.java` | 逻辑增强 | ✅ 已完成 |
| `WeChatConfig.java` | 新增 | ✅ 已完成 |
| `WeChatService.java` | 新增 | ✅ 已完成 |
| `WeChatSessionResponse.java` | 新增 | ✅ 已完成 |
| `SecurityIntegrationTest.java` | 测试更新 | ✅ 已完成 |
| `application.properties` | 配置新增 | ✅ 已完成 |
| 小程序前端代码 | 参数调整 | ⚠️ 待更新 |
| API 文档 | 文档更新 | ⚠️ 待更新 |

---

## 🚀 部署检查清单

- [ ] 配置真实的微信小程序 AppID
- [ ] 配置真实的微信小程序 AppSecret
- [ ] 验证微信接口可访问性
- [ ] 更新小程序前端代码
- [ ] 更新 API 接口文档
- [ ] 清空旧的 Redis/Caffeine 缓存数据
- [ ] 执行完整的集成测试
- [ ] 验证签名算法兼容性
- [ ] 监控微信接口调用频率（避免触发限流）

---

## 📚 相关文档

- [微信小程序登录流程](https://developers.weixin.qq.com/miniprogram/dev/framework/open-ability/login.html)
- [code2Session API](https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/user-login/code2Session.html)
- [微信小程序开发者平台](https://mp.weixin.qq.com/)

---

## 🔄 回滚方案

如需回滚到 `openId` 直传方式：

```bash
git checkout HEAD~1 -- src/main/java/xw/szbz/cn/model/BaZiRequest.java
git checkout HEAD~1 -- src/main/java/xw/szbz/cn/controller/BaZiController.java
git checkout HEAD~1 -- src/test/java/xw/szbz/cn/SecurityIntegrationTest.java

# 删除新增文件
rm src/main/java/xw/szbz/cn/config/WeChatConfig.java
rm src/main/java/xw/szbz/cn/service/WeChatService.java
rm src/main/java/xw/szbz/cn/model/WeChatSessionResponse.java
```

---

**迁移完成！** ✅

所有代码已更新并通过编译测试。建议在真实小程序环境中进行完整的端到端测试。
