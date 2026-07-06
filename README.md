# Syncox 🚀

**Syncox** 是专为 **Kotlin Multiplatform (KMP)** 和 **Android** 打造的、高可用的轻量级**客户端离线同步引擎**。它基于经典的**发件箱模式 (Outbox Pattern)** 架构，旨在将瞬息万变、充满不确定性的网络请求完全隔离在 UI 层之外，为用户提供极其丝滑的离线与弱网交互体验。

## 💡 为什么选择 Syncox？

在移动端弱网或断网环境下，传统的“前台阻塞等待网络响应”开发范式会导致严重的体验问题：

- 🔴 **阻塞 UI**：用户点击按钮后必须被迫死等 Loading 圈，网络超时或断网时会直接崩出错误弹窗，体验极差。
- 🔴 **样板代码臃肿**：ViewModel 里充斥着海量的 `try-catch`、网络状态监听器和复杂的 `RequestState` 重试状态机。
- 🔴 **重试对电量不友好**：简单的轮询在断网状态下会频繁空转，耗尽手机电量。

**Syncox 彻底终结了这些痛点！**

### 🌟 核心技术优势

- ⚡️ **发送即忘 (Fire-and-Forget)**：UI 层的任何操作均瞬间写入本地 SQLite 数据库并立即返回。对 UI 来说，网络请求已经“成功”，剩下的重传交由后台默默搞定。
- 🪄 **KSP 静态路由（编译期魔法）**：采用 KSP (Kotlin Symbol Processing) 编译期代码生成技术。一行代码都不需要改动你现有的 Ktorfit/Retrofit 接口，只需一个 `@OfflineSync` 注解，编译时自动生成零反射、安全的静态路由表。
- 🌍 **KMP 跨端一致性**：核心调度算法和 SQLite 数据库逻辑完全封装，各端表现完全一致。
- 📊 **完全响应式状态流**：提供 `observePendingCount()` 的 Flow 观察流，UI 层只需一行代码即可对“后台积压任务数”进行完全无感的动态渲染。

## 🏗 架构图解

```
 [ 响应式 UI (Compose/SwiftUI) ]
              |
              | 1. 调用 enqueue(Action) （强类型安全对象）
              v
 [ 内存序列化 (JSON String) ]
              |
              | 2. 毫秒级写入本地（对 UI 隐身，100% 成功率）
              v
 [ 本地发件箱缓冲区 (SQLite / Room Box) ] <--- (持续断网？安静地躺在里面)
              ^
              | 3. 后台守护进程 (Syncox Daemon) 轮询检索与退避精算
              v
 [ 编译期生成路由表 (GeneratedSyncoxRouter) ]
              |
              | 4. 零反射、安全的本地分发
              v
 [ 旁路网络代理层 (Ktor/Ktorfit/Retrofit) ] 
              |
              | 5. 跨端网络流发送
              v
       ( 外网服务器终点 )
```

## 📦 依赖引入

请确保你的项目使用了 Kotlin 2.0+，并在共享模块 `shared/build.gradle.kts` 中进行如下配置：

```kotlin
plugins {
    id("com.google.devtools.ksp") version "2.3.9" // 请与你的 Kotlin 版本严格对应
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
            // 暴露核心库给 Swift 可见
            export("io.github.samiuzhong:syncox-core:0.1.0")
        }
    }
    
    sourceSets {
        commonMain.dependencies {
            // 引入核心库
            api("io.github.samiuzhong:syncox-core:0.1.0")
        }
    }
}

dependencies {
    // 由于 iOS 和 Android 在 KMP 中的编译目标不同，必须为所有编译目标注入 KSP 依赖！
    add("kspAndroid", "io.github.samiuzhong:syncox-compiler:0.1.0")
    add("kspIosArm64", "io.github.samiuzhong:syncox-compiler:0.1.0")
    add("kspIosSimulatorArm64", "io.github.samiuzhong:syncox-compiler:0.1.0")
    add("kspIosX64", "io.github.samiuzhong:syncox-compiler:0.1.0")
}
```

## ⚙️ 引擎初始化

### 1. Android 端或纯 Android 项目

在宿主的全局 `Application` 中，直接喂入 KSP 生成的 `autoRouter`，即可实现路由无反射注册：

```kotlin
class SyncoxApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Syncox.initialize(this, Syncox.autoRouter)
    }
}
```

### 2. iOS 端

在宿主 `iOSApp` 启动时进行初始化：

```swift
import Shared
import SwiftUI

@main
struct iOSApp: App {
    init() {
        Syncox.shared.initialize(networkHandler: Syncox.shared.autoRouter)
    }
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
```

### 3. Desktop 端

在 Desktop 入口 `main` 函数中进行初始化：

```kotlin
fun main() = application {
    Syncox.initialize(Syncox.autoRouter)
    Window(onCloseRequest = ::exitApplication, title = "Syncox") {
        App()
    }
}
```

## 🛠️ 极简使用三步走

接入 Syncox 非常纯粹，业务开发同学只需掌握以下 3 个核心 API 的使用即可：

### 1. 定义业务 Action (数据落盘契约)

业务方只需实现 `SyncoxAction` 接口，定义其路由标识以及 JSON 序列化逻辑：

```kotlin
@Serializable
data class CreatePostRequest(val title: String, val body: String, val userId: Int)

class CreatePostAction(request: CreatePostRequest) : SyncoxAction {
    override val actionType: String = "CREATE_POST" // 路由寻址 Key
    override val payloadJson: String = Json.encodeToString(request) // 序列化
}
```

### 2. 挂载网络执行器 (离线消费端)

在任意负责发起真实网络请求的 `suspend` 方法上，标记 `@OfflineSync` 注解。**此方法仅供引擎后台守护进程自动调度，业务代码无需（且严禁）手动调用**：

```kotlin
@OfflineSync(action = "CREATE_POST")
suspend fun executeCreatePost(payloadJson: String): NetworkResult {
    return try {
        val request = Json.decodeFromString<CreatePostRequest>(payloadJson)
        val response = yourHttpClient.post("posts", request)
        
        if (response.status.isSuccess()) {
            NetworkResult.Success // 成功：引擎会自动从本地 SQLite 擦除该记录
        } else if (response.status.value in 400..499) {
            NetworkResult.Failure(isFatal = true) // 客户端致命错：引擎会将其转为 FATAL_ERROR 并放弃重试
        } else {
            NetworkResult.Failure(isFatal = false) // 服务端临时错：引擎自动触发指数退避重试
        }
    } catch (e: Exception) {
        NetworkResult.Failure(isFatal = false, error = e) // 断网波动：引擎自动进入静默等待与退避重试
    }
}
```

### 3. 任务入队与状态监听 (生产端与观察者)

当需要发送请求时，调用 `enqueue` 将 Action 瞬间写入本地发件箱；同时，可按需通过 `observePendingCount()` 流响应式监听当前积压的队列深度：

```kotlin
// A. 瞬间入队（本地毫秒级入库，完全不卡顿主线程）
Syncox.enqueue(CreatePostAction(request))

// B. 观察队列深度（Flow 流，可直接用于在前台优雅地渲染“同步中...”等无感提示）
val pendingCountFlow: Flow<Int> = Syncox.observePendingCount()
```

## ⚖️ License

本项目基于 **Apache License 2.0** 协议开源。详细协议内容请查看项目下的 [LICENSE](./LICENSE) 文件。
