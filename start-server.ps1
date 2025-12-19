# Spring Boot 启动脚本
# 用于启动四柱八字API服务

Write-Host "====================================" -ForegroundColor Cyan
Write-Host "    四柱八字API服务启动脚本" -ForegroundColor Cyan
Write-Host "====================================" -ForegroundColor Cyan
Write-Host ""

# 检查Redis服务
Write-Host "1. 检查Redis服务..." -ForegroundColor Yellow
$redisPort = netstat -ano | findstr ":6379" | findstr "LISTENING"
if ($redisPort) {
    Write-Host "   ✓ Redis服务已运行 (端口6379)" -ForegroundColor Green
} else {
    Write-Host "   ✗ Redis服务未运行！" -ForegroundColor Red
    Write-Host "   请先启动Redis服务: redis-server" -ForegroundColor Yellow
    Write-Host ""
    $continue = Read-Host "是否继续启动？(y/n)"
    if ($continue -ne "y" -and $continue -ne "Y") {
        exit
    }
}
Write-Host ""

# 检查8080端口
Write-Host "2. 检查端口占用..." -ForegroundColor Yellow
$port8080 = netstat -ano | findstr ":8080" | findstr "LISTENING"
if ($port8080) {
    Write-Host "   ✗ 端口8080已被占用！" -ForegroundColor Red
    Write-Host "   $port8080" -ForegroundColor Gray
    Write-Host ""
    $kill = Read-Host "是否终止占用进程？(y/n)"
    if ($kill -eq "y" -or $kill -eq "Y") {
        $pid = ($port8080 -split '\s+')[-1]
        Write-Host "   正在终止进程 PID: $pid" -ForegroundColor Yellow
        Stop-Process -Id $pid -Force
        Start-Sleep -Seconds 2
        Write-Host "   ✓ 进程已终止" -ForegroundColor Green
    } else {
        exit
    }
} else {
    Write-Host "   ✓ 端口8080可用" -ForegroundColor Green
}
Write-Host ""

# 编译项目
Write-Host "3. 编译项目..." -ForegroundColor Yellow
$compileResult = mvn clean compile -DskipTests -q 2>&1
if ($LASTEXITCODE -eq 0) {
    Write-Host "   ✓ 编译成功" -ForegroundColor Green
} else {
    Write-Host "   ✗ 编译失败！" -ForegroundColor Red
    Write-Host $compileResult
    exit
}
Write-Host ""

# 启动服务
Write-Host "4. 启动Spring Boot服务..." -ForegroundColor Yellow
Write-Host "   提示: 按 Ctrl+C 可停止服务" -ForegroundColor Gray
Write-Host ""
Write-Host "====================================" -ForegroundColor Cyan
Write-Host ""

# 启动服务
mvn spring-boot:run
