package io.github.samiuzhong.syncox

import io.github.samiuzhong.syncox.db.MutationDao
import io.github.samiuzhong.syncox.db.MutationRecordEntity
import io.github.samiuzhong.syncox.db.MutationStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.pow
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.uuid.Uuid

/**
 * @author samiu 2026/7/4
 * @email samiuzhong@foxmail.com
 */
class SyncoxEngineImpl(
    private val dao: MutationDao,
    private val networkHandler: SyncoxNetworkHandler,
    private val config: SyncoxConfig = SyncoxConfig(),
    private val engineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : SyncoxEngine {
    private var loopJob: Job? = null

    private fun currentTimeMs(): Long = Clock.System.now().toEpochMilliseconds()

    override suspend fun enqueue(action: SyncoxAction) {
        val mutation =
            MutationRecordEntity(
                mutationId = Uuid.random().toString(),
                mutationType = action.actionType,
                payloadJson = action.payloadJson,
                status = MutationStatus.PENDING,
                createAt = currentTimeMs(),
                nextRetryAt = 0L,
                retryCount = 0,
            )
        dao.insertMutation(mutation)
    }

    override fun observePendingCount(): Flow<Int> = dao.observePendingCount()

    override fun start() {
        if (loopJob?.isActive == true) return
        loopJob =
            engineScope.launch {
                // 1. 冷启动自愈：将上次 App 崩溃时可能卡在飞行中的任务全部重置
                dao.resetInFlightToPending()
                // 2. 启动循环
                while (isActive) {
                    try {
                        processPendingMutations()
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    delay(config.pollIntervalMs.milliseconds)
                }
            }
    }

    override fun stop() {
        loopJob?.cancel()
        loopJob = null
    }

    /**
     * 引擎核心调度
     */
    private suspend fun processPendingMutations() {
        val now = currentTimeMs()
        val mutations = dao.getPendingMutations(now, config.batchSize)
        if (mutations.isEmpty()) return

        val ids = mutations.map { it.mutationId }
        dao.markAsInFlight(ids)

        for (mutation in mutations) {
            val result = networkHandler.execute(mutation.mutationType, mutation.payloadJson)
            when (result) {
                is NetworkResult.Failure -> handleFailure(mutation, result.isFatal)
                NetworkResult.Success -> dao.deleteMutation(mutation.mutationId)
            }
        }
    }

    /**
     * 指数退避算法
     */
    private suspend fun handleFailure(
        mutation: MutationRecordEntity,
        isFatal: Boolean,
    ) {
        val newRetryCount = mutation.retryCount + 1

        // 判断是否应该放弃抢救
        val newStatus =
            if (isFatal || newRetryCount >= config.maxRetries) {
                MutationStatus.FATAL_ERROR
            } else {
                MutationStatus.PENDING
            }

        val nextRetryAt =
            if (newStatus == MutationStatus.FATAL_ERROR) {
                0L
            } else {
                // 核心算法：基础时间 * (2 的重试次数次方)
                val backoffMultiplier = 2.0.pow(mutation.retryCount).toLong()
                val delayMs =
                    (config.baseBackoffDelayMs * backoffMultiplier).coerceAtMost(config.maxBackoffDelayMs)
                currentTimeMs() + delayMs
            }

        // 更新回数据库，等待下一次循环捞取
        dao.updateMutationState(
            mutationId = mutation.mutationId,
            status = newStatus,
            nextRetryAt = nextRetryAt,
            retryCount = newRetryCount,
        )
    }
}
