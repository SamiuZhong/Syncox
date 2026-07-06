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

/**
 * 真实请求实现
 * KSP 编译器会自动扫描 @OfflineSync 注解，将其注册进生成的路由表。
 *
 * @param payloadJson 由[CreatePostAction.payloadJson]生成
 */
@OfflineSync(action = "CREATE_POST")
suspend fun executeCreatePost(payloadJson: String): NetworkResult =
    try {
        // 发起真实的 HTTP POST 请求
        val response: HttpResponse =
            httpClient.post("https://jsonplaceholder.typicode.com/posts") {
                contentType(ContentType.Application.Json)
                setBody(payloadJson)
            }

        // 将 Ktor 的响应映射为 Syncox 引擎需要的结果
        if (response.status.isSuccess()) {
            println("✅ [Ktor网络层] 帖子发布成功！引擎将自动清理该记录。")
            NetworkResult.Success
        } else if (response.status.value in 400..499) {
            println("❌ [Ktor网络层] 客户端致命错误 (${response.status})，放弃重试。")
            NetworkResult.Failure(isFatal = true, error = Exception("HTTP 4xx Error"))
        } else {
            println("⚠️ [Ktor网络层] 服务器波动 (${response.status})，交由 Syncox 触发指数退避重试...")
            NetworkResult.Failure(isFatal = false, error = Exception("HTTP 5xx Error"))
        }
    } catch (e: Exception) {
        // 真正的断网（如 DNS 解析失败、Timeout）会被这里捕获
        println("📵 [Ktor网络层] 捕获到底层网络异常 (已断网): ${e.message}")
        NetworkResult.Failure(isFatal = false, error = e)
    }
