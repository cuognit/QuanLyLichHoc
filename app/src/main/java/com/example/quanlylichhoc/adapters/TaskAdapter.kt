package com.example.quanlylichhoc.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlylichhoc.ui.custom.TaskCardView
import com.example.quanlylichhoc.utils.TaskItem

class TaskAdapter(
    private val tasks: List<TaskItem>,
    private val onTaskStatusChanged: (TaskItem, Boolean) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    class TaskViewHolder(val view: TaskCardView) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = TaskCardView(parent.context)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val item = tasks[position]
        holder.view.setData(item, onTaskStatusChanged)
    }

    override fun getItemCount(): Int = tasks.size
}
