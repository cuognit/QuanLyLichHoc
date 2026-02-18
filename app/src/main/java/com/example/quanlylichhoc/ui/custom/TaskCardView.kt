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

    fun setData(item: TaskItem) {
        with(binding) {
            tvTaskTitle.text = item.title
            tvTaskDesc.text = item.description
            chipPriority.text = item.deadline
            cbComplete.isChecked = item.isCompleted
            
            // Logic: Strike through text if completed?
            // if (item.isCompleted) tvTaskTitle.paintFlags = ...
        }
    }
}
