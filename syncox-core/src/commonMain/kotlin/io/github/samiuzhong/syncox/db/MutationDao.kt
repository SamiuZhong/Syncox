package io.github.samiuzhong.syncox.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * @author samiu 2026/7/4
 * @email samiuzhong@foxmail.com
 */
@Dao
interface MutationDao {
    /**
     * 业务端调用：将新的离线操作推入发件箱
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMutation(mutation: MutationRecordEntity)

    /**
     * 抓取待处理任务
     */
    @Query(
        "SELECT * FROM syncox_mutation_outbox WHERE status = 'PENDING' AND nextRetryAt <= :currentTimeMs ORDER BY createAt ASC LIMIT :limit",
    )
    suspend fun getPendingMutations(
        currentTimeMs: Long,
        limit: Int = 50,
    ): List<MutationRecordEntity>

    /**
     * 防并发锁：原子级状态更新
     * 当协程把任务抓出来准备发网络请求前，必须立刻调用这个方法。
     */
    @Query("UPDATE syncox_mutation_outbox SET status = 'IN_FLIGHT' WHERE mutationId IN (:mutationIds)")
    suspend fun markAsInFlight(mutationIds: List<String>)

    /**
     * 任务结算：网络请求成功或失败后的更新
     * 如果失败：status 变回 PENDING 或 FATAL_ERROR，计算并更新下次重试时间 nextRetryAt，重试次数 +1。
     */
    @Query(
        "UPDATE syncox_mutation_outbox SET status = :status, nextRetryAt = :nextRetryAt, retryCount = :retryCount WHERE mutationId = :mutationId",
    )
    suspend fun updateMutationState(
        mutationId: String,
        status: MutationStatus,
        nextRetryAt: Long,
        retryCount: Int,
    )

    /**
     * 任务成功发送后，物理删除
     */
    @Query("DELETE FROM syncox_mutation_outbox WHERE mutationId = :mutationId")
    suspend fun deleteMutation(mutationId: String)

    /**
     * 异常恢复机制：App 崩溃自愈
     * 如果任务正在 IN_FLIGHT 时 App 崩溃了，重启时它们会永远卡在 IN_FLIGHT。
     * 引擎初始化时需要调用此方法，把所有飞行中的任务强行拉回 PENDING 状态。
     */
    @Query("UPDATE syncox_mutation_outbox SET status = 'PENDING' WHERE status = 'IN_FLIGHT'")
    suspend fun resetInFlightToPending()

    /**
     * 返回当前仍卡在本地（未成功）的任务总数。
     */
    @Query("SELECT COUNT(*) FROM syncox_mutation_outbox WHERE status IN ('PENDING','IN_FLIGHT')")
    fun observePendingCount(): Flow<Int>
}
