package io.github.samiuzhong.syncox

import androidx.room.Room
import androidx.room.RoomDatabase
import io.github.samiuzhong.syncox.db.SyncoxDatabase
import java.io.File

/**
 * @author samiu 2026/7/4
 * @email samiuzhong@foxmail.com
 */
fun getDatabaseBuilder(): RoomDatabase.Builder<SyncoxDatabase> {
    val dbFile = File(System.getProperty("java.io.tmpdir"), "syncox.db")
    return Room.databaseBuilder<SyncoxDatabase>(name = dbFile.absolutePath)
}
