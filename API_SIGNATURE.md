# API 签名验证文档

## 概述

本系统对 WebAuthController 的接口（除了注册接口）实施了签名验证机制，用于防止参数篡改和重放攻击。

## 签名验证机制

### 核心组件
- **时间戳（timestamp）**：毫秒级时间戳，用于防止重放攻击，有效期 5 分钟
- **随机串（nonce）**：32 位随机字符串，增强签名唯一性
- **签名（sign）**：使用 SHA-256 算法生成的签名字符串

### 验证流程

```
前端请求 → Filter包装请求 → 拦截器验证签名 → 通过/拒绝 → Controller处理业务
```

## 请求头规范

所有需要签名验证的接口必须在请求头中包含以下参数：

| 请求头名称 | 说明 | 格式要求 | 示例 |
|-----------|------|---------|------|
| X-Sign-Timestamp | 毫秒级时间戳 | 数字，13位 | 1738000000000 |
| X-Sign-Nonce | 随机串 | 32位字符串 | a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6 |
| X-Sign | 签名 | 64位十六进制字符串 | 3a7bd3e2... |

## 排除列表

以下接口不需要签名验证：
- `/api/web-auth/register` - 用户注册接口
- `/api/bazi/**` - 所有八字相关接口

## 签名生成算法

### 步骤 1: 收集业务参数
提取所有业务参数，包括：
- URL 路径参数
- Query 参数
- Request Body 参数

### 步骤 2: 参数标准化
1. 移除空值参数（null、''、undefined）
2. 移除签名参数本身（sign）
3. 添加 timestamp 和 nonce

### 步骤 3: 字典序排序
按参数 key 的 ASCII 码升序排序

### 步骤 4: 拼接参数字符串
格式：`key1=value1&key2=value2&key3=value3`

### 步骤 5: 添加密钥
将参数字符串与固定密钥拼接：`参数字符串 + 密钥`

### 步骤 6: SHA-256 加密
对拼接后的字符串进行 SHA-256 加密，得到最终签名

## 前端实现示例

### JavaScript/TypeScript 实现

```javascript
// 配置
const API_SIGN_SECRET = 'szbz-api-sign-key-2026';

/**
 * SHA-256 加密
 */
async function sha256(message) {
  const msgBuffer = new TextEncoder().encode(message);
  const hashBuffer = await crypto.subtle.digest('SHA-256', msgBuffer);
  const hashArray = Array.from(new Uint8Array(hashBuffer));
  return hashArray.map(b => b.toString(16).padStart(2, '0')).join('');
}

/**
 * 生成32位随机串
 */
function generateNonce() {
  const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
  let nonce = '';
  for (let i = 0; i < 32; i++) {
    nonce += chars.charAt(Math.floor(Math.random() * chars.length));
  }
  return nonce;
}

/**
 * 生成签名
 * @param params 业务参数（对象）
 * @param timestamp 时间戳（毫秒）
 * @param nonce 随机串（32位）
 * @returns 签名字符串
 */
async function generateSignature(params, timestamp, nonce) {
  // 1. 创建参数对象，添加 timestamp 和 nonce
  const allParams = {
    ...params,
    timestamp: timestamp.toString(),
    nonce: nonce
  };

  // 2. 过滤空值参数
  const filteredParams = {};
  for (const [key, value] of Object.entries(allParams)) {
    if (value !== null && value !== undefined && value !== '') {
      filteredParams[key] = value;
    }
  }

  // 3. 按 key 排序
  const sortedKeys = Object.keys(filteredParams).sort();

  // 4. 拼接参数字符串
  const paramString = sortedKeys
    .map(key => `${key}=${filteredParams[key]}`)
    .join('&');

  // 5. 拼接密钥
  const signSource = paramString + API_SIGN_SECRET;

  // 6. SHA-256 加密
  return await sha256(signSource);
}

/**
 * 发送带签名的请求
 */
async function sendSignedRequest(url, method, data = null) {
  // 1. 生成时间戳和随机串
  const timestamp = Date.now();
  const nonce = generateNonce();

  // 2. 准备业务参数
  const params = data || {};

  // 3. 生成签名
  const sign = await generateSignature(params, timestamp, nonce);

  // 4. 构建请求选项
  const options = {
    method: method,
    headers: {
      'Content-Type': 'application/json',
      'X-Sign-Timestamp': timestamp.toString(),
      'X-Sign-Nonce': nonce,
      'X-Sign': sign
    }
  };

  // 5. 如果是 POST/PUT/PATCH 请求，添加 body
  if (method !== 'GET' && data) {
    options.body = JSON.stringify(data);
  }

  // 6. 发送请求
  const response = await fetch(url, options);
  return await response.json();
}

// 使用示例：登录
async function login(email, password, randomSalt) {
  const data = {
    email: email,
    password: password,
    randomSalt: randomSalt
  };

  return await sendSignedRequest('/api/web-auth/login', 'POST', data);
}

// 使用示例：获取用户信息
async function getUserInfo() {
  return await sendSignedRequest('/api/web-auth/me', 'GET');
}
```

### React 示例（使用 Axios）

```jsx
import axios from 'axios';

const API_SIGN_SECRET = 'szbz-api-sign-key-2026';

// SHA-256 加密函数
async function sha256(message) {
  const msgBuffer = new TextEncoder().encode(message);
  const hashBuffer = await crypto.subtle.digest('SHA-256', msgBuffer);
  const hashArray = Array.from(new Uint8Array(hashBuffer));
  return hashArray.map(b => b.toString(16).padStart(2, '0')).join('');
}

// 生成随机串
function generateNonce() {
  const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
  return Array.from({ length: 32 }, () =>
    chars.charAt(Math.floor(Math.random() * chars.length))
  ).join('');
}

// 生成签名
async function generateSignature(params, timestamp, nonce) {
  const allParams = { ...params, timestamp: timestamp.toString(), nonce };

  const filteredParams = {};
  for (const [key, value] of Object.entries(allParams)) {
    if (value !== null && value !== undefined && value !== '') {
      filteredParams[key] = value;
    }
  }

  const sortedKeys = Object.keys(filteredParams).sort();
  const paramString = sortedKeys
    .map(key => `${key}=${filteredParams[key]}`)
    .join('&');
  const signSource = paramString + API_SIGN_SECRET;

  return await sha256(signSource);
}

// Axios 请求拦截器
axios.interceptors.request.use(
  async (config) => {
    // 排除不需要签名的接口
    if (config.url === '/api/web-auth/register' || config.url?.startsWith('/api/bazi/')) {
      return config;
    }

    // 生成签名
    const timestamp = Date.now();
    const nonce = generateNonce();
    const params = config.method === 'get' ? config.params : config.data;
    const sign = await generateSignature(params || {}, timestamp, nonce);

    // 添加签名头
    config.headers['X-Sign-Timestamp'] = timestamp.toString();
    config.headers['X-Sign-Nonce'] = nonce;
    config.headers['X-Sign'] = sign;

    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 使用示例
function LoginComponent() {
  const handleLogin = async () => {
    try {
      const response = await axios.post('/api/web-auth/login', {
        email: 'test@example.com',
        password: 'hashed_password',
        randomSalt: 'random_salt_from_server'
      });
      console.log('登录成功:', response.data);
    } catch (error) {
      console.error('登录失败:', error);
    }
  };

  return <button onClick={handleLogin}>登录</button>;
}
```

## 签名生成示例

假设请求数据如下：

```json
{
  "email": "test@example.com",
  "password": "hashed_password",
  "randomSalt": "abc123"
}
```

时间戳：`1738000000000`
随机串：`a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6`
密钥：`szbz-api-sign-key-2026`

### 步骤详解

1. **合并参数**
```javascript
{
  email: "test@example.com",
  password: "hashed_password",
  randomSalt: "abc123",
  timestamp: "1738000000000",
  nonce: "a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6"
}
```

2. **字典序排序**
```
email
nonce
password
randomSalt
timestamp
```

3. **拼接参数字符串**
```
email=test@example.com&nonce=a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6&password=hashed_password&randomSalt=abc123&timestamp=1738000000000
```

4. **添加密钥**
```
email=test@example.com&nonce=a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6&password=hashed_password&randomSalt=abc123&timestamp=1738000000000szbz-api-sign-key-2026
```

5. **SHA-256 加密**
```
3a7bd3e2f8c1d9a4b5e6f7c8d9e0f1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8
```

## 验证流程

### 服务端验证步骤

1. **基础校验**
   - 检查 X-Sign-Timestamp、X-Sign-Nonce、X-Sign 是否存在
   - 验证 timestamp 是否为数字
   - 验证 nonce 是否为32位字符串

2. **防重放校验**
   - 验证 timestamp 是否在5分钟有效期内

3. **签名一致性校验**
   - 提取所有业务参数
   - 按相同规则生成服务端签名
   - 对比客户端签名和服务端签名

### 验证失败响应

```json
{
  "code": 403,
  "message": "签名验证失败",
  "data": null
}
```

常见错误信息：
- `缺少时间戳参数`
- `缺少随机串参数`
- `缺少签名参数`
- `时间戳格式错误`
- `随机串格式错误`
- `请求已过期`
- `签名验证失败`
- `参数解析失败`

## 配置说明

### 服务端配置

在 `application.properties` 中配置：

```properties
# 签名密钥（建议使用环境变量）
api.sign.secret=${API_SIGN_SECRET:szbz-api-sign-key-2026}

# 签名有效期（毫秒）：默认5分钟
api.sign.expiration=300000
```

### 环境变量配置

生产环境建议通过环境变量配置：

```bash
export API_SIGN_SECRET="your-secure-sign-secret-key"
```

## 测试建议

### 单元测试

1. 测试签名生成函数
2. 测试参数提取函数
3. 测试时间戳验证
4. 测试 nonce 验证

### 集成测试

1. 正常签名请求通过
2. 缺少签名参数被拒绝
3. 错误的签名被拒绝
4. 过期的时间戳被拒绝
5. 错误的 nonce 格式被拒绝

### 测试工具

可以使用 Postman 或 curl 进行测试：

```bash
# 生成签名（使用 Node.js）
node -e "
const crypto = require('crypto');
const params = 'email=test@example.com&nonce=a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6&timestamp=1738000000000';
const secret = 'szbz-api-sign-key-2026';
const sign = crypto.createHash('sha256').update(params + secret).digest('hex');
console.log(sign);
"

# 发送请求
curl -X POST http://localhost:8080/api/web-auth/login \
  -H "Content-Type: application/json" \
  -H "X-Sign-Timestamp: 1738000000000" \
  -H "X-Sign-Nonce: a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6" \
  -H "X-Sign: [生成的签名]" \
  -d '{"email":"test@example.com","password":"hashed_password"}'
```

## 安全建议

1. **密钥管理**
   - 使用强随机密钥
   - 定期轮换密钥
   - 使用环境变量，不要硬编码

2. **时间同步**
   - 确保客户端和服务端时间同步
   - 允许合理的时间偏差（当前为5分钟）

3. **Nonce 唯一性**
   - 使用加密安全的随机数生成器
   - 确保每次请求使用不同的 nonce

4. **HTTPS**
   - 生产环境必须使用 HTTPS
   - 防止中间人攻击

5. **日志监控**
   - 监控签名验证失败的频率
   - 及时发现异常请求

## 故障排查

### 常见问题

1. **签名验证总是失败**
   - 检查密钥是否一致
   - 检查参数排序是否正确
   - 检查参数值是否被 URL 编码

2. **请求总是过期**
   - 检查客户端和服务端时间是否同步
   - 检查时间戳单位（毫秒）

3. **Nonce 格式错误**
   - 确保 nonce 长度为32位
   - 使用字母和数字组合

## 总结

本签名验证机制通过 **时间戳** + **随机串** + **SHA-256签名** 三重防护，有效防止：
- ✅ 参数篡改
- ✅ 重放攻击
- ✅ 中间人攻击（配合HTTPS）

前端开发者需要严格按照本文档的规范实现签名生成逻辑，确保系统的安全性。
