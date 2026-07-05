package io.github.samiuzhong.syncox

import io.github.samiuzhong.syncox.db.getRoomDatabase

/**
 * @author samiu 2026/7/5
 * @email samiuzhong@foxmail.com
 */
fun Syncox.initialize(
    networkHandler: SyncoxNetworkHandler,
    config: SyncoxConfig = SyncoxConfig(),
) {
    val db = getRoomDatabase(getDatabaseBuilder())
    initInternal(db, networkHandler, config)
}
