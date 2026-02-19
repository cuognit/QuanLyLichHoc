package com.example.quanlylichhoc.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlylichhoc.R
import com.example.quanlylichhoc.database.SubjectItem

class SubjectsAdapter(
    private var subjects: List<SubjectItem>,
    private val onSubjectClick: (SubjectItem) -> Unit
) : RecyclerView.Adapter<SubjectsAdapter.SubjectViewHolder>() {

    class SubjectViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val strip: View = view.findViewById(R.id.view_strip)
        val tvName: TextView = view.findViewById(R.id.tv_subject_name)
        val tvTeacher: TextView = view.findViewById(R.id.tv_teacher)
        val tvRoom: TextView = view.findViewById(R.id.tv_room)
        val tvSchedule: TextView = view.findViewById(R.id.tv_schedule)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubjectViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_subject_card, parent, false)
        return SubjectViewHolder(view)
    }

    override fun onBindViewHolder(holder: SubjectViewHolder, position: Int) {
        val item = subjects[position]

        holder.tvName.text = item.name
        holder.tvTeacher.text = if (item.teacher.isNotEmpty()) item.teacher else "Chưa cập nhật"
        holder.tvRoom.text = if (item.room.isNotEmpty()) item.room else "Chưa cập nhật"
        holder.tvSchedule.text = if (item.schedule.isNotEmpty()) item.schedule else "Chưa có lịch"

        // Set color
        try {
            val color = android.graphics.Color.parseColor(item.color)
            holder.strip.setBackgroundColor(color)
        } catch (e: Exception) {
            holder.strip.setBackgroundColor(android.graphics.Color.GRAY)
        }

        holder.itemView.setOnClickListener { onSubjectClick(item) }
    }

    override fun getItemCount(): Int = subjects.size

    fun updateData(newSubjects: List<SubjectItem>) {
        subjects = newSubjects
        notifyDataSetChanged()
    }
}
