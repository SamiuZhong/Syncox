package com.samiu.syncox

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.samiuzhong.syncox.Syncox
import io.github.samiuzhong.syncox.SyncoxAction
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

/**
 * @author samiu 2026/7/5
 * @email samiuzhong@foxmail.com
 */
class CreatePostAction(
    request: CreatePostRequest,
) : SyncoxAction {
    override val actionType: String = "CREATE_POST"

    // 利用 kotlinx.serialization 在入队前瞬间将其转为 JSON 字符串
    override val payloadJson: String = Json.encodeToString(request)
}

@Composable
fun CoffeeScreen() {
    val coroutineScope = rememberCoroutineScope()

    // 🌟 核心亮点：全自动响应式的未完成任务数统计
    // 业务层只看这个数字！不需要去监听复杂的网络状态！
    val pendingCount by Syncox.observePendingCount().collectAsState(initial = 0)

    // 简单的 UI 操作日志展示
    val uiLogs = remember { mutableStateListOf<String>() }

    fun addLog(msg: String) {
//        val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val time = "123"
        uiLogs.add(0, "[$time] $msg")
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F7FA))
                .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            "Syncox 跨端网络演示",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(16.dp))

        // --- 区域 1：测试操作指南 ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("💡 真实网络测试指南", fontWeight = FontWeight.Bold, color = Color(0xFF1976D2))
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text =
                        "底层已接入 Ktor 请求 JSONPlaceholder API。\n" +
                            "👉 请下拉手机状态栏，手动关闭 Wi-Fi 和数据网络来测试离线能力！",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- 区域 2：监控大屏 (真实的业务侧逻辑) ---
        AnimatedVisibility(visible = pendingCount > 0) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color(0xFF1976D2),
                        strokeWidth = 2.dp,
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "🚀 后台安全队列同步中，当前排队数: $pendingCount",
                        color = Color(0xFF1976D2),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- 区域 3：业务操作区 ---
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(
                onClick = {
                    coroutineScope.launch {
                        // ✨ 2. UI 层的极致调用！
                        // 业务同学只管 new 出一个真实的数据对象，根本看不见 JSON！
                        val requestObj =
                            CreatePostRequest(
                                title = "Hello KMP",
                                body = "这是使用 Ktor 发送的强类型帖子",
                                userId = 1,
                            )
                        val action = CreatePostAction(requestObj)

                        Syncox.enqueue(action)
                        addLog("📝 提交发帖请求 -> 瞬间落盘至 Syncox！")
                    }
                },
                modifier = Modifier.weight(1f).padding(end = 8.dp),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text("🚀 发布测试帖子")
            }

            Button(
                onClick = {
                    coroutineScope.launch {
                        // 构建第二个测试对象
                        val requestObj =
                            CreatePostRequest(
                                title = "Syncox Awesome",
                                body = "离线同步真香，完全不阻塞 UI",
                                userId = 2,
                            )
                        Syncox.enqueue(CreatePostAction(requestObj))
                        addLog("📝 提交发帖请求 -> 瞬间落盘至 Syncox！")
                    }
                },
                modifier = Modifier.weight(1f).padding(start = 8.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5D4037)),
            ) {
                Text("🚀 发布第二篇")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- 区域 4：UI 响应日志 ---
        Text(
            "UI 层操作日志 (非底层 Ktor Log)",
            modifier = Modifier.align(Alignment.Start),
            color = Color.Gray,
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .padding(8.dp),
        ) {
            items(uiLogs) { log ->
                Text(
                    text = log,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(vertical = 4.dp),
                )
                HorizontalDivider(color = Color(0xFFEEEEEE))
            }
        }
    }
}
