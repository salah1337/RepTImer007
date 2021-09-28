package com.example.reptimer007.ui.stopWatch

import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.widget.Chronometer
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.reptimer007.R
import com.example.reptimer007.data.Lap
import com.example.reptimer007.databinding.FragmentTimerBinding
import kotlinx.android.synthetic.main.fragment_timer.*
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit


class stopWatchFragment : Fragment(R.layout.fragment_timer) {

    private lateinit var chronometer_: Chronometer
    private var running: Boolean = false
    private var pauseOffset: Long = 0
    private val viewModel: stopWatchViewModel by viewModels()
    private var laps: MutableList<Lap> = emptyList<Lap>().toMutableList()
    private var startLap: Long = 0
    private var endLap: Long = 0

    val lapsAdapter = stopWatchLapsAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentTimerBinding.bind(view)

        binding.apply {

            recyclerViewLaps.apply {
                adapter = lapsAdapter
                layoutManager = LinearLayoutManager(requireContext())
            }

            chronometer_ = chronometer
            timer_start_button.setOnClickListener { startTimer() }
            timer_start_button.isVisible = true
            timer_pause_button.setOnClickListener { pauseTimer() }
            timer_lap_button.setOnClickListener { lapTimer() }
            timer_reset_button.setOnClickListener { resetTimer() }

            lapsAdapter.submitList(laps.toList())

        }


    }

    fun startTimer() {
        if (!running) {
            chronometer_.setBase(SystemClock.elapsedRealtime() - pauseOffset)
            chronometer_.start()
            running = true
            timer_start_button.isVisible = false
            timer_pause_button.isVisible = true
            btns.isVisible = true
            timer_lap_button.isVisible = true
        }
    }

    fun pauseTimer() {
        if (running) {
            chronometer_.stop()
            pauseOffset = SystemClock.elapsedRealtime() - chronometer_.getBase()
            endLap = pauseOffset
            running = false
            timer_start_button.isVisible = true
            timer_pause_button.isVisible = false
        }
    }

    fun lapTimer() {
        if(running) {
            endLap = SystemClock.elapsedRealtime() - chronometer_.getBase()
        }else{
            timer_lap_button.isVisible = false
        }

        laps.add(Lap(startLap, endLap))
        startLap = endLap


        lapsAdapter.submitList(laps.toList())

    }

    fun resetTimer() {
        chronometer_.setBase(SystemClock.elapsedRealtime())
        laps = emptyList<Lap>().toMutableList()
        lapsAdapter.submitList(laps.toList())
        chronometer_.stop()
        endLap = 0
        startLap = 0
        running = false
        pauseOffset = 0
        timer_start_button.isVisible = true
        timer_pause_button.isVisible = false
        btns.isVisible = false
    }
}