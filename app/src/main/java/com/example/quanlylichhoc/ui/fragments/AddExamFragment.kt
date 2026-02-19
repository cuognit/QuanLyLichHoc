package com.example.quanlylichhoc.ui.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.quanlylichhoc.R
import com.example.quanlylichhoc.databinding.FragmentAddExamBinding

class AddExamFragment : Fragment() {

    private var _binding: FragmentAddExamBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddExamBinding.inflate(inflater, container, false)
        return binding.root
    }

    private lateinit var dbHelper: com.example.quanlylichhoc.database.DatabaseHelper
    private var selectedSubjectId: Long = -1
    private var selectedDate = ""
    private var selectedTime = ""
    private var selectedType = "Cuối kỳ"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbHelper = com.example.quanlylichhoc.database.DatabaseHelper(requireContext())

        setupActions()
        setupChipSelection()
        setupSubjectSelection()
    }

    private fun setupActions() {
        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnSaveExam.setOnClickListener {
            saveExam()
        }
        
        binding.tvDate.setOnClickListener {
            pickDate()
        }
        binding.layoutDate.setOnClickListener {
            pickDate()
        }
        
        binding.tvTime.setOnClickListener {
            pickTime()
        }
        binding.layoutTime.setOnClickListener {
            pickTime()
        }
        
        binding.layoutSubjectSelect.setOnClickListener {
             binding.tvSubjectValue.performClick()
        }
        
        binding.layoutDuration.setOnClickListener {
            focusInput(binding.etDuration)
        }
        
        binding.layoutRoom.setOnClickListener {
            focusInput(binding.etRoom)
        }
        
        binding.layoutSbd.setOnClickListener {
            focusInput(binding.etSbd)
        }
        
        binding.layoutNote.setOnClickListener {
            focusInput(binding.etNote)
        }
    }
    
    private fun pickDate() {
        val c = java.util.Calendar.getInstance()
        android.app.DatePickerDialog(requireContext(), { _, y, m, d ->
            selectedDate = String.format("%02d/%02d/%d", d, m + 1, y)
            binding.tvDate.text = selectedDate
        }, c.get(java.util.Calendar.YEAR), c.get(java.util.Calendar.MONTH), c.get(java.util.Calendar.DAY_OF_MONTH)).show()
    }

    private fun pickTime() {
        val c = java.util.Calendar.getInstance()
        android.app.TimePickerDialog(requireContext(), { _, h, min ->
            selectedTime = String.format("%02d:%02d", h, min)
            binding.tvTime.text = selectedTime
        }, c.get(java.util.Calendar.HOUR_OF_DAY), c.get(java.util.Calendar.MINUTE), true).show()
    }
    
    private fun focusInput(editText: android.widget.EditText) {
        editText.requestFocus()
        val imm = androidx.core.content.ContextCompat.getSystemService(requireContext(), android.view.inputmethod.InputMethodManager::class.java)
        imm?.showSoftInput(editText, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
    }
    
    private fun setupSubjectSelection() {
        val subjectNames = dbHelper.getSubjectNames()
        binding.tvSubjectValue.setOnClickListener {
            val popup = androidx.appcompat.widget.PopupMenu(requireContext(), it)
            subjectNames.forEach { name -> popup.menu.add(name) }
            popup.setOnMenuItemClickListener { item ->
                binding.tvSubjectValue.text = item.title
                selectedSubjectId = dbHelper.getSubjectIdByName(item.title.toString())
                
                // Auto-fill room if needed from subject defaults?
                // For now keep manual or default
                true
            }
            popup.show()
        }
        binding.ivChevron.setOnClickListener { binding.tvSubjectValue.performClick() }
    }

    private fun setupChipSelection() {
        val chips = listOf(binding.chipFinal, binding.chipMidterm, binding.chipTest, binding.chipOral)
        
        for (chip in chips) {
            chip.setOnClickListener {
                // Reset all
                chips.forEach { c -> 
                    c.setBackgroundResource(R.drawable.bg_chip_unselected)
                    c.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_subtitle))
                    c.typeface = android.graphics.Typeface.DEFAULT
                }
                
                // Set selected
                chip.setBackgroundResource(R.drawable.bg_chip_selected_red)
                chip.setTextColor(Color.WHITE)
                chip.typeface = android.graphics.Typeface.DEFAULT_BOLD
                selectedType = chip.text.toString()
            }
        }
    }
    
    private fun saveExam() {
        if (selectedSubjectId == -1L) {
            Toast.makeText(context, "Vui lòng chọn môn thi", Toast.LENGTH_SHORT).show()
            return
        }
        if (selectedDate.isEmpty()) {
             Toast.makeText(context, "Vui lòng chọn ngày thi", Toast.LENGTH_SHORT).show()
            return
        }
         if (selectedTime.isEmpty()) {
             Toast.makeText(context, "Vui lòng chọn giờ thi", Toast.LENGTH_SHORT).show()
            return
        }
        
        val durationStr = binding.etDuration.text.toString()
        val duration = durationStr.toIntOrNull() ?: 90
        val room = binding.etRoom.text.toString()
        val sbd = binding.etSbd.text.toString()
        val note = binding.etNote.text.toString()
        
        dbHelper.insertExam(selectedSubjectId, selectedType, selectedDate, selectedTime, duration, room, sbd, note)
        
        Toast.makeText(context, "Đã lưu lịch thi thành công!", Toast.LENGTH_SHORT).show()
        parentFragmentManager.popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dbHelper.close()
        _binding = null
    }
}
