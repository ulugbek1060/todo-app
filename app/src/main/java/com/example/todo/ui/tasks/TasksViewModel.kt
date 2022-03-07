package com.example.todo.ui.tasks

import androidx.lifecycle.*
import com.example.todo.data.Task
import com.example.todo.db.TaskDao
import com.example.todo.ui.ADD_TASK_RESULT_OK
import com.example.todo.ui.EDIT_TASK_RESULT_OK
import com.example.todo.util.PreferencesManager
import com.example.todo.util.SortOrder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TasksViewModel @Inject constructor(
  private val taskDao: TaskDao,
  private val preferencesManager: PreferencesManager,
  state: SavedStateHandle
) : ViewModel() {

  val searchQuery = state.getLiveData("searchQuery", "")

  private val preferencesFlow = preferencesManager.preferencesFlow

  private val taskEventChanel = Channel<TaskEvent>()
  val taskChannel = taskEventChanel.receiveAsFlow()

  @ExperimentalCoroutinesApi
  private val taskFlow = combine(
    searchQuery.asFlow(),
    preferencesFlow
  ) { query, filterPreferences ->
    Pair(query, filterPreferences)
  }.flatMapLatest { (query, filterPreferences) ->
    taskDao.getTasks(query, filterPreferences.sortOrder, filterPreferences.hideCompleted)
  }

  @ExperimentalCoroutinesApi
  val tasks = taskFlow.asLiveData()

  fun onSortOrderClick(sortOrder: SortOrder) = viewModelScope.launch {
    preferencesManager.updateSortOrder(sortOrder)
  }

  fun onHideCompleteClick(hideCompleted: Boolean) = viewModelScope.launch {
    preferencesManager.updateHideCompleted(hideCompleted)
  }

  suspend fun getHideCompletedFirst(): Boolean {
    return preferencesFlow.first().hideCompleted
  }

  fun onTaskCheckBoxSelected(task: Task, isChecked: Boolean) = viewModelScope.launch {
    taskDao.update(task.copy(completed = isChecked))
  }

  fun onTaskSwiped(task: Task) = viewModelScope.launch {
    taskDao.delete(task)
    taskEventChanel.send(TaskEvent.ShowUndoDeleteTaskManager(task))
  }

  fun onUndoDeleteClick(task: Task) = viewModelScope.launch {
    taskDao.insert(task)
  }

  fun onAddNewTaskClick() = viewModelScope.launch {
    taskEventChanel.send(TaskEvent.NavigateToAddTaskScreen)
  }

  fun onTaskSelected(task: Task) = viewModelScope.launch {
    taskEventChanel.send(TaskEvent.NavigateToEditTaskScreen(task))
  }

  fun onAddTaskEditResult(result: Int) {
    when (result) {
      ADD_TASK_RESULT_OK -> ShowTaskSavedConfirmationMessage("Task added")
      EDIT_TASK_RESULT_OK -> ShowTaskSavedConfirmationMessage("Task Edited")
    }
  }

  private fun ShowTaskSavedConfirmationMessage(msg: String) = viewModelScope.launch {
    taskEventChanel.send(TaskEvent.ShowTaskConfirmationMessage(msg))
  }

  fun onDeleteAllCompletedTaskClick() = viewModelScope.launch {
    taskEventChanel.send(TaskEvent.NavigateToDeleteAllCompleteTasks)
  }

  sealed class TaskEvent {
    object NavigateToAddTaskScreen : TaskEvent()
    data class NavigateToEditTaskScreen(val task: Task) : TaskEvent()
    data class ShowUndoDeleteTaskManager(val task: Task) : TaskEvent()
    data class ShowTaskConfirmationMessage(val msg: String) : TaskEvent()
    object NavigateToDeleteAllCompleteTasks : TaskEvent()
  }

}
