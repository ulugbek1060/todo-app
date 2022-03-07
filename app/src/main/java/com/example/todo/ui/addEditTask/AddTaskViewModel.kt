package com.example.todo.ui.addEditTask

import androidx.hilt.Assisted
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todo.data.Task
import com.example.todo.db.TaskDao
import com.example.todo.ui.ADD_TASK_RESULT_OK
import com.example.todo.ui.EDIT_TASK_RESULT_OK
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddTaskViewModel @Inject constructor(
  private val taskDao: TaskDao,
  @Assisted private val state: SavedStateHandle
) : ViewModel() {

  val task = state.get<Task>("task")

  private val addEditTaskEventChanel = Channel<AddEditTaskEvent>()

  val getTaskEvent = addEditTaskEventChanel.receiveAsFlow()

  var taskName = state.get<String>("taskName") ?: task?.name ?: ""
    set(value) {
      field = value
      state["taskName"] = value
    }

  var taskImportance = state.get<Boolean>("taskImportance") ?: task?.importance ?: false
    set(value) {
      field = value
      state["taskImportance"] = value
    }

  fun onSaveClick() {
    if (taskName.isBlank()) {
      showInvalidInputMessage("Name cannot be empty")
      return
    }

    if (task != null) {
      val updateTask = task.copy(name = taskName, importance = taskImportance)
      updateTask(updateTask)
    } else {
      val newTask = Task(name = taskName, importance = taskImportance)
      createTask(newTask)
    }
  }

  private fun createTask(newTask: Task) = viewModelScope.launch {
    taskDao.insert(newTask)
    addEditTaskEventChanel.send(AddEditTaskEvent.NavigateBackWithResult(ADD_TASK_RESULT_OK))
  }

  private fun updateTask(updateTask: Task) = viewModelScope.launch {
    taskDao.update(updateTask)
    addEditTaskEventChanel.send(AddEditTaskEvent.NavigateBackWithResult(EDIT_TASK_RESULT_OK))
  }

  private fun showInvalidInputMessage(text: String) = viewModelScope.launch {
    addEditTaskEventChanel.send(AddEditTaskEvent.ShowInvalidInputMessage(text))
  }

  sealed class AddEditTaskEvent {
    data class ShowInvalidInputMessage(val msg: String) : AddEditTaskEvent()
    data class NavigateBackWithResult(val result: Int) : AddEditTaskEvent()
  }
}