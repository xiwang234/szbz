# VS Code Java 开发环境配置指南

## 安装必需的扩展

### 方式一：安装扩展包（推荐）
在 VS Code 中安装以下扩展：

1. **Extension Pack for Java**（Java 扩展包）
   - 包含了所有必需的 Java 开发工具
   - 扩展 ID: `vscjava.vscode-java-pack`

安装方法：
- 打开 VS Code
- 按 `Cmd+Shift+X`（Mac）或 `Ctrl+Shift+X`（Windows）打开扩展面板
- 搜索 "Extension Pack for Java"
- 点击安装

### 方式二：使用命令行安装
```bash
code --install-extension vscjava.vscode-java-pack
```

## 扩展包包含的工具

Extension Pack for Java 包含：
- **Language Support for Java(TM)** - Java 语言支持
- **Debugger for Java** - Java 调试器
- **Test Runner for Java** - Java 测试运行器
- **Maven for Java** - Maven 集成
- **Project Manager for Java** - Java 项目管理
- **IntelliCode** - AI 辅助代码补全

## 配置项目

### 1. 设置 Java Home
在项目根目录创建 `.vscode/settings.json`：
```json
{
  "java.configuration.runtimes": [
    {
      "name": "JavaSE-17",
      "path": "/opt/homebrew/opt/openjdk@17",
      "default": true
    }
  ],
  "java.home": "/opt/homebrew/opt/openjdk@17"
}
```

### 2. 导入 Maven 项目
- 打开项目文件夹后，VS Code 会自动检测到 `pom.xml`
- 等待 Java 语言服务器完成索引（右下角会显示进度）
- 索引完成后就可以使用跳转功能了

## 使用跳转功能

安装扩展后，可以使用以下快捷键：

### Mac
- **Cmd + 鼠标左键点击** - 跳转到定义
- **Cmd + Shift + O** - 查看当前文件的符号
- **Cmd + T** - 跳转到符号

### Windows/Linux
- **Ctrl + 鼠标左键点击** - 跳转到定义
- **Ctrl + Shift + O** - 查看当前文件的符号
- **Ctrl + T** - 跳转到符号

### 其他有用的快捷键
- **F12** - 跳转到定义
- **Alt + F12**（Mac: Option + F12） - 查看定义（不跳转）
- **Shift + F12** - 查看所有引用
- **F2** - 重命名符号

## 验证配置是否成功

1. 打开 Java 文件（如 `GeminiService.java`）
2. 等待右下角 Java 语言服务器加载完成（显示 ✓）
3. 将鼠标悬停在 `BaZiResult` 上，应该显示类型信息
4. 按住 Cmd（或 Ctrl）+ 鼠标左键点击 `BaZiResult`
5. 应该会跳转到 `BaZiResult.java` 文件

## 故障排除

### 问题1: 跳转不工作
**解决方案**：
1. 确认 Java 扩展已安装并启用
2. 检查右下角是否显示 Java 项目加载完成
3. 重新加载窗口：`Cmd+Shift+P` → 输入 "Reload Window"
4. 清理并重新索引：`Cmd+Shift+P` → 输入 "Java: Clean Java Language Server Workspace"

### 问题2: Java 版本不对
**解决方案**：
检查 VS Code 右下角显示的 Java 版本是否为 17，如果不是：
1. 点击右下角的 Java 版本
2. 选择 Java 17
3. 或者在设置中配置 `java.home`

### 问题3: 索引一直在进行
**解决方案**：
- 首次打开项目时，索引需要一些时间
- 确保网络畅通，Maven 需要下载依赖
- 查看输出面板（`Cmd+Shift+U`）→ 选择 "Language Support for Java" 查看详细日志

## 推荐的额外设置

在 `.vscode/settings.json` 中添加：
```json
{
  "java.configuration.runtimes": [
    {
      "name": "JavaSE-17",
      "path": "/opt/homebrew/opt/openjdk@17",
      "default": true
    }
  ],
  "java.home": "/opt/homebrew/opt/openjdk@17",
  "java.completion.enabled": true,
  "java.completion.importOrder": ["java", "javax", "org", "com"],
  "java.saveActions.organizeImports": true,
  "editor.formatOnSave": true,
  "java.format.enabled": true,
  "java.autobuild.enabled": true
}
```

## IntelliJ IDEA 用户

如果你使用 IntelliJ IDEA：
- 跳转功能是内置的，无需额外配置
- Mac: **Cmd + 鼠标左键点击** 或 **Cmd + B**
- Windows/Linux: **Ctrl + 鼠标左键点击** 或 **Ctrl + B**
- 确保项目已正确导入为 Maven 项目

## Eclipse 用户

如果你使用 Eclipse：
- 跳转功能也是内置的
- Mac: **Cmd + 鼠标左键点击** 或 **F3**
- Windows/Linux: **Ctrl + 鼠标左键点击** 或 **F3**
- 确保项目已导入并且 Maven 依赖已解析
