package com.example.quanlylichhoc.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quanlylichhoc.adapters.ClassAdapter
import com.example.quanlylichhoc.adapters.TaskAdapter
import com.example.quanlylichhoc.R
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

        // Setup Exam Countdown (Horizontal List)
        val exams = dbHelper.getAllExams()
        // Filter for upcoming exams (optional, user said "all", but "countdown" implies upcoming)
        val upcomingExams = exams.filter { 
            try {
                val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
                val examDate = sdf.parse("${it.date} ${it.time}")
                examDate != null && examDate.after(java.util.Date())
            } catch (e: Exception) { false }
        }.sortedBy { it.date + " " + it.time }

        if (upcomingExams.isEmpty()) {
            binding.layoutExamSection.visibility = View.GONE
        } else {
            binding.layoutExamSection.visibility = View.VISIBLE
            val examAdapter = com.example.quanlylichhoc.adapters.HomeExamAdapter(upcomingExams) { exam ->
                val fragment = AddExamFragment()
                val bundle = Bundle()
                bundle.putString("exam_id", exam.id)
                fragment.arguments = bundle
                
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit()
            }
            binding.rvExams.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            binding.rvExams.adapter = examAdapter
        }
        
        val prefs = requireActivity().getSharedPreferences("AppPrefs", android.content.Context.MODE_PRIVATE)
        val userName = prefs.getString("user_name", "Bạn")
        
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        val greetingPrefix = when (hour) {
            in 0..11 -> "Chào buổi sáng"
            in 12..17 -> "Chào buổi chiều"
            else -> "Chào buổi tối"
        }
        
        binding.tvGreeting.text = "$greetingPrefix, $userName"

        val dateFormat = java.text.SimpleDateFormat("EEEE, d MMMM", java.util.Locale("vi", "VN"))
        val currentDate = java.util.Date()
        binding.tvDate.text = dateFormat.format(currentDate)


        // Setup Urgent Tasks
        val allTasks = dbHelper.getAllTasks()
        
        val sdfDate = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        val cal = java.util.Calendar.getInstance()
        val todayStr = sdfDate.format(cal.time)
        cal.add(java.util.Calendar.DAY_OF_YEAR, 1)
        val tomorrowStr = sdfDate.format(cal.time)

        val urgentTasks = allTasks.filter { 
            val isNotDone = !it.isCompleted
            
            val isToday = it.deadline.startsWith(todayStr) || it.deadline == "Hôm nay"
            val isTomorrow = it.deadline.startsWith(tomorrowStr) || it.deadline == "Ngày mai"
            
            isNotDone && (isToday || isTomorrow)
        }.sortedBy { 
            // Sort logic: "Hôm nay" < "Ngày mai", then by time if present
            val prefix = if (it.deadline.contains("Hôm nay") || it.deadline.startsWith(todayStr)) "0" else "1"
            val timePart = if (it.deadline.contains(":")) {
                it.deadline.split(" ").last()
            } else "00:00"
            prefix + timePart
        }
        
        binding.tvTaskCount.text = urgentTasks.size.toString()
        
        if (urgentTasks.isEmpty()) {
            binding.rvUrgentTasks.visibility = View.GONE
            binding.tvEmptyTasks.visibility = View.VISIBLE
            binding.tvTaskCount.visibility = View.GONE
        } else {
            binding.rvUrgentTasks.visibility = View.VISIBLE
            binding.tvEmptyTasks.visibility = View.GONE
            binding.tvTaskCount.visibility = View.VISIBLE
            val taskAdapter = TaskAdapter(urgentTasks) { task, isCompleted ->
                dbHelper.updateTaskStatus(task.id, isCompleted)
                setupHomeData()
            }
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
