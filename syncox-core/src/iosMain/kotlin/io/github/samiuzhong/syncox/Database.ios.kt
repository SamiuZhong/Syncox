package io.github.samiuzhong.syncox

import androidx.room.Room
import androidx.room.RoomDatabase
import io.github.samiuzhong.syncox.db.SyncoxDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

/**
 * @author samiu 2026/7/4
 * @email samiuzhong@foxmail.com
 */
fun getDatabaseBuilder(): RoomDatabase.Builder<SyncoxDatabase> {
    val dbFilePath = documentDirectory() + "/syncox.db"
    return Room.databaseBuilder<SyncoxDatabase>(name = dbFilePath)
}

@OptIn(ExperimentalForeignApi::class)
private fun documentDirectory(): String {
    val documentDirectory =
        NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null,
        )
    return requireNotNull(documentDirectory?.path)
}
