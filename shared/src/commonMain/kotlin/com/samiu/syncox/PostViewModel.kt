package com.samiu.syncox

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

/**
 * @author samiu 2026/7/6
 * @email samiuzhong@foxmail.com
 */
class PostViewModel(
    private val repository: PostRepository = PostRepository(),
) : ViewModel() {
    val pendingCount: StateFlow<Int> =
        repository
            .observePendingCount()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = 0,
            )

    private val _uiLogs = MutableStateFlow<List<String>>(emptyList())
    val uiLogs: StateFlow<List<String>> = _uiLogs.asStateFlow()

    /**
     * 发帖方法
     */
    fun publishPost(
        title: String,
        content: String,
    ) {
        viewModelScope.launch {
            repository.createPost(title, content)
            addLog("📝 请求已落入 Syncox 发件箱 (标题: $title)")
        }
    }

    @OptIn(FormatStringsInDatetimeFormats::class)
    private fun addLog(msg: String) {
        val time =
            LocalDateTime.Format { byUnicodePattern("HH:mm:ss") }.format(
                Clock.System.now().toLocalDateTime(TimeZone.of("Asia/Shanghai")),
            )
        val currentLogs = _uiLogs.value.toMutableList()
        currentLogs.add(0, "[$time] $msg")
        _uiLogs.value = currentLogs
    }
}
