# Web应用认证系统实现文档

## 📋 概述

本文档说明了按照plan实现的Web应用认证系统，包括用户注册、登录、JWT Token管理、数据加密脱敏、Stripe支付集成等功能。

## ✅ 已实现功能清单

### 1. 核心实体和Repository

#### ✅ WebUser实体类
- 路径：`src/main/java/xw/szbz/cn/entity/WebUser.java`
- 功能：Web应用用户实体，支持邮箱密码登录
- 特性：
  - 邮箱加密存储（AES-256-GCM）
  - 密码BCrypt哈希存储
  - 邮箱验证状态管理
  - 密码重置令牌管理
  - 账户状态控制

#### ✅ WebUserRepository
- 路径：`src/main/java/xw/szbz/cn/repository/WebUserRepository.java`
- 功能：Web用户数据访问接口

#### ✅ PaymentRecord实体类
- 路径：`src/main/java/xw/szbz/cn/entity/PaymentRecord.java`
- 功能：Stripe支付记录实体

#### ✅ PaymentRecordRepository
- 路径：`src/main/java/xw/szbz/cn/repository/PaymentRecordRepository.java`
- 功能：支付记录数据访问接口

---

### 2. 加密和安全工具类

#### ✅ EnhancedUserIdEncryption（用户ID加密）
- 路径：`src/main/java/xw/szbz/cn/util/EnhancedUserIdEncryption.java`
- 功能：生成13位加密用户ID
- 算法：AES-256-GCM + Base58编码 + Luhn校验
- 格式：`u + 11位Base58 + 1位校验位`
- 安全强度：58^11 ≈ 5.08×10^19 组合

**使用示例：**
```java
@Autowired
private EnhancedUserIdEncryption userIdEncryption;

// 加密
String encryptedId = userIdEncryption.encryptUserId(userId, createdAt);
// 结果：u1a2b3c4d5e6f

// 解密
Long userId = userIdEncryption.decryptUserId("u1a2b3c4d5e6f");
```

#### ✅ FieldEncryptionUtil（字段加密）
- 路径：`src/main/java/xw/szbz/cn/util/FieldEncryptionUtil.java`
- 功能：敏感字段加密解密（邮箱、手机号等）
- 算法：AES-256-GCM
- 输出：Base64编码

**使用示例：**
```java
@Autowired
private FieldEncryptionUtil fieldEncryptionUtil;

// 加密邮箱
String encrypted = fieldEncryptionUtil.encryptEmail("user@example.com");
// 解密邮箱
String decrypted = fieldEncryptionUtil.decryptEmail(encrypted);
```

#### ✅ DataMaskingService（数据脱敏）
- 路径：`src/main/java/xw/szbz/cn/service/DataMaskingService.java`
- 功能：敏感信息脱敏展示
- 支持：邮箱、手机号、身份证、姓名、银行卡号

**使用示例：**
```java
@Autowired
private DataMaskingService maskingService;

// 邮箱脱敏：user@example.com -> u***@example.com
String masked = maskingService.maskEmail("user@example.com");

// 手机号脱敏：13812345678 -> 138****5678
String masked = maskingService.maskPhone("13812345678");
```

#### ✅ EnhancedJwtUtil（增强JWT工具）
- 路径：`src/main/java/xw/szbz/cn/util/EnhancedJwtUtil.java`
- 功能：生成和验证Access Token和Refresh Token
- 特性：
  - Access Token：1小时有效期
  - Refresh Token：7天有效期
  - 支持设备绑定、IP验证、会话管理
  - JWT Payload不包含email（安全加固）

**使用示例：**
```java
@Autowired
private EnhancedJwtUtil jwtUtil;

// 生成Access Token
String accessToken = jwtUtil.generateAccessToken(
    encryptedUserId, username, emailVerified, 
    sessionId, deviceId, ipAddress
);

// 生成Refresh Token
String refreshToken = jwtUtil.generateRefreshToken(
    encryptedUserId, sessionId, deviceId
);

// 验证Token
boolean valid = jwtUtil.validateToken(token);

// 从Token提取信息
String userId = jwtUtil.getEncryptedUserIdFromToken(token);
String username = jwtUtil.getUsernameFromToken(token);
```

---

### 3. 认证服务

#### ✅ AuthService（认证服务）
- 路径：`src/main/java/xw/szbz/cn/service/AuthService.java`
- 功能：完整的认证服务实现

**核心方法：**

##### 1. 用户注册
```java
public void register(RegisterRequest request, String ipAddress)
```
- 验证用户名和邮箱唯一性
- 加密存储邮箱
- BCrypt哈希密码
- 生成邮箱验证令牌

##### 2. 用户登录
```java
public AuthResponse login(WebLoginRequest request, String ipAddress, String userAgent)
```
- 验证邮箱和密码
- 生成Access Token和Refresh Token
- 更新最后登录信息
- 返回脱敏邮箱

##### 3. 刷新Token
```java
public AuthResponse refreshToken(String refreshToken, String deviceId, String ipAddress)
```
- 验证Refresh Token
- 检查设备ID匹配
- 生成新的Token对
- 将旧Token加入黑名单

##### 4. 登出
```java
public void logout(String accessToken, String refreshToken)
```
- 将Token加入黑名单
- 使Token立即失效

##### 5. 密码重置
```java
public void requestPasswordReset(String email)
public void resetPassword(String resetToken, String newPassword)
```

##### 6. 邮箱验证
```java
public void verifyEmail(String verificationToken)
```

---

### 4. 支付服务

#### ✅ StripePaymentService（Stripe支付）
- 路径：`src/main/java/xw/szbz/cn/service/StripePaymentService.java`
- 功能：Stripe Checkout集成和Webhook处理

**核心方法：**

```java
// 创建支付会话
public String createCheckoutSession(Long userId, String productName, 
                                   long amount, String currency)

// 处理Webhook回调
public void handleWebhook(String payload, String signatureHeader)

// 处理退款
public void processRefund(String sessionId)

// 获取支付记录
public PaymentRecord getPaymentBySessionId(String sessionId)
```

---

### 5. 请求/响应Model

#### ✅ 已创建的Model类
- `RegisterRequest.java` - 注册请求
- `WebLoginRequest.java` - 登录请求
- `AuthResponse.java` - 认证响应
- `RefreshTokenRequest.java` - 刷新Token请求

---

### 6. 测试类

#### ✅ 单元测试
- `EnhancedUserIdEncryptionTest.java` - 用户ID加密测试
- `FieldEncryptionUtilTest.java` - 字段加密测试
- `DataMaskingServiceTest.java` - 数据脱敏测试
- `EnhancedJwtUtilTest.java` - JWT工具测试
- `AuthServiceTest.java` - 认证服务测试（Mock）

#### ✅ 集成测试
- `WebAuthIntegrationTest.java` - 完整认证流程集成测试

**测试覆盖：**
- ✅ 用户注册流程
- ✅ 用户登录流程
- ✅ Token刷新流程
- ✅ Token验证和解析
- ✅ 用户ID加密解密
- ✅ 邮箱加密解密
- ✅ 数据脱敏
- ✅ 设备绑定验证
- ✅ IP验证

---

## 🔧 配置说明

### application.properties配置

```properties
# Enhanced JWT Configuration
jwt.access-token.expiration=3600000      # Access Token: 1小时
jwt.refresh-token.expiration=604800000   # Refresh Token: 7天

# User ID Encryption (AES-256-GCM)
user.id.encryption.key=${USER_ID_ENCRYPTION_KEY:your-encryption-key-32-chars-256bits}

# Email Field Encryption (AES-256-GCM)
field.encryption.key=${FIELD_ENCRYPTION_KEY:your-field-encryption-key-32-chars}

# Stripe Configuration
stripe.api.secret-key=${STRIPE_SECRET_KEY:}
stripe.api.publishable-key=${STRIPE_PUBLISHABLE_KEY:}
stripe.webhook.secret=${STRIPE_WEBHOOK_SECRET:}

# Email Configuration (Optional)
spring.mail.host=${MAIL_HOST:smtp.gmail.com}
spring.mail.port=${MAIL_PORT:587}
spring.mail.username=${MAIL_USERNAME:}
spring.mail.password=${MAIL_PASSWORD:}
```

### 环境变量设置

**生产环境必须设置以下环境变量：**

```bash
# 生成32字节密钥
export USER_ID_ENCRYPTION_KEY=$(openssl rand -base64 32)
export FIELD_ENCRYPTION_KEY=$(openssl rand -base64 32)

# Stripe密钥
export STRIPE_SECRET_KEY=sk_live_xxxxx
export STRIPE_PUBLISHABLE_KEY=pk_live_xxxxx
export STRIPE_WEBHOOK_SECRET=whsec_xxxxx

# 邮件配置
export MAIL_USERNAME=your-email@gmail.com
export MAIL_PASSWORD=your-app-password
```

---

## 🧪 运行测试

### 运行所有测试
```bash
mvn test
```

### 运行特定测试类
```bash
# 用户ID加密测试
mvn test -Dtest=EnhancedUserIdEncryptionTest

# 字段加密测试
mvn test -Dtest=FieldEncryptionUtilTest

# 数据脱敏测试
mvn test -Dtest=DataMaskingServiceTest

# JWT工具测试
mvn test -Dtest=EnhancedJwtUtilTest

# 认证服务测试
mvn test -Dtest=AuthServiceTest

# 集成测试
mvn test -Dtest=WebAuthIntegrationTest
```

### 查看测试输出
测试类会输出详细的测试结果，包括：
- 加密/解密的原文和密文
- Token的生成和验证
- 数据脱敏的前后对比
- 完整认证流程的每个步骤

---

## 📊 数据库Schema

### web_user表（SQLite自动创建）
```sql
CREATE TABLE web_user (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(500) NOT NULL UNIQUE,  -- 加密存储
    password_hash VARCHAR(100) NOT NULL,
    email_verified BOOLEAN NOT NULL DEFAULT 0,
    email_verification_token VARCHAR(100),
    email_verification_expiry DATETIME,
    password_reset_token VARCHAR(100),
    password_reset_expiry DATETIME,
    active BOOLEAN NOT NULL DEFAULT 1,
    create_time BIGINT NOT NULL,
    last_login_time BIGINT,
    last_login_ip VARCHAR(50)
);
```

### payment_record表（SQLite自动创建）
```sql
CREATE TABLE payment_record (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id BIGINT NOT NULL,
    session_id VARCHAR(200) NOT NULL UNIQUE,
    payment_intent_id VARCHAR(200),
    product_name VARCHAR(200) NOT NULL,
    amount BIGINT NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(20) NOT NULL,
    create_time BIGINT NOT NULL,
    paid_time BIGINT,
    refund_time BIGINT,
    note VARCHAR(500)
);
```

---

## 🔐 安全最佳实践

### 1. 密钥管理
- ✅ 使用环境变量注入密钥，不要硬编码
- ✅ user.id.encryption.key和field.encryption.key必须不同
- ✅ 密钥长度必须为32字节（256位）
- ✅ 生产环境使用KMS（密钥管理系统）

### 2. Token安全
- ✅ Access Token有效期1小时
- ✅ Refresh Token有效期7天
- ✅ Token Payload不包含email敏感信息
- ✅ 支持设备绑定验证
- ✅ 支持IP验证
- ✅ 登出时Token加入黑名单

### 3. 数据保护
- ✅ 邮箱加密存储（AES-256-GCM）
- ✅ 密码BCrypt哈希
- ✅ 用户ID加密传输（13位Base58）
- ✅ 前端展示时邮箱脱敏

### 4. 用户ID安全
- ✅ 数据库ID不直接暴露
- ✅ 使用加密ID进行API通信
- ✅ 加密ID包含时间戳盐值
- ✅ Luhn校验防篡改

---

## 🚀 使用示例

### 完整认证流程示例

```java
// 1. 用户注册
RegisterRequest registerReq = new RegisterRequest();
registerReq.setUsername("john_doe");
registerReq.setEmail("john@example.com");
registerReq.setPassword("SecurePass123!");

authService.register(registerReq, "192.168.1.1");

// 2. 用户登录
WebLoginRequest loginReq = new WebLoginRequest();
loginReq.setEmail("john@example.com");
loginReq.setPassword("SecurePass123!");
loginReq.setDeviceId("device_123");

AuthResponse authResp = authService.login(
    loginReq, "192.168.1.1", "Mozilla/5.0"
);

// 响应包含：
// - accessToken: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
// - refreshToken: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
// - encryptedUserId: "u1a2b3c4d5e6f"
// - username: "john_doe"
// - maskedEmail: "j***@example.com"

// 3. API请求验证Token
String token = request.getHeader("Authorization").replace("Bearer ", "");
boolean valid = jwtUtil.validateToken(token);

if (valid && jwtUtil.isAccessToken(token)) {
    String encryptedUserId = jwtUtil.getEncryptedUserIdFromToken(token);
    WebUser user = authService.getUserByEncryptedId(encryptedUserId);
    // 处理业务逻辑...
}

// 4. Token过期时刷新
RefreshTokenRequest refreshReq = new RefreshTokenRequest();
refreshReq.setRefreshToken(authResp.getRefreshToken());
refreshReq.setDeviceId("device_123");

AuthResponse newAuthResp = authService.refreshToken(
    refreshReq.getRefreshToken(),
    refreshReq.getDeviceId(),
    "192.168.1.1"
);

// 5. 登出
authService.logout(accessToken, refreshToken);
```

---

## 📝 待实现功能（Controller层）

虽然Service层已完整实现，但还需要创建Controller来暴露API接口：

### 建议创建的Controller：

1. **AuthController** - 认证接口
   - POST `/api/auth/register` - 用户注册
   - POST `/api/auth/login` - 用户登录
   - POST `/api/auth/refresh` - 刷新Token
   - POST `/api/auth/logout` - 登出
   - POST `/api/auth/verify-email` - 验证邮箱
   - POST `/api/auth/request-reset` - 请求密码重置
   - POST `/api/auth/reset-password` - 重置密码

2. **PaymentController** - 支付接口
   - POST `/api/payment/create-session` - 创建支付会话
   - POST `/api/payment/webhook` - Stripe Webhook回调
   - GET `/api/payment/history` - 查询支付历史
   - POST `/api/payment/refund` - 申请退款

3. **UserController** - 用户管理接口
   - GET `/api/user/profile` - 获取用户信息
   - PUT `/api/user/profile` - 更新用户信息
   - POST `/api/user/change-password` - 修改密码

---

## 🎯 总结

### ✅ 已完成
- WebUser实体和Repository
- PaymentRecord实体和Repository
- 用户ID加密工具（13位加密ID）
- 邮箱字段加密工具（AES-256-GCM）
- 数据脱敏服务
- 增强JWT工具（Access/Refresh Token）
- 完整认证服务（注册/登录/刷新/登出）
- Stripe支付服务
- 完整的单元测试和集成测试
- Maven依赖配置
- application.properties配置

### 🔨 下一步
- 创建Controller层暴露API接口
- 配置Spring Security
- 实现邮件发送服务
- 创建前端页面（Next.js）

---

## 📞 技术支持

如有问题，请参考：
- Plan文档：`plan.md`
- 测试类输出：运行测试查看详细示例
- Spring Boot文档：https://spring.io/projects/spring-boot
- Stripe文档：https://stripe.com/docs
