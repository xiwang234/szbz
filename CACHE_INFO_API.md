# 缓存信息查看接口文档

## 概述

此接口用于查看系统中所有本地缓存的详细信息，包括缓存类型、大小、过期策略以及具体的 key-value 数据。方便开发者了解当前缓存状态，为后期迁移到 Redis 等分布式缓存做准备。

## 接口信息

- **路径**: `/api/web-auth/cache-info`
- **方法**: `GET`
- **认证**: 需要 Bearer Token（Access Token）
- **签名**: 需要签名验证（X-Sign-Timestamp、X-Sign-Nonce、X-Sign）

## 请求头

| 请求头名称 | 说明 | 格式 | 示例 |
|-----------|------|------|------|
| Authorization | 访问令牌 | Bearer {token} | Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6... |
| X-Sign-Timestamp | 签名时间戳 | 毫秒级时间戳 | 1738000000000 |
| X-Sign-Nonce | 签名随机串 | 32位字符串 | a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6 |
| X-Sign | 签名 | SHA-256签名字符串 | 3a7bd3e2f8c1... |

## 请求参数

无需请求参数，GET 请求，直接访问即可。

## 响应结果

### 成功响应（200 OK）

```json
{
  "code": 200,
  "message": "获取缓存信息成功",
  "data": [
    {
      "cacheName": "RandomSaltCache",
      "cacheType": "Guava Cache",
      "size": 3,
      "maxSize": 10000,
      "expiration": "5分钟（expireAfterWrite）",
      "entries": {
        "a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6": {
          "salt": "a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6",
          "status": "UNUSED",
          "createTime": 1738000000000,
          "age": "15000ms"
        },
        "x9y8z7w6v5u4t3s2r1q0p9o8n7m6l5k4": {
          "salt": "x9y8z7w6v5u4t3s2r1q0p9o8n7m6l5k4",
          "status": "USED",
          "createTime": 1738000050000,
          "age": "65000ms"
        }
      },
      "stats": "Salt Cache - Size: 3, Stats: CacheStats{...}"
    },
    {
      "cacheName": "TokenBlacklist",
      "cacheType": "ConcurrentHashMap",
      "size": 2,
      "maxSize": null,
      "expiration": "根据Token过期时间自动清理",
      "entries": {
        "jti-123456": {
          "expiryTime": 1738003600000,
          "remainingTime": "3600000ms",
          "isExpired": false
        },
        "jti-789012": {
          "expiryTime": 1738007200000,
          "remainingTime": "7200000ms",
          "isExpired": false
        }
      },
      "stats": "活跃Token黑名单数量: 2"
    },
    {
      "cacheName": "IpRequestCounts",
      "cacheType": "ConcurrentHashMap",
      "size": 5,
      "maxSize": null,
      "expiration": "每分钟重置",
      "entries": {
        "192.168.1.100": {
          "requestCount": 15,
          "resetTime": 1738000060000,
          "remainingTime": "45000ms"
        },
        "10.0.0.50": {
          "requestCount": 8,
          "resetTime": 1738000060000,
          "remainingTime": "45000ms"
        }
      },
      "stats": "当前监控IP数量: 5"
    },
    {
      "cacheName": "IpBlockCounts",
      "cacheType": "ConcurrentHashMap",
      "size": 2,
      "maxSize": null,
      "expiration": "永久保存（需手动清理）",
      "entries": {
        "192.168.1.200": {
          "blockCount": 3,
          "isBlocked": false,
          "threshold": 5
        },
        "10.0.0.100": {
          "blockCount": 5,
          "isBlocked": true,
          "threshold": 5
        }
      },
      "stats": "累计拦截记录数量: 2"
    },
    {
      "cacheName": "BlockedIps",
      "cacheType": "ConcurrentHashMap.KeySet",
      "size": 1,
      "maxSize": null,
      "expiration": "永久封禁（需手动解封）",
      "entries": {
        "10.0.0.100": {
          "blocked": true,
          "totalBlockCount": 5
        }
      },
      "stats": "当前封禁IP数量: 1"
    }
  ]
}
```

### 响应字段说明

#### 顶层字段

| 字段名 | 类型 | 说明 |
|-------|------|------|
| code | Integer | 响应码，200表示成功 |
| message | String | 响应消息 |
| data | Array | 缓存信息列表 |

#### 缓存信息对象（data中的每个元素）

| 字段名 | 类型 | 说明 |
|-------|------|------|
| cacheName | String | 缓存名称 |
| cacheType | String | 缓存类型（Guava Cache/ConcurrentHashMap） |
| size | Integer | 当前缓存大小 |
| maxSize | Long | 最大缓存大小（null表示无限制） |
| expiration | String | 过期策略说明 |
| entries | Object | 缓存条目（key-value对） |
| stats | String | 缓存统计信息 |

### 各类缓存详细说明

#### 1. RandomSaltCache（随机盐缓存）

**用途**: 存储登录时的随机盐，防止重放攻击

**缓存条目结构**:
```json
{
  "salt": "实际的盐值",
  "status": "UNUSED/USED",
  "createTime": 1738000000000,
  "age": "15000ms"
}
```

**字段说明**:
- `salt`: 32位随机字符串
- `status`: 盐的状态，UNUSED（未使用）/USED（已使用）
- `createTime`: 创建时间戳（毫秒）
- `age`: 已存活时间

**特点**:
- 类型: Guava Cache
- 最大容量: 10,000
- 过期策略: 5分钟后自动删除
- 用途: 每次登录前获取，使用后标记为 USED

#### 2. TokenBlacklist（Token黑名单）

**用途**: 存储已登出的 Token（JTI），防止 Token 被重复使用

**缓存条目结构**:
```json
{
  "expiryTime": 1738003600000,
  "remainingTime": "3600000ms",
  "isExpired": false
}
```

**字段说明**:
- `expiryTime`: 过期时间戳（毫秒）
- `remainingTime`: 剩余有效时间
- `isExpired`: 是否已过期

**特点**:
- 类型: ConcurrentHashMap
- 最大容量: 无限制（建议迁移到 Redis）
- 过期策略: 根据 Token 本身的过期时间自动清理
- 用途: 用户登出时将 Token 加入黑名单

#### 3. IpRequestCounts（IP请求计数）

**用途**: 限制每个 IP 的请求频率，防止暴力攻击

**缓存条目结构**:
```json
{
  "requestCount": 15,
  "resetTime": 1738000060000,
  "remainingTime": "45000ms"
}
```

**字段说明**:
- `requestCount`: 当前时间窗口内的请求次数
- `resetTime`: 计数重置时间
- `remainingTime`: 距离重置还有多久

**特点**:
- 类型: ConcurrentHashMap
- 最大容量: 无限制
- 过期策略: 每分钟自动重置
- 限流规则: 默认每分钟最多 60 次请求（可配置）

#### 4. IpBlockCounts（IP拦截计数）

**用途**: 记录每个 IP 被拦截的次数，累计达到阈值自动封禁

**缓存条目结构**:
```json
{
  "blockCount": 3,
  "isBlocked": false,
  "threshold": 5
}
```

**字段说明**:
- `blockCount`: 累计被拦截次数
- `isBlocked`: 是否已被封禁
- `threshold`: 封禁阈值

**特点**:
- 类型: ConcurrentHashMap
- 最大容量: 无限制
- 过期策略: 永久保存（需手动清理）
- 封禁规则: 默认累计拦截 5 次后自动封禁

#### 5. BlockedIps（封禁IP列表）

**用途**: 存储已被封禁的 IP 地址

**缓存条目结构**:
```json
{
  "blocked": true,
  "totalBlockCount": 5
}
```

**字段说明**:
- `blocked`: 是否被封禁（始终为 true）
- `totalBlockCount`: 累计拦截次数

**特点**:
- 类型: ConcurrentHashMap.KeySet
- 最大容量: 无限制
- 过期策略: 永久封禁（需手动解封）
- 用途: 自动或手动封禁恶意 IP

### 错误响应

#### 1. Token 无效（401 Unauthorized）

```json
{
  "code": 401,
  "message": "无效的访问令牌",
  "data": null
}
```

#### 2. 账户被禁用（403 Forbidden）

```json
{
  "code": 403,
  "message": "账户已被禁用",
  "data": null
}
```

#### 3. 签名验证失败（403 Forbidden）

```json
{
  "code": 403,
  "message": "签名验证失败",
  "data": null
}
```

#### 4. 服务器内部错误（500 Internal Server Error）

```json
{
  "code": 500,
  "message": "服务器内部错误：具体错误信息",
  "data": null
}
```

## 请求示例

### cURL 示例

```bash
# 假设已经获取了 Access Token
ACCESS_TOKEN="your_access_token_here"
TIMESTAMP=$(date +%s)000
NONCE="a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6"

# 生成签名（需要根据实际参数生成）
# 由于是 GET 请求且无参数，签名只需包含 timestamp 和 nonce
SIGN="your_generated_signature"

curl -X GET http://localhost:8080/api/web-auth/cache-info \
  -H "Authorization: Bearer ${ACCESS_TOKEN}" \
  -H "X-Sign-Timestamp: ${TIMESTAMP}" \
  -H "X-Sign-Nonce: ${NONCE}" \
  -H "X-Sign: ${SIGN}"
```

### JavaScript/TypeScript 示例

```javascript
async function getCacheInfo(accessToken) {
  // 生成签名（参考 API_SIGNATURE.md）
  const timestamp = Date.now();
  const nonce = generateNonce(); // 32位随机串
  const sign = await generateSignature({}, timestamp, nonce); // 空参数

  const response = await fetch('/api/web-auth/cache-info', {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${accessToken}`,
      'X-Sign-Timestamp': timestamp.toString(),
      'X-Sign-Nonce': nonce,
      'X-Sign': sign
    }
  });

  return await response.json();
}

// 使用示例
const result = await getCacheInfo(accessToken);
console.log('缓存信息:', result.data);

// 分析各个缓存
result.data.forEach(cache => {
  console.log(`\n=== ${cache.cacheName} ===`);
  console.log(`类型: ${cache.cacheType}`);
  console.log(`大小: ${cache.size}${cache.maxSize ? '/' + cache.maxSize : ''}`);
  console.log(`过期策略: ${cache.expiration}`);
  console.log(`详细数据:`, cache.entries);
});
```

### React 示例

```jsx
import React, { useState, useEffect } from 'react';
import axios from 'axios';

function CacheInfoViewer() {
  const [cacheInfo, setCacheInfo] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const loadCacheInfo = async () => {
    setLoading(true);
    setError(null);

    try {
      // Axios 拦截器会自动添加签名头
      const response = await axios.get('/api/web-auth/cache-info');
      setCacheInfo(response.data.data);
    } catch (err) {
      setError(err.response?.data?.message || '加载失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadCacheInfo();
  }, []);

  return (
    <div>
      <h2>系统缓存信息</h2>
      <button onClick={loadCacheInfo} disabled={loading}>
        {loading ? '加载中...' : '刷新'}
      </button>

      {error && <div className="error">{error}</div>}

      {cacheInfo.map((cache, index) => (
        <div key={index} className="cache-item">
          <h3>{cache.cacheName}</h3>
          <p>类型: {cache.cacheType}</p>
          <p>大小: {cache.size} {cache.maxSize && `/ ${cache.maxSize}`}</p>
          <p>过期策略: {cache.expiration}</p>
          <p>统计: {cache.stats}</p>

          <details>
            <summary>查看详细数据 ({Object.keys(cache.entries).length} 条)</summary>
            <pre>{JSON.stringify(cache.entries, null, 2)}</pre>
          </details>
        </div>
      ))}
    </div>
  );
}

export default CacheInfoViewer;
```

## 使用场景

### 1. 开发调试

在开发环境中，可以实时查看缓存状态，帮助理解系统行为：

```javascript
// 定期检查缓存状态
setInterval(async () => {
  const result = await getCacheInfo(accessToken);
  console.log('当前缓存状态:', result);
}, 30000); // 每30秒检查一次
```

### 2. 系统监控

监控缓存大小和性能：

```javascript
const result = await getCacheInfo(accessToken);

// 检查缓存是否过大
result.data.forEach(cache => {
  if (cache.maxSize && cache.size > cache.maxSize * 0.8) {
    console.warn(`警告: ${cache.cacheName} 使用率超过80%`);
  }
});

// 检查 Token 黑名单大小
const tokenBlacklist = result.data.find(c => c.cacheName === 'TokenBlacklist');
if (tokenBlacklist && tokenBlacklist.size > 1000) {
  console.warn('Token黑名单过大，建议迁移到 Redis');
}
```

### 3. 迁移准备

为迁移到 Redis 做准备，了解数据结构：

```javascript
const result = await getCacheInfo(accessToken);

// 分析每个缓存的数据结构
result.data.forEach(cache => {
  console.log(`\n=== ${cache.cacheName} Redis 迁移方案 ===`);

  switch(cache.cacheName) {
    case 'RandomSaltCache':
      console.log('Redis 数据类型: String');
      console.log('Key 格式: salt:{salt_value}');
      console.log('TTL: 300秒（5分钟）');
      break;

    case 'TokenBlacklist':
      console.log('Redis 数据类型: String');
      console.log('Key 格式: token:blacklist:{jti}');
      console.log('TTL: 根据 Token 过期时间');
      break;

    case 'IpRequestCounts':
      console.log('Redis 数据类型: Hash');
      console.log('Key 格式: ip:requests:{ip}');
      console.log('TTL: 60秒');
      break;

    // ... 其他缓存
  }
});
```

## 安全建议

1. **访问控制**
   - 此接口仅供开发和运维使用
   - 生产环境建议限制特定角色访问
   - 可以添加额外的权限验证

2. **数据脱敏**
   - Token 的 JTI 已经是哈希值，相对安全
   - IP 地址可能包含敏感信息，建议脱敏或限制访问

3. **频率限制**
   - 建议对此接口添加频率限制
   - 避免频繁查询影响性能

## 迁移到 Redis 的建议

基于缓存信息，以下是迁移到 Redis 的建议方案：

### 1. RandomSaltCache

```redis
# Redis 命令示例
SET salt:{salt_value} "{json_data}" EX 300

# 查询
GET salt:{salt_value}
```

### 2. TokenBlacklist

```redis
# Redis 命令示例
SET token:blacklist:{jti} "1" EX {token_ttl}

# 查询
EXISTS token:blacklist:{jti}
```

### 3. IpRequestCounts

```redis
# Redis 命令示例
INCR ip:requests:{ip}
EXPIRE ip:requests:{ip} 60

# 查询
GET ip:requests:{ip}
```

### 4. IpBlockCounts

```redis
# Redis 命令示例
HINCRBY ip:blocks {ip} 1
HGET ip:blocks {ip}
```

### 5. BlockedIps

```redis
# Redis 命令示例
SADD ip:blocked {ip}
SISMEMBER ip:blocked {ip}
```

## 总结

`/api/web-auth/cache-info` 接口提供了系统所有本地缓存的详细信息，包括：

- ✅ 5种不同类型的缓存
- ✅ 详细的 key-value 数据
- ✅ 缓存统计信息
- ✅ 过期策略说明

这些信息对于：
- 📊 系统监控和调试
- 🔄 迁移到分布式缓存（Redis）
- 🎯 性能优化和容量规划

非常有价值。建议定期查看缓存状态，及时发现并解决问题。
