package com.example.reptimer007.ui.addEditWorkout

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.reptimer007.R
import com.example.reptimer007.data.Step
import com.example.reptimer007.databinding.FragmentAddEditWorkoutBinding
import com.example.reptimer007.util.exhaust
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_add_edit_workout.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


@AndroidEntryPoint
class addEditWorkoutFragment : Fragment(R.layout.fragment_add_edit_workout),
    StepAdapter.onStepItemClickListener {

    private val viewModel: addEditWorkoutViewModel by viewModels()
    private val Tag_ = "Add Edit Workout Frag"
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val stepAdapter = StepAdapter(this)
        val binding = FragmentAddEditWorkoutBinding.bind(view)

        binding.apply {
            recycler_view_steps.adapter = stepAdapter
            recycler_view_steps.layoutManager = LinearLayoutManager(requireContext())
            text_input_workout_name.setText(viewModel.workout?.name)

            text_input_workout_name.addTextChangedListener {
                viewModel.workoutName = it.toString()
            }
            add_step_button.setOnClickListener {
                viewModel.onAddStepClick()
            }

            button_add.setOnClickListener {
                viewModel.onSaveClick()
            }
            button_delete_workout.setOnClickListener {
                viewModel.onDeleteClick()
            }
        }

        setFragmentResultListener("add_edit_steps") { _, bundle ->
            val result = bundle.getInt("add_edit_result")
            viewModel.firstTime = false
            viewModel.onAddEditResult(result)
            viewModel.refreshSteps()
        }

        viewModel.steps.observe(viewLifecycleOwner) {
            lifecycleScope.launch {
                it.forEach {
                    it.workoutParentId?.let { it1 -> viewModel.updateworkoutDuration(it1) }
                }
            }
            stepAdapter.submitList(it)
        }
        viewModel.refreshSteps()

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.addEditWorkoutEvent.collect { event ->
                when (event) {
                    is addEditWorkoutViewModel.AddEditWorkoutEvent.NavigateBackWithResult -> {
                        binding.textInputWorkoutName.clearFocus()
                        setFragmentResult(
                            "add_edit_workouts",
                            bundleOf("add_edit_result" to event.result)
                        )
                        findNavController().popBackStack()
                    }
                    is addEditWorkoutViewModel.AddEditWorkoutEvent.ShowInvalidInputMessage -> {
                        Snackbar.make(requireView(), event.msg, Snackbar.LENGTH_LONG).show()
                    }
                    is addEditWorkoutViewModel.AddEditWorkoutEvent.NavigateToAddStep -> {
                        val action =
                            addEditWorkoutFragmentDirections.actionAddEditWorkoutFragmentToAddEditStepFragment(
                                workout = event.workout
                            )
                        findNavController().navigate(action)
                    }
                    is addEditWorkoutViewModel.AddEditWorkoutEvent.NavigateToEditStep -> {
                        val action =
                            addEditWorkoutFragmentDirections.actionAddEditWorkoutFragmentToAddEditStepFragment(
                                workout = event.workout,
                                step = event.step
                            )
                        findNavController().navigate(action)
                    }
                    is addEditWorkoutViewModel.AddEditWorkoutEvent.ShowStepSavedConfirmation -> {
                        Snackbar.make(requireView(), event.msg, Snackbar.LENGTH_SHORT).show()
                    }
                    is addEditWorkoutViewModel.AddEditWorkoutEvent.ShowDeleteWorkoutDialog -> {
                        val dialog: AlertDialog = AlertDialog.Builder(context)
                            .setTitle("Confirm Workout Deletion")
                            .setMessage("Delete Workout?")
                            .setPositiveButton("Ok", null)
                            .setNegativeButton("Cancel", null)
                            .show();
                        val positiveButton: Button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                        positiveButton.setOnClickListener {
                            viewModel.deleteWorkout(event.workout.workout)
                            dialog.cancel()
                            setFragmentResult(
                                "delete_workouts",
                                bundleOf("delete_result" to event.workout)
                            )
                            findNavController().popBackStack()
                        }
                    }
                    is addEditWorkoutViewModel.AddEditWorkoutEvent.ShowDeleteConfirmation -> {
                        Snackbar.make(requireView(), "Workout deleted", Snackbar.LENGTH_LONG)
                            .setAction("UNDO") {
                                viewModel.onUndoDeleteClick(event.workout)
                            }.show()
                    }
                    is addEditWorkoutViewModel.AddEditWorkoutEvent.ShowStepDeleteConfirmation -> {
                        Snackbar.make(requireView(), "Step deleted", Snackbar.LENGTH_LONG)
                            .setAction("UNDO") {
                                viewModel.onUndoStepDeleteClick(event.step)
                            }.show()
                    }
                }.exhaust
            }
        }

        // This callback will only be called when MyFragment is at least Started.
        val callback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            // Handle the back button event
            Log.e(Tag_, "callback")
            val dialog: AlertDialog = AlertDialog.Builder(context)
                .setTitle("Are you sure you want to leave?")
                .setMessage("All progress will be lost!")
                .setPositiveButton("Leave", null)
                .setNegativeButton("Cancel", null)
                .show();
            val positiveButton: Button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener {
                if(viewModel.workout == null) {
                    viewModel.deleteWorkout(viewModel.newWorkout)
                    Log.e(Tag_, "deleting workout")
                }
                dialog.cancel()
                findNavController().popBackStack()
            }
        }

        callback.isEnabled = true
    }

    override fun onEditStepClick(step: Step) {
        viewModel.onEditStepClick(step)
    }

    override fun onDeleteStepClick(step: Step) {
        viewModel.onDeleteStepClick(step)
    }
}