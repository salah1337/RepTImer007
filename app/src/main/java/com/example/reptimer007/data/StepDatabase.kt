package com.example.reptimer007.data

import androidx.room.Database
import androidx.room.RoomDatabase
import dagger.Provides
import dagger.hilt.android.AndroidEntryPoint

@Database(entities = [Step::class], version = 1, exportSchema = false)
abstract class StepDatabase : RoomDatabase() {

    abstract fun stepDao() : StepDao
}