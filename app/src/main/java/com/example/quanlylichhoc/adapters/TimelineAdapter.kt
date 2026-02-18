package com.example.quanlylichhoc.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlylichhoc.R
import com.example.quanlylichhoc.utils.ClassItem

class TimelineAdapter(
    private val classes: List<ClassItem>,
    private val onItemClick: (ClassItem) -> Unit
) : RecyclerView.Adapter<TimelineAdapter.TimelineViewHolder>() {

    class TimelineViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvStartTime: TextView = view.findViewById(R.id.tv_start_time)
        val tvEndTime: TextView = view.findViewById(R.id.tv_end_time)
        val tvSubject: TextView = view.findViewById(R.id.tv_subject_name)
        val tvRoom: TextView = view.findViewById(R.id.tv_room)
        val tvTeacher: TextView = view.findViewById(R.id.tv_teacher)
        val tvStatus: TextView = view.findViewById(R.id.tv_status_tag)
        val cardClass: CardView = view.findViewById(R.id.card_class)
        val viewAccent: View = view.findViewById(R.id.view_accent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimelineViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_timeline_class, parent, false)
        return TimelineViewHolder(view)
    }

    override fun onBindViewHolder(holder: TimelineViewHolder, position: Int) {
        val item = classes[position]
        
        holder.tvStartTime.text = item.startTime
        holder.tvEndTime.text = item.endTime
        holder.tvSubject.text = item.subjectName
        holder.tvRoom.text = item.room
        holder.tvTeacher.text = item.teacher

        // Dynamic Styling based on position or data (Mock logic)
        val context = holder.itemView.context
        
        // Cycle colors: Purple, Blue, Green, Orange
        // Apply User Selected Color
        try {
            val colorInt = android.graphics.Color.parseColor(item.color)
            
            // Generate Pastel Background (Blend with White 85%)
            val r = android.graphics.Color.red(colorInt)
            val g = android.graphics.Color.green(colorInt)
            val b = android.graphics.Color.blue(colorInt)
            
            val pastelR = (r * 0.15 + 255 * 0.85).toInt()
            val pastelG = (g * 0.15 + 255 * 0.85).toInt()
            val pastelB = (b * 0.15 + 255 * 0.85).toInt()
            val pastelColor = android.graphics.Color.rgb(pastelR, pastelG, pastelB)

            holder.cardClass.setCardBackgroundColor(pastelColor)
            holder.viewAccent.setBackgroundColor(colorInt)
            
            // Vibrant colors on White (Pastel) are usually readable.
            holder.tvSubject.setTextColor(colorInt)
            holder.tvRoom.setTextColor(colorInt)
            holder.tvTeacher.setTextColor(colorInt)
            
            holder.itemView.findViewById<ImageView>(R.id.ic_location).setColorFilter(colorInt)
            holder.itemView.findViewById<ImageView>(R.id.ic_teacher).setColorFilter(colorInt)

            holder.tvStatus.visibility = View.GONE
            
        } catch (e: Exception) {
             // Fallback
             when (position % 4) {
                0 -> {
                    holder.cardClass.setCardBackgroundColor(context.resources.getColor(R.color.pastel_purple_bg))
                    holder.viewAccent.setBackgroundColor(context.resources.getColor(R.color.purple_500))
                    holder.tvSubject.setTextColor(context.resources.getColor(R.color.purple_700))
                    holder.tvRoom.setTextColor(context.resources.getColor(R.color.purple_700))
                    holder.tvTeacher.setTextColor(context.resources.getColor(R.color.purple_700))
                    holder.itemView.findViewById<ImageView>(R.id.ic_location).setColorFilter(context.resources.getColor(R.color.purple_700))
                    holder.itemView.findViewById<ImageView>(R.id.ic_teacher).setColorFilter(context.resources.getColor(R.color.purple_700))
                }
                1 -> {
                    holder.cardClass.setCardBackgroundColor(context.resources.getColor(R.color.pastel_blue_bg))
                    holder.viewAccent.setBackgroundColor(context.resources.getColor(R.color.pastel_blue_text))
                    val blueText = context.resources.getColor(R.color.pastel_blue_text)
                    holder.tvSubject.setTextColor(blueText)
                    holder.tvRoom.setTextColor(blueText)
                    holder.tvTeacher.setTextColor(blueText)
                    holder.itemView.findViewById<ImageView>(R.id.ic_location).setColorFilter(blueText)
                    holder.itemView.findViewById<ImageView>(R.id.ic_teacher).setColorFilter(blueText)
                }
                2 -> {
                    holder.cardClass.setCardBackgroundColor(context.resources.getColor(R.color.pastel_green_bg))
                    holder.viewAccent.setBackgroundColor(context.resources.getColor(R.color.pastel_green_text))
                    val greenText = context.resources.getColor(R.color.pastel_green_text)
                    holder.tvSubject.setTextColor(greenText)
                    holder.tvRoom.setTextColor(greenText)
                    holder.tvTeacher.setTextColor(greenText)
                    holder.itemView.findViewById<ImageView>(R.id.ic_location).setColorFilter(greenText)
                    holder.itemView.findViewById<ImageView>(R.id.ic_teacher).setColorFilter(greenText)
                }
                3 -> {
                    holder.cardClass.setCardBackgroundColor(context.resources.getColor(R.color.pastel_orange_bg))
                    holder.viewAccent.setBackgroundColor(context.resources.getColor(R.color.pastel_orange_text))
                    val orangeText = context.resources.getColor(R.color.pastel_orange_text)
                    holder.tvSubject.setTextColor(orangeText)
                    holder.tvRoom.setTextColor(orangeText)
                    holder.tvTeacher.setTextColor(orangeText)
                    holder.itemView.findViewById<ImageView>(R.id.ic_location).setColorFilter(orangeText)
                    holder.itemView.findViewById<ImageView>(R.id.ic_teacher).setColorFilter(orangeText)
                }
            }
        }
        
        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount(): Int = classes.size
}
