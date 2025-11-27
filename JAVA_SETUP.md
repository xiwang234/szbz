# Java 环境安装完成

## 已安装的软件

### 1. OpenJDK 17
- **版本**: OpenJDK 17.0.17
- **安装位置**: `/opt/homebrew/opt/openjdk@17`
- **验证命令**: `java -version`

### 2. Apache Maven
- **版本**: 3.9.11
- **安装位置**: `/opt/homebrew/bin/mvn`
- **验证命令**: `mvn -version`

## 环境配置

### 永久配置（已完成）
已将以下配置添加到 `~/.zshrc`：
```bash
export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"
```

### 临时配置（每次新终端会话）
如果不想重启终端，可以使用项目中的环境设置脚本：
```bash
source setup-env.sh
```
或
```bash
. setup-env.sh
```

## 项目操作

### 编译项目
```bash
mvn clean compile
```

### 运行测试
```bash
# 运行所有测试
mvn test

# 运行单个测试类
mvn test -Dtest=BaZiServiceTest

# 运行单个测试方法
mvn test -Dtest=BaZiServiceTest#testCalculate_20251124_1648
```

### 打包项目
```bash
mvn clean package
```

### 运行应用
```bash
# 方式1: 使用 Maven 插件运行
mvn spring-boot:run

# 方式2: 运行打包后的 jar 文件
java -jar target/szbzApi-0.0.1-SNAPSHOT.jar
```

## Gemini AI 配置

### 1. 获取 API Key
访问 https://ai.google.dev/ 申请免费的 API Key

### 2. 配置 API Key
编辑 `src/main/resources/application.properties`：
```properties
gemini.api.key=你的真实API_Key
```

或者设置环境变量（推荐）：
```bash
export GEMINI_API_KEY=你的真实API_Key
```

### 3. 测试 Gemini 集成
```bash
mvn test -Dtest=GeminiServiceTest
```

## 访问应用

启动应用后，可以通过以下方式访问：

### 1. 仅计算八字
```bash
curl -X POST http://localhost:8080/api/bazi/generate \
  -H "Content-Type: application/json" \
  -d '{"gender":"男","year":1984,"month":11,"day":23,"hour":23}'
```

### 2. 计算八字并使用 AI 分析
```bash
curl -X POST http://localhost:8080/api/bazi/analyze \
  -H "Content-Type: application/json" \
  -d '{"gender":"男","year":1984,"month":11,"day":23,"hour":23}'
```

## 注意事项

1. **首次启动**: 首次启动或测试时，Maven 会下载项目依赖，可能需要几分钟
2. **Java 版本**: 项目需要 Java 17，已安装 OpenJDK 17.0.17
3. **Gemini API**: 使用 AI 分析功能需要有效的 Gemini API Key
4. **端口**: 应用默认运行在 8080 端口，确保端口未被占用

## 故障排除

### Java 命令找不到
```bash
# 运行环境设置脚本
source setup-env.sh

# 或者重启终端让 ~/.zshrc 生效
```

### Maven 命令找不到
```bash
# 使用绝对路径
/opt/homebrew/bin/mvn --version
```

### 编译错误
```bash
# 清理并重新编译
mvn clean compile
```

### Gemini API 调用失败
- 检查 API Key 是否正确配置
- 确认网络可以访问 Google API 服务
- 检查是否超出 API 使用限制

## 相关文档

- `CLAUDE.md` - 项目架构和开发指南
- `GEMINI_USAGE.md` - Gemini AI 集成详细使用说明
- `GEMINI_INTEGRATION_SUMMARY.md` - Gemini 集成总结
- `README.md` - 项目基本信息
