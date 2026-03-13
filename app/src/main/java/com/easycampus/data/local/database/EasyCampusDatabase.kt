package com.easycampus.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.easycampus.data.local.dao.AccountDao
import com.easycampus.data.local.dao.CourseDao
import com.easycampus.data.local.dao.CheckInDao
import com.easycampus.data.local.entity.AccountEntity
import com.easycampus.data.local.entity.CourseEntity
import com.easycampus.data.local.entity.CheckInEntity
import com.easycampus.data.local.entity.CheckInTaskEntity

@Database(
    entities = [
        AccountEntity::class,
        CourseEntity::class,
        CheckInEntity::class,
        CheckInTaskEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class EasyCampusDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun courseDao(): CourseDao
    abstract fun checkInDao(): CheckInDao
}
