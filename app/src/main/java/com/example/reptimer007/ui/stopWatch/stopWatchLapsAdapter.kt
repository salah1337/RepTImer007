package com.example.reptimer007.ui.stopWatch

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.reptimer007.data.Lap
import com.example.reptimer007.data.Step
import com.example.reptimer007.databinding.ItemTimerLapsBinding
import com.example.reptimer007.util.convertTime
import java.util.concurrent.TimeUnit

class stopWatchLapsAdapter : ListAdapter<Lap, stopWatchLapsAdapter.lapViewHolder>(DiffCallback()) {

    class lapViewHolder(private val binding: ItemTimerLapsBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(lap: Lap)  {
            binding.apply {
                lapIndex.text = layoutPosition.toString()
                lapText1.text = convertTime(lap.start)
                lapText2.text = convertTime(lap.finish)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): lapViewHolder {
        val binding = ItemTimerLapsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return stopWatchLapsAdapter.lapViewHolder(binding)
    }



    override fun onBindViewHolder(holder: lapViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }

    class DiffCallback : DiffUtil.ItemCallback<Lap>() {
        override fun areItemsTheSame(oldItem: Lap, newItem: Lap) =
            oldItem.start == newItem.start

        override fun areContentsTheSame(oldItem: Lap, newItem: Lap) =
            oldItem == newItem
    }
}