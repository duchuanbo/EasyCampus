# EasyCampus - Android Studio 使用指南

## 目录
1. [环境准备](#环境准备)
2. [导入项目](#导入项目)
3. [项目配置](#项目配置)
4. [运行调试](#运行调试)
5. [打包发布](#打包发布)
6. [常见问题](#常见问题)

---

## 环境准备

### 1. 安装 Android Studio

**下载地址**: https://developer.android.com/studio

**推荐版本**: Android Studio Ladybug | 2024.2.1 或更高版本

**安装要求**:
- Windows 10/11 64位
- 16GB RAM (推荐 32GB)
- 50GB 可用磁盘空间
- 1280 x 800 屏幕分辨率

### 2. 配置 SDK

打开 Android Studio 后，配置 SDK:

```
File → Settings → Appearance & Behavior → System Settings → Android SDK
```

**必须安装的组件**:
- Android SDK Platform 35
- Android SDK Build-Tools 35
- Android SDK Command-line Tools
- Android Emulator (可选)
- Android SDK Platform-Tools

### 3. 配置 JDK

项目使用 JDK 17，配置路径:

```
File → Settings → Build, Execution, Deployment → Build Tools → Gradle
```

选择 Gradle JDK 为 JDK 17 或更高版本

---

## 导入项目

### 方法一: 直接打开

1. 打开 Android Studio
2. 选择 `Open` (不要选择 `Import Project`)
3. 导航到项目目录 `c:\Users\32592\Desktop\EasyCampus`
4. 点击 `OK`

### 方法二: 通过版本控制 (推荐)

1. 打开 Android Studio
2. 选择 `Get from VCS`
3. 输入仓库 URL (如果已上传到GitHub)
4. 点击 `Clone`

### 导入后的操作

Android Studio 会自动:
1. 检测 Gradle 配置
2. 下载依赖库
3. 同步项目

**首次同步可能需要 10-30 分钟**，取决于网络速度。

---

## 项目配置

### 1. Gradle 同步

如果项目没有自动同步，手动触发:

```
File → Sync Project with Gradle Files
```

或点击工具栏的 🔄 图标

### 2. 检查依赖

打开 `gradle/libs.versions.toml` 确认所有依赖版本:

```toml
[versions]
agp = "8.8.0"
kotlin = "2.1.0"
composeBom = "2025.02.00"
hilt = "2.55"
room = "2.6.1"
```

### 3. 配置签名 (调试)

调试版本使用默认签名，无需配置。

---

## 运行调试

### 1. 连接设备

#### 真机调试

1. **开启开发者选项**:
   - 设置 → 关于手机 → 连续点击"版本号"7次

2. **开启USB调试**:
   - 设置 → 系统 → 开发者选项 → USB调试

3. **连接手机**:
   - 使用USB线连接电脑
   - 手机上允许USB调试

4. **验证连接**:
   ```
   在 Android Studio 底部工具栏查看设备列表
   应该显示你的设备型号
   ```

#### 模拟器调试

1. 打开 Device Manager:
   ```
   View → Tool Windows → Device Manager
   ```

2. 创建虚拟设备:
   - 点击 `+` 或 `Create Device`
   - 选择 Phone → Pixel 8
   - 选择系统镜像: Android 15 (API 35)
   - 完成创建

3. 启动模拟器:
   - 点击设备旁边的 `▶` 按钮

### 2. 运行应用

#### 方式一: 点击运行按钮

1. 选择目标设备 (真机或模拟器)
2. 点击工具栏的绿色 `▶` 按钮 (Run 'app')
3. 等待编译和安装

#### 方式二: 使用快捷键

- **Windows/Linux**: `Shift + F10`
- **Mac**: `Control + R`

#### 方式三: 命令行

```bash
# 进入项目目录
cd c:\Users\32592\Desktop\EasyCampus

# 编译并安装Debug版本
./gradlew installDebug

# 或 Windows
gradlew.bat installDebug
```

### 3. 调试技巧

#### 断点调试

1. 在代码左侧点击设置断点
2. 使用 `Debug` 模式运行 (虫子图标 🐛)
3. 使用快捷键:
   - `F8` Step Over (单步跳过)
   - `F7` Step Into (单步进入)
   - `Shift + F8` Step Out (单步跳出)
   - `F9` Resume (继续运行)

#### 查看日志

```
View → Tool Windows → Logcat
```

过滤日志:
```
tag:EasyCampus    # 只显示应用日志
tag:AndroidRuntime  # 查看崩溃日志
```

#### 布局检查

```
View → Tool Windows → Layout Inspector
```

可以实时查看Compose UI的层级结构。

---

## 打包发布

### 1. 生成 Debug APK

Debug版本用于测试，已配置好可以直接生成:

#### 方式一: 通过 Android Studio

```
Build → Build Bundle(s) / APK(s) → Build APK(s)
```

APK位置:
```
app/build/outputs/apk/debug/app-debug.apk
```

#### 方式二: 命令行

```bash
./gradlew assembleDebug
```

### 2. 生成 Release APK

Release版本用于发布，需要配置签名。

#### 步骤一: 创建签名密钥

```
Build → Generate Signed Bundle / APK...
```

选择 `APK`，然后 `Create new...`

填写密钥信息:
- **Key store path**: 选择保存位置 (如 `easycampus.keystore`)
- **Password**: 设置密钥库密码
- **Key alias**: `easycampus`
- **Key password**: 设置密钥密码
- **Validity**: 25年
- **Certificate**: 填写基本信息

#### 步骤二: 配置签名信息

创建 `keystore.properties` 文件 (不要提交到版本控制):

```properties
storeFile=easycampus.keystore
storePassword=你的密钥库密码
keyAlias=easycampus
keyPassword=你的密钥密码
```

修改 `app/build.gradle.kts`:

```kotlin
import java.util.Properties

// 读取签名配置
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()

if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(keystorePropertiesFile.inputStream())
}

android {
    // ... 其他配置
    
    signingConfigs {
        create("release") {
            if (keystorePropertiesFile.exists()) {
                storeFile = file(keystoreProperties["storeFile"]!!)
                storePassword = keystoreProperties["storePassword"] as String
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["keyPassword"] as String
            }
        }
    }
    
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (keystorePropertiesFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
}
```

#### 步骤三: 生成 Release APK

```
Build → Generate Signed Bundle / APK...
```

或命令行:
```bash
./gradlew assembleRelease
```

APK位置:
```
app/build/outputs/apk/release/app-release.apk
```

### 3. 生成 AAB (Google Play)

如果要在Google Play发布，需要生成AAB格式:

```
Build → Generate Signed Bundle / APK... → Android App Bundle
```

或命令行:
```bash
./gradlew bundleRelease
```

AAB位置:
```
app/build/outputs/bundle/release/app-release.aab
```

### 4. 安装到手机

#### 方式一: ADB 命令

```bash
# 连接手机后
adb install app/build/outputs/apk/debug/app-debug.apk

# 如果已安装，强制重新安装
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

#### 方式二: 直接传输

1. 将APK文件传输到手机
2. 在手机上找到APK文件
3. 点击安装
4. 允许"安装未知应用"权限

#### 方式三: 扫码安装

使用工具生成二维码:
- 将APK上传到文件传输服务 (如 https://wormhole.app)
- 生成二维码
- 手机扫码下载安装

---

## 常见问题

### Q1: Gradle 同步失败

**问题**: `Could not resolve all dependencies`

**解决**:
1. 检查网络连接
2. 配置国内镜像 (阿里云):
   ```kotlin
   // 在 settings.gradle.kts 中
   pluginManagement {
       repositories {
           maven { url = uri("https://maven.aliyun.com/repository/public") }
           google()
           mavenCentral()
       }
   }
   ```
3. 清除缓存:
   ```
   File → Invalidate Caches / Restart
   ```

### Q2: 编译报错 `Unresolved reference`

**解决**:
1. 检查Kotlin插件版本
2. 重新同步Gradle:
   ```
   ./gradlew clean
   ./gradlew build
   ```

### Q3: 安装失败 `INSTALL_FAILED_UPDATE_INCOMPATIBLE`

**解决**:
```bash
# 先卸载旧版本
adb uninstall com.easycampus

# 再安装
adb install app-debug.apk
```

### Q4: Compose 预览不显示

**解决**:
1. 确保使用最新版Android Studio
2. 点击预览面板的 `Build & Refresh`
3. 检查 `@Preview` 注解参数

### Q5: Hilt 依赖注入报错

**解决**:
1. 确保添加了KSP插件
2. 重建项目:
   ```
   Build → Rebuild Project
   ```
3. 检查 `@HiltAndroidApp` 注解是否在Application类上

---

## 开发工作流

### 日常开发步骤

1. **打开项目**:
   ```
   Android Studio → Open → 选择 EasyCampus 文件夹
   ```

2. **同步项目**:
   ```
   点击 🔄 Sync Project with Gradle Files
   ```

3. **编写代码**:
   - 修改代码
   - 使用预览查看UI效果

4. **运行测试**:
   ```
   点击 ▶ Run 或 Shift + F10
   ```

5. **提交代码**:
   ```
   使用Git工具窗口或命令行
   ```

### 构建变体

项目配置了两种构建类型:

| 类型 | 用途 | 签名 | 优化 |
|------|------|------|------|
| Debug | 开发调试 | 自动 | 无 |
| Release | 发布 | 自定义 | ProGuard |

切换构建类型:
```
Build → Select Build Variant
```

---

## 性能优化

### 1. 启用 Gradle 守护进程

在 `gradle.properties` 中已配置:
```properties
org.gradle.jvmargs=-Xmx2048m
org.gradle.daemon=true
org.gradle.parallel=true
org.gradle.configureondemand=true
```

### 2. 使用 Configuration Cache

```bash
./gradlew assembleDebug --configuration-cache
```

### 3. 增量编译

Kotlin和Compose都支持增量编译，确保使用最新版本。

---

## 参考资源

- [Android Studio 官方文档](https://developer.android.com/studio/intro)
- [Gradle 配置指南](https://developer.android.com/studio/build)
- [Compose 预览](https://developer.android.com/jetpack/compose/tooling/previews)
- [应用签名](https://developer.android.com/studio/publish/app-signing)

---

**如有问题，请查看项目 README.md 或提交 Issue!**
