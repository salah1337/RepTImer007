package com.example.reptimer007.ui.workouts

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.reptimer007.R
import com.example.reptimer007.data.Workout
import com.example.reptimer007.data.WorkoutAndSteps
import com.example.reptimer007.databinding.ItemWorkoutBinding
import com.example.reptimer007.util.convertTime
import kotlinx.coroutines.CoroutineScope
import java.text.DecimalFormat

class WorkoutAdapter(private val listener: OnItemClickListener): ListAdapter<WorkoutAndSteps, WorkoutAdapter.WorkoutViewHolder>(DiffCallback()) {



    inner class WorkoutViewHolder(private val binding: ItemWorkoutBinding) : RecyclerView.ViewHolder(binding.root){

        init {
            binding.apply {
                playWorkoutButton.setOnClickListener {
                    val position = adapterPosition
                    if(position != RecyclerView.NO_POSITION) {
                        val workout = getItem(position)
                        listener.onPlayIconClicked(workout.workout)
                    }
                }
                editWorkoutButton.setOnClickListener {
                    val position = adapterPosition
                    if(position != RecyclerView.NO_POSITION) {
                        val workout = getItem(position)
                        listener.onEditIconCLicked(workout.workout)
                    }
                }
                addToFavButton.setOnClickListener {
                    val position = adapterPosition
                    if(position != RecyclerView.NO_POSITION) {
                        val workout = getItem(position)
                        listener.onFavIconClicked(workout.workout).also { notifyDataSetChanged() }
                    }
                }
            }
        }
        @SuppressLint("ResourceAsColor")
        fun bind(workout: WorkoutAndSteps, position: Int) {

            binding.apply {
                workoutNameText.text = workout.workout.name
                if (workout.workout.finishedEditing) {
                    descriptionText.text = convertTime(workout.workout.duration.toInt().toLong())
                } else {
                    descriptionText.text = "Finish creating this workout!"
                    descriptionText.setTextColor(R.color.green)
                }
                if (workout.workout.isFavorite) {
                    addToFavButton.setImageResource(R.drawable.ic_star_full)
                    DrawableCompat.setTint(
                        DrawableCompat.wrap(addToFavButton.getDrawable()),
                        ContextCompat.getColor(itemView.context, R.color.yellow)
                    );
                }else{
                    addToFavButton.setImageResource(R.drawable.ic_star_empty)
                }

            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutViewHolder {
        val binding = ItemWorkoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WorkoutViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem, position)
    }

    interface OnItemClickListener {
        fun onItemClick(workout: Workout)
        fun onPlayIconClicked(workout: Workout)
        fun onEditIconCLicked(workout: Workout)
        fun onFavIconClicked(workout: Workout)
    }

    class DiffCallback : DiffUtil.ItemCallback<WorkoutAndSteps>() {
        override fun areItemsTheSame(oldItem: WorkoutAndSteps, newItem: WorkoutAndSteps) =
            oldItem.workout.workoutId == newItem.workout.workoutId

        override fun areContentsTheSame(oldItem: WorkoutAndSteps, newItem: WorkoutAndSteps) =
            oldItem == newItem

    }
}