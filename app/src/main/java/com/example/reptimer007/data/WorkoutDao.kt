package com.example.reptimer007.data

import androidx.room.*
import com.example.reptimer007.ui.workouts.WorkoutViewModel
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {

    fun getWorkouts(search: String, sortOrder: sort_Order, onlyFav: Boolean): Flow<List<WorkoutAndSteps>> =
        when(onlyFav) {
            true -> {
                when(sortOrder) {
                    sort_Order.BY_DATE -> getWorkoutsWithStepsSortedByDateByFav(search)
                    sort_Order.BY_NAME -> getWorkoutsWithStepsSortedByNameByFav(search)
                }
            }
            false-> {
                when(sortOrder) {
                    sort_Order.BY_DATE -> getWorkoutsWithStepsSortedByDate(search)
                    sort_Order.BY_NAME -> getWorkoutsWithStepsSortedByName(search)
                }
            }
        }

    @Transaction
    @Query("SELECT * FROM workout_table WHERE name LIKE '%' || :search || '%' ORDER BY name")
    fun getWorkoutsWithStepsSortedByName(search: String): Flow<List<WorkoutAndSteps>>

    @Transaction
    @Query("SELECT * FROM workout_table WHERE name LIKE '%' || :search || '%' ORDER BY created")
    fun getWorkoutsWithStepsSortedByDate(search: String): Flow<List<WorkoutAndSteps>>

    @Transaction
    @Query("SELECT * FROM workout_table WHERE (isFavorite == 1) AND name LIKE '%' || :search || '%' ORDER BY name")
    fun getWorkoutsWithStepsSortedByNameByFav(search: String): Flow<List<WorkoutAndSteps>>

    @Transaction
    @Query("SELECT * FROM workout_table WHERE (isFavorite == 1) AND name LIKE '%' || :search || '%' ORDER BY created")
    fun getWorkoutsWithStepsSortedByDateByFav(search: String): Flow<List<WorkoutAndSteps>>

    @Transaction
    @Query("SELECT * FROM workout_table WHERE workoutId==:id")
    suspend fun getById(id: Int): WorkoutAndSteps

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(workout: Workout) : Long

    @Update
    suspend fun update(workout: Workout)

    @Delete
    suspend fun delete(workout: Workout)
}