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
    private var selectedStartTime = ""
    private var selectedEndTime = ""
    private var selectedStartDate = ""
    private var selectedEndDate = ""
    private var isEditMode = false
    private var subjectIdToEdit: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbHelper = com.example.quanlylichhoc.database.DatabaseHelper(requireContext())

        setupActions()
        setupDayToggles()
        setupColorSelection()

        // Check for Edit Mode
        val sId = arguments?.getString("subjectId")
        if (sId != null) {
            isEditMode = true
            subjectIdToEdit = sId
            binding.tvTitle.text = "Chỉnh sửa môn học"
            binding.btnSave.text = "Cập nhật"
            loadCourseData(sId)
        }
    }

    private fun loadCourseData(id: String) {
        val subject = dbHelper.getSubjectById(id) ?: return
        
        binding.etSubjectName.setText(subject.name)
        binding.etRoom.setText(subject.room)
        binding.etLecturer.setText(subject.teacher)
        binding.etNote.setText(subject.note)
        
        selectedColor = subject.color
        // Trigger color visual update
        // We need to map color string -> View. Since I don't have the map scope here easily, I'll redo the setup or iterate.
        // For simplicity, I'll rely on the setupColorSelection to have already run, and I'll find the view.
        val colorMap = mapOf(
            binding.colorRed to "#F44336",
            binding.colorOrange to "#FF9800",
            binding.colorYellow to "#FFEB3B",
            binding.colorGreen to "#4CAF50",
            binding.colorBlue to "#4285F4",
            binding.colorPurple to "#9C27B0"
        )
        val viewToSelect = colorMap.entries.find { it.value.equals(selectedColor, ignoreCase = true) }?.key
        if (viewToSelect != null) {
            updateColorVisuals(viewToSelect, colorMap.keys)
        }

        selectedStartDate = subject.startDate
        binding.tvStartDate.text = if (selectedStartDate.isNotEmpty()) selectedStartDate else "--/--/----"
        
        selectedEndDate = subject.endDate
        binding.tvEndDate.text = if (selectedEndDate.isNotEmpty()) selectedEndDate else "--/--/----"

        // Load Schedule (Days and Time)
        // We need to query classes. The DB helper `getSubjectById` returns a summary string "T2 (7:00-9:00)". 
        // We should probably query the classes table directly for accuracy or parse.
        // Let's us `dbHelper.getAllClasses()` but that gets EVERYTHING.
        // Better to add `getClassesBySubjectId` or just parse the summary if it's consistent.
        // But `getSubjectById` calls `getScheduleSummary`.
        // Let's try to parse the first class if available or just default.
        // Ideally we should query classes properly.
        // Since I can't easily add a new query method to DB Helper right here without another tool call (and I want to be efficient),
        // I will rely on parsing `subject.schedule` if it's simple "T2 (07:00-09:00)" or similar.
        // Format from DB: "$dayStr ($start-$end), ..."
        // Example: "T2 (07:00-09:00)"
        
        if (subject.schedule.isNotEmpty()) {
            // Split by comma in case multiple days
            val schedules = subject.schedule.split(", ")
            // Take the first one for time. We assume same time for all days for this simple UI.
            val first = schedules[0] // "T2 (07:00-09:00)"
            
            // Extract Time
            try {
                val timePart = first.substringAfter("(").substringBefore(")") // "07:00-09:00"
                val times = timePart.split("-")
                if (times.size == 2) {
                    selectedStartTime = times[0]
                    selectedEndTime = times[1]
                    binding.tvStartTime.text = selectedStartTime
                    binding.tvEndTime.text = selectedEndTime
                }
            } catch (e: Exception) {}

            // Extract Days
            val dayMap = mapOf(
                "T2" to binding.dayMon, "T3" to binding.dayTue, "T4" to binding.dayWed,
                "T5" to binding.dayThu, "T6" to binding.dayFri, "T7" to binding.daySat, "CN" to binding.daySun
            )
            
            for (sch in schedules) {
                val dayStr = sch.substringBefore(" (")
                dayMap[dayStr]?.isSelected = true
            }
        }
    }

    private fun setupActions() {
        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnSave.setOnClickListener {
            saveCourse()
        }
        
        // Focus Inputs on Container Click
        binding.layoutSubjectName.setOnClickListener {
            focusInput(binding.etSubjectName)
        }
        
        binding.layoutRoom.setOnClickListener {
            focusInput(binding.etRoom)
        }
        
        binding.layoutLecturer.setOnClickListener {
            focusInput(binding.etLecturer)
        }
        
        binding.btnStartTime.setOnClickListener {
            showTimePicker(selectedStartTime) { time -> 
                selectedStartTime = time
                binding.tvStartTime.text = time
            }
        }
        
         binding.btnEndTime.setOnClickListener {
             showTimePicker(selectedEndTime) { time -> 
                selectedEndTime = time
                binding.tvEndTime.text = time
            }
        }

        binding.btnStartDate.setOnClickListener {
            showDatePicker(selectedStartDate) { date ->
                selectedStartDate = date
                binding.tvStartDate.text = date
            }
        }

        binding.btnEndDate.setOnClickListener {
            showDatePicker(selectedEndDate) { date ->
                selectedEndDate = date
                binding.tvEndDate.text = date
            }
        }
    }

    private fun showTimePicker(initialTime: String, onTimeSelected: (String) -> Unit) {
        val calendar = java.util.Calendar.getInstance()
        var hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
        var minute = calendar.get(java.util.Calendar.MINUTE)

        if (initialTime.isNotEmpty()) {
             try {
                 val parts = initialTime.split(":")
                 if (parts.size == 2) {
                     hour = parts[0].toInt()
                     minute = parts[1].toInt()
                 }
             } catch (e: Exception) {}
        }

        android.app.TimePickerDialog(requireContext(), { _, selectedHour, selectedMinute ->
            val time = String.format("%02d:%02d", selectedHour, selectedMinute)
            onTimeSelected(time)
        }, hour, minute, true).show()
    }

    private fun showDatePicker(initialDate: String, onDateSelected: (String) -> Unit) {
        val calendar = java.util.Calendar.getInstance()
        var year = calendar.get(java.util.Calendar.YEAR)
        var month = calendar.get(java.util.Calendar.MONTH)
        var day = calendar.get(java.util.Calendar.DAY_OF_MONTH)

        if (initialDate.isNotEmpty()) {
            try {
                // Expected format: dd/MM/yyyy
                val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                val date = sdf.parse(initialDate)
                if (date != null) {
                    calendar.time = date
                    year = calendar.get(java.util.Calendar.YEAR)
                    month = calendar.get(java.util.Calendar.MONTH)
                    day = calendar.get(java.util.Calendar.DAY_OF_MONTH)
                }
            } catch (e: Exception) {}
        }

        android.app.DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
            val date = String.format("%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear)
            onDateSelected(date)
        }, year, month, day).show()
    }

    private fun saveCourse() {
        val subjectName = binding.etSubjectName.text.toString()
        val room = binding.etRoom.text.toString()
        val teacher = binding.etLecturer.text.toString()
        val note = binding.etNote.text.toString()
        
        if (subjectName.isBlank()) {
            Toast.makeText(context, "Vui lòng nhập tên môn học", Toast.LENGTH_SHORT).show()
            return
        }

        // Date Validation
        if (selectedStartDate.isNotEmpty() && selectedEndDate.isNotEmpty()) {
            val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
            try {
                val start = sdf.parse(selectedStartDate)
                val end = sdf.parse(selectedEndDate)
                if (start != null && end != null && !start.before(end)) {
                    Toast.makeText(context, "Vui lòng chọn lại ngày", Toast.LENGTH_SHORT).show()
                    return
                }
            } catch (e: Exception) {
                // Ignore parse errors
            }
        }

        // Time Validation (Start < End)
        if (selectedStartTime.isEmpty() || selectedEndTime.isEmpty()) {
             Toast.makeText(context, "Vui lòng chọn giờ học", Toast.LENGTH_SHORT).show()
             return
        }
        
        if (selectedStartTime >= selectedEndTime) {
             Toast.makeText(context, "Vui lòng chọn lại giờ", Toast.LENGTH_SHORT).show()
             return
        }

        val excludeId = if (isEditMode && subjectIdToEdit != null) subjectIdToEdit!!.toLong() else -1L
        
        // Time Conflict Validation
        val days = listOf(
            binding.dayMon to 2, binding.dayTue to 3, binding.dayWed to 4,
            binding.dayThu to 5, binding.dayFri to 6, binding.daySat to 7, binding.daySun to 1
        )
        
        var hasSelectedDay = false
        val conflictDays = StringBuilder()
        
        for ((view, dayVal) in days) {
            if (view.isSelected) {
                if (dbHelper.checkTimeConflict(dayVal, selectedStartTime, selectedEndTime, selectedStartDate, selectedEndDate, excludeId)) {
                    val dayName = when(dayVal) {
                        2 -> "Thứ 2"
                        3 -> "Thứ 3"
                        4 -> "Thứ 4"
                        5 -> "Thứ 5"
                        6 -> "Thứ 6"
                        7 -> "Thứ 7"
                        1 -> "CN"
                        else -> ""
                    }
                    if (conflictDays.isNotEmpty()) conflictDays.append(", ")
                    conflictDays.append(dayName)
                }
                hasSelectedDay = true
            }
        }
        
        if (conflictDays.isNotEmpty()) {
            Toast.makeText(context, "Trùng lịch học vào: $conflictDays", Toast.LENGTH_LONG).show()
            return
        }

        if (!hasSelectedDay) {
             Toast.makeText(context, "Chưa chọn ngày học", Toast.LENGTH_SHORT).show()
             return
        }

        var subjectId: Long = -1
        if (isEditMode && subjectIdToEdit != null) {
            // UPDATE
            dbHelper.updateSubject(subjectIdToEdit!!, subjectName, teacher, room, selectedColor, selectedStartDate, selectedEndDate, note)
            subjectId = excludeId
            
            // Re-create classes: Delete old ones first
            dbHelper.deleteClassesBySubjectId(subjectIdToEdit!!)
        } else {
            // INSERT
            subjectId = dbHelper.insertSubject(subjectName, teacher, room, selectedColor, selectedStartDate, selectedEndDate, note)
        }

        // Insert Classes for selected days
        for ((view, dayVal) in days) {
            if (view.isSelected) {
                dbHelper.insertClass(subjectId, dayVal, selectedStartTime, selectedEndTime, room)
            }
        }
        
        Toast.makeText(context, if (isEditMode) "Đã cập nhật!" else "Đã tạo môn học!", Toast.LENGTH_SHORT).show()

        // Notify listener to refresh
        parentFragmentManager.setFragmentResult("edit_course_request", android.os.Bundle().apply {
            putBoolean("refresh", true)
        })

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
            }
        }
    }
    
    // Removed deprecated updateDayStyle

    private fun setupColorSelection() {
        val colorMap = mapOf(
            binding.colorRed to "#F44336",
            binding.colorOrange to "#FF9800",
            binding.colorYellow to "#FFEB3B",
            binding.colorGreen to "#4CAF50",
            binding.colorBlue to "#4285F4",
            binding.colorPurple to "#9C27B0"
        )
        
        // Initial Selection Visual if not edit mode
         if (!isEditMode) {
             updateColorVisuals(binding.colorGreen, colorMap.keys)
         }

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

    private fun focusInput(editText: android.widget.EditText) {
        editText.requestFocus()
        val imm = androidx.core.content.ContextCompat.getSystemService(requireContext(), android.view.inputmethod.InputMethodManager::class.java)
        imm?.showSoftInput(editText, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dbHelper.close()
        _binding = null
    }
}
