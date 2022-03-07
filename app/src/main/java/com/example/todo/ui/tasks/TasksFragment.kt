package com.example.todo.ui.tasks

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.example.todo.R
import com.example.todo.data.Task
import com.example.todo.databinding.FragmentTasksBinding
import com.example.todo.util.SortOrder
import com.example.todo.util.exhaustive
import com.example.todo.util.onQueryTextChanged
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class TasksFragment : Fragment(R.layout.fragment_tasks), TasksAdapter.OnItemClickListener {

  private val viewModel: TasksViewModel by viewModels()
  private val binding by viewBinding(FragmentTasksBinding::bind)
  private val adapterTask by lazy { TasksAdapter(this) }
  private lateinit var searchView: SearchView

  @SuppressLint("ShowToast")
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.recyclerViewTasks.apply {
      adapter = adapterTask
      setHasFixedSize(true)
    }

    ItemTouchHelper(object :
      ItemTouchHelper.SimpleCallback(
        0,
        ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
      ) {
      override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
      ): Boolean {
        return false
      }

      override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val task = adapterTask.currentList[viewHolder.absoluteAdapterPosition]
        viewModel.onTaskSwiped(task)
      }

    }).attachToRecyclerView(binding.recyclerViewTasks)

    setFragmentResultListener("add_edit_request") { _, bundle ->
      val result = bundle.getInt("add_edit_result")
      viewModel.onAddTaskEditResult(result)
    }

    viewModel.tasks.observe(viewLifecycleOwner) {
      adapterTask.submitList(it)
    }

    viewLifecycleOwner.lifecycleScope.launchWhenStarted {
      viewModel.taskChannel.collect { event ->
        when (event) {
          is TasksViewModel.TaskEvent.ShowUndoDeleteTaskManager -> {
            Snackbar.make(requireView(), "Task Deleted", Snackbar.LENGTH_LONG)
              .setAction("UNDO") {
                viewModel.onUndoDeleteClick(event.task)
              }.show()
          }
          is TasksViewModel.TaskEvent.NavigateToAddTaskScreen -> {
            val action =
              TasksFragmentDirections.actionTasksFragmentToAddEditTaskFragment(title = "New Task")
            findNavController().navigate(action)
          }
          is TasksViewModel.TaskEvent.NavigateToEditTaskScreen -> {
            val action =
              TasksFragmentDirections.actionTasksFragmentToAddEditTaskFragment(
                task = event.task,
                title = "Edit Task"
              )
            findNavController().navigate(action)
          }
          is TasksViewModel.TaskEvent.ShowTaskConfirmationMessage -> {
            Snackbar.make(requireView(), event.msg, Snackbar.LENGTH_SHORT).show()
          }
          TasksViewModel.TaskEvent.NavigateToDeleteAllCompleteTasks -> {
            val action = TasksFragmentDirections.actionGlobalDeleteAllCompletedTasksFragment()
            findNavController().navigate(action)
          }
        }.exhaustive
      }
    }

    binding.fabAddTask.setOnClickListener {
      viewModel.onAddNewTaskClick()
    }

    setHasOptionsMenu(true)
  }

  override fun onItemClick(task: Task) {
    viewModel.onTaskSelected(task)
  }

  override fun onTaskCheckBoxClick(task: Task, isChecked: Boolean) {
    viewModel.onTaskCheckBoxSelected(task, isChecked)
  }

  override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
    inflater.inflate(R.menu.menu_fragment_tasks, menu)
    val searchItem = menu.findItem(R.id.action_search)
    searchView = searchItem.actionView as SearchView

    val pendingQuery = viewModel.searchQuery.value
    if (pendingQuery != null && pendingQuery.isNotEmpty()) {
      searchItem.expandActionView()
      searchView.setQuery(pendingQuery, false)
    }

    searchView.onQueryTextChanged {
      viewModel.searchQuery.value = it
    }

    viewLifecycleOwner.lifecycleScope.launch {
      menu.findItem(R.id.action_hide_completed_task).isChecked = viewModel.getHideCompletedFirst()
    }
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
      R.id.action_sort_by_name -> {
        viewModel.onSortOrderClick(SortOrder.BY_NAME)
        true
      }
      R.id.action_sort_date -> {
        viewModel.onSortOrderClick(SortOrder.BY_DATE)
        true
      }
      R.id.action_hide_completed_task -> {
        item.isChecked = !item.isChecked
        viewModel.onHideCompleteClick(item.isChecked)
        true
      }
      R.id.action_delete_all_completed_tasks -> {
        viewModel.onDeleteAllCompletedTaskClick()
        true
      }
      else -> super.onOptionsItemSelected(item)
    }
  }
}