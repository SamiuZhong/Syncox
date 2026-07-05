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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.samiuzhong.syncox.Syncox
import io.github.samiuzhong.syncox.SyncoxAction
import kotlinx.coroutines.launch

/**
 * @author samiu 2026/7/5
 * @email samiuzhong@foxmail.com
 */
data class BuyCoffeeAction(
    val coffeeName: String,
    val price: Double,
) : SyncoxAction {
    override val actionType: String = "BUY_COFFEE"
    override val payloadJson: String = """{"name":"$coffeeName", "price":$price}"""
}

@Composable
fun CoffeeScreen() {
    val coroutineScope = rememberCoroutineScope()

    // 🌟 核心亮点：全自动响应式的未完成任务数统计
    val pendingCount by Syncox.observePendingCount().collectAsState(initial = 0)

    // 模拟网络状态 UI 绑定
    var isNetworkOnline by remember { mutableStateOf(NetworkSimulator.isOnline) }

    // 简单的 UI 操作日志展示
    val uiLogs = remember { mutableStateListOf<String>() }

    fun addLog(msg: String) {
//        val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val time = "123"
        uiLogs.add(0, "[$time] $msg") // 插入到最前面
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
            "Syncox 引擎演示",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(16.dp))

        // --- 区域 1：控制台 (网络模拟) ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text("当前网络状态", fontWeight = FontWeight.Bold)
                    Text(
                        text = if (isNetworkOnline) "🟢 畅通无阻" else "🔴 已断开连接",
                        color = if (isNetworkOnline) Color(0xFF4CAF50) else Color(0xFFF44336),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                Switch(
                    checked = isNetworkOnline,
                    onCheckedChange = {
                        isNetworkOnline = it
                        NetworkSimulator.isOnline = it
                        addLog(if (it) "网络已恢复！Syncox 将自动清理积压队列。" else "网络已断开！接下来的请求将转入离线队列。")
                    },
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- 区域 2：监控大屏 ---
        AnimatedVisibility(visible = pendingCount > 0) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD)),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
//                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFF856404))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "检测到网络异常，当前有 $pendingCount 笔订单正在后台使用指数退避算法默默重试...",
                        color = Color(0xFF856404),
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
                        Syncox.enqueue(BuyCoffeeAction("生椰拿铁", 20.0))
                        addLog("🛒 提交生椰拿铁订单 -> Syncox 落盘瞬间完成！")
                    }
                },
                modifier = Modifier.weight(1f).padding(end = 8.dp),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text("☕ 购买 生椰拿铁")
            }

            Button(
                onClick = {
                    coroutineScope.launch {
                        Syncox.enqueue(BuyCoffeeAction("特浓美式", 15.0))
                        addLog("🛒 提交特浓美式订单 -> Syncox 落盘瞬间完成！")
                    }
                },
                modifier = Modifier.weight(1f).padding(start = 8.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5D4037)),
            ) {
                Text("☕ 购买 特浓美式")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- 区域 4：UI 响应日志 ---
        Text(
            "UI 层交互日志 (非引擎底层 Log)",
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
                Divider(color = Color(0xFFEEEEEE))
            }
        }
    }
}
