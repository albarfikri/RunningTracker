package com.albar.runningtracker.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [Run::class],
    version = 1
)

@TypeConverters(BitmapConverters::class)
abstract class RunningDatabase : RoomDatabase() {
    abstract fun getRunDao(): RunDAO
}