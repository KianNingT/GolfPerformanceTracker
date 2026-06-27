package com.play.golf.perf.tracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.play.golf.perf.tracker.data.local.dao.PlayerDao
import com.play.golf.perf.tracker.data.local.dao.ShotDao
import com.play.golf.perf.tracker.data.local.entity.PlayerDetailEntity
import com.play.golf.perf.tracker.data.local.entity.PlayerEntity
import com.play.golf.perf.tracker.data.local.entity.ShotEntity

@Database(
    entities = [
        PlayerEntity::class,
        PlayerDetailEntity::class,
        ShotEntity::class,
    ],
    version = 1,
    exportSchema = false
)
abstract class GolfDatabase : RoomDatabase() {
    abstract fun playerDao(): PlayerDao
    abstract fun shotDao(): ShotDao

    companion object {
        const val DATABASE_NAME = "golf_perf_tracker.db"
    }
}