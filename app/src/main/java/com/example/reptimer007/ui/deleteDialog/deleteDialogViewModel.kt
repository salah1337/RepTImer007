package com.example.reptimer007.ui.deleteDialog

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.reptimer007.data.Step
import com.example.reptimer007.data.StepDao
import com.example.reptimer007.data.Workout
import com.example.reptimer007.data.WorkoutDao
import com.example.reptimer007.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class deleteDialogViewModel @ViewModelInject constructor(
    private val workoutDao: WorkoutDao,
    private val stepDao: StepDao,
    @ApplicationScope private val applicationScope: CoroutineScope,
    @Assisted private val state: SavedStateHandle
) : ViewModel() {
    val workout = state.get<Workout>("workout")
    val step = state.get<Step>("step")

    private val deleteDialogEventChannel = Channel<DeleteDialogEvent>()
    val deleteDialogEvent = deleteDialogEventChannel.receiveAsFlow()



    fun onDeleteWorkout(workout: Workout) = applicationScope.launch {
        workoutDao.delete(workout)
        deleteDialogEventChannel.send(DeleteDialogEvent.NavigateBackWithResult("workout"))
    }
    fun onDeleteStep(step: Step) = applicationScope.launch {
        stepDao.delete(step)
        deleteDialogEventChannel.send(DeleteDialogEvent.NavigateBackWithResult("step"))
    }

    sealed class DeleteDialogEvent {
        data class NavigateBackWithResult(val deleted: String) : DeleteDialogEvent()
    }
}