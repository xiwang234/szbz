# API测试脚本 - PowerShell版本
# 用于快速测试八字分析接口的安全功能

Write-Host "====================================" -ForegroundColor Cyan
Write-Host "    八字分析API安全功能测试" -ForegroundColor Cyan
Write-Host "====================================" -ForegroundColor Cyan
Write-Host ""

# 配置参数
$baseUrl = "http://localhost:8080"
$apiPath = "/api/bazi/analyze"
$signKey = "szbz-api-sign-key-2024"

# 请求参数
$params = @{
    openId = "oABCD1234567890"
    gender = "男"
    year = 1984
    month = 11
    day = 27
    hour = 0
}

Write-Host "1. 准备请求参数" -ForegroundColor Yellow
Write-Host "   OpenId: $($params.openId)"
Write-Host "   性别: $($params.gender)"
Write-Host "   出生日期: $($params.year)年$($params.month)月$($params.day)日 $($params.hour)时"
Write-Host ""

# 获取当前时间戳（毫秒）
$timestamp = [DateTimeOffset]::Now.ToUnixTimeMilliseconds()
Write-Host "2. 生成时间戳" -ForegroundColor Yellow
Write-Host "   Timestamp: $timestamp"
Write-Host ""

# 生成签名
Write-Host "3. 计算签名" -ForegroundColor Yellow

# 参数排序并拼接
$sortedKeys = $params.Keys | Sort-Object
$signString = ""
foreach ($key in $sortedKeys) {
    $signString += "$key=$($params[$key])&"
}
$signString += "timestamp=$timestamp&"
$signString += "key=$signKey"

Write-Host "   签名字符串: $signString"

# MD5加密
$md5 = [System.Security.Cryptography.MD5]::Create()
$hash = $md5.ComputeHash([System.Text.Encoding]::UTF8.GetBytes($signString))
$signature = [System.BitConverter]::ToString($hash).Replace("-", "").ToLower()

Write-Host "   签名结果: $signature" -ForegroundColor Green
Write-Host ""

# 准备请求体
$body = $params | ConvertTo-Json -Compress

Write-Host "4. 构建请求" -ForegroundColor Yellow
Write-Host "   URL: $baseUrl$apiPath"
Write-Host "   Method: POST"
Write-Host "   Headers:"
Write-Host "     Content-Type: application/json"
Write-Host "     X-Timestamp: $timestamp"
Write-Host "     X-Sign: $signature"
Write-Host "   Body: $body"
Write-Host ""

# 生成curl命令
Write-Host "5. CURL命令（可直接复制使用）" -ForegroundColor Yellow
Write-Host "curl -X POST $baseUrl$apiPath ``" -ForegroundColor Cyan
Write-Host "  -H 'Content-Type: application/json' ``" -ForegroundColor Cyan
Write-Host "  -H 'X-Timestamp: $timestamp' ``" -ForegroundColor Cyan
Write-Host "  -H 'X-Sign: $signature' ``" -ForegroundColor Cyan
Write-Host "  -d '$body'" -ForegroundColor Cyan
Write-Host ""

# 发送请求（需要确保服务已启动）
Write-Host "6. 发送请求" -ForegroundColor Yellow
Write-Host "   提示: 请确保Spring Boot应用和Redis服务已启动" -ForegroundColor Red
Write-Host ""

$confirm = Read-Host "是否发送请求？(y/n)"

if ($confirm -eq "y" -or $confirm -eq "Y") {
    try {
        $headers = @{
            "Content-Type" = "application/json"
            "X-Timestamp" = $timestamp.ToString()
            "X-Sign" = $signature
        }
        
        Write-Host "   正在发送请求..." -ForegroundColor Yellow
        $response = Invoke-RestMethod -Uri "$baseUrl$apiPath" -Method Post -Headers $headers -Body $body -TimeoutSec 30
        
        Write-Host ""
        Write-Host "7. 响应结果" -ForegroundColor Green
        Write-Host "====================================" -ForegroundColor Green
        $response | ConvertTo-Json -Depth 10 | Write-Host
        Write-Host "====================================" -ForegroundColor Green
        
        if ($response.code -eq 200) {
            Write-Host ""
            Write-Host "✅ 请求成功！" -ForegroundColor Green
            if ($response.token) {
                Write-Host "   JWT Token: $($response.token.Substring(0, 50))..." -ForegroundColor Cyan
            }
        } else {
            Write-Host ""
            Write-Host "❌ 请求失败: $($response.message)" -ForegroundColor Red
        }
        
    } catch {
        Write-Host ""
        Write-Host "❌ 请求失败: $($_.Exception.Message)" -ForegroundColor Red
        Write-Host "   请检查:" -ForegroundColor Yellow
        Write-Host "   1. Spring Boot应用是否已启动 (端口8080)" -ForegroundColor Yellow
        Write-Host "   2. Redis服务是否已启动" -ForegroundColor Yellow
        Write-Host "   3. 防火墙是否允许访问" -ForegroundColor Yellow
    }
} else {
    Write-Host "   已取消发送" -ForegroundColor Gray
}

Write-Host ""
Write-Host "====================================" -ForegroundColor Cyan
Write-Host "测试完成" -ForegroundColor Cyan
Write-Host "====================================" -ForegroundColor Cyan
