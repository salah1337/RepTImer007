package com.example.reptimer007.ui.workouts

import android.util.Log
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.example.reptimer007.data.*
import com.example.reptimer007.ui.EDIT_WORKOUT_RESULT_OK
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class WorkoutViewModel @ViewModelInject constructor(
    private val workoutDao: WorkoutDao,
    private val stepdao: StepDao,
    private val preferenceManager: preferenceManager,
    @Assisted private val state: SavedStateHandle
) : ViewModel() {
    val searchQuery = state.getLiveData("searchQuery", "")

    val preferenceFLow = preferenceManager.preferenceFlow

    val workoutEventChannel = Channel<WorkoutEvent>()
    val workoutEvent = workoutEventChannel.receiveAsFlow()

    private val workoutFlow = combine(
        searchQuery.asFlow(),
        preferenceFLow
    ) { searchQuery, filterPreferences ->
        Pair(searchQuery, filterPreferences)
    }.flatMapLatest { (searchQuery, filterPreferences) ->
        workoutDao.getWorkouts(searchQuery, filterPreferences.sortOrder, filterPreferences.onlyFav)
    }
    val workouts = workoutFlow

    fun onSortOrderSelected(sortOrder: sort_Order) = viewModelScope.launch {
        preferenceManager.updateSortOrder(sortOrder)
    }

    fun showOnlyFav(onlyFav: Boolean) = viewModelScope.launch {
        preferenceManager.updateOnlyFav(onlyFav)
    }

    init {
        viewModelScope.launch {
            //addSteps()
        }
    }

    fun onWorkoutSwiped(workoutAndSteps: WorkoutAndSteps) = viewModelScope.launch {
        stepdao.getWorkoutSteps(workoutAndSteps.workout.workoutId).forEach { stepdao.delete(it) }
        workoutDao.delete(workoutAndSteps.workout)
        workoutEventChannel.send(WorkoutEvent.ShowUndoDeletedMessage(workoutAndSteps.workout))
    }
    fun onAddNewWorkoutlick() = viewModelScope.launch {
        workoutEventChannel.send(WorkoutEvent.NavigateToAddWorkoutPage)
    }
    fun onTimerClick() = viewModelScope.launch {
        workoutEventChannel.send(WorkoutEvent.NavigateToTimerPage)
    }
    fun onPlayWorkoutClicked(workout: Workout) =  viewModelScope.launch {
        workoutEventChannel.send(WorkoutEvent.NavigateToPlayWorkoutPage(workout))
    }
    fun onEditWorkoutlick(workout: Workout) = viewModelScope.launch {
        workoutEventChannel.send(WorkoutEvent.NavigateToEditWorkoutPage(workout))
    }
    fun onFavWorkoutClick(workout: Workout) = viewModelScope.launch{
        var updatedWorkout = workout.copy(isFavorite = !workout.isFavorite)
        workoutDao.update(updatedWorkout)
    }
    fun onAddEditResult(result: Int) {
        when(result) {
            EDIT_WORKOUT_RESULT_OK -> showWorkoutConfirmationMsg("Workout Saved")
        }
    }
    private fun showWorkoutConfirmationMsg(msg: String) = viewModelScope.launch {
        workoutEventChannel.send(WorkoutEvent.showWorkoutSavedConfirmation(msg))
    }
    fun onDeleteResult(workout: Workout) = viewModelScope.launch {
        workoutEventChannel.send(WorkoutEvent.ShowWorkoutDeletedConfirmation(workout))
    }
    suspend fun updateworkoutDuration(workout: Workout) {
        workout.let {
            var duration: Int = 0
            var steps_ = stepdao.getWorkoutSteps(workout.workoutId)
            steps_.forEach {
                duration += it.length
            }
            var updatedWorkout = workout.copy(duration = duration.toString())
            workoutDao.update(updatedWorkout)
        }
    }

    sealed class WorkoutEvent {
        data class ShowUndoDeletedMessage(val workout: Workout) : WorkoutEvent()
        object NavigateToAddWorkoutPage : WorkoutEvent()
        object NavigateToTimerPage : WorkoutEvent()
        data class NavigateToEditWorkoutPage(val workout: Workout) : WorkoutEvent()
        data class NavigateToPlayWorkoutPage(val workout: Workout) : WorkoutEvent()
        data class showWorkoutSavedConfirmation(val msg: String) : WorkoutEvent()
        data class ShowWorkoutDeletedConfirmation(val workout: Workout) : WorkoutEvent()
    }

    /*suspend fun addSteps() {
        stepdao.insert(Step("Warm up", 1, false, "minute", 1))
        stepdao.insert(Step("Rest", 20, true, "second", 1))
        stepdao.insert(Step("Warm up", 1, false, "minute", 1))
        stepdao.insert(Step("Rest", 20, true, "second", 1))
    }*/

    fun onUndoDeleteClick(workout: Workout) = viewModelScope.launch {
        workoutDao.insert(workout)
    }

}


