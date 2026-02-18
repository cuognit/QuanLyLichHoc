package com.example.quanlylichhoc.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quanlylichhoc.adapters.CalendarAdapter
import com.example.quanlylichhoc.adapters.CalendarDate
import com.example.quanlylichhoc.adapters.TimelineAdapter
import com.example.quanlylichhoc.databinding.FragmentScheduleBinding
import com.example.quanlylichhoc.utils.MockData

class ScheduleFragment : Fragment() {

    private var _binding: FragmentScheduleBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScheduleBinding.inflate(inflater, container, false)
        return binding.root
    }

    private lateinit var dbHelper: com.example.quanlylichhoc.database.DatabaseHelper


    private val currentCalendar = java.util.Calendar.getInstance()
    private val selectedDate = java.util.Calendar.getInstance()
    private var isCalendarExpanded = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbHelper = com.example.quanlylichhoc.database.DatabaseHelper(requireContext())
        
        // Initial Draw
        updateCalendarData()
        updateTimeline()

        // Navigation Listeners
        binding.btnPrevMonth.setOnClickListener {
            if (isCalendarExpanded) {
                currentCalendar.add(java.util.Calendar.MONTH, -1)
            } else {
                currentCalendar.add(java.util.Calendar.WEEK_OF_YEAR, -1)
            }
            updateCalendarData()
        }

        binding.btnNextMonth.setOnClickListener {
             if (isCalendarExpanded) {
                currentCalendar.add(java.util.Calendar.MONTH, 1)
            } else {
                currentCalendar.add(java.util.Calendar.WEEK_OF_YEAR, 1)
            }
            updateCalendarData()
        }

        binding.tvHeaderMonth.setOnClickListener {
            val datePickerDialog = android.app.DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    currentCalendar.set(java.util.Calendar.YEAR, year)
                    currentCalendar.set(java.util.Calendar.MONTH, month)
                    currentCalendar.set(java.util.Calendar.DAY_OF_MONTH, dayOfMonth)
                    
                    selectedDate.time = currentCalendar.time
                    updateCalendarData()
                    updateTimeline()
                },
                currentCalendar.get(java.util.Calendar.YEAR),
                currentCalendar.get(java.util.Calendar.MONTH),
                currentCalendar.get(java.util.Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }

        binding.imgViewMode.setOnClickListener {
             parentFragmentManager.beginTransaction()
                .replace(com.example.quanlylichhoc.R.id.fragment_container, MySubjectsFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.fabAdd.setOnClickListener {
             parentFragmentManager.beginTransaction()
                .replace(com.example.quanlylichhoc.R.id.fragment_container, AddCourseFragment())
                .addToBackStack(null)
                .commit()
        }

        // Handle Bar Interaction - Drag Logic
        var startY = 0f
        val dragThreshold = 50f 

        binding.containerHandle.setOnTouchListener { v, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    startY = event.rawY
                    true 
                }
                android.view.MotionEvent.ACTION_UP -> {
                    val endY = event.rawY
                    val deltaY = endY - startY

                    if (Math.abs(deltaY) > dragThreshold) {
                        if (deltaY > 0) {
                            // Dragged Down -> Expand
                            if (!isCalendarExpanded) {
                                isCalendarExpanded = true
                                updateCalendarData()
                            }
                        } else {
                            // Dragged Up -> Collapse
                            if (isCalendarExpanded) {
                                isCalendarExpanded = false
                                updateCalendarData()
                            }
                        }
                    } else {
                        v.performClick()
                    }
                    true
                }
                else -> false
            }
        }
        
        binding.containerHandle.setOnClickListener {
            isCalendarExpanded = !isCalendarExpanded
            updateCalendarData()
        }
    }

    private fun updateCalendarData() {
        val displayList = mutableListOf<CalendarDate>()
        val cloneCalendar = currentCalendar.clone() as java.util.Calendar
        val dateFormatTitle = java.text.SimpleDateFormat("MMMM, yyyy", java.util.Locale("vi", "VN"))
        val dateFormatDayOfWeek = java.text.SimpleDateFormat("EE", java.util.Locale("vi", "VN")) 
        val dateFormatFullDate = java.text.SimpleDateFormat("EEEE, d 'tháng' M", java.util.Locale("vi", "VN"))
        
        binding.tvHeaderMonth.text = dateFormatTitle.format(currentCalendar.time)
        binding.tvFullDate.text = dateFormatFullDate.format(selectedDate.time)

        // Handle "Today" / "Tomorrow" Label
        val today = java.util.Calendar.getInstance()
        val tomorrow = java.util.Calendar.getInstance()
        tomorrow.add(java.util.Calendar.DAY_OF_YEAR, 1)

        val isToday = (selectedDate.get(java.util.Calendar.YEAR) == today.get(java.util.Calendar.YEAR) &&
                selectedDate.get(java.util.Calendar.DAY_OF_YEAR) == today.get(java.util.Calendar.DAY_OF_YEAR))

        val isTomorrow = (selectedDate.get(java.util.Calendar.YEAR) == tomorrow.get(java.util.Calendar.YEAR) &&
                selectedDate.get(java.util.Calendar.DAY_OF_YEAR) == tomorrow.get(java.util.Calendar.DAY_OF_YEAR))

        if (isToday) {
            binding.tvTodayStatus.text = "Hôm nay"
            binding.tvTodayStatus.visibility = View.VISIBLE
            binding.tvFullDate.text = dateFormatFullDate.format(selectedDate.time)
            binding.tvFullDate.visibility = View.VISIBLE
        } else if (isTomorrow) {
            binding.tvTodayStatus.text = "Ngày mai"
            binding.tvTodayStatus.visibility = View.VISIBLE
            binding.tvFullDate.text = dateFormatFullDate.format(selectedDate.time)
            binding.tvFullDate.visibility = View.VISIBLE
        } else {
            // Promote Date to Title (Bold)
            binding.tvTodayStatus.text = dateFormatFullDate.format(selectedDate.time)
            binding.tvTodayStatus.visibility = View.VISIBLE
            binding.tvFullDate.visibility = View.GONE
        }

        // Pre-fetch classes for indicator logic
        val allClasses = dbHelper.getAllClasses()
        val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale("vi", "VN"))

        fun hasClassOnDate(checkDate: java.util.Calendar): Boolean {
             val dayOfWeek = checkDate.get(java.util.Calendar.DAY_OF_WEEK)
             val checkTime = checkDate.time
             
             // Reset time for strict date comparison
             val cleanCheckDate = java.util.Calendar.getInstance()
             cleanCheckDate.time = checkTime
             cleanCheckDate.set(java.util.Calendar.HOUR_OF_DAY, 0)
             cleanCheckDate.set(java.util.Calendar.MINUTE, 0)
             cleanCheckDate.set(java.util.Calendar.SECOND, 0)
             cleanCheckDate.set(java.util.Calendar.MILLISECOND, 0)
             val dateToCompare = cleanCheckDate.time

             return allClasses.any { classItem ->
                 if (classItem.dayOfWeek != dayOfWeek) return@any false
                 
                 if (classItem.startDate.isNotEmpty() && classItem.endDate.isNotEmpty()) {
                     try {
                         val start = dateFormat.parse(classItem.startDate)
                         val end = dateFormat.parse(classItem.endDate)
                         return@any (dateToCompare.compareTo(start) >= 0 && dateToCompare.compareTo(end) <= 0)
                     } catch (e: Exception) {
                         return@any true // Assume true on error
                     }
                 }
                 true // No date range, just day of week match
             }
        }

         if (isCalendarExpanded) {
             // Month View
             cloneCalendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
             val maxDays = cloneCalendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
             
             for (i in 1..maxDays) {
                 cloneCalendar.set(java.util.Calendar.DAY_OF_MONTH, i)
                 val dayOfWeek = dateFormatDayOfWeek.format(cloneCalendar.time).uppercase()
                 
                 val isSelected = (cloneCalendar.get(java.util.Calendar.DAY_OF_YEAR) == 
                                   selectedDate.get(java.util.Calendar.DAY_OF_YEAR) &&
                                   cloneCalendar.get(java.util.Calendar.YEAR) == 
                                   selectedDate.get(java.util.Calendar.YEAR))

                 val hasEvent = hasClassOnDate(cloneCalendar)
                 displayList.add(CalendarDate(dayOfWeek, i.toString(), isSelected, hasEvent))
             }
             
             val gridLayoutManager = androidx.recyclerview.widget.GridLayoutManager(context, 7)
             binding.rvCalendar.layoutManager = gridLayoutManager
             binding.rvCalendar.adapter = CalendarAdapter(displayList) { clickedDate ->
                 val newSelected = currentCalendar.clone() as java.util.Calendar
                 newSelected.set(java.util.Calendar.DAY_OF_MONTH, clickedDate.dayOfMonth.toInt())
                 
                 selectedDate.time = newSelected.time
                 updateCalendarData()
                 updateTimeline()
             }
             
         } else {
             // Week View
             val tempCal = currentCalendar.clone() as java.util.Calendar
             while (tempCal.get(java.util.Calendar.DAY_OF_WEEK) != java.util.Calendar.MONDAY) {
                 tempCal.add(java.util.Calendar.DAY_OF_MONTH, -1)
             }
             
             for (i in 0 until 7) {
                 val dayOfWeek = dateFormatDayOfWeek.format(tempCal.time).uppercase()
                 val dayOfMonth = tempCal.get(java.util.Calendar.DAY_OF_MONTH).toString()
                 
                 val isSelected = (tempCal.get(java.util.Calendar.DAY_OF_YEAR) == 
                                   selectedDate.get(java.util.Calendar.DAY_OF_YEAR) &&
                                   tempCal.get(java.util.Calendar.YEAR) == 
                                   selectedDate.get(java.util.Calendar.YEAR))
                 
                 val hasEvent = hasClassOnDate(tempCal)
                 displayList.add(CalendarDate(dayOfWeek, dayOfMonth, isSelected, hasEvent))
                 tempCal.add(java.util.Calendar.DAY_OF_MONTH, 1)
             }
 
             binding.rvCalendar.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
             binding.rvCalendar.adapter = CalendarAdapter(displayList) { clickedDate ->
                 val newSelected = currentCalendar.clone() as java.util.Calendar
                 while (newSelected.get(java.util.Calendar.DAY_OF_WEEK) != java.util.Calendar.MONDAY) {
                     newSelected.add(java.util.Calendar.DAY_OF_MONTH, -1)
                 }
                 
                 for(k in 0 until 7) {
                     if (newSelected.get(java.util.Calendar.DAY_OF_MONTH).toString() == clickedDate.dayOfMonth) {
                         break
                     }
                     newSelected.add(java.util.Calendar.DAY_OF_MONTH, 1)
                 }
                 
                 selectedDate.time = newSelected.time
                 updateCalendarData()
                 updateTimeline()
             }
         }
     }
     
     private fun updateTimeline() {
         val allClasses = dbHelper.getAllClasses()
         val dayOfWeek = selectedDate.get(java.util.Calendar.DAY_OF_WEEK) 
         val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale("vi", "VN"))
         
         val filteredClasses = allClasses.filter { classItem ->
             // 1. Check Day of Week
             if (classItem.dayOfWeek != dayOfWeek) return@filter false
             
             // 2. Check Date Range (if exists)
             if (classItem.startDate.isNotEmpty() && classItem.endDate.isNotEmpty()) {
                 try {
                     val start = dateFormat.parse(classItem.startDate)
                     val end = dateFormat.parse(classItem.endDate)
                     // Reset time part of dates for accurate comparison
                     val current = selectedDate.clone() as java.util.Calendar
                     current.set(java.util.Calendar.HOUR_OF_DAY, 0)
                     current.set(java.util.Calendar.MINUTE, 0)
                     current.set(java.util.Calendar.SECOND, 0)
                     current.set(java.util.Calendar.MILLISECOND, 0)
                     val currentDate = current.time
                     
                     return@filter (currentDate.compareTo(start) >= 0 && currentDate.compareTo(end) <= 0)
                 } catch (e: Exception) {
                     // Parse error, default to true or false? Let's default to showing it to be safe.
                     return@filter true 
                 }
             }
             true
         }
         
         // Update Class Count Badge
         if (filteredClasses.isNotEmpty()) {
             binding.tvClassCount.text = "${filteredClasses.size} lớp học"
             binding.tvClassCount.visibility = View.VISIBLE
         } else {
             binding.tvClassCount.visibility = View.GONE
         }
         
         if (filteredClasses.isEmpty()) {
            binding.rvScheduleTimeline.visibility = View.GONE
            binding.tvEmptyTimeline.visibility = View.VISIBLE
         } else {
            binding.rvScheduleTimeline.visibility = View.VISIBLE
            binding.tvEmptyTimeline.visibility = View.GONE
            val timelineAdapter = TimelineAdapter(filteredClasses) { classItem ->
                val fragment = CourseDetailFragment()
                val bundle = Bundle()
                bundle.putSerializable("classItem", classItem)
                fragment.arguments = bundle

                parentFragmentManager.beginTransaction()
                    .replace(com.example.quanlylichhoc.R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit()
            }
            binding.rvScheduleTimeline.layoutManager = LinearLayoutManager(context)
            binding.rvScheduleTimeline.adapter = timelineAdapter
         }
     }

    override fun onResume() {
        super.onResume()
        updateTimeline()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
