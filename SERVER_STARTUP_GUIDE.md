# 服务启动指南

## 🚀 快速启动

### 方式一：前台启动（推荐用于开发调试）

```powershell
# 在PowerShell中执行
.\start-server.ps1
```

**特点**：
- ✅ 自动检查Redis服务
- ✅ 自动检查端口占用
- ✅ 自动编译项目
- ✅ 实时显示日志
- ⚠️ 终端窗口不能关闭
- ⚠️ 按 `Ctrl+C` 停止服务

---

### 方式二：后台启动（推荐用于测试运行）

```powershell
# 在PowerShell中执行
.\start-server-background.ps1
```

**特点**：
- ✅ 自动打包为JAR
- ✅ 后台运行，不占用终端
- ✅ 日志输出到文件
- ✅ 返回进程ID

**停止服务**：
```powershell
# 使用显示的PID停止
Stop-Process -Id <PID>

# 或者查找并停止
jps -l | findstr szbz
Stop-Process -Id <找到的PID>
```

---

### 方式三：Maven直接启动

```powershell
# 切换到项目目录
cd d:/project/szbz

# 启动服务
mvn spring-boot:run
```

---

## 📋 启动前检查清单

### 1. Redis服务必须运行

**检查Redis**：
```powershell
netstat -ano | findstr :6379
```

**启动Redis**（如果未运行）：
```powershell
# Windows
redis-server.exe

# 或使用配置文件
redis-server.exe redis.conf
```

### 2. 端口8080未被占用

**检查端口**：
```powershell
netstat -ano | findstr :8080
```

**终止占用进程**（如果被占用）：
```powershell
# 查看PID
netstat -ano | findstr :8080

# 终止进程
Stop-Process -Id <PID> -Force
```

### 3. 配置文件正确

检查 `src/main/resources/application.properties`：
```properties
# Redis配置
spring.data.redis.host=localhost
spring.data.redis.port=6379

# JWT配置
jwt.secret=szbz-api-secret-key-for-wechat-miniprogram-authentication-2024

# Gemini API配置
yesCode.api.key=cr_eb5f1a47c692841a0f5408e48514c2b8d1e98f8024b6d6af14ffd60767195bf2
```

---

## 🔍 服务状态检查

### 检查服务是否运行

**方法1：检查端口**
```powershell
netstat -ano | findstr :8080
```
如果有输出且包含 `LISTENING`，说明服务已启动。

**方法2：检查Java进程**
```powershell
jps -l | findstr szbz
```

**方法3：访问健康检查接口**
```powershell
curl http://localhost:8080/api/bazi/generate?gender=男&year=1984&month=11&day=27&hour=0
```

---

## 🧪 测试接口

### 测试基础接口（无安全验证）

```powershell
# GET方式
curl http://localhost:8080/api/bazi/generate?gender=男&year=1984&month=11&day=27&hour=0
```

### 测试安全接口（含签名验证）

**使用测试脚本**：
```powershell
.\test-api.ps1
```

**或手动构建请求**（参见 [API_SECURITY_GUIDE.md](API_SECURITY_GUIDE.md)）

---

## 📊 启动过程说明

服务启动时会经历以下阶段：

```
1. Maven扫描项目
   ↓
2. 编译Java代码
   ↓
3. 处理资源文件
   ↓
4. 启动Spring Boot
   ↓
5. 初始化Spring容器
   ↓
6. 连接Redis
   ↓
7. 注册REST接口
   ↓
8. 监听8080端口
   ↓
9. ✓ 服务就绪
```

**预计启动时间**：30-60秒（首次启动）

---

## 🎯 启动成功标志

当您看到以下日志时，说明服务已成功启动：

```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.2.0)

...
Started SzbzApiApplication in X.XXX seconds
```

---

## ❌ 常见启动问题

### 问题1：端口8080已被占用

**错误信息**：
```
Port 8080 was already in use
```

**解决方法**：
```powershell
# 查找占用进程
netstat -ano | findstr :8080

# 终止进程
Stop-Process -Id <PID> -Force

# 或修改端口
# 在application.properties中修改：
server.port=8081
```

---

### 问题2：Redis连接失败

**错误信息**：
```
Unable to connect to Redis
Connection refused
```

**解决方法**：
```powershell
# 启动Redis
redis-server.exe

# 检查Redis状态
redis-cli ping
# 应该返回: PONG
```

---

### 问题3：编译失败

**错误信息**：
```
[ERROR] COMPILATION ERROR
```

**解决方法**：
```powershell
# 清理并重新编译
mvn clean compile

# 查看详细错误
mvn compile -e -X
```

---

### 问题4：依赖下载失败

**错误信息**：
```
Could not resolve dependencies
```

**解决方法**：
```powershell
# 清理本地仓库缓存
mvn clean -U

# 强制更新依赖
mvn clean install -U

# 配置国内镜像（如果下载慢）
# 编辑 ~/.m2/settings.xml
```

---

## 📝 日志查看

### 查看实时日志（前台启动）
直接在终端窗口查看

### 查看日志文件（后台启动）
```powershell
# 查看输出日志
Get-Content logs\output.log -Tail 50 -Wait

# 查看错误日志
Get-Content logs\error.log -Tail 50 -Wait
```

### 查看Spring Boot日志
```powershell
# 如果配置了日志文件
Get-Content logs\spring.log -Tail 100
```

---

## 🛑 停止服务

### 前台启动的服务
按 `Ctrl+C` 停止

### 后台启动的服务
```powershell
# 方法1：使用PID停止
Stop-Process -Id <PID>

# 方法2：查找并停止
$pid = (jps -l | findstr szbz | ForEach-Object { ($_ -split ' ')[0] })
Stop-Process -Id $pid

# 方法3：强制停止所有Java进程（谨慎使用）
Get-Process java | Stop-Process -Force
```

---

## 🔄 重启服务

```powershell
# 停止服务
Stop-Process -Id <PID>

# 等待端口释放
Start-Sleep -Seconds 3

# 重新启动
.\start-server.ps1
```

---

## 📚 相关文档

- [API安全使用指南](API_SECURITY_GUIDE.md) - 接口调用说明
- [安全功能特性](SECURITY_FEATURES.md) - 功能详解
- [测试脚本](test-api.ps1) - 自动化测试

---

## 🎯 快速开始示例

```powershell
# 1. 启动Redis（如果未运行）
redis-server.exe

# 2. 启动Spring Boot服务
.\start-server.ps1

# 3. 等待服务启动（约30-60秒）

# 4. 新开一个PowerShell窗口，测试接口
.\test-api.ps1

# 5. 或使用curl测试
curl http://localhost:8080/api/bazi/generate?gender=男&year=1984&month=11&day=27&hour=0
```

---

**提示**：首次启动可能需要下载依赖，请耐心等待。
