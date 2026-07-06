package com.samiu.syncox

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.github.samiuzhong.syncox.Syncox
import io.github.samiuzhong.syncox.autoRouter
import io.github.samiuzhong.syncox.initialize

/**
 * @author samiu 2026/7/5
 * @email samiuzhong@foxmail.com
 */
fun main() =
    application {
        Syncox.initialize(Syncox.autoRouter)
        Window(
            onCloseRequest = ::exitApplication,
            title = "Syncox",
        ) {
            App()
        }
    }
