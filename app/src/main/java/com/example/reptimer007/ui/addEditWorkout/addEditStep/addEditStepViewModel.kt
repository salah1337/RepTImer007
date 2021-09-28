package com.example.reptimer007.ui.addEditWorkout.addEditStep

import android.annotation.SuppressLint
import android.util.Log
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reptimer007.data.Step
import com.example.reptimer007.data.StepDao
import com.example.reptimer007.data.Workout
import com.example.reptimer007.ui.ADD_WORKOUT_RESULT_OK
import com.example.reptimer007.ui.EDIT_WORKOUT_RESULT_OK
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import kotlin.math.min

class addEditStepViewModel @ViewModelInject constructor(
    @Assisted private val state: SavedStateHandle,
    private val stepDao: StepDao
    ) : ViewModel() {

    val workout = state.get<Workout>("workout")
    val step = state.get<Step>("step")
    private val addEditStepEventChannel = Channel<AddEditStepEvent>()
    val addEditStepEvent = addEditStepEventChannel.receiveAsFlow()

    var stepName: String = step?.name ?: ""
    var isRest: Boolean = step?.rest ?: false
    var minutes: Int = if(step != null) TimeUnit.MILLISECONDS.toMinutes(step.length.toLong()).toInt() else 0
    var seconds: Int = if(step != null) (TimeUnit.MILLISECONDS.toSeconds(step.length.toLong()) % TimeUnit.MINUTES.toSeconds(1)).toInt() else 0

    init {
        Log.e("KEEK", step?.length.toString())
    }

    fun onSaveClick() {
        if (stepName.isBlank()) {
            showInvalidInputMessage("Step name can not be empty!")
            return
        }
        if(step != null) {
            val length = (minutes * 1000 * 60) + (seconds * 1000)
            val updatedStep = step.copy(stepName, length, isRest)
            updateStep(updatedStep)
            Log.e("hh", "h55555")
        } else {
            val length = (minutes * 1000 * 60) + (seconds * 1000)
            val newStep = Step(stepName, length, isRest, workoutParentId = workout?.workoutId)
            createStep(newStep)
            Log.e("hh", "h")
        }
    }
    private fun createStep(step: Step) = viewModelScope.launch {
        val stepID = stepDao.insert(step)
        Log.e("stepid", stepID.toString())
        addEditStepEventChannel.send(AddEditStepEvent.NavigateBackWithResult(ADD_WORKOUT_RESULT_OK))
    }
    private fun updateStep(step: Step) = viewModelScope.launch {
        stepDao.update(step)
        addEditStepEventChannel.send(AddEditStepEvent.NavigateBackWithResult(EDIT_WORKOUT_RESULT_OK))
    }
    private fun showInvalidInputMessage(msg: String) = viewModelScope.launch {
        addEditStepEventChannel.send(AddEditStepEvent.ShowInvalidInputMessage(msg))
    }

    sealed class AddEditStepEvent {
        data class ShowInvalidInputMessage(val msg: String) : AddEditStepEvent()
        data class NavigateBackWithResult(val result: Int) : AddEditStepEvent()
    }
}