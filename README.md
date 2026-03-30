# GithubChatDemo

这是一个 Kotlin Multiplatform + Compose Multiplatform 示例工程，包含以下目标平台：

- Android
- iOS (iosX64 / iosArm64 / iosSimulatorArm64)
- JVM Desktop

## 环境要求

- JDK 17+
- Android Studio（用于 Android 与 iOS 开发体验）
- Xcode（仅 macOS，构建与运行 iOS 目标时需要）

## 快速开始

### 1) 构建项目

```bash
./gradlew build
```

### 2) 运行 JVM Desktop

```bash
./gradlew :composeApp:run
```

### 3) 运行 Android

在 Android Studio 中打开项目后，选择 Android 运行目标执行。

也可以使用命令行构建 APK：

```bash
./gradlew :androidApp:assembleDebug
```

### 4) 构建 iOS Framework

在 macOS 上执行：

```bash
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64
```

## 项目结构

- `androidApp`：Android 应用入口模块
- `composeApp`：共享 UI 与多平台目标配置（作为共享库）
- `composeApp/src/commonMain`：跨平台共享 Compose 代码
- `composeApp/src/iosMain`：iOS 入口
- `composeApp/src/jvmMain`：JVM Desktop 入口