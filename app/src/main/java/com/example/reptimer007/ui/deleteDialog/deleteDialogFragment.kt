package com.codinginflow.mvvmtodo.ui.deleteDialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.reptimer007.ui.deleteDialog.deleteDialogViewModel
import com.example.reptimer007.util.exhaust
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow

@AndroidEntryPoint
class DeleteDialogFragment : DialogFragment() {

    private val viewModel: deleteDialogViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.deleteDialogEvent.collect { event ->
                when(event) {
                    is deleteDialogViewModel.DeleteDialogEvent.NavigateBackWithResult -> {
                        setFragmentResult(
                            "delete_fragment",
                            bundleOf("deleted" to event.deleted)
                        )
                        findNavController().popBackStack()
                    }
                }.exhaust
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        var title: String = ""
        var message: String = ""
        if (viewModel.workout != null) {
            title = "Confirm Workout Deletion"
            message = "Delete this workout?"
        }
        if (viewModel.step != null) {
            title = "Confirm Step Deletion"
            message = "Delete this step?"
        }


        return AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setNegativeButton("cancel", null)
            .setPositiveButton("yes") { _, _ ->
                if (viewModel.workout != null) viewModel.onDeleteWorkout(viewModel.workout!!)
                if (viewModel.step != null) viewModel.onDeleteStep(viewModel.step!!)
            }
            .create()


        /*val positiveButton: Button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setOnClickListener {
            Toast.makeText(this, "Do not dismiss", Toast.LENGTH_SHORT).show();
        }*/

    }



}