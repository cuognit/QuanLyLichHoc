package com.example.quanlylichhoc.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlylichhoc.R

data class CalendarDate(
    val dayOfWeek: String, // "T2", "T3"
    val dayOfMonth: String, // "21", "22"
    var isSelected: Boolean = false,
    val hasEvent: Boolean = false
)

class CalendarAdapter(
    private val dates: List<CalendarDate>,
    private val onDateClick: (CalendarDate) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {

    class CalendarViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDayOfWeek: TextView = view.findViewById(R.id.tv_day_of_week)
        val tvDate: TextView = view.findViewById(R.id.tv_date)
        val indicator: View = view.findViewById(R.id.view_indicator)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_calendar_date, parent, false)
        
        // Dynamically set width to fit 7 items exactly in the available space
        var availableWidth = parent.measuredWidth - parent.paddingStart - parent.paddingEnd
        
        // Fallback if parent not measured yet
        if (availableWidth <= 0) {
            val displayMetrics = parent.context.resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels
            // Approximate padding (16dp * 2 = 32dp) based on XML
            val padding = (32 * displayMetrics.density).toInt()
            availableWidth = screenWidth - padding
        }
        
        var itemWidth = availableWidth / 7
        if (itemWidth <= 0) itemWidth = 100 // Safety default

        view.layoutParams.width = itemWidth
        
        return CalendarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        val item = dates[position]
        holder.tvDayOfWeek.text = item.dayOfWeek
        holder.tvDate.text = item.dayOfMonth

        val context = holder.itemView.context
        if (item.isSelected) {
            holder.tvDate.setBackgroundResource(R.drawable.circle_purple)
            holder.tvDate.setTextColor(androidx.core.content.ContextCompat.getColor(context, R.color.white))
            holder.tvDayOfWeek.setTextColor(androidx.core.content.ContextCompat.getColor(context, R.color.purple_700))
        } else {
            holder.tvDate.background = null
            holder.tvDate.setTextColor(androidx.core.content.ContextCompat.getColor(context, R.color.text_title))
            holder.tvDayOfWeek.setTextColor(androidx.core.content.ContextCompat.getColor(context, R.color.text_secondary))
        }
        
        // Show/Hide Indicator
        if (item.hasEvent) {
             holder.indicator.visibility = View.VISIBLE
        } else {
             holder.indicator.visibility = View.GONE
        }

        
        holder.itemView.setOnClickListener {
            onDateClick(item)
        }
    }

    override fun getItemCount(): Int = dates.size
}
