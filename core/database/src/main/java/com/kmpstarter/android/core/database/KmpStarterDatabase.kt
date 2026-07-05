package com.kmpstarter.android.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [UserEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class KmpStarterDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}
