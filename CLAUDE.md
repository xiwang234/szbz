# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

这是一个基于 Spring Boot 3.2.0 的四柱八字(八字命理)计算 API 服务。项目使用公历日期计算传统中国命理学中的八字排盘,包括年柱、月柱、日柱和时柱的天干地支组合。

**新增功能**: 集成了 Google Gemini AI，可以对计算出的八字结果进行智能分析，提供五行分析、用神喜忌、性格特点、事业财运等多维度的命理解读。

## 常用命令

### 构建和运行
```bash
# 编译项目
mvn clean compile

# 运行测试
mvn test

# 运行单个测试类
mvn test -Dtest=BaZiServiceTest

# 运行单个测试方法
mvn test -Dtest=BaZiServiceTest#testCalculate_20251124_1648

# 打包应用
mvn clean package

# 运行应用(默认端口8080)
mvn spring-boot:run

# 直接运行打包后的jar
java -jar target/szbzApi-0.0.1-SNAPSHOT.jar
```

### 手动测试
```bash
# 运行BaZiCalculator进行手动测试
mvn compile exec:java -Dexec.mainClass="xw.szbz.cn.BaZiCalculator"
```

### API测试
```bash
# POST方式请求
curl -X POST http://localhost:8080/api/bazi/generate \
  -H "Content-Type: application/json" \
  -d '{"gender":"男","year":1984,"month":11,"day":23,"hour":23}'

# GET方式请求
curl "http://localhost:8080/api/bazi/generate?gender=男&year=1984&month=11&day=23&hour=23"

# 使用 Gemini AI 分析八字
curl -X POST http://localhost:8080/api/bazi/analyze \
  -H "Content-Type: application/json" \
  -d '{"gender":"男","year":1984,"month":11,"day":23,"hour":23}'
```

## 核心架构

### 包结构
- `xw.szbz.cn.controller`: REST API控制器层,处理HTTP请求
- `xw.szbz.cn.service`: 业务逻辑层
  - `BaZiService`: 核心八字计算逻辑
  - `GeminiService`: Gemini AI 分析服务
- `xw.szbz.cn.model`: 数据模型,包括请求/响应对象
- `xw.szbz.cn.enums`: 枚举类,定义天干(TianGan)、地支(DiZhi)、性别(Gender)

### 八字计算原理

**四柱计算顺序**:
1. **年柱**: 根据公历年份使用固定公式计算,基准年4年推算天干地支
2. **月柱**: 基于节气月(非公历月也非农历月),使用"五虎遁月"口诀,根据年干推算月干
3. **日柱**: 使用基准日期(1985年5月14日癸丑日)推算任意日期的干支
4. **时柱**: 根据出生时辰确定时支,使用"五鼠遁时"口诀根据日干推算时干

**关键算法**:
- 年柱公式: `年干 = (年份-4)%10`, `年支 = (年份-4)%12`
- 月柱: 使用节气分界,简化算法按公历日期近似推算节气月
- 日柱: 使用已知基准日累加天数推算
- 时柱: 时支固定(子时0/23点,丑时1-2点...), 时干根据日干推算

**特殊处理**:
- **子时跨日**: 23点属于次日子时,日柱需要按次日计算,但年柱和月柱仍按原日期(参见 `BaZiService.java:30-44`)
- **节气月**: 月柱使用节气月而非公历月,立春为年的开始(参见 `BaZiService.java:115-169`)
- **性别支持**: 支持中文("男"/"女")和英文("male"/"female")输入

### 数据流
1. 请求通过 `BaZiController` 接收(支持GET/POST)
2. 参数验证: 年份1900-2100,月份1-12,日期1-31,小时0-23
3. `BaZiService.calculate()` 执行八字计算
4. 返回 `BaZiResult` 包含四柱干支、完整八字字符串、性别和出生信息

### 测试覆盖
测试文件位于 `src/test/java/xw/szbz/cn/`,包含:
- 单元测试: 各个柱的独立计算测试
- 集成测试: 完整八字计算验证,包含7个真实案例
- 边界测试: 子时跨日、年末跨年等特殊场景
- 枚举测试: 天干、地支、性别枚举的正确性

### 配置
- 应用配置: `src/main/resources/application.properties`
- 默认端口: 8080
- Java版本: 17
- Spring Boot版本: 3.2.0
- Gemini API: 需要配置 `gemini.api.key` (获取地址: https://ai.google.dev/)

## Gemini AI 集成

### 功能说明
项目集成了 Google Gemini AI (版本 1.28.0)，可对八字结果进行智能分析。`GeminiService` 提供了 `analyzeBaZi(BaZiResult)` 方法，可在任何 Service 中调用。

### 配置要求
1. 在 `application.properties` 中配置 API Key: `gemini.api.key=YOUR_KEY`
2. 或通过环境变量: `GEMINI_API_KEY=YOUR_KEY`
3. 可选配置模型: `gemini.model=gemini-2.0-flash-exp` (默认)

### API 端点
- `/api/bazi/generate`: 仅计算八字，不调用 AI
- `/api/bazi/analyze`: 计算八字并使用 Gemini AI 分析

详细使用说明请参阅 `GEMINI_USAGE.md`。

## 开发注意事项

### 修改计算逻辑时
- 所有八字计算核心逻辑在 `BaZiService` 中
- 修改后必须运行完整测试套件确保已有案例仍然正确
- 节气日期是简化算法,如需精确计算需要引入天文算法库

### 添加新功能
- API接口定义在 `BaZiController`
- 统一异常处理在 `GlobalExceptionHandler`
- 新增枚举值需同步更新对应的测试

### 测试数据
测试用例使用的真实八字案例已通过专业八字工具验证,修改计算逻辑时不应改变这些测试用例的预期结果,除非发现原有逻辑确实有误。
