package com.example.reptimer007.ui.workouts

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.reptimer007.R
import com.example.reptimer007.data.Workout
import com.example.reptimer007.data.WorkoutAndSteps
import com.example.reptimer007.data.sort_Order
import com.example.reptimer007.databinding.FragmentWorkoutsBinding
import com.example.reptimer007.util.exhaust
import com.example.reptimer007.util.OnQueryTextChanged
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_workouts.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WorkoutsFragment: Fragment(R.layout.fragment_workouts), WorkoutAdapter.OnItemClickListener {

    private val viewModel: WorkoutViewModel by viewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val binding = FragmentWorkoutsBinding.bind(view)

        val workoutsAdapter = WorkoutAdapter(this)

        binding.apply {
            recycler_view_routines.apply {
                adapter = workoutsAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
            }
            button_add.setOnClickListener{
                viewModel.onAddNewWorkoutlick()
            }
            button_timer.setOnClickListener{
                viewModel.onTimerClick()
            }

            ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val workout = workoutsAdapter.currentList[viewHolder.adapterPosition]
                    Log.e("on swipe", "${workout.workout.workoutId}")
                    viewModel.onWorkoutSwiped(workout)
                }
            }).attachToRecyclerView(recyclerViewRoutines)
        }

        setFragmentResultListener("add_edit_workouts"){_, bundle ->
            val result = bundle.getInt("add_edit_result")
            viewModel.onAddEditResult(result)
        }

        setFragmentResultListener("delete_workouts"){_, bundle ->
            val result = bundle.getParcelable<WorkoutAndSteps>("delete_result")
            viewModel.onDeleteResult(result!!.workout)
        }

        viewModel.workouts.asLiveData().observe(viewLifecycleOwner){

            lifecycleScope.launch{it.forEach {
                viewModel.updateworkoutDuration(it.workout)
            }}

            workoutsAdapter.submitList(it)
        }

        setHasOptionsMenu(true)


        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.workoutEvent.collect {
                event -> when(event) {
                    is WorkoutViewModel.WorkoutEvent.ShowUndoDeletedMessage -> {
                        Snackbar.make(requireView(), "Workout deleted", Snackbar.LENGTH_LONG)
                            .setAction("UNDO") {
                                viewModel.onUndoDeleteClick(event.workout)
                            }.show()
                    }
                is WorkoutViewModel.WorkoutEvent.NavigateToAddWorkoutPage -> {
                    val action = WorkoutsFragmentDirections.actionWorkoutsFragmentToAddEditWorkoutFragment()
                    findNavController().navigate(action)
                }
                is WorkoutViewModel.WorkoutEvent.NavigateToPlayWorkoutPage -> {
                    val action = WorkoutsFragmentDirections.actionWorkoutsFragmentToPlayWorkoutFragment(event.workout)
                    findNavController().navigate(action)
                }
                is WorkoutViewModel.WorkoutEvent.NavigateToEditWorkoutPage -> {
                    val action = WorkoutsFragmentDirections.actionWorkoutsFragmentToAddEditWorkoutFragment(event.workout)
                    findNavController().navigate(action)
                }
                is WorkoutViewModel.WorkoutEvent.NavigateToTimerPage -> {
                    val action = WorkoutsFragmentDirections.actionWorkoutsFragmentToStopWatchFragment()
                    findNavController().navigate(action)
                }
                is WorkoutViewModel.WorkoutEvent.showWorkoutSavedConfirmation -> {
                    Snackbar.make(requireView(), event.msg, Snackbar.LENGTH_SHORT).show()
                }
                is WorkoutViewModel.WorkoutEvent.ShowWorkoutDeletedConfirmation -> {
                    Snackbar.make(requireView(), "Workout deleted", Snackbar.LENGTH_LONG)
                        .setAction("UNDO") {
                            viewModel.onUndoDeleteClick(event.workout)
                        }.show()
                }
            }.exhaust
            }
        }


    }

    override fun onItemClick(workout: Workout) {
        TODO("Not yet implemented")
    }

    override fun onPlayIconClicked(workout: Workout) {
        viewModel.onPlayWorkoutClicked(workout)
    }
    override fun onEditIconCLicked(workout: Workout) {
        viewModel.onEditWorkoutlick(workout)
    }
   override fun onFavIconClicked(workout: Workout) {
       viewModel.onFavWorkoutClick(workout)
   }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.workouts_task_menu, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView

        searchView.OnQueryTextChanged {
            viewModel.searchQuery.value = it
        }


    }



    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.action_sort_by_name -> {
                viewModel.onSortOrderSelected(sort_Order.BY_NAME)
                true
            }
            R.id.action_sort_by_date_created -> {
                viewModel.onSortOrderSelected(sort_Order.BY_DATE)
                true
            }
            R.id.action_sort_by_fav -> {
                item.isChecked = !item.isChecked
                viewModel.showOnlyFav(item.isChecked)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}