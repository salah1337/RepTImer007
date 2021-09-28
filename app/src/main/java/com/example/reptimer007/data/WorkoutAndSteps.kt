package com.example.reptimer007.data

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.android.parcel.Parcelize

@Parcelize
data class WorkoutAndSteps(
    @Embedded val workout: Workout,
    @Relation(
        parentColumn = "workoutId",
        entityColumn = "workoutParentId"
    )
    val steps: List<Step>

) : Parcelable