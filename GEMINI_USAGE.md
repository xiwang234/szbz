# Gemini AI 八字分析功能使用指南

本文档介绍如何使用集成的 Google Gemini AI 进行八字分析。

## 配置

### 1. 获取 Gemini API Key

访问 [Google AI Studio](https://ai.google.dev/) 申请免费的 Gemini API Key。

### 2. 配置 API Key

在 `src/main/resources/application.properties` 中配置您的 API Key：

```properties
gemini.api.key=YOUR_ACTUAL_API_KEY_HERE
gemini.model=gemini-2.0-flash-exp
```

或者通过环境变量配置（推荐用于生产环境）：

```bash
export GEMINI_API_KEY=your_actual_api_key_here
```

## 使用方式

### 1. 通过 API 调用

#### POST 方式

```bash
curl -X POST http://localhost:8080/api/bazi/analyze \
  -H "Content-Type: application/json" \
  -d '{
    "gender": "男",
    "year": 1984,
    "month": 11,
    "day": 23,
    "hour": 23
  }'
```

#### GET 方式

```bash
curl "http://localhost:8080/api/bazi/analyze?gender=男&year=1984&month=11&day=23&hour=23"
```

#### 响应示例

```json
{
  "baziResult": {
    "gender": "男",
    "yearPillar": {
      "tianGan": "甲",
      "diZhi": "子",
      "fullName": "甲子"
    },
    "monthPillar": {
      "tianGan": "乙",
      "diZhi": "亥",
      "fullName": "乙亥"
    },
    "dayPillar": {
      "tianGan": "壬",
      "diZhi": "戌",
      "fullName": "壬戌"
    },
    "hourPillar": {
      "tianGan": "庚",
      "diZhi": "子",
      "fullName": "庚子"
    },
    "fullBaZi": "甲子 乙亥 壬戌 庚子",
    "birthInfo": {
      "year": 1984,
      "month": 11,
      "day": 23,
      "hour": 23,
      "shiChen": "子时",
      "adjusted": true,
      "dayPillarDate": "1984-11-24"
    }
  },
  "aiAnalysis": "【这里是 Gemini AI 生成的详细八字分析内容】\n\n一、五行分析...\n\n二、日主强弱...\n..."
}
```

### 2. 在其他 Service 中调用

```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xw.szbz.cn.model.BaZiResult;
import xw.szbz.cn.service.BaZiService;
import xw.szbz.cn.service.GeminiService;

@Service
public class YourService {

    @Autowired
    private BaZiService baZiService;

    @Autowired
    private GeminiService geminiService;

    public void yourMethod() {
        // 1. 计算八字
        BaZiRequest request = new BaZiRequest("男", 1984, 11, 23, 23);
        BaZiResult baZiResult = baZiService.calculate(request);

        // 2. 使用 Gemini AI 分析
        String analysis = geminiService.analyzeBaZi(baZiResult);

        // 3. 使用分析结果
        System.out.println("AI 分析结果：" + analysis);
    }
}
```

## 运行测试

```bash
# 设置环境变量
export GEMINI_API_KEY=your_actual_api_key_here

# 运行所有测试
mvn test

# 只运行 Gemini 相关测试
mvn test -Dtest=GeminiServiceTest
```

## API 端点说明

| 端点 | 方法 | 说明 |
|------|------|------|
| `/api/bazi/generate` | POST/GET | 仅生成八字，不进行 AI 分析 |
| `/api/bazi/analyze` | POST/GET | 生成八字并使用 Gemini AI 进行分析 |

## 注意事项

1. **API Key 安全**：不要将 API Key 提交到版本控制系统，建议使用环境变量
2. **请求限制**：免费版 Gemini API 有请求频率限制，请注意使用
3. **网络要求**：需要能够访问 Google API 服务
4. **响应时间**：AI 分析需要调用外部 API，响应时间会比纯八字计算长（通常 2-5 秒）

## Gemini 分析内容

AI 会从以下方面分析八字：

1. **五行分析**：金、木、水、火、土的分布情况
2. **日主强弱**：日柱天干的强弱判断
3. **用神喜忌**：确定用神和忌神
4. **性格特点**：根据八字推断的性格特征
5. **事业财运**：事业和财运方面的分析
6. **健康建议**：健康方面的注意事项
7. **综合评价**：总体的命理评价

## 故障排除

### 错误：未配置 API Key

```
IllegalStateException: Gemini API key 未配置
```

**解决方案**：在 `application.properties` 中设置 `gemini.api.key`

### 错误：API 调用失败

```
RuntimeException: 调用 Gemini API 失败
```

**可能原因**：
- API Key 无效
- 网络连接问题
- API 服务不可用
- 超出请求限制

## 参考资料

- [Google Gemini API 文档](https://ai.google.dev/gemini-api/docs)
- [Google GenAI Java SDK](https://github.com/googleapis/java-genai)
- [Maven Central - google-genai](https://central.sonatype.com/artifact/com.google.genai/google-genai)
