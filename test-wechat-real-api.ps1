# 微信小程序真实API测试脚本
# 使用说明：
# 1. 先从微信开发者工具获取真实的code
# 2. 将code作为参数运行此脚本: .\test-wechat-real-api.ps1 "your_real_code_here"

param(
    [string]$WeChatCode = ""
)

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  微信小程序 code2Session 真实API测试" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

if ([string]::IsNullOrEmpty($WeChatCode)) {
    Write-Host "⚠️  未提供微信小程序code" -ForegroundColor Yellow
    Write-Host "`n请按以下步骤获取code：" -ForegroundColor White
    Write-Host "1. 打开微信开发者工具" -ForegroundColor Gray
    Write-Host "2. 在控制台输入以下代码：" -ForegroundColor Gray
    Write-Host "   wx.login({ success: res => console.log('Code:', res.code) })" -ForegroundColor Green
    Write-Host "3. 复制输出的code" -ForegroundColor Gray
    Write-Host "4. 运行此脚本：" -ForegroundColor Gray
    Write-Host "   .\test-wechat-real-api.ps1 `"你的code`"`n" -ForegroundColor Green
    
    # 提示用户输入code
    $WeChatCode = Read-Host "或者直接在这里输入code（回车跳过）"
    
    if ([string]::IsNullOrEmpty($WeChatCode)) {
        Write-Host "`n❌ 未输入code，退出测试" -ForegroundColor Red
        exit 1
    }
}

Write-Host "✅ 接收到微信code: $WeChatCode`n" -ForegroundColor Green

# 读取测试文件
$testFile = "src/test/java/xw/szbz/cn/service/WeChatServiceTest.java"
$content = Get-Content $testFile -Raw

# 替换testCode
$originalPattern = 'String testCode = "请替换为真实的微信小程序code";'
$newCode = "String testCode = `"$WeChatCode`";"

if ($content -match [regex]::Escape($originalPattern)) {
    Write-Host "正在更新测试文件..." -ForegroundColor Yellow
    $newContent = $content -replace [regex]::Escape($originalPattern), $newCode
    Set-Content -Path $testFile -Value $newContent -NoNewline
    Write-Host "✅ 测试代码已更新`n" -ForegroundColor Green
} else {
    Write-Host "⚠️  测试文件格式可能已变更，尝试继续..." -ForegroundColor Yellow
}

# 运行测试
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "开始执行测试..." -ForegroundColor White
Write-Host "========================================`n" -ForegroundColor Cyan

mvn test -Dtest=WeChatServiceTest#testCode2Session_Success

$testResult = $LASTEXITCODE

# 恢复原始代码
Write-Host "`n正在恢复测试文件..." -ForegroundColor Yellow
$restoredContent = $newContent -replace [regex]::Escape($newCode), $originalPattern
Set-Content -Path $testFile -Value $restoredContent -NoNewline
Write-Host "✅ 测试文件已恢复`n" -ForegroundColor Green

# 输出结果
Write-Host "========================================" -ForegroundColor Cyan
if ($testResult -eq 0) {
    Write-Host "✅ 测试成功！" -ForegroundColor Green
} else {
    Write-Host "❌ 测试失败！" -ForegroundColor Red
    Write-Host "`n可能的原因：" -ForegroundColor Yellow
    Write-Host "1. code已过期（5分钟有效期）" -ForegroundColor Gray
    Write-Host "2. code已被使用过（每个code只能使用一次）" -ForegroundColor Gray
    Write-Host "3. AppID或AppSecret配置错误" -ForegroundColor Gray
    Write-Host "4. 网络连接问题" -ForegroundColor Gray
    Write-Host "`n请获取新的code后重试" -ForegroundColor Yellow
}
Write-Host "========================================`n" -ForegroundColor Cyan

exit $testResult
