package io.github.samiuzhong.syncox

import io.github.samiuzhong.syncox.db.SyncoxDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

/**
 * @author samiu 2026/7/4
 * @email samiuzhong@foxmail.com
 */
object Syncox {
    private var engine: SyncoxEngine? = null

    /**
     * 引擎初始化
     */
    internal fun initInternal(
        database: SyncoxDatabase,
        networkHandler: SyncoxNetworkHandler,
        config: SyncoxConfig = SyncoxConfig(),
    ) {
        if (engine != null) return
        engine =
            SyncoxEngineImpl(
                dao = database.getDao(),
                networkHandler = networkHandler,
                config = config,
            ).apply {
                start()
            }
        println("[Syncox] 🚀 离线同步引擎初始化成功！后台守护大循环已启动。")
    }

    /**
     * 提交离线任务
     *
     * @throws IllegalStateException SDK 未初始化
     */
    suspend fun enqueue(action: SyncoxAction) {
        val currentEngine =
            engine
                ?: throw IllegalStateException("[Syncox] ❌ 尚未初始化！请先调用 Syncox.initialize()")
        currentEngine.enqueue(action)
    }

    /**
     * 监听当前未同步成功的任务数量。
     */
    fun observePendingCount(): Flow<Int> = engine?.observePendingCount() ?: emptyFlow()

    fun stop() {
        engine?.stop()
    }
}
