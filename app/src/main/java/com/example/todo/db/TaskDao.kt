package com.example.todo.db

import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE
import com.example.todo.data.Task
import com.example.todo.util.SortOrder
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

  @Insert(onConflict = REPLACE)
  suspend fun insert(task: Task)

  @Update
  suspend fun update(task: Task)

  @Delete
  suspend fun delete(task: Task)

  @Query("DELETE FROM task_table WHERE completed = 1")
  suspend fun deleteCompleteTasks()

  fun getTasks(query: String, sortOrder: SortOrder, hideCompleted: Boolean): Flow<List<Task>> =
    when (sortOrder) {
      SortOrder.BY_NAME -> getTasksByName(query, hideCompleted)
      SortOrder.BY_DATE -> getTasksByDateCreated(query, hideCompleted)
    }

  @Query("SELECT * FROM task_table WHERE (completed != :hideCompleted OR completed = 0) AND name LIKE '%' || :searchQuery || '%' ORDER BY importance DESC, name")
  fun getTasksByName(searchQuery: String, hideCompleted: Boolean): Flow<List<Task>>

  @Query("SELECT * FROM task_table WHERE (completed != :hideCompleted OR completed = 0) AND name LIKE '%' || :searchQuery || '%' ORDER BY importance DESC, created")
  fun getTasksByDateCreated(searchQuery: String, hideCompleted: Boolean): Flow<List<Task>>

}