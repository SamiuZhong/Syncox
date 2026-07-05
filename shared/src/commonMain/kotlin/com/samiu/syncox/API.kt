package com.samiu.syncox

import io.github.samiuzhong.syncox.NetworkResult
import io.github.samiuzhong.syncox.OfflineSync
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

/**
 * @author samiu 2026/7/5
 * @email samiuzhong@foxmail.com
 */
object NetworkSimulator {
    var isOnline: Boolean = true
}

@OfflineSync(action = "BUY_COFFEE")
suspend fun executeBuyCoffeeOffline(payloadJson: String): NetworkResult {
    println("🌐 [真实网络层] 正在处理发往服务器的订单: $payloadJson")

    delay(800.milliseconds)

    return if (NetworkSimulator.isOnline) {
        println("✅ [真实网络层] 订单发送成功！")
        NetworkResult.Success
    } else {
        println("❌ [真实网络层] 检测到断网！发送失败，交由 Syncox 引擎退避重试...")
        NetworkResult.Failure(isFatal = false, error = Exception("Simulated network down"))
    }
}
