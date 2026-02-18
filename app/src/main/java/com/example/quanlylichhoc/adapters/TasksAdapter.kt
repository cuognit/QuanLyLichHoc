package com.example.quanlylichhoc.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlylichhoc.R

sealed class TaskListItem {
    data class Header(val title: String) : TaskListItem()
    data class TaskItem(
        val id: String,
        val title: String,
        val time: String,
        val subject: String,
        val priority: String, // "Cao", "Trung bình", "Thấp"
        val isCompleted: Boolean = false
    ) : TaskListItem()
}

class TasksAdapter(
    private val items: List<TaskListItem>,
    private val onTaskLongClick: (TaskListItem.TaskItem) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is TaskListItem.Header -> TYPE_HEADER
            is TaskListItem.TaskItem -> TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task_header, parent, false)
            HeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task_card_new, parent, false)
            TaskViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is TaskListItem.Header -> (holder as HeaderViewHolder).bind(item)
            is TaskListItem.TaskItem -> (holder as TaskViewHolder).bind(item, onTaskLongClick)
        }
    }

    override fun getItemCount(): Int = items.size

    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvTitle: TextView = view.findViewById(R.id.tv_header_title)
        fun bind(header: TaskListItem.Header) {
            tvTitle.text = header.title
        }
    }

    class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvTitle: TextView = view.findViewById(R.id.tv_task_title)
        private val tvTime: TextView = view.findViewById(R.id.tv_time)
        private val tvSubject: TextView = view.findViewById(R.id.tv_subject)
        private val tvPriority: TextView = view.findViewById(R.id.tv_priority)
        
        fun bind(task: TaskListItem.TaskItem, onLongClick: (TaskListItem.TaskItem) -> Unit) {
            tvTitle.text = task.title
            tvTime.text = task.time
            tvSubject.text = task.subject
            
            // Priority Styling
            tvPriority.text = task.priority
            
            // Background color map
            val priorityBg = when (task.priority) {
                "Cao" -> R.drawable.bg_priority_high
                "Trung bình" -> R.drawable.bg_priority_medium
                "Thấp" -> R.drawable.bg_priority_low
                else -> R.drawable.bg_priority_medium
            }
            tvPriority.setBackgroundResource(priorityBg)
            
            val priorityColor = when (task.priority) {
                 "Cao" -> "#C62828"
                 "Trung bình" -> "#F9A825"
                 "Thấp" -> "#2E7D32"
                 else -> "#F9A825"
            }
            tvPriority.setTextColor(android.graphics.Color.parseColor(priorityColor))

            itemView.setOnLongClickListener {
                onLongClick(task)
                true
            }
        }
    }
}
