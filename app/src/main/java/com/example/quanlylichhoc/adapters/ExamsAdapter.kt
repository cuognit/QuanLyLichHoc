package com.example.quanlylichhoc.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlylichhoc.R

sealed class ExamItem {
    data class Upcoming(
        val id: String,
        val title: String,
        val date: String,
        val location: String,
        val sbd: String,
        val daysLeft: Int
    ) : ExamItem()

    data class Completed(
        val id: String,
        val title: String,
        val completedDate: String,
        val score: String
    ) : ExamItem()
}

class ExamsAdapter(
    private val items: List<ExamItem>,
    private val onExamLongClick: (ExamItem) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_UPCOMING = 0
        private const val TYPE_COMPLETED = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is ExamItem.Upcoming -> TYPE_UPCOMING
            is ExamItem.Completed -> TYPE_COMPLETED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_UPCOMING) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_exam_upcoming, parent, false)
            UpcomingViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_exam_completed, parent, false)
            CompletedViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is ExamItem.Upcoming -> (holder as UpcomingViewHolder).bind(item, onExamLongClick)
            is ExamItem.Completed -> (holder as CompletedViewHolder).bind(item, onExamLongClick)
        }
    }

    override fun getItemCount(): Int = items.size

    class UpcomingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvTitle: TextView = view.findViewById(R.id.tv_title)
        private val tvDate: TextView = view.findViewById(R.id.tv_location_time) 
        private val tvDaysLeft: TextView = view.findViewById(R.id.tv_days_left)
        
        fun bind(item: ExamItem.Upcoming, onLongClick: (ExamItem) -> Unit) {
            tvTitle.text = item.title
            tvDate.text = "${item.location} • ${item.date}"
            tvDaysLeft.text = "còn ${item.daysLeft} ngày"
            
            itemView.setOnLongClickListener {
                onLongClick(item)
                true
            }
        }
    }

    class CompletedViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvTitle: TextView = view.findViewById(R.id.tv_title)
        private val tvDate: TextView = view.findViewById(R.id.tv_date)
        private val tvScore: TextView = view.findViewById(R.id.tv_score)

        fun bind(item: ExamItem.Completed, onLongClick: (ExamItem) -> Unit) {
            tvTitle.text = item.title
            tvDate.text = "Hoàn thành ${item.completedDate}"
            tvScore.text = "Điểm: ${item.score}"
            
            itemView.setOnLongClickListener {
                onLongClick(item)
                true
            }
        }
    }
}
