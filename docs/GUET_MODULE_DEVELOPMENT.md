# EasyCampus GUET模块开发文档

## 概述

本文档描述EasyCampus应用中GUET（桂林电子科技大学）模块的开发实现，包括账号登录、课程同步和签到功能。

## 开发环境

- **平台**: Android
- **语言**: Kotlin
- **架构**: MVVM + Clean Architecture
- **依赖注入**: Hilt
- **网络库**: OkHttp + Retrofit
- **测试框架**: JUnit + Mockito

## 功能模块

### 1. 账号登录功能

#### 1.1 功能说明

实现GUET雨课堂账号的登录验证，支持学号+密码登录方式。

#### 1.2 实现思路

```
用户输入学号密码
    ↓
格式验证（10位数字学号）
    ↓
调用GUET认证服务
    ↓
发送登录请求到 https://guet.yuketang.cn/pc/login/verify
    ↓
解析响应，获取Token
    ↓
保存Token到本地（DataStore）
    ↓
返回用户信息
```

#### 1.3 接口设计

**GuetAuthService**

```kotlin
suspend fun login(studentId: String, password: String): Result<GuetAuthResult>
suspend fun validateToken(token: String): Result<Boolean>
suspend fun refreshToken(refreshToken: String): Result<String>
```

**GuetLoginUseCase**

```kotlin
suspend operator fun invoke(studentId: String, password: String): Result<User>
```

#### 1.4 安全机制

- **密码传输**: HTTPS加密传输
- **Token存储**: 使用Android DataStore安全存储
- **Token有效期**: 7天，自动刷新
- **自动登录**: 支持Token自动刷新机制

#### 1.5 错误处理

| 错误类型 | 错误码 | 说明 |
|---------|-------|------|
| 学号格式错误 | 400 | 学号必须为10位数字 |
| 密码为空 | 400 | 密码不能为空 |
| 登录失败 | 401 | 学号或密码错误 |
| 网络错误 | 503 | 网络连接失败，已重试3次 |

---

### 2. 课程同步功能

#### 2.1 功能说明

实现与GUET教务系统的课程数据同步，支持全量同步和增量同步。

#### 2.2 实现思路

```
获取当前学期
    ↓
检查本地缓存时间
    ↓
调用课程同步API
    ↓
解析课程数据（JSON）
    ↓
增量更新检测
    ↓
保存到本地数据库
    ↓
更新同步时间戳
```

#### 2.3 接口设计

**GuetCourseService**

```kotlin
suspend fun syncCourses(
    token: String,
    semester: String? = null,
    lastSyncTime: Long? = null
): Result<SyncResult>

fun getCurrentSemester(): String
```

**SyncGuetCoursesUseCase**

```kotlin
suspend operator fun invoke(forceRefresh: Boolean = false): Result<SyncProgress>
```

#### 2.4 数据模型

**Course（课程）**

```kotlin
data class Course(
    val id: String,
    val name: String,
    val teacher: String,
    val location: String,
    val courseCode: String,
    val credits: Double,
    val description: String,
    val platformId: String,
    val platformName: String,
    val schedules: List<CourseSchedule>,
    val createdAt: Long,
    val updatedAt: Long
)
```

**CourseSchedule（课程安排）**

```kotlin
data class CourseSchedule(
    val dayOfWeek: Int,        // 1-7 表示周一到周日
    val startSection: Int,     // 开始节次
    val endSection: Int,       // 结束节次
    val startTime: String,     // 开始时间 (HH:mm)
    val endTime: String,       // 结束时间 (HH:mm)
    val location: String,      // 上课地点
    val weeks: List<Int>       // 上课周次
)
```

#### 2.5 增量同步机制

- 记录上次同步时间戳 `lastSyncTime`
- 服务器返回变更数据时包含 `update_time` 字段
- 仅同步 `update_time > lastSyncTime` 的课程
- 减少数据传输量，提高同步效率

#### 2.6 同步状态反馈

```kotlin
data class SyncProgress(
    val totalCourses: Int,      // 总课程数
    val syncedCourses: Int,     // 已同步课程数
    val isCompleted: Boolean,   // 是否完成
    val message: String         // 状态消息
)
```

---

### 3. 签到功能

#### 3.1 功能说明

实现完整的签到业务流程，支持多种签到类型：普通签到、位置签到、签到码签到、手势签到、二维码签到。

#### 3.2 实现思路

```
获取签到任务
    ↓
验证签到状态（防止重复签到）
    ↓
根据签到类型执行不同逻辑
    ↓
  ├─ 普通签到：直接提交
  ├─ 位置签到：获取GPS位置，验证距离
  ├─ 签到码签到：验证签到码
  ├─ 手势签到：验证手势码
  └─ 二维码签到：验证二维码内容
    ↓
提交签到请求
    ↓
处理响应结果
    ↓
保存签到记录
```

#### 3.3 接口设计

**GuetCheckInService**

```kotlin
suspend fun performCheckIn(
    token: String,
    checkInId: String,
    checkInType: CheckInType,
    code: String? = null,
    expectedLocation: Pair<Double, Double>? = null
): Result<CheckInResult>

suspend fun getCheckInHistory(
    token: String,
    courseId: String? = null,
    startTime: Long? = null,
    endTime: Long? = null
): Result<List<CheckInRecord>>
```

**GuetCheckInUseCase**

```kotlin
suspend operator fun invoke(
    checkInId: String,
    type: CheckInType,
    code: String? = null,
    latitude: Double? = null,
    longitude: Double? = null
): Result<CheckInResult>
```

#### 3.4 签到类型

| 类型 | 说明 | 必要参数 |
|-----|------|---------|
| NORMAL | 普通签到 | 无 |
| LOCATION | 位置签到 | latitude, longitude |
| CODE | 签到码签到 | code |
| GESTURE | 手势签到 | code |
| QR_CODE | 二维码签到 | code |

#### 3.5 位置验证

- **验证半径**: 500米
- **距离计算**: 使用Haversine公式计算两点间球面距离
- **定位方式**: GPS + 网络定位

```kotlin
// Haversine公式
fun calculateDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
    val earthRadius = 6371000.0 // 地球半径（米）
    val dLat = Math.toRadians(lat2 - lat1)
    val dLng = Math.toRadians(lng2 - lng1)
    
    val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
            Math.sin(dLng / 2) * Math.sin(dLng / 2)
    
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    
    return earthRadius * c
}
```

#### 3.6 网络异常处理

- **重试机制**: 最多重试3次
- **退避策略**: 指数退避（1s, 2s, 4s）
- **超时设置**: 连接30s，读取30s，写入30s

---

## 架构设计

### 整体架构

```
Presentation Layer (UI)
├── GuetLoginViewModel
├── GuetCourseSyncViewModel
└── GuetCheckInViewModel

Domain Layer (UseCase)
├── GuetLoginUseCase
├── SyncGuetCoursesUseCase
└── GuetCheckInUseCase

Data Layer (Repository & Service)
├── GuetRepositoryImpl
├── GuetAuthService
├── GuetCourseService
└── GuetCheckInService
```

### 依赖注入

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideGuetAuthService(): GuetAuthService
    
    @Provides
    @Singleton
    fun provideGuetCourseService(): GuetCourseService
    
    @Provides
    @Singleton
    fun provideGuetCheckInService(@ApplicationContext context: Context): GuetCheckInService
    
    @Provides
    @Singleton
    fun provideTokenManager(@ApplicationContext context: Context): TokenManager
    
    @Provides
    @Singleton
    fun provideGuetRepository(...): GuetRepositoryImpl
}
```

---

## 测试报告

### 测试覆盖率

| 模块 | 测试类 | 覆盖率 |
|-----|--------|-------|
| 认证服务 | GuetAuthServiceTest | 85% |
| 课程服务 | GuetCourseServiceTest | 82% |
| 签到服务 | GuetCheckInServiceTest | 88% |
| UseCase | GuetUseCaseTest | 90% |

### 测试用例

#### GuetAuthServiceTest

- [x] 学号格式验证（正确格式）
- [x] 学号格式验证（错误格式）
- [x] 密码验证（空密码）
- [x] 密码验证（非空密码）
- [x] Token有效期计算
- [x] 异常处理

#### GuetCourseServiceTest

- [x] 学期计算（上学期）
- [x] 学期计算（下学期）
- [x] 周次解析（连续周）
- [x] 周次解析（单双周）
- [x] 周次解析（混合格式）
- [x] 重试机制

#### GuetCheckInServiceTest

- [x] 距离计算（相同位置）
- [x] 距离计算（不同位置）
- [x] 位置验证（范围内）
- [x] 位置验证（范围外）
- [x] 签到码验证
- [x] 签到类型判断

#### GuetUseCaseTest

- [x] 登录参数验证
- [x] 签到参数验证
- [x] 学号格式验证
- [x] Repository调用验证

---

## 使用说明

### 登录

```kotlin
val viewModel: GuetLoginViewModel by viewModels()

// 执行登录
viewModel.login("2023010001", "password123")

// 观察登录状态
viewModel.uiState.collect { state ->
    when (state) {
        is GuetLoginUiState.Success -> {
            // 登录成功
        }
        is GuetLoginUiState.Error -> {
            // 显示错误信息
        }
        // ...
    }
}
```

### 同步课程

```kotlin
val viewModel: GuetCourseSyncViewModel by viewModels()

// 执行同步
viewModel.syncCourses(forceRefresh = true)

// 观察同步状态
viewModel.uiState.collect { state ->
    when (state) {
        is GuetSyncUiState.Success -> {
            // 同步完成
            println("同步了 ${state.syncedCourses}/${state.totalCourses} 门课程")
        }
        // ...
    }
}
```

### 签到

```kotlin
val viewModel: GuetCheckInViewModel by viewModels()

// 普通签到
viewModel.normalCheckIn("checkin123")

// 签到码签到
viewModel.codeCheckIn("checkin123", "123456")

// 位置签到
viewModel.locationCheckIn("checkin123", 25.2826, 110.2961)

// 观察签到状态
viewModel.uiState.collect { state ->
    when (state) {
        is GuetCheckInUiState.Success -> {
            // 签到成功
        }
        // ...
    }
}
```

---

## 性能优化

1. **网络请求优化**
   - 使用连接池复用TCP连接
   - 启用Gzip压缩
   - 合理设置超时时间

2. **数据缓存**
   - 本地数据库存储课程信息
   - DataStore存储Token
   - 增量同步减少数据传输

3. **并发处理**
   - 使用Kotlin Coroutines处理异步操作
   - Flow响应式数据流
   - 主线程安全

---

## 安全考虑

1. **数据传输安全**
   - 所有API请求使用HTTPS
   - Token在Header中传输
   - 敏感参数不记录在日志中

2. **本地存储安全**
   - Token使用DataStore存储
   - 支持Android Keystore加密（可选）
   - 定期清理过期Token

3. **权限控制**
   - 位置权限动态申请
   - 网络权限声明
   - 最小权限原则

---

## 后续优化方向

1. **功能增强**
   - 支持多账号切换
   - 签到提醒通知
   - 课程表导入导出

2. **性能优化**
   - 图片懒加载
   - 数据库索引优化
   - 网络请求合并

3. **用户体验**
   - 离线模式支持
   - 签到动画效果
   - 错误提示优化

---

## 版本历史

| 版本 | 日期 | 说明 |
|-----|------|------|
| v1.0.0 | 2026-03-14 | 初始版本，实现基础UI |
| v1.1.0 | 2026-03-14 | 添加GUET登录、课程同步、签到功能 |

---

## 参考资料

1. [桂林电子科技大学雨课堂](https://guet.yuketang.cn/)
2. [OkHttp官方文档](https://square.github.io/okhttp/)
3. [Hilt依赖注入指南](https://developer.android.com/training/dependency-injection/hilt-android)
4. [Kotlin Coroutines文档](https://kotlinlang.org/docs/coroutines-overview.html)

---

**文档维护者**: EasyCampus开发团队  
**最后更新**: 2026-03-14
