package com.example.quanlylichhoc.utils

import java.util.Calendar

data class ClassItem(
    val id: String,
    val subjectName: String,
    val room: String,
    val teacher: String,
    val startTime: String,
    val endTime: String,
    val dayOfWeek: Int, // Added for filtering
    val isToday: Boolean = false,
    val startDate: String = "",
    val endDate: String = "",
    val color: String = "" // Added for user-selected color
) : java.io.Serializable

data class TaskItem(
    val id: String,
    val title: String,
    val description: String,
    val priority: String, // "Cao", "Trung bình", "Thấp"
    val isCompleted: Boolean = false,
    val deadline: String = "Hôm nay",
    val subjectId: Long = -1 // Added for DB mapping
)

data class ExamItem(
    val id: String,
    val subjectName: String,
    val date: String,
    val time: String,
    val type: String,
    val room: String,
    val sbd: String
)

object MockData {
    fun getClasses(): List<ClassItem> {
        return emptyList()
    }

    fun getTasks(): List<TaskItem> {
        return emptyList()
    }

    fun getExams(): List<ExamItem> {
        return emptyList()
    }
}
