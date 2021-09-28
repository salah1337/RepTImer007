package com.example.reptimer007.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize


@Entity(tableName = "step_table")
@Parcelize
data class Step (
    var name: String,
    val length: Int,
    val rest: Boolean,
    val workoutParentId: Int? = null,
    @PrimaryKey(autoGenerate = true) val id: Int = 0
        ) : Parcelable {
}