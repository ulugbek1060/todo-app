package com.example.todo.ui.tasks

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.todo.data.Task
import com.example.todo.databinding.ItemTaskBinding

class TasksAdapter(val listener: OnItemClickListener) :
  ListAdapter<Task, TasksAdapter.Vh>(DiffUtilCallback) {

  inner class Vh(private val binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root) {

    init {
      binding.apply {
        root.setOnClickListener {
          val position = absoluteAdapterPosition
          if (position != RecyclerView.NO_POSITION) {
            val task = getItem(position)
            listener.onItemClick(task)
          }
        }
        checkboxTask.setOnClickListener {
          val position = absoluteAdapterPosition
          if (position != RecyclerView.NO_POSITION) {
            val task = getItem(position)
            listener.onTaskCheckBoxClick(task, checkboxTask.isChecked)
          }
        }
      }
    }

    fun bind(task: Task) {
      binding.apply {
        checkboxTask.isChecked = task.completed
        textViewTask.text = task.name
        textViewTask.paint.isStrikeThruText = task.completed
        labelPriority.isVisible = task.importance
      }
    }
  }

  interface OnItemClickListener {
    fun onItemClick(task: Task)
    fun onTaskCheckBoxClick(task: Task, isChecked: Boolean)
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = Vh(
    ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
  )

  override fun onBindViewHolder(holder: Vh, position: Int) {
    holder.bind(getItem(position))
  }

  companion object DiffUtilCallback : DiffUtil.ItemCallback<Task>() {
    override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
      return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
      return oldItem == newItem
    }
  }
}