package io.github.samiuzhong.syncox

import kotlinx.coroutines.flow.Flow

/**
 * @author samiu 2026/7/4
 * @email samiuzhong@foxmail.com
 */
interface SyncoxAction {
    /**
     * 业务路由表示
     */
    val actionType: String

    /**
     * 业务数据的 JSON 序列化字符串
     */
    val payloadJson: String
}

data class SyncoxConfig(
    /**
     * 指数退避的基础时间（毫秒）
     */
    val baseBackoffDelayMs: Long = 2000L,
    /**
     * 最大重试间隔（毫秒），防止指数爆炸
     */
    val maxBackoffDelayMs: Long = 60 * 60 * 1000L,
    /**
     * 每条记录的最大允许重试次数。超过此次数则进入 FATAL_ERROR 状态
     */
    val maxRetries: Int = 10,
    /**
     * 每次从数据库抓取任务的最大数量
     */
    val batchSize: Int = 50,
    /**
     * 轮询间隔（毫秒）
     */
    val pollIntervalMs: Long = 5000L,
)

interface SyncoxEngine {
    /**
     * 提交一个离线任务到发件箱
     * 这是一个极其轻量的本地数据库写操作，瞬间完成，绝不阻塞 UI。
     *
     * @param action 业务方定义的操作动作
     */
    suspend fun enqueue(action: SyncoxAction)

    /**
     * 响应式监听当前仍在队列中（PENDING 或 IN_FLIGHT）等待同步的任务总数。
     */
    fun observePendingCount(): Flow<Int>

    fun start()

    fun stop()
}

sealed class NetworkResult {
    data object Success : NetworkResult()

    /**
     * @param isFatal 是否为致命错误
     *
     * 如果为 true (例如 400 参数错误、签名过期)，引擎将放弃重试，将其标记为 FATAL_ERROR。
     * 如果为 false (例如 500、网络超时)，引擎将进入指数退避，等待下一次重试。
     */
    data class Failure(
        val isFatal: Boolean,
        val error: Throwable? = null,
    ) : NetworkResult()
}

/**
 * 业务方需要实现的网络动作委托
 */
interface SyncoxNetworkHandler {
    suspend fun execute(
        actionType: String,
        payloadJson: String,
    ): NetworkResult
}
