# API测试指南

## 🌐 API端点列表

### 基础URL
```
http://localhost:8080/api/web-auth
```

---

## 📝 API接口详情

### 1. 用户注册
**POST** `/api/web-auth/register`

**请求体：**
```json
{
  "username": "testuser",
  "email": "test@example.com",
  "password": "Password123!"
}
```

**成功响应（200）：**
```json
{
  "code": 200,
  "message": "注册成功，请验证邮箱",
  "data": null,
  "timestamp": 1704067200000
}
```

**失败响应（400）：**
```json
{
  "code": 400,
  "message": "用户名已存在",
  "data": null,
  "timestamp": 1704067200000
}
```

**Curl命令：**
```bash
curl -X POST http://localhost:8080/api/web-auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "Password123!"
  }'
```

---

### 2. 用户登录
**POST** `/api/web-auth/login`

**请求体：**
```json
{
  "email": "test@example.com",
  "password": "Password123!",
  "deviceId": "device_123"
}
```

**成功响应（200）：**
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "accessTokenExpiresAt": 1704070800000,
    "refreshTokenExpiresAt": 1704672000000,
    "encryptedUserId": "u1a2b3c4d5e6f",
    "username": "testuser",
    "maskedEmail": "t***@example.com"
  },
  "timestamp": 1704067200000
}
```

**失败响应（401）：**
```json
{
  "code": 401,
  "message": "邮箱或密码错误",
  "data": null,
  "timestamp": 1704067200000
}
```

**Curl命令：**
```bash
curl -X POST http://localhost:8080/api/web-auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Password123!",
    "deviceId": "device_123"
  }'
```

---

### 3. 刷新Token
**POST** `/api/web-auth/refresh`

**请求体：**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "deviceId": "device_123"
}
```

**成功响应（200）：**
```json
{
  "code": 200,
  "message": "Token刷新成功",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "accessTokenExpiresAt": 1704070800000,
    "refreshTokenExpiresAt": 1704672000000,
    "encryptedUserId": "u1a2b3c4d5e6f",
    "username": "testuser",
    "maskedEmail": "t***@example.com"
  },
  "timestamp": 1704067200000
}
```

**Curl命令：**
```bash
curl -X POST http://localhost:8080/api/web-auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "YOUR_REFRESH_TOKEN",
    "deviceId": "device_123"
  }'
```

---

### 4. 获取当前用户信息
**GET** `/api/web-auth/me`

**请求头：**
```
Authorization: Bearer <access_token>
```

**成功响应（200）：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "encryptedUserId": "u1a2b3c4d5e6f",
    "username": "testuser",
    "maskedEmail": "t***@example.com",
    "emailVerified": true,
    "active": true,
    "createTime": 1704067200000,
    "lastLoginTime": 1704067200000
  },
  "timestamp": 1704067200000
}
```

**Curl命令：**
```bash
curl -X GET http://localhost:8080/api/web-auth/me \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

---

### 5. 登出
**POST** `/api/web-auth/logout`

**请求头：**
```
Authorization: Bearer <access_token>
```

**请求体（可选）：**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**成功响应（200）：**
```json
{
  "code": 200,
  "message": "登出成功",
  "data": null,
  "timestamp": 1704067200000
}
```

**Curl命令：**
```bash
curl -X POST http://localhost:8080/api/web-auth/logout \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "YOUR_REFRESH_TOKEN"
  }'
```

---

### 6. 请求密码重置
**POST** `/api/web-auth/request-reset`

**请求体：**
```json
{
  "email": "test@example.com"
}
```

**成功响应（200）：**
```json
{
  "code": 200,
  "message": "如果该邮箱存在，我们已发送重置链接",
  "data": null,
  "timestamp": 1704067200000
}
```

**Curl命令：**
```bash
curl -X POST http://localhost:8080/api/web-auth/request-reset \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com"
  }'
```

---

### 7. 重置密码
**POST** `/api/web-auth/reset-password`

**请求体：**
```json
{
  "token": "reset-token-uuid",
  "newPassword": "NewPassword123!"
}
```

**成功响应（200）：**
```json
{
  "code": 200,
  "message": "密码重置成功",
  "data": null,
  "timestamp": 1704067200000
}
```

**Curl命令：**
```bash
curl -X POST http://localhost:8080/api/web-auth/reset-password \
  -H "Content-Type: application/json" \
  -d '{
    "token": "RESET_TOKEN",
    "newPassword": "NewPassword123!"
  }'
```

---

### 8. 验证邮箱
**POST** `/api/web-auth/verify-email?token=<verification_token>`

**成功响应（200）：**
```json
{
  "code": 200,
  "message": "邮箱验证成功",
  "data": null,
  "timestamp": 1704067200000
}
```

**Curl命令：**
```bash
curl -X POST "http://localhost:8080/api/web-auth/verify-email?token=VERIFICATION_TOKEN"
```

---

## 🧪 完整测试流程

### 使用Postman/Curl进行完整流程测试

#### 1. 注册新用户
```bash
curl -X POST http://localhost:8080/api/web-auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "apitest",
    "email": "apitest@example.com",
    "password": "ApiTest123!"
  }'
```

保存响应的`timestamp`。

#### 2. 登录获取Token
```bash
curl -X POST http://localhost:8080/api/web-auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "apitest@example.com",
    "password": "ApiTest123!",
    "deviceId": "test_device_001"
  }'
```

保存响应中的：
- `accessToken` - 后续API调用需要
- `refreshToken` - 刷新Token时需要
- `encryptedUserId` - 用户唯一标识

#### 3. 使用Access Token访问受保护接口
```bash
# 替换YOUR_ACCESS_TOKEN为实际的token
curl -X GET http://localhost:8080/api/web-auth/me \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

应该返回用户信息，包括脱敏的邮箱。

#### 4. 刷新Token
```bash
# 替换YOUR_REFRESH_TOKEN为实际的refresh token
curl -X POST http://localhost:8080/api/web-auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "YOUR_REFRESH_TOKEN",
    "deviceId": "test_device_001"
  }'
```

应该返回新的Access Token和Refresh Token。

#### 5. 登出
```bash
curl -X POST http://localhost:8080/api/web-auth/logout \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "YOUR_REFRESH_TOKEN"
  }'
```

#### 6. 验证登出后Token失效
```bash
# 使用旧的Access Token
curl -X GET http://localhost:8080/api/web-auth/me \
  -H "Authorization: Bearer OLD_ACCESS_TOKEN"
```

应该返回401错误或Token失效提示。

---

## 🔍 测试检查点

### ✅ 注册测试
- [ ] 成功注册新用户
- [ ] 重复用户名被拒绝
- [ ] 重复邮箱被拒绝
- [ ] 弱密码被拒绝（少于8位）
- [ ] 无效邮箱格式被拒绝

### ✅ 登录测试
- [ ] 正确邮箱密码登录成功
- [ ] 错误密码被拒绝
- [ ] 不存在的邮箱被拒绝
- [ ] 返回的accessToken有效
- [ ] 返回的refreshToken有效
- [ ] encryptedUserId格式正确（13位，u开头）
- [ ] 邮箱已脱敏（包含***）

### ✅ Token验证测试
- [ ] 有效的Access Token可以访问/me接口
- [ ] 无效的Token被拒绝
- [ ] 过期的Token被拒绝
- [ ] Refresh Token不能用于访问/me接口

### ✅ 刷新Token测试
- [ ] 有效的Refresh Token可以刷新
- [ ] 返回新的Token对
- [ ] 旧的Refresh Token失效
- [ ] 设备ID不匹配被拒绝

### ✅ 登出测试
- [ ] 登出成功
- [ ] 登出后Token加入黑名单
- [ ] 黑名单中的Token无法使用

### ✅ 密码重置测试
- [ ] 发送重置邮件（模拟）
- [ ] 使用重置Token更新密码
- [ ] 过期的重置Token被拒绝

### ✅ 安全性测试
- [ ] 用户ID加密格式验证（13位）
- [ ] JWT Payload不包含email
- [ ] 邮箱在数据库中加密存储
- [ ] 前端只能看到脱敏邮箱
- [ ] 密码BCrypt哈希存储

---

## 📊 性能测试

### 使用Apache Bench进行压力测试

#### 登录接口性能测试
```bash
# 创建测试数据文件 login.json
echo '{"email":"apitest@example.com","password":"ApiTest123!","deviceId":"test_device"}' > login.json

# 100并发，1000次请求
ab -n 1000 -c 100 -p login.json -T application/json \
  http://localhost:8080/api/web-auth/login
```

**预期性能：**
- 吞吐量：> 500 req/s
- 平均响应时间：< 200ms
- 95%响应时间：< 500ms

#### Token验证接口性能测试
```bash
# 先获取一个有效Token
TOKEN="YOUR_ACCESS_TOKEN"

# 100并发，1000次请求
ab -n 1000 -c 100 \
  -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/web-auth/me
```

**预期性能：**
- 吞吐量：> 1000 req/s
- 平均响应时间：< 100ms
- 95%响应时间：< 200ms

---

## 🐛 常见问题

### Q1: 401 Unauthorized错误
**原因：** Token无效、过期或格式错误

**检查：**
```bash
# 验证Token格式
echo "YOUR_TOKEN" | cut -d'.' -f1 | base64 -d

# 应该是有效的JWT header
```

### Q2: 设备ID不匹配
**原因：** 刷新Token时使用了不同的deviceId

**解决：** 确保使用相同的deviceId

### Q3: 邮箱已加密，无法查询
**原因：** 邮箱在数据库中是加密存储的

**解决：** 使用AuthService的方法查询，不要直接查数据库

### Q4: 跨域问题（CORS）
**原因：** Spring Security默认阻止跨域请求

**解决：** 添加CORS配置（需要在SecurityConfig中配置）

---

## 📈 监控和日志

### 查看应用日志
```bash
tail -f logs/application.log
```

### 查看业务日志
```bash
tail -f syslog/business_$(date +%Y%m%d).log
```

### 关键日志查找
```bash
# 查找登录日志
grep "登录成功" logs/application.log

# 查找Token刷新日志
grep "Token刷新" logs/application.log

# 查找错误日志
grep "ERROR" logs/application.log
```

---

## ✅ 验收标准

所有测试点通过后，系统应该满足：

1. ✅ 用户可以成功注册
2. ✅ 用户可以成功登录并获取Token
3. ✅ Access Token可以访问受保护接口
4. ✅ Token过期后可以使用Refresh Token刷新
5. ✅ 用户可以成功登出
6. ✅ 邮箱在数据库中加密存储
7. ✅ 用户ID使用13位加密ID传输
8. ✅ 前端只能看到脱敏邮箱
9. ✅ JWT Token不包含email敏感信息
10. ✅ 所有接口响应时间 < 500ms
