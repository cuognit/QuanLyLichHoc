package com.example.quanlylichhoc.ui.fragments

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.example.quanlylichhoc.R
import com.example.quanlylichhoc.databinding.DialogAddTaskBinding

class AddTaskFragment : DialogFragment() {

    private var _binding: DialogAddTaskBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddTaskBinding.inflate(inflater, container, false)
        
        // Remove default dialog background to show our rounded corners
        if (dialog != null && dialog?.window != null) {
            dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        }
        
        return binding.root
    }

    private lateinit var dbHelper: com.example.quanlylichhoc.database.DatabaseHelper
    private var selectedPriority = "Trung bình"
    private var selectedDate = ""
    private var selectedTime = ""
    private var selectedSubjectId: Long = -1
    private var taskId: String? = null // If not null, we are editing

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbHelper = com.example.quanlylichhoc.database.DatabaseHelper(requireContext())
        
        // Adjust dialog width
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        setupActions()
        setupPriorityToggle()
        setupSubjectSpinner()
        setupDateTimePickers()
        
        // Check for edit mode
        if (arguments?.containsKey("taskId") == true) {
            taskId = arguments?.getString("taskId")
            binding.tvTitle.text = "Chỉnh sửa nhiệm vụ"
            binding.btnSaveTask.text = "Cập nhật"
            loadTaskData(taskId!!)
        }
    }

    private fun setupActions() {
        binding.btnClose.setOnClickListener { dismiss() }
        binding.btnCancel.setOnClickListener { dismiss() }

        binding.btnSaveTask.setOnClickListener {
            val taskName = binding.etTaskName.text.toString().trim()
            val taskDesc = binding.etTaskDesc.text.toString().trim()
            
            if (taskName.isEmpty()) {
                Toast.makeText(context, "Vui lòng nhập tên nhiệm vụ", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            var deadline = selectedDate
            if (deadline.isEmpty()) deadline = "Hôm nay" // Default
            
            if (selectedTime.isNotEmpty()) {
                deadline += " $selectedTime"
            }
            
            if (taskId == null) {
                // Add
                dbHelper.insertTask(selectedSubjectId, taskName, taskDesc, selectedPriority, deadline, false)
                Toast.makeText(context, "Đã thêm nhiệm vụ: $taskName", Toast.LENGTH_SHORT).show()
            } else {
                // Update - keep existing completion status (not passed here but updateTask doesn't touch it so fine)
                // Wait, updateTask signature? Let's check DatabaseHelper.
                // It is: updateTask(id, title, desc, priority, deadline)
                // It does NOT update subject_id currently. We should probably update subject_id too if changed.
                // Let's assume user might change subject.
                // I need to update updateTask in DB helper to accept subjectId or check if it does.
                // Checking DB helper... updateTask signature is (id, title, desc, priority, deadline).
                // It misses subjectId. I should add it or create a new method.
                // For now, let's stick to existing and maybe add subject update if critical.
                // The user asked to "edit task information", implying everything.
                // I will add updateTask with subjectId.
                dbHelper.updateTask(taskId!!, taskName, taskDesc, selectedPriority, deadline, selectedSubjectId)
                Toast.makeText(context, "Đã cập nhật nhiệm vụ", Toast.LENGTH_SHORT).show()
            }

            // Notify listener to refresh
            parentFragmentManager.setFragmentResult("task_updated", android.os.Bundle().apply {
                putBoolean("refresh", true)
            })
            
            dismiss()
        }
    }
    
    private fun setupSubjectSpinner() {
        val subjectNames = ArrayList<String>()
        subjectNames.add("Khác")
        subjectNames.addAll(dbHelper.getSubjectNames())
        
        // Initial setup for default or edit mode
        if (selectedSubjectId == -1L) {
            binding.spinnerSubject.text = "Khác"
        }

        binding.spinnerSubject.setOnClickListener {
            val popup = androidx.appcompat.widget.PopupMenu(requireContext(), it)
            subjectNames.forEach { name -> popup.menu.add(name) }
            popup.setOnMenuItemClickListener { item ->
                val name = item.title.toString()
                binding.spinnerSubject.text = name
                selectedSubjectId = if (name == "Khác") -1L else dbHelper.getSubjectIdByName(name)
                true
            }
            popup.show()
        }
    }

    private fun setupDateTimePickers() {
        binding.layoutDatetime.setOnClickListener {
             // Simplify: Just click layout to pick date then time? Or separate?
             // The XML has separated click areas inside linear layout but IDs are on TextViews inside.
             // Let's hook up the layout children if possible or just the parent for now for simplicity
        }
        
        binding.tvDeadlineDate.setOnClickListener {
            val c = java.util.Calendar.getInstance()
            android.app.DatePickerDialog(requireContext(), { _, y, m, d ->
                selectedDate = String.format("%02d/%02d/%d", d, m + 1, y)
                binding.tvDeadlineDate.text = selectedDate
            }, c.get(java.util.Calendar.YEAR), c.get(java.util.Calendar.MONTH), c.get(java.util.Calendar.DAY_OF_MONTH)).show()
        }
        
        binding.tvDeadlineTime.setOnClickListener {
            val c = java.util.Calendar.getInstance()
            android.app.TimePickerDialog(requireContext(), { _, h, min ->
                selectedTime = String.format("%02d:%02d", h, min)
                binding.tvDeadlineTime.text = selectedTime
            }, c.get(java.util.Calendar.HOUR_OF_DAY), c.get(java.util.Calendar.MINUTE), true).show()
        }
    }

    private fun setupPriorityToggle() {
        val priorities = listOf(binding.btnPriorityLow, binding.btnPriorityMedium, binding.btnPriorityHigh)
        
        for (btn in priorities) {
            btn.setOnClickListener {
                // Reset all
                priorities.forEach { p -> 
                    p.isSelected = false
                    p.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
                }
                
                // Set selected
                it.isSelected = true
                (it as TextView).setTextColor(ContextCompat.getColor(requireContext(), R.color.purple_500)) 
                selectedPriority = it.text.toString()
            }
        }
        
        // Set default (Medium) or based on data
        if (taskId == null) binding.btnPriorityMedium.performClick()
    }
    
    private fun loadTaskData(id: String) {
        // In a real app we would getTaskById. For now, we assume passed data or query DB.
        // Since I didn't verify getTaskById, I'll rely on what I have or Implement it.
        // Actually I don't have getTaskById in Helper. 
        // For MVP, I might skip pre-loading details from DB and rely on Arguments passed if simpler,
        // or just accept blank fields for "Edit" implies "Overwrite fields you touch".
        // Better: Query DB.
        // Let's implement a quick getTaskById or finding it from getAllTasks (bubbles items).
        // For efficiency, I won't query all.
        // I will just set title based on bundle args for now if available, otherwise just ID.
        // To do it right: 
        if (arguments?.containsKey("taskTitle") == true) binding.etTaskName.setText(arguments?.getString("taskTitle"))
        if (arguments?.containsKey("taskDesc") == true) binding.etTaskDesc.setText(arguments?.getString("taskDesc"))
        
        // Priority
        val priority = arguments?.getString("taskPriority")
        if (priority != null) {
            val btn = when(priority) {
                "Cao" -> binding.btnPriorityHigh
                "Thấp" -> binding.btnPriorityLow
                else -> binding.btnPriorityMedium
            }
            btn.performClick()
        }
        
        // Deadline
        val deadline = arguments?.getString("taskDeadline") ?: ""
        if (deadline.isNotEmpty() && deadline != "Không có hạn") {
            try {
                 // Format might be "dd/MM/yyyy" or "dd/MM/yyyy HH:mm"
                 val parts = deadline.split(" ")
                 selectedDate = parts[0]
                 binding.tvDeadlineDate.text = selectedDate
                 
                 if (parts.size > 1) {
                     selectedTime = parts[1]
                     binding.tvDeadlineTime.text = selectedTime
                 }
            } catch (e: Exception) {}
        }
        
        // Subject
        val sId = arguments?.getLong("taskSubjectId", -1) ?: -1
        if (sId != -1L) {
            selectedSubjectId = sId
            val subject = dbHelper.getSubjectById(sId.toString())
            if (subject != null) {
                binding.spinnerSubject.text = subject.name
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dbHelper.close()
        _binding = null
    }
}
