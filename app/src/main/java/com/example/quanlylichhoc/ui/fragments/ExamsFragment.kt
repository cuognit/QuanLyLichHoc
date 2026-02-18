package com.example.quanlylichhoc.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quanlylichhoc.R
import com.example.quanlylichhoc.adapters.ExamItem
import com.example.quanlylichhoc.adapters.ExamsAdapter
import com.example.quanlylichhoc.databinding.FragmentExamsBinding

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExamsFragment : Fragment() {

    private var _binding: FragmentExamsBinding? = null
    private val binding get() = _binding!!

    private lateinit var dbHelper: com.example.quanlylichhoc.database.DatabaseHelper
    private var allExams: List<com.example.quanlylichhoc.utils.ExamItem> = emptyList()
    private var isUpcomingTab = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExamsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbHelper = com.example.quanlylichhoc.database.DatabaseHelper(requireContext())

        setupTabs()
        loadExams()

        binding.fabAddExam.setOnClickListener {
            val fragment = AddExamFragment()
             parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
            
            // Register lifecycle callback to reload on return? 
            // Or simpler: overload onResume. 
            // Since we use replace+backstack, onCreateView might be called again or onResume.
            // onResume is safer.
        }
    }
    
    override fun onResume() {
        super.onResume()
        loadExams()
    }

    private fun setupTabs() {
        binding.tabUpcoming.setOnClickListener {
            updateTabState(isUpcoming = true)
        }
        binding.tabCompleted.setOnClickListener {
            updateTabState(isUpcoming = false)
        }
    }

    private fun updateTabState(isUpcoming: Boolean) {
        isUpcomingTab = isUpcoming
        if (isUpcoming) {
            binding.tabUpcoming.setBackgroundResource(R.drawable.bg_tab_selected)
            binding.tabUpcoming.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue_500))
            
            binding.tabCompleted.background = null
            binding.tabCompleted.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
        } else {
            binding.tabCompleted.setBackgroundResource(R.drawable.bg_tab_selected)
            binding.tabCompleted.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue_500))
            
            binding.tabUpcoming.background = null
            binding.tabUpcoming.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
        }
        filterAndDisplayExams()
    }

    private fun loadExams() {
        allExams = dbHelper.getAllExams()
        filterAndDisplayExams()
    }
    
    private fun filterAndDisplayExams() {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("vi", "VN"))
        val today = Date()
        
        val upcomingList = ArrayList<ExamItem.Upcoming>()
        val completedList = ArrayList<ExamItem.Completed>()
        
        for (exam in allExams) {
            try {
                val examDate = sdf.parse(exam.date)
                if (examDate != null) {
                    if (examDate.after(today) || isSameDay(examDate, today)) {
                        val diff = examDate.time - today.time
                        val daysLeft = java.util.concurrent.TimeUnit.DAYS.convert(diff, java.util.concurrent.TimeUnit.MILLISECONDS).toInt()
                         upcomingList.add(ExamItem.Upcoming(
                             id = exam.id,
                             title = "${exam.subjectName} - ${exam.type}",
                             date = "${exam.date} ${exam.time}",
                             location = exam.room,
                             sbd = exam.sbd,
                             daysLeft = Math.max(0, daysLeft)
                         ))
                    } else {
                        completedList.add(ExamItem.Completed(
                            id = exam.id,
                            title = "${exam.subjectName} - ${exam.type}",
                            completedDate = exam.date,
                            score = "--" // Score not in DB yet
                        ))
                    }
                }
            } catch (e: Exception) {
                // Ignore parsing errors
            }
        }
        
        if (isUpcomingTab) {
            binding.rvExamsCompleted.visibility = View.GONE
            binding.tvEmptyCompleted.visibility = View.GONE
            
             if (upcomingList.isEmpty()) {
                binding.rvExamsUpcoming.visibility = View.GONE
                binding.tvEmptyUpcoming.visibility = View.VISIBLE
            } else {
                binding.rvExamsUpcoming.visibility = View.VISIBLE
                binding.tvEmptyUpcoming.visibility = View.GONE
                val adapter = ExamsAdapter(upcomingList) { item -> showDeleteDialog(item) }
                binding.rvExamsUpcoming.layoutManager = LinearLayoutManager(context)
                binding.rvExamsUpcoming.adapter = adapter
            }
        } else {
            binding.rvExamsUpcoming.visibility = View.GONE
            binding.tvEmptyUpcoming.visibility = View.GONE
            
            if (completedList.isEmpty()) {
                binding.rvExamsCompleted.visibility = View.GONE
                binding.tvEmptyCompleted.visibility = View.VISIBLE
            } else {
                binding.rvExamsCompleted.visibility = View.VISIBLE
                binding.tvEmptyCompleted.visibility = View.GONE
                val adapter = ExamsAdapter(completedList) { item -> showDeleteDialog(item) }
                binding.rvExamsCompleted.layoutManager = LinearLayoutManager(context)
                binding.rvExamsCompleted.adapter = adapter
            }
        }
    }
    
    private fun isSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = java.util.Calendar.getInstance()
        val cal2 = java.util.Calendar.getInstance()
        cal1.time = date1
        cal2.time = date2
        return cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR) &&
               cal1.get(java.util.Calendar.DAY_OF_YEAR) == cal2.get(java.util.Calendar.DAY_OF_YEAR)
    }
    
    private fun showDeleteDialog(item: ExamItem) {
        val id = when(item) {
            is ExamItem.Upcoming -> item.id
            is ExamItem.Completed -> item.id
        }
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Xóa lịch thi?")
            .setMessage("Bạn có chắc muốn xóa lịch thi này không?")
            .setPositiveButton("Xóa") { _, _ ->
                dbHelper.deleteExam(id)
                loadExams()
                android.widget.Toast.makeText(context, "Đã xóa", android.widget.Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dbHelper.close()
        _binding = null
    }
}
