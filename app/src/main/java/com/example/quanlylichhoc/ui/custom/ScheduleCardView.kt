package com.example.quanlylichhoc.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.example.quanlylichhoc.databinding.ItemClassCardBinding
import com.example.quanlylichhoc.utils.ClassItem

class ScheduleCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding: ItemClassCardBinding

    init {
        binding = ItemClassCardBinding.inflate(LayoutInflater.from(context), this, true)
        // Ensure the root CardView in XML matches parent width if needed, or handled by RecyclerView
        val params = MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        val marginHorizontal = (4 * resources.displayMetrics.density).toInt()
        val marginVertical = (8 * resources.displayMetrics.density).toInt()
        params.setMargins(marginHorizontal, marginVertical, marginHorizontal, marginVertical)
        layoutParams = params
    }

    fun setData(item: ClassItem) {
        with(binding) {
            tvSubjectName.text = item.subjectName
            tvTime.text = "${item.startTime} - ${item.endTime}"
            tvRoom.text = item.room
            
            // Logic for Tag
            if (item.subjectName.contains("Thực hành") || item.teacher.equals("Thực hành", ignoreCase = true)) {
                tvTag.text = "Thực hành"
                // Optional: Change tag color
            } else {
                tvTag.text = "Lý thuyết"
            }

            // Logic for Image (Can be expanded later)
            imgSubject.setImageResource(com.example.quanlylichhoc.R.drawable.bg_subject_math) 
        }
    }
}
