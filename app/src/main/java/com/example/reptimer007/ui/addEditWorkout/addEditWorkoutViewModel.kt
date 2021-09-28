package com.example.reptimer007.ui.addEditWorkout

import android.util.Log
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.example.reptimer007.data.*
import com.example.reptimer007.di.ApplicationScope
import com.example.reptimer007.ui.ADD_WORKOUT_RESULT_OK
import com.example.reptimer007.ui.EDIT_WORKOUT_RESULT_OK
import com.example.reptimer007.util.convertTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class addEditWorkoutViewModel @ViewModelInject constructor(
    private val workoutDao: WorkoutDao,
    private val stepDao: StepDao,
    @Assisted private val state: SavedStateHandle,
    @ApplicationScope private val applicationScope: CoroutineScope
) : ViewModel() {
    val workout = state.get<Workout>("workout")
    var workoutWithSteps: Flow<List<WorkoutAndSteps>> = emptyFlow()
    val _steps = MutableLiveData<List<Step>>()
    val steps: LiveData<List<Step>> get() = _steps
    var workoutSteps: Flow<List<Step>> = emptyFlow()
    var newWorkoutAndStepsId: Int = 0
    var tag = "ADD_EDIT_VIEWMODEL"
    var firstTime: Boolean = true
    lateinit var newWorkout: Workout

    private val addEditWorkoutEventChannel = Channel<AddEditWorkoutEvent>()
    val addEditWorkoutEvent = addEditWorkoutEventChannel.receiveAsFlow()

    suspend fun getSteps(): List<Step> = when (val workout = workout) {
        null -> { // Workout is null, let's create it
            stepDao.getWorkoutSteps(newWorkoutAndStepsId)
        }
        else -> stepDao.getWorkoutSteps(workout.workoutId)
            .also { updateworkoutDuration(workout.workoutId) }
    }

    fun refreshSteps() = viewModelScope.launch {
        _steps.value = when (val workout = workout) {
            null -> { // Workout is null, let's create it
                if (firstTime) {
                    newWorkoutAndStepsId = workoutDao.insert(Workout("My New Workout")).toInt()
                    newWorkout = workoutDao.getById(newWorkoutAndStepsId).workout
                    stepDao.insert(
                        Step(
                            "Warm Up Jumping Jacks",
                            1 * 1000 * 60 + 30 * 1000,
                            false,
                            newWorkoutAndStepsId
                        )
                    )
                    stepDao.insert(Step("Rest", 15 * 1000, true, newWorkoutAndStepsId))
                    firstTime = false
                    Log.e("kek", "heyo")
                }
                Log.e("kek", "hey")
                updateworkoutDuration(newWorkoutAndStepsId)
                stepDao.getWorkoutSteps(newWorkoutAndStepsId)
            }
            else -> stepDao.getWorkoutSteps(workout.workoutId).also {
                updateworkoutDuration(workout.workoutId)
                Log.e("kek", "heyyy ${workout.workoutId}")
            }
        }
    }

    suspend fun updateworkoutDuration(workoutId: Int) {
        var duration: Int = 0
        var steps_ = stepDao.getWorkoutSteps(workoutId)
        steps_.forEach {
            duration += it.length
        }
        var oldWorkout = workoutDao.getById(workoutId).workout
        var updatedWorkout = oldWorkout.copy(duration = duration.toString())
        workoutDao.update(updatedWorkout)
    }

    fun onAddStepClick() = viewModelScope.launch {
        if (workout != null) {
            addEditWorkoutEventChannel.send(AddEditWorkoutEvent.NavigateToAddStep(workout))
        } else {
            addEditWorkoutEventChannel.send(AddEditWorkoutEvent.NavigateToAddStep(newWorkout))
        }
    }

    fun onEditStepClick(step: Step) = viewModelScope.launch {
        if (workout != null) {
            addEditWorkoutEventChannel.send(AddEditWorkoutEvent.NavigateToEditStep(workout, step))
        } else {
            addEditWorkoutEventChannel.send(
                AddEditWorkoutEvent.NavigateToEditStep(
                    newWorkout,
                    step
                )
            )
        }
    }

    fun onDeleteStepClick(step: Step) = viewModelScope.launch {
        stepDao.delete(step)
        refreshSteps()
        addEditWorkoutEventChannel.send(AddEditWorkoutEvent.ShowStepDeleteConfirmation(step))
    }

    fun onDeleteClick() = viewModelScope.launch {
        if(workout != null) {
            addEditWorkoutEventChannel.send(AddEditWorkoutEvent.ShowDeleteWorkoutDialog(workoutDao.getById(workout.workoutId)))
        }
    }

    fun onSaveClick() {
        if (workoutName.isBlank()) {
            showInvalidInputMessage("Workout name can not be empty!")
            return
        }
        if (steps.value!!.size < 2) {
            showInvalidInputMessage("Workout must have at least two steps!")
            return
        }
        if (workout != null) {
            val updatedWorkout = workout.copy(name = workoutName, finishedEditing = true)
            updateWorkout(updatedWorkout)
        } else {
            val updatedWorkout = newWorkout.copy(name = workoutName, finishedEditing = true)
            viewModelScope.launch { workoutDao.update(updatedWorkout) }
            createWorkout()
        }
    }

    fun onAddEditResult(result: Int) {
        when (result) {
            EDIT_WORKOUT_RESULT_OK -> showStepConfirmationMsg("Step Saved")
            ADD_WORKOUT_RESULT_OK -> showStepConfirmationMsg("Step Added")
        }
    }
    fun onDeleteResult(workout: Workout) {
        showDeleteConfirmationMsg(workout)
    }
    fun deleteWorkout(workout: Workout) = applicationScope.launch {
        stepDao.getWorkoutSteps(workout.workoutId).forEach { stepDao.delete(it) }
        workoutDao.delete(workout)
    }.also { onDeleteResult(workout) }

    private fun showDeleteConfirmationMsg(workout: Workout) = viewModelScope.launch {
        addEditWorkoutEventChannel.send(AddEditWorkoutEvent.ShowDeleteConfirmation(workout))
    }

    private fun showStepConfirmationMsg(msg: String) = viewModelScope.launch {
        addEditWorkoutEventChannel.send(AddEditWorkoutEvent.ShowStepSavedConfirmation(msg))
    }
    fun onUndoDeleteClick(workout: Workout) = viewModelScope.launch {
        workoutDao.insert(workout)
    }

    fun onUndoStepDeleteClick(step: Step) = viewModelScope.launch {
        stepDao.insert(step)
        refreshSteps()
    }

    private fun createWorkout() = viewModelScope.launch {
        addEditWorkoutEventChannel.send(
            AddEditWorkoutEvent.NavigateBackWithResult(
                EDIT_WORKOUT_RESULT_OK
            )
        )
    }

    private fun updateWorkout(workout: Workout) = viewModelScope.launch {
        workoutDao.update(workout)
        addEditWorkoutEventChannel.send(
            AddEditWorkoutEvent.NavigateBackWithResult(
                EDIT_WORKOUT_RESULT_OK
            )
        )
    }

    private fun showInvalidInputMessage(msg: String) = viewModelScope.launch {
        addEditWorkoutEventChannel.send(AddEditWorkoutEvent.ShowInvalidInputMessage(msg))
    }

    sealed class AddEditWorkoutEvent {
        data class ShowInvalidInputMessage(val msg: String) : AddEditWorkoutEvent()
        data class NavigateBackWithResult(val result: Int) : AddEditWorkoutEvent()
        data class NavigateToAddStep(val workout: Workout) : AddEditWorkoutEvent()
        data class ShowStepSavedConfirmation(val msg: String) : AddEditWorkoutEvent()
        data class ShowDeleteWorkoutDialog(val workout: WorkoutAndSteps) : AddEditWorkoutEvent()
        data class ShowDeleteConfirmation(val workout: Workout) : AddEditWorkoutEvent()
        data class ShowStepDeleteConfirmation(val step: Step) : AddEditWorkoutEvent()
        data class NavigateToEditStep(val workout: Workout, val step: Step) : AddEditWorkoutEvent()

    }

    init {
        viewModelScope.launch {
            getSteps()
        }
    }

    var workoutName = state.get<String>("workoutName") ?: workout?.name ?: ""
        set(value) {
            field = value
            state.set("workoutName", value)
        }


}