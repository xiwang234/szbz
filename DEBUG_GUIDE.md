# VS Code/Cursor Java 调试指南

## 🎯 在 GeminiAnalysisExample 类的第 21 行设置断点并调试

### 步骤 1：打开文件
打开 `src/main/java/xw/szbz/cn/GeminiAnalysisExample.java`

### 步骤 2：设置断点
在第 21 行（`GeminiService geminiService = createGeminiService();`）设置断点：

**方法 1：使用鼠标**
1. 将鼠标移到第 21 行的行号左侧
2. 点击行号左侧的空白区域
3. 会出现一个红色圆点，表示断点已设置

**方法 2：使用键盘**
1. 将光标移到第 21 行
2. 按 `F9` 键
3. 会出现一个红色圆点

**方法 3：使用命令**
1. 将光标移到第 21 行
2. Mac: `Cmd + Shift + P`，Windows: `Ctrl + Shift + P`
3. 输入 "Toggle Breakpoint"
4. 按回车

### 步骤 3：启动调试

**方法 1：使用侧边栏（推荐）**
1. 点击左侧的调试图标（虫子图标）或按 `Cmd/Ctrl + Shift + D`
2. 在顶部下拉菜单中选择 **"Debug GeminiAnalysisExample"**
3. 点击绿色的播放按钮（▶️）或按 `F5` 启动调试

**方法 2：使用快捷键**
1. 确保当前打开的是 `GeminiAnalysisExample.java` 文件
2. 按 `F5` 启动调试
3. 如果弹出选择框，选择 **"Debug GeminiAnalysisExample"**

**方法 3：右键菜单**
1. 在编辑器中右键点击
2. 选择 "Debug Java"
3. 选择 "Debug GeminiAnalysisExample"

### 步骤 4：调试操作

程序会在第 21 行暂停，此时你可以：

#### 查看变量
- **悬停查看**：鼠标悬停在变量上查看其值
- **变量面板**：左侧"变量"面板显示所有局部变量
- **监视表达式**：在"监视"面板添加表达式

#### 控制执行流程
使用顶部的调试工具栏或快捷键：

| 操作 | 快捷键 | 说明 |
|------|--------|------|
| 继续 (Continue) | `F5` | 继续执行到下一个断点 |
| 单步跳过 (Step Over) | `F10` | 执行当前行，不进入方法内部 |
| 单步进入 (Step Into) | `F11` | 进入方法内部逐行执行 |
| 单步跳出 (Step Out) | `Shift + F11` | 跳出当前方法 |
| 重启 | `Cmd/Ctrl + Shift + F5` | 重新启动调试 |
| 停止 | `Shift + F5` | 停止调试 |

#### 调试面板说明
左侧会显示以下面板：
- **变量 (Variables)**：显示当前作用域的所有变量
- **监视 (Watch)**：添加要监视的表达式
- **调用堆栈 (Call Stack)**：显示方法调用链
- **断点 (Breakpoints)**：管理所有断点

### 步骤 5：调试技巧

#### 条件断点
1. 右键点击断点（红色圆点）
2. 选择 "Edit Breakpoint..."
3. 输入条件，例如：`request.getYear() == 1984`
4. 只有满足条件时才会暂停

#### 日志断点（不暂停，只打印）
1. 右键点击行号
2. 选择 "Add Logpoint..."
3. 输入要打印的内容，例如：`baZiService = {baZiService}`
4. 程序执行到这里会打印日志但不暂停

#### 查看调用栈
当程序暂停时，可以在"调用堆栈"面板中：
- 查看方法调用链
- 点击不同的栈帧查看不同层级的变量

## 🔍 调试示例流程

### 完整调试演练

1. **设置断点**
   - 第 21 行：`GeminiService geminiService = createGeminiService();`
   - 第 27 行：`BaZiResult baZiResult = baZiService.calculate(request);`
   - 第 43 行：`String analysis = geminiService.analyzeBaZi(baZiResult);`

2. **启动调试** (F5)
   - 程序在第 21 行暂停

3. **查看变量**
   - 此时 `baZiService` 已创建
   - `geminiService` 还未创建

4. **单步进入** (F11)
   - 进入 `createGeminiService()` 方法
   - 可以看到如何通过反射设置配置

5. **继续** (F5)
   - 跳到下一个断点（第 27 行）
   - 查看 `request` 的内容

6. **单步跳过** (F10)
   - 执行八字计算
   - 查看 `baZiResult` 的结果

7. **继续** (F5)
   - 跳到第 43 行
   - 即将调用 Gemini API

8. **单步进入** (F11)
   - 进入 `analyzeBaZi()` 方法
   - 看看内部如何调用 API

## ⚠️ 注意事项

### Gemini API 配额
由于之前测试时用完了 API 配额，调试时可能会遇到 429 错误。建议：
- 在第 43 行之前停止调试，不实际调用 API
- 或等待 1-2 分钟让配额重置

### 修改后的代码
我已经修改了 `GeminiAnalysisExample`，使其能够独立运行：
- 添加了 `createGeminiService()` 方法
- 使用反射设置配置，不依赖 Spring
- 会自动读取环境变量中的 API Key

### 查看输出
- **调试控制台**：显示程序的标准输出
- **终端**：也会显示输出信息

## 🚀 快捷方式总结

| 功能 | Mac | Windows |
|------|-----|---------|
| 打开调试面板 | `Cmd + Shift + D` | `Ctrl + Shift + D` |
| 启动/继续调试 | `F5` | `F5` |
| 设置/取消断点 | `F9` | `F9` |
| 单步跳过 | `F10` | `F10` |
| 单步进入 | `F11` | `F11` |
| 单步跳出 | `Shift + F11` | `Shift + F11` |
| 停止调试 | `Shift + F5` | `Shift + F5` |

## 🎬 现在开始调试

1. 打开 `GeminiAnalysisExample.java`
2. 在第 21 行点击左侧空白处设置断点（会出现红色圆点）
3. 按 `F5` 或点击左侧调试图标 → 选择 "Debug GeminiAnalysisExample" → 点击绿色播放按钮
4. 程序会在第 21 行暂停，现在可以开始调试了！

## 📚 扩展阅读

- [VS Code Java 调试文档](https://code.visualstudio.com/docs/java/java-debugging)
- [Java 调试技巧](https://code.visualstudio.com/docs/java/java-debugging#_tips)
