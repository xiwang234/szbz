# 运行 WeChatService 测试
Write-Host "开始编译和测试 WeChatService..." -ForegroundColor Cyan

# 设置环境变量
$env:JAVA_HOME = "D:\tools\Java\jdk-17.0.2"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

# 进入项目目录
Set-Location "D:\project\szbz"

# 编译测试代码
Write-Host "`n1. 编译测试代码..." -ForegroundColor Yellow
mvn test-compile -DskipTests

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ 编译成功" -ForegroundColor Green
    
    # 运行测试
    Write-Host "`n2. 运行 WeChatServiceTest..." -ForegroundColor Yellow
    mvn test -Dtest=WeChatServiceTest -DfailIfNoTests=false
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "`n✅ 测试通过！" -ForegroundColor Green
    } else {
        Write-Host "`n❌ 测试失败" -ForegroundColor Red
    }
} else {
    Write-Host "❌ 编译失败" -ForegroundColor Red
}

Write-Host "`n按任意键退出..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
