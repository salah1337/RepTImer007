package com.example.reptimer007.di

import android.app.Application
import androidx.room.Room
import com.example.reptimer007.data.StepDatabase
import com.example.reptimer007.data.WorkoutDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object AppModule {


    @Provides
    @Singleton
    fun provideWorkoutDB(app: Application, callback: WorkoutDatabase.callBack)
    = Room.databaseBuilder(app, WorkoutDatabase::class.java, "workout_database")
        .fallbackToDestructiveMigration()
        .addCallback(callback)
        .build()

    @Provides
    @Singleton
    fun provideStepDB(app: Application)
            = Room.databaseBuilder(app, StepDatabase::class.java, "step_database")
        .fallbackToDestructiveMigration()
        .build()

    @Provides
    fun provideWorkoutDao(db: WorkoutDatabase) = db.workoutDao()

    @Provides
    fun provideStepDao(db: StepDatabase) = db.stepDao()

    @ApplicationScope
    @Provides
    @Singleton
    fun provideApplicationScope() = CoroutineScope(SupervisorJob())
}

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope