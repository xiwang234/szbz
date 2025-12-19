# Spring Boot 后台启动脚本
# 用于在后台启动四柱八字API服务

Write-Host "====================================" -ForegroundColor Cyan
Write-Host "  四柱八字API服务后台启动脚本" -ForegroundColor Cyan
Write-Host "====================================" -ForegroundColor Cyan
Write-Host ""

# 检查Redis服务
Write-Host "1. 检查Redis服务..." -ForegroundColor Yellow
$redisPort = netstat -ano | findstr ":6379" | findstr "LISTENING"
if ($redisPort) {
    Write-Host "   ✓ Redis服务已运行" -ForegroundColor Green
} else {
    Write-Host "   ✗ Redis服务未运行！请先启动Redis" -ForegroundColor Red
    exit
}
Write-Host ""

# 检查8080端口
Write-Host "2. 检查端口占用..." -ForegroundColor Yellow
$port8080 = netstat -ano | findstr ":8080" | findstr "LISTENING"
if ($port8080) {
    Write-Host "   ✗ 端口8080已被占用！" -ForegroundColor Red
    exit
} else {
    Write-Host "   ✓ 端口8080可用" -ForegroundColor Green
}
Write-Host ""

# 编译并打包
Write-Host "3. 编译打包..." -ForegroundColor Yellow
mvn clean package -DskipTests -q
if ($LASTEXITCODE -eq 0) {
    Write-Host "   ✓ 打包成功" -ForegroundColor Green
} else {
    Write-Host "   ✗ 打包失败！" -ForegroundColor Red
    exit
}
Write-Host ""

# 后台启动
Write-Host "4. 后台启动服务..." -ForegroundColor Yellow
$jarFile = "target\szbzApi-0.0.1-SNAPSHOT.jar"

if (Test-Path $jarFile) {
    # 启动Java进程（后台运行）
    $process = Start-Process -FilePath "java" -ArgumentList "-jar", $jarFile -PassThru -WindowStyle Hidden -RedirectStandardOutput "logs\output.log" -RedirectStandardError "logs\error.log"
    
    Write-Host "   ✓ 服务已启动（后台运行）" -ForegroundColor Green
    Write-Host "   进程ID: $($process.Id)" -ForegroundColor Cyan
    Write-Host ""
    
    # 等待服务启动
    Write-Host "5. 等待服务就绪..." -ForegroundColor Yellow
    $maxRetry = 30
    $retry = 0
    $started = $false
    
    while ($retry -lt $maxRetry) {
        Start-Sleep -Seconds 1
        $checkPort = netstat -ano | findstr ":8080" | findstr "LISTENING"
        if ($checkPort) {
            $started = $true
            break
        }
        $retry++
        Write-Host "   等待中... ($retry/$maxRetry)" -NoNewline -ForegroundColor Gray
        Write-Host "`r" -NoNewline
    }
    
    Write-Host ""
    if ($started) {
        Write-Host "   ✓ 服务启动成功！" -ForegroundColor Green
        Write-Host ""
        Write-Host "====================================" -ForegroundColor Green
        Write-Host "    服务信息" -ForegroundColor Green
        Write-Host "====================================" -ForegroundColor Green
        Write-Host "  URL: http://localhost:8080" -ForegroundColor Cyan
        Write-Host "  API: http://localhost:8080/api/bazi/analyze" -ForegroundColor Cyan
        Write-Host "  PID: $($process.Id)" -ForegroundColor Cyan
        Write-Host "  日志: logs\output.log" -ForegroundColor Cyan
        Write-Host "  错误: logs\error.log" -ForegroundColor Cyan
        Write-Host "====================================" -ForegroundColor Green
        Write-Host ""
        Write-Host "停止服务命令:" -ForegroundColor Yellow
        Write-Host "  Stop-Process -Id $($process.Id)" -ForegroundColor Gray
    } else {
        Write-Host "   ✗ 服务启动超时！请检查日志" -ForegroundColor Red
        Write-Host "   查看错误日志: cat logs\error.log" -ForegroundColor Yellow
    }
} else {
    Write-Host "   ✗ JAR文件不存在: $jarFile" -ForegroundColor Red
}
