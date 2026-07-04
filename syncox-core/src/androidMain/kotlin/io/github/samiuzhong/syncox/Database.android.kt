package io.github.samiuzhong.syncox

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import io.github.samiuzhong.syncox.db.SyncoxDatabase

/**
 * @author samiu 2026/7/4
 * @email samiuzhong@foxmail.com
 */
fun getDatabaseBuilder(context: Context): RoomDatabase.Builder<SyncoxDatabase> {
    val appContext = context.applicationContext
    val dbFile = appContext.getDatabasePath("syncox.db")
    return Room.databaseBuilder<SyncoxDatabase>(
        context = appContext,
        name = dbFile.absolutePath,
    )
}
