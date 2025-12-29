# 六壬预测API使用指南

## 概述

本文档描述了如何使用六壬预测API进行占卜预测。该API集成了大六壬金口直断的专业提示词模板，能够生成通俗易懂的预测报告。

## 核心组件

### 1. 提示词模板文件
**位置**: `src/main/resources/prompts/liuren_prediction_template.txt`

模板包含完整的六壬分析逻辑和输出规范，支持以下变量替换：
- `${courseInfo}` - 课传信息
- `${question}` - 占问事项
- `${background}` - 占问背景
- `${birthYear}` - 出生年份（本命干支）

### 2. 工具类
**类名**: `PromptTemplateUtil`
**位置**: `src/main/java/xw/szbz/cn/util/PromptTemplateUtil.java`

提供模板加载和变量替换功能：
```java
// 六壬预测专用方法
public String renderLiuRenTemplate(String courseInfo, String question, 
                                   String background, String birthYear)

// 通用模板渲染方法
public String renderTemplate(String templatePath, Map<String, String> variables)
```

### 3. 请求模型
**类名**: `LiuRenRequest`
**位置**: `src/main/java/xw/szbz/cn/model/LiuRenRequest.java`

请求参数：
```java
{
  "courseInfo": "课传信息（如四课三传、天地盘等）",
  "question": "占问事项（如求财、求职、问感情等）",
  "background": "占问背景（可选，提供更多上下文信息）",
  "birthYear": "出生年份（用于判断本命干支）"
}
```

### 4. API接口
**接口**: `POST /api/bazi/liuren/predict`
**Controller**: `BaZiController`

## API使用示例

### 请求示例

#### 1. 获取Token（首次需要登录）
```bash
curl -X POST http://localhost:8080/api/bazi/login \
  -H "Content-Type: application/json" \
  -d '{
    "code": "微信小程序code"
  }'
```

响应：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresAt": 1735564800000
  }
}
```

#### 2. 调用六壬预测接口

**准备签名**（使用 `SignatureUtil`）：
```java
// 1. 准备参数
Map<String, Object> params = new HashMap<>();
params.put("courseInfo", "辰月戊寅日...");
params.put("question", "问近期财运");
params.put("background", "最近想投资");
params.put("birthYear", "1984");

// 2. 生成时间戳
Long timestamp = System.currentTimeMillis();

// 3. 生成签名
String sign = signatureUtil.generateSignature(params, timestamp);
```

**发送请求**：
```bash
curl -X POST http://localhost:8080/api/bazi/liuren/predict \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "X-Timestamp: 1703001234567" \
  -H "X-Sign: abc123def456..." \
  -d '{
    "courseInfo": "辰月戊寅日申时，日干戊土，支上神申金，干上神子水。初传子水（发端门），中传巳火（移易门），末传未土（归计门）。贵人登天门，青龙临财爻，白虎乘官鬼，朱雀落空亡。",
    "question": "问近期财运如何？",
    "background": "最近想投资一个项目，不知道能否顺利。",
    "birthYear": "1984"
  }'
```

### 响应示例

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "prediction": "## 预测结论\n这次投资能成，但过程会有波折...",
    "courseInfo": "辰月戊寅日申时...",
    "question": "问近期财运如何？"
  }
}
```

## 提示词模板特点

### 1. 零术语化输出
- ❌ 错误：目前会有口舌争吵（朱雀临门）
- ✅ 正确：目前会发生激烈的争吵

### 2. 结构化分析
输出包含四个部分：
1. **预测结论** - 直接回答问题（能/不能，吉/凶）
2. **现状分析** - 根据初传分析目前局面
3. **走向推演** - 根据中传分析发展过程
4. **最终结局** - 根据末传分析最终结果

### 3. 深度分析逻辑
模板内置12个分析维度：
- 定课体（元首、重审、伏吟等）
- 审干支（日干日支关系）
- 看三传（初中末传）
- 查神煞（贵人、青龙、朱雀等）
- 参本命（冲克关系）
- 占断八门
- 遁干深挖（财遁暗鬼、鬼遁暗财）
- 空亡辨证
- 应期判断

## 安全特性

### 1. JWT认证
所有请求必须提供有效的JWT Token

### 2. 时间戳验证
请求时间戳不能超过2秒（防重放攻击）

### 3. 签名验证
使用HMAC-SHA256算法验证参数完整性

### 4. 业务日志
所有请求都会记录到 `syslog/business_YYYYMMDD.log`

## 集成到现有业务

### 在Service中使用模板
```java
@Service
public class MyService {
    
    @Autowired
    private PromptTemplateUtil promptTemplateUtil;
    
    @Autowired
    private GeminiService geminiService;
    
    public String predict(String courseInfo, String question, 
                         String background, String birthYear) {
        // 1. 渲染模板
        String prompt = promptTemplateUtil.renderLiuRenTemplate(
            courseInfo, question, background, birthYear
        );
        
        // 2. 调用AI
        String prediction = geminiService.generateContent(prompt);
        
        return prediction;
    }
}
```

### 创建自定义模板
```java
// 1. 在 resources/prompts/ 目录创建模板文件
// 2. 使用 ${变量名} 作为占位符
// 3. 使用工具类加载模板

Map<String, String> variables = new HashMap<>();
variables.put("customVar1", "value1");
variables.put("customVar2", "value2");

String result = promptTemplateUtil.renderTemplate(
    "prompts/my_custom_template.txt", 
    variables
);
```

## 注意事项

1. **API Key配置**
   - 在 `application.properties` 中配置 `yesCode.api.key`
   - 否则会抛出 `IllegalStateException`

2. **模板文件位置**
   - 开发环境：`src/main/resources/prompts/`
   - 生产环境：会自动从jar包中读取

3. **字符编码**
   - 模板文件使用UTF-8编码
   - 支持中文内容

4. **缓存机制**
   - 当前未对六壬预测结果缓存
   - 如需缓存，可参考八字分析的缓存实现

## 扩展建议

1. **添加缓存**
   - 使用 `BaZiCacheService` 为六壬预测添加缓存
   - Key可以是 `courseInfo + question` 的hash值

2. **批量预测**
   - 创建批量预测接口
   - 支持一次请求预测多个问题

3. **历史记录**
   - 存储用户的预测历史
   - 提供查询历史预测的接口

4. **模板管理**
   - 创建模板管理界面
   - 支持在线编辑和版本控制

## 相关文件清单

```
项目根目录/
├── src/main/java/xw/szbz/cn/
│   ├── controller/
│   │   └── BaZiController.java          # 六壬预测接口
│   ├── model/
│   │   └── LiuRenRequest.java           # 请求模型
│   ├── service/
│   │   └── GeminiService.java           # AI服务（新增generateContent方法）
│   └── util/
│       └── PromptTemplateUtil.java      # 模板工具类
└── src/main/resources/
    └── prompts/
        └── liuren_prediction_template.txt  # 提示词模板
```

## 技术支持

如有问题，请查看：
- 业务日志：`syslog/business_YYYYMMDD.log`
- 应用日志：控制台输出
- API文档：`API_SECURITY_GUIDE.md`
