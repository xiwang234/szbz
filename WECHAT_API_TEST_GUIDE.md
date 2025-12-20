# 微信小程序API真实调用测试指南

## 概述

`WeChatServiceTest.testCode2Session_Success` 测试方法已配置为**真实调用微信API**，不使用Mock数据。这允许您验证微信小程序登录功能是否正常工作。

## 快速开始

### 方法一：使用自动化脚本（推荐）

```powershell
# 运行脚本，按提示输入code
.\test-wechat-real-api.ps1

# 或直接传入code
.\test-wechat-real-api.ps1 "081nBp0w3MqiWf27BQ2w3UWgRg1nBp0P"
```

### 方法二：手动修改测试代码

1. **获取微信小程序code**

   打开微信开发者工具，在控制台输入：
   ```javascript
   wx.login({ 
     success: res => console.log('Code:', res.code) 
   })
   ```

2. **修改测试代码**

   编辑 `src/test/java/xw/szbz/cn/service/WeChatServiceTest.java`，找到：
   ```java
   String testCode = "请替换为真实的微信小程序code";
   ```
   
   替换为真实code：
   ```java
   String testCode = "081nBp0w3MqiWf27BQ2w3UWgRg1nBp0P";
   ```

3. **立即运行测试**

   ```bash
   mvn test -Dtest=WeChatServiceTest#testCode2Session_Success
   ```

## 重要提示

### ⚠️ Code使用限制

1. **有效期**：每个code有效期为**5分钟**
2. **一次性**：每个code**只能使用一次**
3. **时效性**：获取code后需要**立即测试**

### ⚠️ 常见错误

| 错误码 | 错误信息 | 原因 | 解决方案 |
|--------|----------|------|----------|
| -1 | system error | 系统错误 | 检查网络连接 |
| 40013 | invalid appid | AppID错误 | 检查`application.properties`配置 |
| 40029 | invalid code | code无效 | 获取新的code |
| 40163 | code been used | code已使用 | 获取新的code |

## 测试流程

### 成功的测试输出

```
=== 测试真实微信API调用 ===
正在调用微信API...
AppID: wx4725394b76c26ee2
Code: 081nBp0w3MqiWf27BQ2w3UWgRg1nBp0P

✅ 微信API调用成功！
OpenID: oABCD1234567890
SessionKey: HyVFkGl5F5OQWJZZaNzBBg==

Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### 失败的测试输出

```
❌ 微信API调用失败：微信登录失败: errcode=40163, errmsg=code been used

可能的原因：
1. code已过期（5分钟有效期）
2. code已被使用过（每个code只能使用一次）
3. AppID或AppSecret配置错误
4. 网络连接问题

请获取新的code后重试
```

## 配置检查

测试使用的配置来自 `application.properties`：

```properties
wechat.miniapp.app-id=wx4725394b76c26ee2
wechat.miniapp.app-secret=eb695e6713208cec6925e9248cbf295a
```

确保这些配置与您的微信小程序一致。

## 测试方法说明

### testCode2Session_Success()

- **功能**：测试完整的code2Session响应
- **返回数据**：openId, sessionKey, unionId（可选）
- **验证内容**：
  - 响应不为null
  - 调用成功标志
  - openId存在且有效
  - sessionKey存在且有效

### testGetOpenId_Success()

- **功能**：测试简化的getOpenId方法
- **返回数据**：仅返回openId
- **验证内容**：
  - openId不为null
  - openId格式正确（通常以'o'开头）

## 调试建议

### 1. 启用详细日志

在测试前查看完整的HTTP请求/响应：

```java
// 可以在WeChatService中添加日志
System.out.println("Request URL: " + CODE_2_SESSION_URL);
System.out.println("Response: " + response);
```

### 2. 验证网络连接

```bash
# 测试是否能访问微信API
curl "https://api.weixin.qq.com/sns/jscode2session?appid=TEST&secret=TEST&js_code=TEST&grant_type=authorization_code"
```

### 3. 检查配置

```bash
# 查看当前配置
cat src/main/resources/application.properties | grep wechat
```

## 从Mock测试切换回来

如果需要切换回Mock测试（不调用真实API），可以参考之前的Mock版本：

```java
@Mock
private RestTemplate restTemplate;

@Test
public void testCode2Session_Success() throws Exception {
    String testCode = "test_code";
    String mockResponse = "{\"openid\":\"oABCD1234567890\",\"session_key\":\"HyVFkGl5F5OQWJZZaNzBBg==\"}";
    
    when(restTemplate.getForObject(anyString(), eq(String.class), anyString(), anyString(), anyString()))
            .thenReturn(mockResponse);
    
    WeChatSessionResponse response = weChatService.code2Session(testCode);
    // 验证...
}
```

## 相关文档

- [微信小程序登录API文档](https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/user-login/code2Session.html)
- [微信错误码说明](https://developers.weixin.qq.com/miniprogram/dev/framework/usability/PublicErrno.html)
- 项目文档：`WECHAT_LOGIN_MIGRATION.md`

## 故障排除

### 问题：code总是提示已使用

**原因**：code被多次使用或缓存了旧code

**解决**：
1. 确保每次测试都获取新的code
2. 清除微信开发者工具缓存
3. 重新登录小程序

### 问题：AppID无效

**原因**：配置文件中的AppID与小程序不匹配

**解决**：
1. 登录[微信公众平台](https://mp.weixin.qq.com/)
2. 进入"开发" -> "开发管理" -> "开发设置"
3. 复制正确的AppID和AppSecret
4. 更新`application.properties`

### 问题：网络超时

**原因**：无法访问微信API服务器

**解决**：
1. 检查网络连接
2. 确认防火墙设置
3. 尝试使用VPN（如果在特殊网络环境下）

---

**最后更新时间**：2025-12-20
