# EasyCampus - 智能签到助手

[![Version](https://img.shields.io/badge/version-1.0.0-blue.svg)](https://github.com/duchuanbo/EasyCampus/releases/tag/v1.0.0)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/platform-Android-brightgreen.svg)](https://www.android.com/)

一款支持多平台的安卓签到管理应用，帮助大学生轻松管理课堂签到。

## 📱 版本说明

### v1.0.0 - UI界面实现 ✅

**发布日期**: 2026-03-14

**已实现功能**:
- ✅ 基础架构搭建 (MVVM + Clean Architecture)
- ✅ 首页UI界面 (签到列表、今日课程)
- ✅ 登录界面 (多平台账号管理)
- ✅ 课表界面 (课程展示)
- ✅ 设置界面 (应用配置)
- ✅ Hilt依赖注入框架
- ✅ Room数据库配置
- ✅ 多平台适配器框架
- ✅ Material Design 3 设计

**待实现功能**:
- ⏳ 各平台API实际对接
- ⏳ 自动签到功能
- ⏳ 通知推送

## 功能特性

### 多平台支持
- 课堂派
- 雨课堂
- 长江雨课堂
- 畅课

### 核心功能
- ✅ 签到状态实时查看
- ✅ 多类型签到支持（普通、手势、位置、二维码、签到码）
- ✅ 课程表管理
- ✅ 多账号管理
- ✅ 签到历史记录
- ✅ 签到统计

## 技术架构

- **架构模式**: MVVM + Clean Architecture
- **UI框架**: Jetpack Compose
- **编程语言**: Kotlin
- **依赖注入**: Hilt
- **本地存储**: Room + DataStore
- **网络通信**: Retrofit + OkHttp

## 项目结构

```
app/src/main/java/com/easycampus/
├── data/           # 数据层
├── domain/         # 领域层
├── presentation/   # 表示层（UI）
└── EasyCampusApplication.kt
```

## 构建要求

- Android Studio Hedgehog (2023.1.1) 或更高版本
- JDK 17
- Android SDK 34
- minSdk: 31 (Android 12)

## 快速开始

### 1. 克隆项目
```bash
git clone <repository-url>
cd EasyCampus
```

### 2. 使用 Android Studio 打开
1. 打开 Android Studio
2. 选择 `File -> Open`
3. 选择项目根目录

### 3. 构建 APK
```bash
./gradlew assembleDebug
```

APK 将生成在 `app/build/outputs/apk/debug/app-debug.apk`

## 开发指南

### 添加新平台支持

1. 在 `data/remote/platform/` 创建新的平台适配器
2. 实现 `PlatformAdapter` 接口
3. 在 `PlatformAdapterFactory` 中注册新适配器

### 添加新功能

1. 在 `domain/usecase/` 创建用例类
2. 在 `presentation/screens/` 创建UI界面
3. 在 `data/repository/` 实现数据访问

## 项目状态

- ✅ 核心框架搭建完成
- ✅ 数据库设计实现
- ✅ 网络模块配置
- ✅ UI界面开发
- ✅ 项目构建成功
- ⏳ 平台API对接（需要平台开放接口）

## 文档

- [技术调研与方案规划](技术调研与方案规划.md)
- [AndroidStudio使用指南](AndroidStudio使用指南.md)
- [项目开发总结](项目开发总结.md)

## 许可证

MIT License

## 贡献

欢迎提交 Issue 和 Pull Request！

---

**注意**: 本项目仅供学习交流使用，请遵守各平台的使用条款。
