package com.example.quanlylichhoc.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.quanlylichhoc.R
import com.example.quanlylichhoc.databinding.FragmentAddCourseBinding

class AddCourseFragment : Fragment() {

    private var _binding: FragmentAddCourseBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddCourseBinding.inflate(inflater, container, false)
        return binding.root
    }

    private lateinit var dbHelper: com.example.quanlylichhoc.database.DatabaseHelper
    private var selectedColor = "#4CAF50" // Default Green
    private var selectedStartTime = "07:00"
    private var selectedEndTime = "09:00"
    private var selectedStartDate = ""
    private var selectedEndDate = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbHelper = com.example.quanlylichhoc.database.DatabaseHelper(requireContext())

        setupActions()
        setupDayToggles()
        setupColorSelection()
    }

    private fun setupActions() {
        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnSave.setOnClickListener {
            saveCourse()
        }
        
        binding.btnStartTime.setOnClickListener {
            showTimePicker { time -> 
                selectedStartTime = time
                binding.tvStartTime.text = time
            }
        }
        
         binding.btnEndTime.setOnClickListener {
             showTimePicker { time -> 
                selectedEndTime = time
                binding.tvEndTime.text = time
            }
        }

        binding.btnStartDate.setOnClickListener {
            showDatePicker { date ->
                selectedStartDate = date
                binding.tvStartDate.text = date
            }
        }

        binding.btnEndDate.setOnClickListener {
            showDatePicker { date ->
                selectedEndDate = date
                binding.tvEndDate.text = date
            }
        }
    }

    private fun showTimePicker(onTimeSelected: (String) -> Unit) {
        val calendar = java.util.Calendar.getInstance()
        val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
        val minute = calendar.get(java.util.Calendar.MINUTE)

        android.app.TimePickerDialog(requireContext(), { _, selectedHour, selectedMinute ->
            val time = String.format("%02d:%02d", selectedHour, selectedMinute)
            onTimeSelected(time)
        }, hour, minute, true).show()
    }

    private fun showDatePicker(onDateSelected: (String) -> Unit) {
        val calendar = java.util.Calendar.getInstance()
        val year = calendar.get(java.util.Calendar.YEAR)
        val month = calendar.get(java.util.Calendar.MONTH)
        val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)

        android.app.DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
            val date = String.format("%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear)
            onDateSelected(date)
        }, year, month, day).show()
    }

    private fun saveCourse() {
        val subjectName = binding.etSubjectName.text.toString()
        val room = binding.etRoom.text.toString()
        val teacher = binding.etLecturer.text.toString()
        
        if (subjectName.isBlank()) {
            Toast.makeText(context, "Vui lòng nhập tên môn học", Toast.LENGTH_SHORT).show()
            return
        }

        // 1. Insert Subject
        val subjectId = dbHelper.insertSubject(subjectName, teacher, room, selectedColor, selectedStartDate, selectedEndDate)

        // 2. Insert Classes for selected days
        val days = listOf(
            binding.dayMon to 2, binding.dayTue to 3, binding.dayWed to 4,
            binding.dayThu to 5, binding.dayFri to 6, binding.daySat to 7, binding.daySun to 1
        )
        
        var hasSelectedDay = false
        for ((view, dayVal) in days) {
            if (view.isSelected) {
                dbHelper.insertClass(subjectId, dayVal, selectedStartTime, selectedEndTime, room)
                hasSelectedDay = true
            }
        }
        
        if (!hasSelectedDay) {
            // Default to Monday if none selected or handle error? 
            // For now let's just warn or default.
             Toast.makeText(context, "Chưa chọn ngày học, nhưng môn học đã được tạo", Toast.LENGTH_SHORT).show()
        } else {
             Toast.makeText(context, "Đã lưu lịch học thành công!", Toast.LENGTH_SHORT).show()
        }

        parentFragmentManager.popBackStack()
    }

    private fun setupDayToggles() {
        val days = listOf(
            binding.dayMon, binding.dayTue, binding.dayWed,
            binding.dayThu, binding.dayFri, binding.daySat, binding.daySun
        )

        for (dayView in days) {
            dayView.setOnClickListener {
                it.isSelected = !it.isSelected
                // Styling is handled by XML selector (selector_day_circle and selector_day_text)
            }
        }
    }

    private fun updateDayStyle(view: TextView) {
        // Deprecated: Logic moved to XML selectors
    }
    
    private fun setupColorSelection() {
        val colorMap = mapOf(
            binding.colorRed to "#F44336",
            binding.colorOrange to "#FF9800",
            binding.colorYellow to "#FFEB3B",
            binding.colorGreen to "#4CAF50",
            binding.colorBlue to "#4285F4",
            binding.colorPurple to "#9C27B0"
        )
        
        // Initial Selection Visual (Green)
        updateColorVisuals(binding.colorGreen, colorMap.keys)

        for ((view, color) in colorMap) {
            view.setOnClickListener {
                selectedColor = color
                updateColorVisuals(view, colorMap.keys)
            }
        }
    }
    
    private fun updateColorVisuals(selectedView: View, allViews: Set<View>) {
        // Reset all
        for (view in allViews) {
            view.scaleX = 1.0f
            view.scaleY = 1.0f
            view.alpha = 0.5f // Dim unselected
        }
        
        // Highlight selected
        selectedView.scaleX = 1.3f
        selectedView.scaleY = 1.3f
        selectedView.alpha = 1.0f
    }    

    override fun onDestroyView() {
        super.onDestroyView()
        dbHelper.close()
        _binding = null
    }
}
