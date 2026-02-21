package com.example.quanlylichhoc.adapters

import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlylichhoc.R
import com.example.quanlylichhoc.utils.ExamItem
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class HomeExamAdapter(
    private val exams: List<ExamItem>,
    private val onEditClick: (ExamItem) -> Unit
) : RecyclerView.Adapter<HomeExamAdapter.ExamViewHolder>() {

    class ExamViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvSubject: TextView = view.findViewById(R.id.tv_subject)
        val tvCountdown: TextView = view.findViewById(R.id.tv_countdown)
        val tvExamTag: TextView = view.findViewById(R.id.tv_exam_tag)
        val tvDaysLeft: TextView = view.findViewById(R.id.tv_days_left)
        val btnEdit: View = view.findViewById(R.id.btn_edit_exam)
        var timer: CountDownTimer? = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExamViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_exam_card, parent, false)
        return ExamViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExamViewHolder, position: Int) {
        val exam = exams[position]
        holder.tvSubject.text = exam.subjectName
        val relativeDate = getRelativeDate(exam.date)
        holder.tvExamTag.text = "$relativeDate • ${exam.time}"

        // Set days left badge
        try {
            val sdfDate = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
            val examDateOnly = sdfDate.parse(exam.date)
            val today = java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.HOUR_OF_DAY, 0)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }.time
            
            if (examDateOnly != null) {
                val diff = examDateOnly.time - today.time
                val days = java.util.concurrent.TimeUnit.DAYS.convert(diff, java.util.concurrent.TimeUnit.MILLISECONDS).toInt()
                holder.tvDaysLeft.visibility = View.VISIBLE
                holder.tvDaysLeft.text = if (days == 0) "Hôm nay" else "còn $days ngày"
            }
        } catch (e: Exception) {
            holder.tvDaysLeft.visibility = View.GONE
        }

        holder.btnEdit.setOnClickListener { onEditClick(exam) }

        // Countdown Logic
        holder.timer?.cancel()
        
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        try {
            val examDate = sdf.parse("${exam.date} ${exam.time}")
            val currentTime = Date()

            if (examDate != null && examDate.after(currentTime)) {
                val diffInMillis = examDate.time - currentTime.time
                
                holder.timer = object : CountDownTimer(diffInMillis, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        val days = TimeUnit.MILLISECONDS.toDays(millisUntilFinished)
                        val hours = TimeUnit.MILLISECONDS.toHours(millisUntilFinished) % 24
                        val minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60
                        val seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60

                        holder.tvCountdown.text = when {
                            days > 0 -> "${days}n ${hours}g ${minutes}p"
                            hours > 0 -> "${hours}g ${minutes}p ${seconds}s"
                            else -> "${minutes}p ${seconds}s"
                        }
                    }

                    override fun onFinish() {
                        holder.tvCountdown.text = "Đang diễn ra"
                    }
                }.start()
            } else {
                holder.tvCountdown.text = "Đã kết thúc"
            }
        } catch (e: Exception) {
            holder.tvCountdown.text = "N/A"
        }
    }

    private fun getRelativeDate(dateStr: String): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return try {
            val date = sdf.parse(dateStr) ?: return dateStr
            val today = Calendar.getInstance()
            val target = Calendar.getInstance()
            target.time = date
            
            val isSameDay = today.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                    today.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)
            
            if (isSameDay) return "Hôm nay"
            
            today.add(Calendar.DAY_OF_YEAR, 1)
            val isTomorrow = today.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                    today.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)
            
            if (isTomorrow) "Ngày mai" else dateStr
        } catch (e: Exception) {
            dateStr
        }
    }

    override fun getItemCount() = exams.size

    override fun onViewRecycled(holder: ExamViewHolder) {
        super.onViewRecycled(holder)
        holder.timer?.cancel()
    }
}
