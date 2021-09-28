package com.example.reptimer007.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StepDao {

    @Query("SELECT * FROM step_table")
    suspend fun getSteps(): List<Step>

    @Query("SELECT * FROM step_table WHERE workoutParentId=:workoutId")
    suspend fun getWorkoutSteps(workoutId: Int): List<Step>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(step: Step) : Long

    @Update
    suspend fun update(step: Step)

    @Delete
    suspend fun delete(step: Step)
}