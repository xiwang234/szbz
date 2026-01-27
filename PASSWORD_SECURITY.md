# 密码安全机制文档

## 概述

本系统采用双重加盐（固定盐 + 随机盐）的密码安全机制，确保密码在传输和存储过程中的安全性。

## 安全架构

### 注册流程
1. **前端**：用户输入原始密码
2. **前端**：计算 `SHA256(原密码 + 固定盐)`
3. **前端**：将加密后的密码发送到服务端
4. **服务端**：直接将接收到的哈希密码存储到数据库

### 登录流程
1. **前端**：用户输入原始密码
2. **前端**：调用 `/api/web-auth/random-salt` 获取随机盐（32位，5分钟有效）
3. **前端**：计算 `SHA256(原密码 + 固定盐 + 随机盐)`
4. **前端**：将加密后的密码和随机盐一起发送到服务端
5. **服务端**：验证随机盐是否有效（未使用且在5分钟内）
6. **服务端**：从数据库取出注册时保存的密码（已经是 `SHA256(原密码 + 固定盐)`）
7. **服务端**：计算 `SHA256(数据库密码 + 随机盐)` 并与前端传入的密码比较

## 配置说明

### 固定盐配置
在 `application.properties` 中配置：
```properties
password.fixed.salt=${PASSWORD_FIXED_SALT:szbz-fixed-salt-2024-secure-password}
```

**重要**：
- 生产环境必须通过环境变量 `PASSWORD_FIXED_SALT` 配置
- 固定盐一旦设置后不能更改，否则所有用户将无法登录
- 建议使用至少32位的随机字符串

### 随机盐配置
- 长度：32位随机字符串
- 有效期：5分钟
- 状态：未使用/已使用
- 存储：Guava Cache（内存）

## API 接口

### 1. 获取随机盐
**端点**：`GET /api/web-auth/random-salt`

**响应**：
```json
{
  "code": 200,
  "message": "获取随机盐成功",
  "data": {
    "randomSalt": "a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6",
    "expiresAt": 1706345678000
  }
}
```

### 2. 用户注册
**端点**：`POST /api/web-auth/register`

**请求体**：
```json
{
  "username": "testuser",
  "email": "test@example.com",
  "password": "前端计算的SHA256(原密码 + 固定盐)"
}
```

**响应**：
```json
{
  "code": 200,
  "message": "注册成功，请验证邮箱",
  "data": null
}
```

### 3. 用户登录
**端点**：`POST /api/web-auth/login`

**请求体**：
```json
{
  "email": "test@example.com",
  "password": "前端计算的SHA256(原密码 + 固定盐 + 随机盐)",
  "randomSalt": "从/random-salt接口获取的随机盐",
  "deviceId": "可选的设备ID"
}
```

**响应**：
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "accessTokenExpiresAt": 1706345678000,
    "refreshTokenExpiresAt": 1706949878000,
    "encryptedUserId": "encrypted_user_id",
    "username": "testuser",
    "maskedEmail": "t***@example.com"
  }
}
```

## 前端实现示例

### JavaScript/TypeScript 实现

```javascript
// 固定盐（需要与后端配置一致）
const FIXED_SALT = 'szbz-fixed-salt-2024-secure-password';

/**
 * SHA-256 哈希函数
 */
async function sha256(message) {
  const msgBuffer = new TextEncoder().encode(message);
  const hashBuffer = await crypto.subtle.digest('SHA-256', msgBuffer);
  const hashArray = Array.from(new Uint8Array(hashBuffer));
  const hashHex = hashArray.map(b => b.toString(16).padStart(2, '0')).join('');
  return hashHex;
}

/**
 * 注册
 */
async function register(username, email, rawPassword) {
  // 1. 计算 SHA256(原密码 + 固定盐)
  const hashedPassword = await sha256(rawPassword + FIXED_SALT);

  // 2. 发送注册请求
  const response = await fetch('/api/web-auth/register', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      username: username,
      email: email,
      password: hashedPassword
    })
  });

  return await response.json();
}

/**
 * 登录
 */
async function login(email, rawPassword, deviceId = null) {
  // 1. 获取随机盐
  const saltResponse = await fetch('/api/web-auth/random-salt');
  const saltData = await saltResponse.json();
  const randomSalt = saltData.data.randomSalt;

  // 2. 计算 SHA256(原密码 + 固定盐)
  const hashedPasswordWithFixedSalt = await sha256(rawPassword + FIXED_SALT);

  // 3. 计算 SHA256(已哈希密码 + 随机盐)
  const finalPassword = await sha256(hashedPasswordWithFixedSalt + randomSalt);

  // 4. 发送登录请求
  const response = await fetch('/api/web-auth/login', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      email: email,
      password: finalPassword,
      randomSalt: randomSalt,
      deviceId: deviceId
    })
  });

  return await response.json();
}
```

### React 示例

```jsx
import { useState } from 'react';

const FIXED_SALT = 'szbz-fixed-salt-2024-secure-password';

async function sha256(message) {
  const msgBuffer = new TextEncoder().encode(message);
  const hashBuffer = await crypto.subtle.digest('SHA-256', msgBuffer);
  const hashArray = Array.from(new Uint8Array(hashBuffer));
  return hashArray.map(b => b.toString(16).padStart(2, '0')).join('');
}

function LoginForm() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);

  const handleLogin = async (e) => {
    e.preventDefault();
    setLoading(true);

    try {
      // 1. 获取随机盐
      const saltRes = await fetch('/api/web-auth/random-salt');
      const saltData = await saltRes.json();
      const randomSalt = saltData.data.randomSalt;

      // 2. 计算密码哈希
      const hashedPassword = await sha256(password + FIXED_SALT);
      const finalPassword = await sha256(hashedPassword + randomSalt);

      // 3. 登录
      const loginRes = await fetch('/api/web-auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          email,
          password: finalPassword,
          randomSalt
        })
      });

      const result = await loginRes.json();

      if (result.code === 200) {
        // 保存 token
        localStorage.setItem('accessToken', result.data.accessToken);
        localStorage.setItem('refreshToken', result.data.refreshToken);
        // 跳转到主页
        window.location.href = '/dashboard';
      } else {
        alert(result.message);
      }
    } catch (error) {
      console.error('登录失败:', error);
      alert('登录失败，请重试');
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleLogin}>
      <input
        type="email"
        value={email}
        onChange={(e) => setEmail(e.target.value)}
        placeholder="邮箱"
        required
      />
      <input
        type="password"
        value={password}
        onChange={(e) => setPassword(e.target.value)}
        placeholder="密码"
        required
      />
      <button type="submit" disabled={loading}>
        {loading ? '登录中...' : '登录'}
      </button>
    </form>
  );
}
```

## 安全特性

### 1. 双重加盐
- **固定盐**：防止彩虹表攻击，所有用户共享
- **随机盐**：防止重放攻击，每次登录都不同

### 2. 时间限制
- 随机盐5分钟过期，防止长时间重放攻击

### 3. 一次性使用
- 随机盐使用后立即标记为已使用，防止重复使用

### 4. 密码不明文传输
- 注册时：密码经过一次SHA-256哈希后传输
- 登录时：密码经过两次SHA-256哈希后传输

### 5. 服务端验证
- 验证随机盐是否存在
- 验证随机盐是否在有效期内
- 验证随机盐是否未被使用
- 验证密码哈希是否匹配

## 测试建议

### 单元测试
1. 测试随机盐生成（长度、唯一性）
2. 测试随机盐过期机制
3. 测试随机盐一次性使用
4. 测试密码哈希计算
5. 测试注册流程
6. 测试登录流程

### 集成测试
1. 完整的注册-登录流程
2. 随机盐过期后的登录失败
3. 随机盐重复使用的登录失败
4. 错误密码的登录失败

### 安全测试
1. 重放攻击测试
2. 暴力破解测试
3. 时序攻击测试

## 常见问题

### Q1: 为什么使用固定盐而不是每个用户一个盐？
A: 因为密码在前端加密，如果每个用户使用不同的盐，前端需要先查询用户的盐，这会暴露用户是否存在。固定盐配合随机盐可以在保证安全性的同时避免这个问题。

### Q2: 随机盐为什么只有5分钟有效期？
A: 5分钟是一个平衡点，既能防止长时间的重放攻击，又不会给用户带来太大的时间压力。

### Q3: 如果用户在获取随机盐后5分钟内没有登录怎么办？
A: 前端会收到"随机盐无效或已过期"的错误，需要重新获取随机盐并重试。

### Q4: 固定盐可以更改吗？
A: 不建议更改。如果必须更改，需要：
   1. 通知所有用户重置密码
   2. 或者实现密码迁移机制（使用旧盐验证，用新盐重新保存）

### Q5: 为什么不使用 bcrypt 或 Argon2？
A: bcrypt 和 Argon2 是服务端密码哈希算法，计算成本高，不适合在前端执行。本方案使用 SHA-256 在前端快速计算，然后在服务端进行验证。

## 迁移指南

### 从旧系统迁移

如果你的系统之前使用明文密码或 BCrypt，需要进行迁移：

1. **数据库迁移**：
   - 为用户表添加 `password_migration_flag` 字段
   - 标记哪些用户需要迁移

2. **登录时迁移**：
   ```java
   if (user.needsMigration()) {
       // 使用旧方式验证密码
       if (oldPasswordEncoder.matches(rawPassword, user.getPasswordHash())) {
           // 验证成功，使用新方式重新保存密码
           String newPassword = sha256(rawPassword + fixedSalt);
           user.setPasswordHash(newPassword);
           user.setPasswordMigrationFlag(false);
           userRepository.save(user);
       }
   }
   ```

3. **通知用户**：
   - 发送邮件通知用户密码安全升级
   - 建议用户重新登录以完成迁移

## 生产环境部署

### 环境变量配置
```bash
# 固定盐（必须配置，建议32位以上随机字符串）
export PASSWORD_FIXED_SALT="your-secure-random-fixed-salt-32-chars-or-more"

# 其他安全配置
export USER_ID_ENCRYPTION_KEY="your-encryption-key-32-chars-256bits"
export FIELD_ENCRYPTION_KEY="your-field-encryption-key-32-chars"
export JWT_SECRET="your-jwt-secret-key"
```

### Docker 部署
```yaml
version: '3.8'
services:
  app:
    image: szbz-api:latest
    environment:
      - PASSWORD_FIXED_SALT=${PASSWORD_FIXED_SALT}
      - USER_ID_ENCRYPTION_KEY=${USER_ID_ENCRYPTION_KEY}
      - FIELD_ENCRYPTION_KEY=${FIELD_ENCRYPTION_KEY}
      - JWT_SECRET=${JWT_SECRET}
    ports:
      - "8080:8080"
```

### 监控建议
1. 监控随机盐缓存大小
2. 监控随机盐过期率
3. 监控登录失败率（可能的攻击）
4. 监控密码验证时间（防止时序攻击）

## 总结

本密码安全机制通过双重加盐、时间限制和一次性使用等多重防护措施，确保了密码在传输和存储过程中的安全性。前端开发者需要严格按照本文档的要求实现密码加密逻辑，确保系统的整体安全性。
