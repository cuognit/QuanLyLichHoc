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

import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExamsFragment : Fragment() {

    private var _binding: FragmentExamsBinding? = null
    private val binding get() = _binding!!

    private lateinit var dbHelper: com.example.quanlylichhoc.database.DatabaseHelper
    private var allExams: List<com.example.quanlylichhoc.utils.ExamItem> = emptyList()
    private var isUpcomingTab = true
    private var countdownTimer: android.os.CountDownTimer? = null

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
        
        val padding8dp = (8 * resources.displayMetrics.density).toInt()

        if (isUpcoming) {
            binding.tabUpcoming.setBackgroundResource(R.drawable.bg_tab_selected)
            binding.tabUpcoming.setTextColor(ContextCompat.getColor(requireContext(), R.color.purple_500))
            binding.tabUpcoming.setPadding(0, padding8dp, 0, padding8dp)
            
            binding.tabCompleted.setBackgroundResource(R.drawable.bg_tab_unselected)
            binding.tabCompleted.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
            binding.tabCompleted.setPadding(0, padding8dp, 0, padding8dp)
        } else {
            binding.tabCompleted.setBackgroundResource(R.drawable.bg_tab_selected)
            binding.tabCompleted.setTextColor(ContextCompat.getColor(requireContext(), R.color.purple_500))
            binding.tabCompleted.setPadding(0, padding8dp, 0, padding8dp)
            
            binding.tabUpcoming.setBackgroundResource(R.drawable.bg_tab_unselected)
            binding.tabUpcoming.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
            binding.tabUpcoming.setPadding(0, padding8dp, 0, padding8dp)
        }
        filterAndDisplayExams()
    }

    private fun loadExams() {
        if (_binding == null || context == null) return
        try {
             allExams = dbHelper.getAllExams()
        } catch (e: Exception) {
            e.printStackTrace()
            return
        }
        filterAndDisplayExams()
    }
    
    private fun filterAndDisplayExams() {
        if (_binding == null) return
        
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("vi", "VN"))
        val sdfFull = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("vi", "VN"))
        val today = Date()
        
        val upcomingItems = ArrayList<Pair<ExamItem.Upcoming, Date>>()
        val completedList = ArrayList<ExamItem.Completed>()
        
        for (exam in allExams) {
            try {
                val examDateOnly = sdf.parse(exam.date)
                val examDateTimeStr = "${exam.date} ${exam.time}"
                val examDateTime = sdfFull.parse(examDateTimeStr) ?: examDateOnly

                if (examDateTime != null) {
                    var isItemUpcoming = false
                    if (examDateTime.after(today)) {
                        isItemUpcoming = true
                    } else if (examDateOnly != null && isSameDay(examDateOnly, today)) {
                         // Check strictly if time is specified
                         // If time is specified (e.g. 09:00) and we are here (meaning it is <= today),
                         // then it is a past exam today. Should be completed.
                         // If time is NOT specified (e.g. --:--), it is effectively "All day". Keep in upcoming.
                         val hasSpecificTime = exam.time.matches(Regex(".*\\d{1,2}:\\d{2}.*"))
                         if (!hasSpecificTime) {
                             isItemUpcoming = true
                         }
                    }

                    if (isItemUpcoming) {
                        val diff = examDateTime.time - today.time
                        val daysLeft = java.util.concurrent.TimeUnit.DAYS.convert(diff, java.util.concurrent.TimeUnit.MILLISECONDS).toInt()
                        
                         val relativeDate = when {
                             isSameDay(examDateOnly!!, today) -> "Hôm nay"
                             isTomorrow(examDateOnly, today) -> "Ngày mai"
                             else -> exam.date
                         }
                         
                         val item = ExamItem.Upcoming(
                             id = exam.id,
                             title = "${exam.subjectName} - ${exam.type}",
                             date = "$relativeDate ${exam.time}",
                             location = exam.room,
                             sbd = exam.sbd,
                             duration = exam.duration,
                             note = exam.note,
                             daysLeft = Math.max(0, daysLeft)
                         )
                         upcomingItems.add(Pair(item, examDateTime))
                    } else {
                        completedList.add(ExamItem.Completed(
                            id = exam.id,
                            title = "${exam.subjectName} - ${exam.type}",
                            completedDate = exam.date,
                            score = "--"
                        ))
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        upcomingItems.sortBy { it.second }
        val upcomingList = upcomingItems.map { it.first }

        // Manually find views that might be missing from binding or need explicit reference
        val tvLabelNearest = binding.root.findViewById<TextView>(R.id.tv_label_nearest)
        val tvLabelUpcoming = binding.root.findViewById<TextView>(R.id.tv_label_upcoming)
        val layoutCountdown = binding.root.findViewById<View>(R.id.layout_countdown)
        val labelCompleted = binding.root.findViewById<TextView>(R.id.label_completed)

        if (isUpcomingTab) {
            labelCompleted?.visibility = View.GONE
            binding.rvExamsCompleted.visibility = View.GONE
            binding.tvEmptyCompleted.visibility = View.GONE
            
            tvLabelNearest?.visibility = View.VISIBLE
            tvLabelUpcoming?.visibility = View.VISIBLE
            
            if (upcomingList.isEmpty()) {
                layoutCountdown?.visibility = View.GONE
                tvLabelNearest?.visibility = View.GONE
                tvLabelUpcoming?.visibility = View.GONE 
                
                binding.rvExamsUpcoming.visibility = View.GONE
                binding.tvEmptyUpcoming.visibility = View.VISIBLE
                
                countdownTimer?.cancel()
            } else {
                binding.tvEmptyUpcoming.visibility = View.GONE
                binding.rvExamsUpcoming.visibility = View.VISIBLE
                
                val nearest = upcomingItems.first()
                val nearestItem = nearest.first
                val nearestDate = nearest.second
                
                if (layoutCountdown != null) {
                    layoutCountdown.visibility = View.VISIBLE
                    
                    layoutCountdown.findViewById<TextView>(R.id.tv_tag_exam_type)?.text = nearestItem.title.split("-").lastOrNull()?.trim() ?: "Thi"
                    layoutCountdown.findViewById<TextView>(R.id.tv_exam_subject)?.text = nearestItem.title.split("-").firstOrNull()?.trim() ?: nearestItem.title
                    
                    val relativeDate = when {
                        isSameDay(nearestDate, today) -> "Hôm nay"
                        isTomorrow(nearestDate, today) -> "Ngày mai"
                        else -> SimpleDateFormat("EEEE, dd/MM", Locale("vi", "VN")).format(nearestDate)
                    }
                    layoutCountdown.findViewById<TextView>(R.id.tv_exam_date)?.text = "$relativeDate • ${SimpleDateFormat("HH:mm", Locale("vi", "VN")).format(nearestDate)}"
                    
                    layoutCountdown.findViewById<TextView>(R.id.tv_room)?.text = nearestItem.location
                    layoutCountdown.findViewById<TextView>(R.id.tv_id)?.text = nearestItem.sbd
                    layoutCountdown.findViewById<TextView>(R.id.tv_duration_large)?.text = "${nearestItem.duration} phút"
                    
                    startCountdown(nearestDate.time)
                }
                
                val adapter = ExamsAdapter(upcomingList) { item -> showOptionsDialog(item) }
                binding.rvExamsUpcoming.layoutManager = LinearLayoutManager(context)
                binding.rvExamsUpcoming.adapter = adapter
            }
        } else {
            tvLabelNearest?.visibility = View.GONE
            layoutCountdown?.visibility = View.GONE
            tvLabelUpcoming?.visibility = View.GONE
            binding.rvExamsUpcoming.visibility = View.GONE
            binding.tvEmptyUpcoming.visibility = View.GONE
            
            countdownTimer?.cancel()
            
            labelCompleted?.visibility = View.VISIBLE
            
            if (completedList.isEmpty()) {
                binding.rvExamsCompleted.visibility = View.GONE
                binding.tvEmptyCompleted.visibility = View.VISIBLE
            } else {
                binding.rvExamsCompleted.visibility = View.VISIBLE
                binding.tvEmptyCompleted.visibility = View.GONE
                val adapter = ExamsAdapter(completedList) { item -> showOptionsDialog(item) }
                binding.rvExamsCompleted.layoutManager = LinearLayoutManager(context)
                binding.rvExamsCompleted.adapter = adapter
            }
        }
    }
    
    private fun startCountdown(targetMillis: Long) {
        countdownTimer?.cancel()
        
        if (_binding == null) return
        val layout = binding.root.findViewById<View>(R.id.layout_countdown) ?: return
        
        // Ensure we don't start a negative countdown that triggers immediate finish loop
        if (targetMillis <= System.currentTimeMillis()) {
             val blockIds = listOf(R.id.block_days, R.id.block_hours, R.id.block_minutes, R.id.block_seconds)
             val labels = listOf("NGÀY", "GIỜ", "PHÚT", "GIÂY")
             blockIds.forEachIndexed { index, id ->
                 layout.findViewById<View>(id)?.apply {
                     findViewById<TextView>(R.id.tv_value)?.text = "00"
                     findViewById<TextView>(R.id.tv_label)?.text = labels[index]
                 }
             }
             return
        }

        countdownTimer = object : android.os.CountDownTimer(targetMillis - System.currentTimeMillis(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                if (_binding == null) {
                    cancel()
                    return
                }
                if (millisUntilFinished <= 0) {
                    onFinish()
                    return
                }
                
                val days = java.util.concurrent.TimeUnit.MILLISECONDS.toDays(millisUntilFinished)
                val hours = java.util.concurrent.TimeUnit.MILLISECONDS.toHours(millisUntilFinished) % 24
                val minutes = java.util.concurrent.TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60
                val seconds = java.util.concurrent.TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60
                
                fun updateBlock(blockId: Int, value: Long, label: String) {
                    val block = layout.findViewById<View>(blockId)
                    block?.findViewById<TextView>(R.id.tv_value)?.text = String.format("%02d", value)
                    block?.findViewById<TextView>(R.id.tv_label)?.text = label
                }
                
                updateBlock(R.id.block_days, days, "NGÀY")
                updateBlock(R.id.block_hours, hours, "GIỜ")
                updateBlock(R.id.block_minutes, minutes, "PHÚT")
                updateBlock(R.id.block_seconds, seconds, "GIÂY")
            }

            override fun onFinish() {
                 if (_binding != null) {
                    loadExams()
                 }
            }
        }.start()
    }
    
    private fun isSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = java.util.Calendar.getInstance()
        val cal2 = java.util.Calendar.getInstance()
        cal1.time = date1
        cal2.time = date2
        return cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR) &&
               cal1.get(java.util.Calendar.DAY_OF_YEAR) == cal2.get(java.util.Calendar.DAY_OF_YEAR)
    }

    private fun isTomorrow(date1: Date, date2: Date): Boolean {
        val cal1 = java.util.Calendar.getInstance()
        val cal2 = java.util.Calendar.getInstance()
        cal1.time = date1
        cal2.time = date2
        cal2.add(java.util.Calendar.DAY_OF_YEAR, 1)
        return cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR) &&
               cal1.get(java.util.Calendar.DAY_OF_YEAR) == cal2.get(java.util.Calendar.DAY_OF_YEAR)
    }
    
    private fun showOptionsDialog(item: ExamItem) {
        val id = when(item) {
            is ExamItem.Upcoming -> item.id
            is ExamItem.Completed -> item.id
        }
        
        val options = arrayOf("Chỉnh sửa", "Xóa")
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Tùy chọn lịch thi")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> { // Edit
                        val fragment = AddExamFragment()
                        val bundle = Bundle()
                        bundle.putString("exam_id", id)
                        fragment.arguments = bundle
                        
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, fragment)
                            .addToBackStack(null)
                            .commit()
                    }
                    1 -> { // Delete
                        showDeleteConfirmation(id)
                    }
                }
            }
            .show()
    }

    private fun showDeleteConfirmation(id: String) {
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
        countdownTimer?.cancel()
        dbHelper.close()
        _binding = null
    }
}
