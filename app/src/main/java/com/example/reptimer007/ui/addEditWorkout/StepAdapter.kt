package com.example.reptimer007.ui.addEditWorkout

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.reptimer007.data.Step
import com.example.reptimer007.data.WorkoutAndSteps
import com.example.reptimer007.databinding.FragmentAddEditWorkoutBinding
import com.example.reptimer007.databinding.ItemStepBinding
import com.example.reptimer007.databinding.ItemWorkoutBinding
import com.example.reptimer007.ui.workouts.WorkoutAdapter
import com.example.reptimer007.util.convertTime
import kotlinx.android.synthetic.main.item_step.*

class StepAdapter(private val listener: onStepItemClickListener) : ListAdapter<Step, StepAdapter.stepViewHolder>(DiffCallback()){

    inner class stepViewHolder(private val binding: ItemStepBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(step: Step){
            binding.apply {
                //stepNameText.text = step.name
                stepNameText.text = step.name
                stepDurationText.text = "${convertTime(step.length.toLong())}"
            }
        }

        init {
            binding.apply {
                stepEditButton.setOnClickListener{
                    val position = adapterPosition
                    if(position != RecyclerView.NO_POSITION) {
                        val step = getItem(position)
                        listener.onEditStepClick(step)
                    }
                }

                stepDeleteButton.setOnClickListener{
                    val position = adapterPosition
                    if(position != RecyclerView.NO_POSITION) {
                        val step = getItem(position)
                        listener.onDeleteStepClick(step)
                    }
                }
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): stepViewHolder {
        val binding = ItemStepBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return stepViewHolder(binding)
    }

    override fun onBindViewHolder(holder: stepViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }

    interface onStepItemClickListener {
        fun onEditStepClick(step: Step)
        fun onDeleteStepClick(step: Step)
    }

    class DiffCallback : DiffUtil.ItemCallback<Step>() {
        override fun areItemsTheSame(oldItem: Step, newItem: Step) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Step, newItem: Step) =
            oldItem == newItem

    }

}