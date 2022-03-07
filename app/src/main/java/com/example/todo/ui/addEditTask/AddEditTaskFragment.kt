package com.example.todo.ui.addEditTask

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.example.todo.R
import com.example.todo.databinding.FragmentAddEditTaskBinding
import com.example.todo.util.exhaustive
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddEditTaskFragment : Fragment(R.layout.fragment_add_edit_task) {

  private val binding by viewBinding(FragmentAddEditTaskBinding::bind)

  private val viewModel: AddTaskViewModel by viewModels()

  @SuppressLint("SetTextI18n")
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.apply {
      editTextTaskName.setText(viewModel.taskName)
      checkboxImportant.isChecked = viewModel.taskImportance
      checkboxImportant.jumpDrawablesToCurrentState()
      textViewCreateData.isVisible = viewModel.task != null
      textViewCreateData.text = "Created: ${viewModel.task?.createDateFormat}"

      fabSaveTask.setOnClickListener {
        viewModel.onSaveClick()
      }

      editTextTaskName.addTextChangedListener {
        viewModel.taskName = it.toString()
      }

      checkboxImportant.setOnCheckedChangeListener { _, isChecked ->
        viewModel.taskImportance = isChecked
      }
    }


    viewLifecycleOwner.lifecycleScope.launchWhenStarted {
      viewModel.getTaskEvent.collect { event ->
        when (event) {
          is AddTaskViewModel.AddEditTaskEvent.NavigateBackWithResult -> {
            binding.editTextTaskName.clearFocus()
            setFragmentResult(
              "add_edit_request",
              bundleOf("add_edit_result" to event.result)
            )
            findNavController().popBackStack()
          }
          is AddTaskViewModel.AddEditTaskEvent.ShowInvalidInputMessage -> {
            Snackbar.make(requireView(), event.msg, Snackbar.LENGTH_LONG).show()
          }
        }.exhaustive
      }
    }
  }

}