package com.samiu.syncox

import io.github.samiuzhong.syncox.Syncox
import kotlinx.coroutines.flow.Flow

/**
 * @author samiu 2026/7/6
 * @email samiuzhong@foxmail.com
 */
class PostRepository {
    /**
     * 响应式监听当前发件箱中积压的任务数量
     */
    fun observePendingCount(): Flow<Int> = Syncox.observePendingCount()

    /**
     * 将发帖任务安全入库
     */
    suspend fun createPost(
        title: String,
        content: String,
        userId: Int = 1,
    ) {
        val request = CreatePostRequest(title, content, userId)
        val action = CreatePostAction(request)
        // 将请求推送至 Syncox 发件箱
        Syncox.enqueue(action)
    }
}
