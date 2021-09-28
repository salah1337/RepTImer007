package com.example.reptimer007.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.text.DateFormat
import javax.inject.Inject

@Entity(tableName = "workout_table")
@Parcelize
data class Workout @Inject constructor (
    val name: String,
    val duration: String = "0",
    val isFavorite: Boolean = false,
    val finishedEditing: Boolean = false,
    val created: Long = System.currentTimeMillis(),
    val edited: Long = System.currentTimeMillis(),
    @PrimaryKey(autoGenerate = true) val workoutId: Int = 0
) : Parcelable{
    val createdDateFormatted: String
        get() = DateFormat.getDateTimeInstance().format(created)
    val editedDateFormatted: String
        get() = DateFormat.getDateTimeInstance().format(edited)

}



