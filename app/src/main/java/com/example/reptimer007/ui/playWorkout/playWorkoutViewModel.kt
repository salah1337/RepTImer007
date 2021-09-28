package com.example.reptimer007.ui.playWorkout

import android.util.Log
import android.widget.Chronometer
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.example.reptimer007.data.Step
import com.example.reptimer007.data.StepDao
import com.example.reptimer007.data.Workout
import kotlinx.coroutines.launch

class playWorkoutViewModel @ViewModelInject constructor(
    @Assisted private val state: SavedStateHandle,
    private val stepDao: StepDao,
) : ViewModel() {

    val workout = state.get<Workout>("workout")
    lateinit var chronometer: Chronometer

    val _steps = MutableLiveData<List<Step>>()
    val steps: LiveData<List<Step>> get() = _steps

    fun getSteps() = viewModelScope.launch {
        _steps.value = when (val workout = workout) {
            null -> {
                emptyList()
            }
            else -> {
                stepDao.getWorkoutSteps(workout.workoutId)
            }
        }
        //steps = workout?.let { stepDao.getWorkoutSteps(it.workoutId) }!!
        Log.e("KEK", steps.value!!.size.toString())
        updateSteps()
    }

    fun updateSteps() {
        if (currentIndex >= steps.value!!.size - 1) {
            currentStep = steps.value!![currentIndex]
        }else{
            currentStep = steps.value!![currentIndex]
            nextStep = steps.value!![currentIndex + 1]
        }
    }

    fun nextStep() {
        Log.e("Next Step Log", "current index: ${currentIndex.toString()}, step count: ${steps.value!!.size}")
        if (currentIndex == steps.value!!.size - 1) {
            updateSteps()
            currentIndex++
            Log.e("Next step log", "FINISHED")
        }else {
            Log.e("Next Step Log", "about to intrement index, current: ${currentIndex}, next: ${currentIndex+1}")
            currentIndex++
            updateSteps()
        }
    }

    fun lastStep() {
        Log.e("Next Step Log", "current index: ${currentIndex.toString()}, step count: ${steps.value!!.size}")
        if (currentIndex == steps.value!!.size - 1) {
            currentIndex--
            updateSteps()
            Log.e("Next step log", "FINISHED")
        }else {
            Log.e("Next Step Log", "about to intrement index, current: ${currentIndex}, next: ${currentIndex+1}")
            currentIndex--
            updateSteps()
        }
    }

    fun playWorkout() {

    }

    val fillerStep = Step("kek", 15, false, workoutParentId = workout?.workoutId)
    var currentStep: Step = fillerStep
    var nextStep: Step = fillerStep
    var currentIndex: Int = 0

}