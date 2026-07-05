package io.github.samiuzhong.syncox

import android.content.Context
import io.github.samiuzhong.syncox.db.getRoomDatabase

/**
 * @author samiu 2026/7/5
 * @email samiuzhong@foxmail.com
 */
fun Syncox.initialize(
    context: Context,
    networkHandler: SyncoxNetworkHandler,
    config: SyncoxConfig = SyncoxConfig(),
) {
    val db = getRoomDatabase(getDatabaseBuilder(context))
    initInternal(db, networkHandler, config)
}
