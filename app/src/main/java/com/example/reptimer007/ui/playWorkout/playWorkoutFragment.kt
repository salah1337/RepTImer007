package com.example.reptimer007.ui.playWorkout

import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.widget.Chronometer
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.reptimer007.R
import com.example.reptimer007.databinding.FragmentPlayWorkoutBinding
import com.example.reptimer007.util.convertTime
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_play_workout.*
import kotlinx.android.synthetic.main.fragment_timer.*
import kotlinx.android.synthetic.main.item_workout.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import kotlin.concurrent.schedule

@AndroidEntryPoint
class playWorkoutFragment : Fragment(R.layout.fragment_play_workout) {

    private val viewModel: playWorkoutViewModel by viewModels()
    lateinit var chronometer: Chronometer
    lateinit var countDownTimer: CountDownTimer



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentPlayWorkoutBinding.bind(view)
        var index: Int
        var stepCount = viewModel.steps.value?.size ?: 0
        var pauseOffset: Long = 0
        var paused: Boolean = false
        var Tag: String = "Workout Fragment"

        viewModel.getSteps()


        fun bind() {
            binding.apply {
                currentStepName.text = viewModel.currentStep.name
                currentStepDuration.text = convertTime(viewModel.currentStep.length.toLong())
                nextStep.text =
                    "${viewModel.nextStep.name} : ${convertTime(viewModel.nextStep.length.toLong())}"
            }
        }

        fun toggleRestartUI(toggle: Boolean) {
            if (toggle) {
                binding.apply {
                    workout_start_button.isVisible = false
                    workout_pause_button.isVisible = false
                    stop_button.isVisible = false
                    next_step_button.isVisible = false
                    last_step_button.isVisible = false
                    workout_restart_button.isVisible = true
                    next_workout_subtitle.isVisible = false
                    next_step.isVisible = false
                    workout_completed_text.isVisible = true
                }
                chronometer.isCountDown = true
                chronometer.setBase(SystemClock.elapsedRealtime())
            } else {
                binding.apply {
                    workout_start_button.isVisible = true
                    stop_button.isVisible = false
                    workout_pause_button.isVisible = false
                    next_step_button.isVisible = false
                    last_step_button.isVisible = false
                    workout_restart_button.isVisible = false
                    next_workout_subtitle.isVisible = true
                    next_step.isVisible = true
                    workout_completed_text.isVisible = false
                }
            }
        }

        fun initWorkout() {
            index = viewModel.currentIndex
            chronometer.isCountDown = true
            chronometer.setBase(SystemClock.elapsedRealtime())
            if (index == stepCount - 1) {
                binding.apply {
                    currentStepName.text = viewModel.currentStep.name
                    currentStepDuration.text = convertTime(viewModel.currentStep.length.toLong())
                    nextStep.text = "Last step, keep goin!"
                }
            } else {
                bind()
            }
        }

        fun startWorkout() {
            workout_start_button.isVisible = false
            workout_pause_button.isVisible = true
            stop_button.isVisible = true
            index = viewModel.currentIndex
            last_step_button.isVisible = index > 0
            next_step_button.isVisible = index < viewModel.steps.value!!.size - 1
            Log.e("start workout log", "starting, at ${index}")
            if (index == stepCount) {
                chronometer.stop()
                viewModel.currentIndex = 0
                viewModel.updateSteps()
                initWorkout()
                toggleRestartUI(true)
                Log.e("start workout log", "FINISHED")
            } else {
                val interval =
                    (if (pauseOffset.toInt() != 0) pauseOffset else viewModel.currentStep.length).toLong()
                chronometer.setBase(SystemClock.elapsedRealtime() + interval)
                chronometer.start()
                countDownTimer = object : CountDownTimer(interval, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        if(workout_start_button == null) cancel()
                    }
                    override fun onFinish() {
                        viewModel.nextStep()
                        initWorkout()
                        Log.e("start workout log", "starging workout at index ${index}")
                        startWorkout()
                    }
                }.start()
            }
            pauseOffset = 0
            paused = false

        }

        fun restartWorkout() {
            viewModel.currentIndex = 0
            toggleRestartUI(false)
            viewModel.updateSteps()
            bind()
            initWorkout()
            startWorkout()
        }

        fun pauseWorkout() {
            if (!paused) {
                paused = true
                chronometer.stop()
                countDownTimer.cancel()
                pauseOffset = Math.abs(SystemClock.elapsedRealtime() - chronometer.getBase())
                Log.e(Tag, "pauseoffset: $pauseOffset")
                workout_start_button.isVisible = true
                workout_pause_button.isVisible = false
                stop_button.isVisible = false
                next_step_button.isVisible = false
                last_step_button.isVisible = false
            }
        }
        fun stopWorkout() {
            chronometer.stop()
            countDownTimer.cancel()
            viewModel.currentIndex = 0
            toggleRestartUI(false)
            viewModel.updateSteps()
            bind()
            initWorkout()
        }
        fun toNextStep() {
            chronometer.stop()
            countDownTimer.cancel()
            viewModel.nextStep()
            initWorkout()
            startWorkout()
        }
        fun toLastStep() {
            chronometer.stop()
            countDownTimer.cancel()
            viewModel.lastStep()
            initWorkout()
            startWorkout()
        }
        binding.apply {
            workout_name.text = viewModel.workout!!.name
            workout_start_button.isVisible = true
            workout_start_button.setOnClickListener {
                startWorkout()
            }
            workout_restart_button.setOnClickListener {
                restartWorkout()
            }
            workout_pause_button.setOnClickListener {
                pauseWorkout()
            }
            stop_button.setOnClickListener {
                stopWorkout()
            }
            next_step_button.setOnClickListener {
                toNextStep()
            }
            last_step_button.setOnClickListener {
                toLastStep()
            }
            chronometer = current_step_chronometer
        }

        bind()

        viewModel.steps.observe(viewLifecycleOwner) {
            stepCount = viewModel.steps.value!!.size
            viewModel.updateSteps()
            bind()
            initWorkout()
        }

        // This callback will only be called when MyFragment is at least Started.
        val callback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            // Handle the back button event
            Log.e(Tag, "callback")
            findNavController().popBackStack()
        }

        callback.isEnabled = true


    }
}