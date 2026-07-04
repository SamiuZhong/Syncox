package io.github.samiuzhong.syncox.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter

/**
 * @author samiu 2026/7/4
 * @email samiuzhong@foxmail.com
 */
enum class MutationStatus {
    /** 初始状态 / 等待同步：断网时生成的数据，或者重试冷却时间结束后的数据 */
    PENDING,

    /** 正在发送：引擎大循环正在处理这条数据，防止被其他并发协程重复抓取 */
    IN_FLIGHT,

    /** 彻底失败：如果遇到了 400 校验错误，或者超过最大重试次数，进入此状态不再发送 */
    FATAL_ERROR,

    /** 发送成功：通常成功后会直接删表，但保留此状态可用于历史记录备份 */
    SUCCESS,
}

class MutationStatusConverter {
    @TypeConverter
    fun fromStatus(status: MutationStatus): String = status.name

    @TypeConverter
    fun toStatus(name: String): MutationStatus = MutationStatus.valueOf(name)
}

/**
 * 核心发件箱表
 */
@Entity(tableName = "syncox_mutation_outbox")
class MutationRecordEntity(
    @PrimaryKey
    val mutationId: String,
    val mutationType: String,
    val payloadJson: String,
    val status: MutationStatus = MutationStatus.PENDING,
    val createAt: Long,
    val nextRetryAt: Long = 0L,
    val retryCount: Int = 0,
)
