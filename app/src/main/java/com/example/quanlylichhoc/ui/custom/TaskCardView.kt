package com.example.quanlylichhoc.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.example.quanlylichhoc.databinding.ItemTaskCardBinding
import com.example.quanlylichhoc.utils.TaskItem

class TaskCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding: ItemTaskCardBinding

    init {
        binding = ItemTaskCardBinding.inflate(LayoutInflater.from(context), this, true)
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
    }

    fun setData(item: TaskItem, onStatusChanged: ((TaskItem, Boolean) -> Unit)? = null) {
        with(binding) {
            tvTaskTitle.text = item.title
            tvTaskDesc.text = item.description.trim()
            tvTaskDesc.isSelected = true // Enable marquee
            
            // Format deadline display
            val deadline = item.deadline
            val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
            val cal = java.util.Calendar.getInstance()
            val today = sdf.format(cal.time)
            cal.add(java.util.Calendar.DAY_OF_YEAR, 1)
            val tomorrow = sdf.format(cal.time)
            
            val displayDeadline = when {
                deadline.startsWith(today) -> deadline.replace(today, "Hôm nay")
                deadline.startsWith(tomorrow) -> deadline.replace(tomorrow, "Ngày mai")
                deadline == "Hôm nay" -> "Hôm nay"
                deadline == "Ngày mai" -> "Ngày mai"
                else -> deadline
            }
            chipPriority.text = displayDeadline
            
            // Set listener to null before setting checked state to avoid unwanted callbacks
            cbComplete.setOnCheckedChangeListener(null)
            cbComplete.isChecked = item.isCompleted
            
            cbComplete.setOnCheckedChangeListener { _, isChecked ->
                onStatusChanged?.invoke(item, isChecked)
            }
        }
    }
}
