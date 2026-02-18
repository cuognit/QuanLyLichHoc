package com.example.quanlylichhoc.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlylichhoc.ui.custom.ScheduleCardView
import com.example.quanlylichhoc.utils.ClassItem

class ClassAdapter(private val classes: List<ClassItem>) : RecyclerView.Adapter<ClassAdapter.ClassViewHolder>() {

    class ClassViewHolder(val view: ScheduleCardView) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClassViewHolder {
        val view = ScheduleCardView(parent.context)
        return ClassViewHolder(view)
    }

    override fun onBindViewHolder(holder: ClassViewHolder, position: Int) {
        val item = classes[position]
        holder.view.setData(item)
    }

    override fun getItemCount(): Int = classes.size
}
