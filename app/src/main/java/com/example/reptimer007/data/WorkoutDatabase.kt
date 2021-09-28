package com.example.reptimer007.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.reptimer007.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider


@Database(entities = [Workout::class, Step::class], version = 2, exportSchema = false)
abstract class WorkoutDatabase : RoomDatabase(){
    abstract fun workoutDao() : WorkoutDao


    class callBack @Inject constructor(
        private val stepdao: StepDao,
        private val database: Provider<WorkoutDatabase>,
        @ApplicationScope private val applicationScope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            val workoutdao = database.get().workoutDao()
            var workouts = listOf<String>("chest", "arms", "legs", "shoulders", "abs", "cardio")
            applicationScope.launch {
                workouts.forEach{ workout ->
                    var workout = Workout(name = "$workout", finishedEditing = true)
                    workoutdao.insert(workout)
                    stepdao.insert(Step("Warm up", 1*1000*60, false, workout.workoutId))
                    stepdao.insert(Step("Warm up", 20*1000, true, workout.workoutId))
                    stepdao.insert(Step("Warm up", 1*1000*60, false, workout.workoutId))
                    stepdao.insert(Step("Warm up", 20*1000, true, workout.workoutId))
                }
            }
        }

    }
}