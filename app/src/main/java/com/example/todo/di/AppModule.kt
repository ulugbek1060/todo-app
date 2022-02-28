package com.example.todo.di

import android.app.Application
import androidx.room.Room
import com.example.todo.db.DB_NAME
import com.example.todo.db.TaskDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

  @Provides
  @Singleton
  fun provideTaskDatabase(
    app: Application,
    callback: TaskDatabase.Callback
  ): TaskDatabase {
    return Room.databaseBuilder(app, TaskDatabase::class.java, DB_NAME)
      .fallbackToDestructiveMigration()
      .addCallback(callback)
      .build()
  }

  @Provides
  @Singleton
  fun provideTaskDao(
    db: TaskDatabase
  ) = db.taskDao()

  @ApplicationScope
  @Provides
  @Singleton
  fun provideApplicationScope() = CoroutineScope(SupervisorJob())

}

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope