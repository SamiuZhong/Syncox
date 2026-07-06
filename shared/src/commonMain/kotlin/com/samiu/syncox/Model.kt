package com.samiu.syncox

import io.github.samiuzhong.syncox.SyncoxAction
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * @author samiu 2026/7/6
 * @email samiuzhong@foxmail.com
 */
@Serializable
data class CreatePostRequest(
    val title: String,
    val body: String,
    val userId: Int,
)

/**
 * 实现 Syncox 引擎可识别的 Action
 */
class CreatePostAction(
    request: CreatePostRequest,
) : SyncoxAction {
    // 定义该任务在发件箱中的路由标识
    override val actionType: String = "CREATE_POST"

    // 入队瞬间，自动把参数打包成 JSON 字符串
    override val payloadJson: String = Json.encodeToString(request)
}
