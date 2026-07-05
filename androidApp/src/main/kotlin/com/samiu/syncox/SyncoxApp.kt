package com.samiu.syncox

import android.app.Application
import io.github.samiuzhong.syncox.Syncox
import io.github.samiuzhong.syncox.autoRouter
import io.github.samiuzhong.syncox.initialize

/**
 * @author samiu 2026/7/5
 * @email samiuzhong@foxmail.com
 */
class SyncoxApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Syncox.initialize(this, Syncox.autoRouter)
    }
}
