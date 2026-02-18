package com.example.quanlylichhoc.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quanlylichhoc.adapters.ClassAdapter
import com.example.quanlylichhoc.adapters.TaskAdapter
import com.example.quanlylichhoc.databinding.FragmentHomeBinding
import com.example.quanlylichhoc.utils.MockData

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    private lateinit var dbHelper: com.example.quanlylichhoc.database.DatabaseHelper

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbHelper = com.example.quanlylichhoc.database.DatabaseHelper(requireContext())

        setupHomeData()
    }
    
    override fun onResume() {
        super.onResume()
        setupHomeData()
    }
    
    private fun setupHomeData() {
        // Setup Schedule Today (Horizontal)
        // Logic for "Today" needs to filter getAllClasses based on current day
        // ClassItem has isToday boolean which is calculated in getAllClasses based on current day
        // reusing that logic
        val allClasses = dbHelper.getAllClasses()
        val todayClasses = allClasses.filter { it.isToday }
        
        if (todayClasses.isEmpty()) {
            binding.rvScheduleToday.visibility = View.GONE
            binding.tvEmptySchedule.visibility = View.VISIBLE
        } else {
            binding.rvScheduleToday.visibility = View.VISIBLE
            binding.tvEmptySchedule.visibility = View.GONE
            val classAdapter = ClassAdapter(todayClasses)
            binding.rvScheduleToday.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            binding.rvScheduleToday.adapter = classAdapter
        }

        // Setup Exam Countdown
        val exams = dbHelper.getAllExams()
        if (exams.isEmpty()) {
            binding.layoutExamSection.visibility = View.GONE
        } else {
            binding.layoutExamSection.visibility = View.VISIBLE
            // Bind nearest exam logic here if needed
             val nearest = exams.minByOrNull { it.date } // Simple logic
             if (nearest != null) {
                 binding.cardExamCountdown.tvSubject.text = nearest.subjectName
                 // binding.cardExamCountdown.tvExamTime.text = "${nearest.date} • ${nearest.time}" // specific view not in card
                 binding.cardExamCountdown.tvCountdown.text = "Sắp thi" // logic for days left
                 binding.cardExamCountdown.tvExamTag.text = "${nearest.date} ${nearest.time}"
             }
        }
        
        val dateFormat = java.text.SimpleDateFormat("EEEE, d MMMM", java.util.Locale("vi", "VN"))
        val currentDate = java.util.Date()
        binding.tvDate.text = dateFormat.format(currentDate)
        binding.tvGreeting.text = "Chào buổi sáng, Thi"

        // Setup Urgent Tasks
        val allTasks = dbHelper.getAllTasks()
        val urgentTasks = allTasks.filter { it.priority == "Cao" && !it.isCompleted } // Filter High priority and not done
        
        binding.tvTaskCount.text = urgentTasks.size.toString()
        
        if (urgentTasks.isEmpty()) {
            binding.rvUrgentTasks.visibility = View.GONE
            binding.tvEmptyTasks.visibility = View.VISIBLE
            binding.tvTaskCount.visibility = View.GONE
        } else {
            binding.rvUrgentTasks.visibility = View.VISIBLE
            binding.tvEmptyTasks.visibility = View.GONE
            binding.tvTaskCount.visibility = View.VISIBLE
            val taskAdapter = TaskAdapter(urgentTasks)
            binding.rvUrgentTasks.layoutManager = LinearLayoutManager(context)
            binding.rvUrgentTasks.adapter = taskAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dbHelper.close()
        _binding = null
    }
}
