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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory

/**
 * @author samiu 2026/7/5
 * @email samiuzhong@foxmail.com
 */
@Composable
@Preview
fun App() {
    val factory =
        viewModelFactory {
            initializer {
                PostViewModel(PostRepository())
            }
        }
    val viewModel: PostViewModel = viewModel(factory = factory)
    MaterialTheme {
        Scaffold { paddingValues ->
            SyncoxScreen(
                viewModel,
                Modifier.fillMaxSize().background(Color(0xFFF5F7FA)).padding(paddingValues),
            )
        }
    }
}

@Composable
fun SyncoxScreen(
    viewModel: PostViewModel,
    modifier: Modifier = Modifier,
) {
    // 监听 ViewModel 暴露的完全响应式的状态
    val pendingCount by viewModel.pendingCount.collectAsState()
    val uiLogs by viewModel.uiLogs.collectAsState()

    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // --- 操作指南 ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("💡 测试指南", fontWeight = FontWeight.Bold, color = Color(0xFF1976D2))
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "底层已接入真实 Ktor 请求。\n👉 请手动关闭 Wi-Fi 和数据网络来测试离线发送！",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- 监控大屏 ---
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
                        text = "🚀 后台发件箱安全同步中，当前排队数: $pendingCount",
                        color = Color(0xFF1976D2),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- 业务操作区 ---
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(
                onClick = {
                    viewModel.publishPost(
                        "Hello MVVM",
                        "这是一篇标准 MVVM 架构发出的帖子",
                    )
                },
                modifier = Modifier.weight(1f).padding(end = 8.dp),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text("🚀 发布正常帖子")
            }

            Button(
                onClick = { viewModel.publishPost("断网也不怕", "极其丝滑，毫无卡顿") },
                modifier = Modifier.weight(1f).padding(start = 8.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5D4037)),
            ) {
                Text("🚀 发布测试帖子")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- UI 日志展示区 ---
        Text(
            "操作日志 (发件箱入库极速响应)",
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
