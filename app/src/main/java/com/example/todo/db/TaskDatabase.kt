package com.example.todo.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.todo.data.Task
import com.example.todo.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

const val DB_VERSION = 3
const val DB_NAME = "TODO_DATA"

@Database(entities = [Task::class], version = DB_VERSION)
abstract class TaskDatabase : RoomDatabase() {

  abstract fun taskDao(): TaskDao

  class Callback @Inject constructor(
    private val database: Provider<TaskDatabase>,
    @ApplicationScope private val applicationScope: CoroutineScope
  ) : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
      super.onCreate(db)
      val dao = database.get().taskDao()

    }
  }
}


