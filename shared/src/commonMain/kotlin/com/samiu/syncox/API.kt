package com.samiu.syncox

import io.github.samiuzhong.syncox.NetworkResult
import io.github.samiuzhong.syncox.OfflineSync
import io.ktor.client.HttpClient
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.LoggingFormat
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable

/**
 * @author samiu 2026/7/5
 * @email samiuzhong@foxmail.com
 */
val httpClient =
    HttpClient {
        install(Logging) {
            format = LoggingFormat.OkHttp
            level = LogLevel.ALL
            logger =
                object : Logger {
                    override fun log(message: String) {
                        println(message)
                    }
                }
        }
    }

@Serializable
data class CreatePostRequest(
    val title: String,
    val body: String,
    val userId: Int,
)

@OfflineSync(action = "CREATE_POST")
suspend fun executeCreatePost(payloadJson: String): NetworkResult =
    try {
        println("🌐 [Ktor 网络层] 正在向 JSONPlaceholder 发送 POST 请求...")

        // 3. 发起真实的 HTTP POST 请求
        val response: HttpResponse =
            httpClient.post("https://jsonplaceholder.typicode.com/posts") {
                contentType(ContentType.Application.Json)
                setBody(payloadJson)
            }

        println("🌐 [Ktor 网络层] 服务器返回状态码: ${response.status.value}")

        // 4. 将 Ktor 的响应映射为 Syncox 引擎需要的结果
        if (response.status.isSuccess()) {
            println("✅ [Ktor 网络层] 帖子发布成功！")
            NetworkResult.Success
        } else if (response.status.value in 400..499) {
            // 客户端错误（例如 400 格式错误），不要浪费资源去重试了
            println("❌ [Ktor 网络层] 客户端致命错误，放弃抢救。")
            NetworkResult.Failure(isFatal = true, error = Exception("HTTP 4xx: ${response.status}"))
        } else {
            // 服务器错误（5xx），这属于网络波动，交给引擎退避重试
            println("⚠️ [Ktor 网络层] 服务器异常，交由 Syncox 触发指数退避重试...")
            NetworkResult.Failure(
                isFatal = false,
                error = Exception("HTTP 5xx: ${response.status}"),
            )
        }
    } catch (e: Exception) {
        // 真正的断网（例如：ConnectException, UnknownHostException）会走到这个 Catch 块
        println("📵 [Ktor 网络层] 捕获到底层网络异常 (已断网): ${e.message}")
        NetworkResult.Failure(isFatal = false, error = e)
    }
