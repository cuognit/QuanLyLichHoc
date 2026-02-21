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

            // Apply Theme Gradient to card header
            imgSubject.setImageResource(com.example.quanlylichhoc.R.drawable.bg_gradient_theme)
            
            // Resolve tag colors from theme
            val typedValue = android.util.TypedValue()
            context.theme.resolveAttribute(com.example.quanlylichhoc.R.attr.colorTagText, typedValue, true)
            tvTag.setTextColor(typedValue.data)
            
            context.theme.resolveAttribute(com.example.quanlylichhoc.R.attr.colorTagBg, typedValue, true)
            tvTag.backgroundTintList = android.content.res.ColorStateList.valueOf(typedValue.data)

            // Ongoing logic
            if (item.isToday) {
                try {
                    val sdf = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                    val now = java.util.Calendar.getInstance()
                    val currentTime = sdf.parse(String.format("%02d:%02d", now.get(java.util.Calendar.HOUR_OF_DAY), now.get(java.util.Calendar.MINUTE)))
                    val start = sdf.parse(item.startTime)
                    val end = sdf.parse(item.endTime)
                    
                    if (currentTime != null && start != null && end != null && 
                        currentTime.compareTo(start) >= 0 && currentTime.compareTo(end) <= 0) {
                        tvOngoingBadge.visibility = android.view.View.VISIBLE
                    } else {
                        tvOngoingBadge.visibility = android.view.View.GONE
                    }
                } catch (e: Exception) {
                    tvOngoingBadge.visibility = android.view.View.GONE
                }
            } else {
                tvOngoingBadge.visibility = android.view.View.GONE
            }
        }
    }
}
