package com.example.reptimer007.ui.addEditWorkout.addEditStep

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.reptimer007.R
import com.example.reptimer007.databinding.FragmentAddEditStepBinding
import com.example.reptimer007.util.exhaust
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_add_edit_step.*
import kotlinx.coroutines.flow.collect


@AndroidEntryPoint
class addEditStepFragment : Fragment(R.layout.fragment_add_edit_step) {

    private val viewModel: addEditStepViewModel by viewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentAddEditStepBinding.bind(view)

        binding.apply {


            checkboxRest.isChecked = viewModel.isRest == true

            stepName.setText(viewModel.stepName)
            stepMinutesInput.setText(viewModel.minutes.toString())
            stepSecondsInput.setText(viewModel.seconds.toString())

            stepName.addTextChangedListener {
                viewModel.stepName = it.toString()
            }
            checkboxRest.setOnCheckedChangeListener{_, isChecked ->
                viewModel.isRest = isChecked
            }
            stepMinutesInput.addTextChangedListener{
                if (it != null) {
                    if (it.isNotBlank()) {
                        var duration = it.toString().toInt()
                        minuteErrorText.isVisible = duration > 60
                        if (duration < 60) viewModel.minutes = duration
                    } else {
                        viewModel.minutes = 0
                    }
                }
            }
            stepSecondsInput.addTextChangedListener{
                if (it != null) {
                    if (it.isNotBlank()) {
                        var duration = it.toString().toInt()
                        secondsErrorText.isVisible = duration > 60
                        if (duration < 60) viewModel.seconds = duration

                    } else {
                        viewModel.seconds = 0
                    }
                }
            }

            buttonAdd.setOnClickListener{
                viewModel.onSaveClick()
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.addEditStepEvent.collect { event ->
                when(event) {
                    is addEditStepViewModel.AddEditStepEvent.ShowInvalidInputMessage -> {
                        Snackbar.make(requireView(), event.msg, Snackbar.LENGTH_LONG).show()
                    }
                    is addEditStepViewModel.AddEditStepEvent.NavigateBackWithResult -> {
                        binding.stepSecondsInput.clearFocus()
                        binding.stepMinutesInput.clearFocus()
                        binding.stepName.clearFocus()
                        setFragmentResult(
                            "add_edit_steps",
                            bundleOf("add_edit_result" to event.result)
                        )
                        findNavController().popBackStack()
                    }
                }.exhaust
            }
        }
    }

}