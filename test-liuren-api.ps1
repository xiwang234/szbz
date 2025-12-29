# 六壬预测API测试脚本
# 用法: .\test-liuren-api.ps1

$baseUrl = "http://localhost:8080/api/bazi"

Write-Host "================================" -ForegroundColor Cyan
Write-Host "六壬预测API测试" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan
Write-Host ""

# Step 1: 登录获取Token（如果已有token可以跳过此步骤）
Write-Host "[Step 1] 模拟登录获取Token..." -ForegroundColor Yellow
Write-Host "注意: 实际使用时需要提供真实的微信小程序code" -ForegroundColor Gray
Write-Host ""

# 这里使用模拟token，实际使用时需要先调用login接口
$token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0X29wZW5pZCIsImlhdCI6MTcwMzAwMDAwMCwiZXhwIjoxNzAzMDg2NDAwfQ.test_signature"
Write-Host "使用测试Token: $token" -ForegroundColor Green
Write-Host ""

# Step 2: 准备六壬预测请求参数
Write-Host "[Step 2] 准备六壬预测参数..." -ForegroundColor Yellow

$requestBody = @{
    question = "问近期投资项目能否成功？"
    background = "最近有朋友邀请我一起投资一个互联网项目，需要投入50万，承诺半年回本，一年翻倍。项目看起来很好，但心里没底，想问问这个投资能不能做？"
    birthYear = 1984
    gender = "男"
} | ConvertTo-Json -Depth 10

Write-Host "请求参数:" -ForegroundColor Gray
Write-Host $requestBody -ForegroundColor Gray
Write-Host ""

# Step 3: 生成时间戳
$timestamp = [DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds()
Write-Host "[Step 3] 生成时间戳: $timestamp" -ForegroundColor Yellow
Write-Host ""

# Step 4: 生成签名（实际使用时需要使用SignatureUtil生成）
Write-Host "[Step 4] 生成签名..." -ForegroundColor Yellow
Write-Host "注意: 这里使用模拟签名，实际使用需要通过SignatureUtil.generateSignature()生成" -ForegroundColor Gray
$sign = "mock_signature_for_testing"
Write-Host "签名: $sign" -ForegroundColor Green
Write-Host ""

# Step 5: 发送请求
Write-Host "[Step 5] 发送六壬预测请求..." -ForegroundColor Yellow
Write-Host "URL: $baseUrl/liuren/predict" -ForegroundColor Gray
Write-Host ""

try {
    $headers = @{
        "Content-Type" = "application/json"
        "Authorization" = "Bearer $token"
        "X-Timestamp" = $timestamp.ToString()
        "X-Sign" = $sign
    }

    Write-Host "请求Headers:" -ForegroundColor Gray
    $headers.GetEnumerator() | ForEach-Object {
        Write-Host "  $($_.Key): $($_.Value)" -ForegroundColor Gray
    }
    Write-Host ""

    $response = Invoke-RestMethod -Uri "$baseUrl/liuren/predict" `
        -Method Post `
        -Headers $headers `
        -Body $requestBody `
        -ContentType "application/json"

    Write-Host "[响应结果]" -ForegroundColor Green
    Write-Host "================================" -ForegroundColor Green
    Write-Host "状态码: $($response.code)" -ForegroundColor Cyan
    Write-Host "消息: $($response.message)" -ForegroundColor Cyan
    Write-Host ""
    
    if ($response.code -eq 200) {
        Write-Host "预测结果:" -ForegroundColor Yellow
        Write-Host "--------------------------------" -ForegroundColor Yellow
        Write-Host $response.data.prediction -ForegroundColor White
        Write-Host "--------------------------------" -ForegroundColor Yellow
        Write-Host ""
        Write-Host "课传信息: $($response.data.courseInfo)" -ForegroundColor Gray
        Write-Host "占问事项: $($response.data.question)" -ForegroundColor Gray
        Write-Host "出生信息: $($response.data.birthInfo)" -ForegroundColor Gray
    } else {
        Write-Host "错误信息: $($response.message)" -ForegroundColor Red
    }

} catch {
    Write-Host "[错误]" -ForegroundColor Red
    Write-Host "请求失败: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
    Write-Host "可能的原因:" -ForegroundColor Yellow
    Write-Host "1. 服务器未启动（请运行 .\start-server.ps1）" -ForegroundColor Gray
    Write-Host "2. 端口号错误（默认8080）" -ForegroundColor Gray
    Write-Host "3. Token无效或已过期" -ForegroundColor Gray
    Write-Host "4. 签名验证失败" -ForegroundColor Gray
    Write-Host "5. 时间戳超过2秒" -ForegroundColor Gray
}

Write-Host ""
Write-Host "================================" -ForegroundColor Cyan
Write-Host "测试完成" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "提示: 要使用真实的API，需要:" -ForegroundColor Yellow
Write-Host "1. 先调用 /api/bazi/login 获取真实token" -ForegroundColor Gray
Write-Host "2. 使用 SignatureUtil.generateSignature() 生成真实签名" -ForegroundColor Gray
Write-Host "3. 确保Gemini API Key已配置在application.properties中" -ForegroundColor Gray
Write-Host ""
