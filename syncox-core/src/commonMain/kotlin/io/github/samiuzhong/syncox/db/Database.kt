package io.github.samiuzhong.syncox.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

/**
 * @author samiu 2026/7/4
 * @email samiuzhong@foxmail.com
 */
@Database(entities = [MutationRecordEntity::class], version = 1)
@ConstructedBy(SyncoxDatabaseConstructor::class)
@TypeConverters(MutationStatusConverter::class)
abstract class SyncoxDatabase : RoomDatabase() {
    abstract fun getDao(): MutationDao
}

@Suppress("KotlinNoActualForExpect")
expect object SyncoxDatabaseConstructor : RoomDatabaseConstructor<SyncoxDatabase> {
    override fun initialize(): SyncoxDatabase
}

fun getRoomDatabase(builder: RoomDatabase.Builder<SyncoxDatabase>): SyncoxDatabase =
    builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
