package com.example.todo.ui.tasks

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.example.todo.R
import com.example.todo.databinding.FragmentTasksBinding
import com.example.todo.util.onQueryTextChanged
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class TasksFragment : Fragment(R.layout.fragment_tasks) {

  private val viewModel: TasksViewModel by viewModels()
  private val binding by viewBinding(FragmentTasksBinding::bind)
  private val adapterTask by lazy { TasksAdapter() }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.recyclerViewTasks.apply {
      adapter = adapterTask
      setHasFixedSize(true)
    }
    viewModel.tasks.observe(viewLifecycleOwner) {
      adapterTask.submitList(it)
    }

    setHasOptionsMenu(true)
  }

  override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
    inflater.inflate(R.menu.menu_fragment_tasks, menu)
    val searchItem = menu.findItem(R.id.action_search)
    val searchView = searchItem.actionView as SearchView
    searchView.onQueryTextChanged {
      viewModel.searchQuery.value = it
    }
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
      R.id.action_sort_by_name -> {
        viewModel.sortOrder.value = SortOrder.BY_NAME
        true
      }
      R.id.action_sort_date -> {
        viewModel.sortOrder.value = SortOrder.BY_DATE
        true
      }
      R.id.action_hide_completed_task -> {
        item.isChecked = !item.isChecked
        viewModel.hideCompleted.value = item.isChecked
        true
      }
      R.id.action_delete_all_completed_tasks -> {

        true
      }
      else -> super.onOptionsItemSelected(item)
    }
  }
}