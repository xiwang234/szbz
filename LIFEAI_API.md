# LifeAI 接口文档

## 概述

LifeAI 是一个为用户提供人生建议和咨询服务的接口，需要用户登录后才能访问。接口包含完整的 Token 验证和参数验证逻辑，具体的业务逻辑（AI 分析）待后续实现。

## 接口信息

- **路径**: `/api/web-auth/lifeai`
- **方法**: `POST`
- **认证**: 需要 Bearer Token（Access Token）
- **签名**: 需要签名验证（X-Sign-Timestamp、X-Sign-Nonce、X-Sign）

## 请求头

### 必需请求头

| 请求头名称 | 说明 | 格式 | 示例 |
|-----------|------|------|------|
| Authorization | 访问令牌 | Bearer {token} | Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6... |
| X-Sign-Timestamp | 签名时间戳 | 毫秒级时间戳 | 1738000000000 |
| X-Sign-Nonce | 签名随机串 | 32位字符串 | a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6 |
| X-Sign | 签名 | SHA-256签名字符串 | 3a7bd3e2f8c1... |
| Content-Type | 内容类型 | application/json | application/json |

## 请求参数

### 请求体（JSON）

```json
{
  "background": "背景描述",
  "question": "问题内容",
  "birthYear": 1990,
  "gender": "男",
  "category": "分类"
}
```

### 参数说明

| 参数名 | 类型 | 必填 | 说明 | 验证规则 |
|-------|------|------|------|---------|
| background | String | 是 | 背景描述 | 不能为空，最大1000字符 |
| question | String | 是 | 问题内容 | 不能为空，最大500字符 |
| birthYear | Integer | 是 | 出生年份 | 1900-当前年份之间 |
| gender | String | 是 | 性别 | 男/女/male/female |
| category | String | 是 | 分类 | 不能为空，最大50字符 |

### 参数详细说明

#### background（背景描述）
- **用途**: 描述用户的背景信息，帮助 AI 更好地理解用户的情况
- **示例**:
  - "我是一名软件工程师，工作5年，目前在一家互联网公司"
  - "我是一名大学生，即将毕业，对未来感到迷茫"
  - "我已婚，有两个孩子，目前面临职业转型"

#### question（问题）
- **用途**: 用户想要咨询的具体问题
- **示例**:
  - "如何提升职业发展？"
  - "我应该选择哪个专业？"
  - "如何平衡工作和家庭？"
  - "如何处理人际关系问题？"

#### birthYear（出生年份）
- **用途**: 用于了解用户的年龄段
- **示例**: 1990, 1995, 2000
- **验证**: 必须在 1900 到当前年份之间

#### gender（性别）
- **用途**: 用于提供更个性化的建议
- **支持格式**:
  - 中文: `男`, `女`
  - 英文: `male`, `female`

#### category（分类）
- **用途**: 问题的分类，用于后续统计和分析
- **常见分类**:
  - 职业规划
  - 情感咨询
  - 学业指导
  - 人际关系
  - 健康生活
  - 财务管理
  - 创业咨询
  - 其他

## 响应结果

### 成功响应（200 OK）

```json
{
  "code": 200,
  "message": "请求成功",
  "data": {
    "requestId": "123e4567-e89b-12d3-a456-426614174000",
    "background": "我是一名软件工程师",
    "question": "如何提升职业发展？",
    "birthYear": 1990,
    "gender": "男",
    "category": "职业规划",
    "answer": "业务逻辑待实现",
    "timestamp": 1738000000000
  }
}
```

### 响应字段说明

| 字段名 | 类型 | 说明 |
|-------|------|------|
| requestId | String | 请求唯一标识（UUID） |
| background | String | 回显用户的背景描述 |
| question | String | 回显用户的问题 |
| birthYear | Integer | 回显用户的出生年份 |
| gender | String | 回显用户的性别 |
| category | String | 回显问题分类 |
| answer | String | AI 的回答（待实现） |
| timestamp | Long | 响应时间戳（毫秒） |

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

#### 4. 参数验证失败（400 Bad Request）

```json
{
  "code": 400,
  "message": "背景描述不能为空",
  "data": null
}
```

常见参数错误信息：
- `背景描述不能为空`
- `问题不能为空`
- `出生年份不能为空`
- `出生年份必须在1900-2026之间`
- `性别不能为空`
- `性别格式错误，只支持：男/女/male/female`
- `分类不能为空`
- `背景描述不能超过1000字符`
- `问题不能超过500字符`
- `分类不能超过50字符`

#### 5. 服务器内部错误（500 Internal Server Error）

```json
{
  "code": 500,
  "message": "服务器内部错误：具体错误信息",
  "data": null
}
```

## 请求示例

### JavaScript/TypeScript 示例

```javascript
// 假设已经有签名生成函数（参考 API_SIGNATURE.md）
async function callLifeAI(accessToken, data) {
  // 1. 生成签名
  const timestamp = Date.now();
  const nonce = generateNonce(); // 生成32位随机串
  const sign = await generateSignature(data, timestamp, nonce);

  // 2. 发送请求
  const response = await fetch('/api/web-auth/lifeai', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${accessToken}`,
      'X-Sign-Timestamp': timestamp.toString(),
      'X-Sign-Nonce': nonce,
      'X-Sign': sign
    },
    body: JSON.stringify(data)
  });

  return await response.json();
}

// 使用示例
const requestData = {
  background: "我是一名软件工程师，工作5年，目前在一家互联网公司",
  question: "如何提升职业发展？",
  birthYear: 1990,
  gender: "男",
  category: "职业规划"
};

const result = await callLifeAI(accessToken, requestData);
console.log(result);
```

### React 示例

```jsx
import React, { useState } from 'react';
import axios from 'axios';

function LifeAIForm() {
  const [formData, setFormData] = useState({
    background: '',
    question: '',
    birthYear: new Date().getFullYear() - 30,
    gender: '男',
    category: '职业规划'
  });
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);

    try {
      // Axios 拦截器会自动添加签名头
      const response = await axios.post('/api/web-auth/lifeai', formData);
      setResult(response.data);
    } catch (error) {
      console.error('请求失败:', error);
      alert(error.response?.data?.message || '请求失败');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <h2>LifeAI 咨询</h2>
      <form onSubmit={handleSubmit}>
        <div>
          <label>背景描述:</label>
          <textarea
            value={formData.background}
            onChange={(e) => setFormData({...formData, background: e.target.value})}
            maxLength={1000}
            placeholder="请描述您的背景情况..."
            required
          />
        </div>
        <div>
          <label>问题:</label>
          <textarea
            value={formData.question}
            onChange={(e) => setFormData({...formData, question: e.target.value})}
            maxLength={500}
            placeholder="请输入您的问题..."
            required
          />
        </div>
        <div>
          <label>出生年份:</label>
          <input
            type="number"
            value={formData.birthYear}
            onChange={(e) => setFormData({...formData, birthYear: parseInt(e.target.value)})}
            min={1900}
            max={new Date().getFullYear()}
            required
          />
        </div>
        <div>
          <label>性别:</label>
          <select
            value={formData.gender}
            onChange={(e) => setFormData({...formData, gender: e.target.value})}
            required
          >
            <option value="男">男</option>
            <option value="女">女</option>
          </select>
        </div>
        <div>
          <label>分类:</label>
          <select
            value={formData.category}
            onChange={(e) => setFormData({...formData, category: e.target.value})}
            required
          >
            <option value="职业规划">职业规划</option>
            <option value="情感咨询">情感咨询</option>
            <option value="学业指导">学业指导</option>
            <option value="人际关系">人际关系</option>
            <option value="健康生活">健康生活</option>
            <option value="财务管理">财务管理</option>
            <option value="创业咨询">创业咨询</option>
            <option value="其他">其他</option>
          </select>
        </div>
        <button type="submit" disabled={loading}>
          {loading ? '提交中...' : '提交'}
        </button>
      </form>

      {result && (
        <div>
          <h3>回答:</h3>
          <p>请求ID: {result.data.requestId}</p>
          <p>回答: {result.data.answer}</p>
          <p>时间: {new Date(result.data.timestamp).toLocaleString()}</p>
        </div>
      )}
    </div>
  );
}

export default LifeAIForm;
```

### cURL 示例

```bash
# 1. 先登录获取 Access Token
curl -X POST http://localhost:8080/api/web-auth/login \
  -H "Content-Type: application/json" \
  -H "X-Sign-Timestamp: 1738000000000" \
  -H "X-Sign-Nonce: a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6" \
  -H "X-Sign: [生成的签名]" \
  -d '{
    "email": "test@example.com",
    "password": "hashed_password",
    "randomSalt": "random_salt"
  }'

# 2. 使用获取的 Token 调用 LifeAI 接口
curl -X POST http://localhost:8080/api/web-auth/lifeai \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "X-Sign-Timestamp: $(date +%s)000" \
  -H "X-Sign-Nonce: a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6" \
  -H "X-Sign: [生成的签名]" \
  -d '{
    "background": "我是一名软件工程师，工作5年",
    "question": "如何提升职业发展？",
    "birthYear": 1990,
    "gender": "男",
    "category": "职业规划"
  }'
```

## 安全机制

### 1. Token 验证
- 验证 Token 是否有效
- 验证 Token 是否为 Access Token（非 Refresh Token）
- 验证用户是否存在
- 验证账户是否被禁用

### 2. 签名验证
- 验证时间戳是否在5分钟有效期内
- 验证随机串是否为32位
- 验证签名是否匹配
- 详见 [API_SIGNATURE.md](API_SIGNATURE.md)

### 3. 参数验证
- 验证所有必填字段
- 验证字段格式和长度
- 验证数据范围（如出生年份）
- 防止 SQL 注入和 XSS 攻击

## 业务逻辑（待实现）

当前版本的 `answer` 字段返回固定值 `"业务逻辑待实现"`。后续版本将实现以下功能：

### 计划功能
1. **AI 分析引擎**
   - 集成 AI 模型（如 GPT、Gemini 等）
   - 根据用户背景和问题生成个性化建议

2. **上下文理解**
   - 分析用户的年龄、性别、背景
   - 结合分类提供针对性建议

3. **知识库**
   - 建立各领域的知识库
   - 提供专业的咨询服务

4. **历史记录**
   - 保存用户的咨询历史
   - 支持查看历史咨询记录
   - 支持上下文连续对话

5. **评价反馈**
   - 用户对回答的评价
   - 持续优化回答质量

## 使用限制

### 频率限制（待实现）
- 每个用户每天最多咨询 10 次
- 每小时最多咨询 5 次
- 超过限制返回 429 Too Many Requests

### 内容审核（待实现）
- 敏感词过滤
- 违规内容检测
- 恶意请求拦截

## 测试建议

### 单元测试
- 测试参数验证逻辑
- 测试 Token 验证逻辑
- 测试响应格式

### 集成测试
- 测试完整的请求流程
- 测试各种错误场景
- 测试边界条件

### 压力测试
- 测试并发请求
- 测试大量数据
- 测试系统稳定性

## 常见问题

### Q1: 为什么需要 Token？
A: LifeAI 是需要登录才能使用的服务，Token 用于验证用户身份和权限。

### Q2: 为什么需要签名验证？
A: 签名验证可以防止参数被篡改和重放攻击，保证请求的安全性。

### Q3: 背景描述和问题有什么区别？
A: 背景描述用于提供上下文信息，问题是具体要咨询的内容。例如：
- 背景: "我是一名大学生，学习计算机专业"
- 问题: "毕业后应该选择读研还是工作？"

### Q4: 分类字段有固定的选项吗？
A: 目前没有强制限制，但建议使用常见分类（职业规划、情感咨询等），方便后续统计和分析。

### Q5: 性别字段必须填写吗？
A: 是的，性别是必填字段，用于提供更个性化的建议。

### Q6: 出生年份用来做什么？
A: 用于了解用户的年龄段，不同年龄段可能有不同的需求和关注点。

## 更新日志

### v1.0.0 (2026-01-27)
- ✅ 实现基本的接口框架
- ✅ 实现 Token 验证
- ✅ 实现参数验证
- ✅ 实现签名验证
- ⏳ 业务逻辑待实现

## 总结

LifeAI 接口提供了完整的安全验证机制，包括：
- ✅ Token 认证
- ✅ 签名验证
- ✅ 参数验证
- ✅ 错误处理

目前已经完成了接口的基础框架，具体的 AI 分析业务逻辑将在后续版本中实现。开发者可以先对接接口进行测试，确保前端逻辑正确。
