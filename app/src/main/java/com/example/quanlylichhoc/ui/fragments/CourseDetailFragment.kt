package com.example.quanlylichhoc.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.quanlylichhoc.databinding.FragmentCourseDetailBinding

class CourseDetailFragment : Fragment() {

    private var _binding: FragmentCourseDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        parentFragmentManager.setFragmentResultListener("edit_course_request", this) { _, bundle ->
            if (bundle.getBoolean("refresh")) {
                refreshData()
            }
        }
        parentFragmentManager.setFragmentResultListener("task_updated", this) { _, bundle ->
            if (bundle.getBoolean("refresh")) {
                refreshData()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCourseDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    private lateinit var dbHelper: com.example.quanlylichhoc.database.DatabaseHelper
    private var currentSubjectId: String? = null

    companion object {
        fun newInstance(subjectId: String): CourseDetailFragment {
            val fragment = CourseDetailFragment()
            val args = Bundle()
            args.putString("subjectId", subjectId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbHelper = com.example.quanlylichhoc.database.DatabaseHelper(requireContext())

        // Setup actions
        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnMore.setOnClickListener {
            Toast.makeText(context, "More options", Toast.LENGTH_SHORT).show()
        }
        
        val classItem = arguments?.getSerializable("classItem") as? com.example.quanlylichhoc.utils.ClassItem
        val subjectId = arguments?.getString("subjectId")
        val persistedId = arguments?.getString("persistedSubjectId")

        // Resolve currentSubjectId
        if (persistedId != null) {
             currentSubjectId = persistedId
        } else if (subjectId != null) {
            currentSubjectId = subjectId
        } else if (classItem != null) {
             // Try to get ID by Class ID first (more robust)
             var sId = dbHelper.getSubjectIdByClassId(classItem.id)
             if (sId == -1L) {
                 // Fallback to name if class ID failing (e.g. data weirdness) or if class deleted? 
                 sId = dbHelper.getSubjectIdByName(classItem.subjectName)
             }
             if (sId != -1L) {
                 currentSubjectId = sId.toString()
             }
        }

        // Persist the ID if found
        if (currentSubjectId != null) {
            arguments?.putString("persistedSubjectId", currentSubjectId)
        }

        binding.btnEditCourse.setOnClickListener {
             if (currentSubjectId != null) {
                 val fragment = AddCourseFragment()
                 val args = Bundle()
                 args.putString("subjectId", currentSubjectId)
                 fragment.arguments = args
                 parentFragmentManager.beginTransaction()
                     .replace(android.R.id.content, fragment)
                     .addToBackStack(null)
                     .commit()
             } else {
                 Toast.makeText(context, "Không tìm thấy ID môn học", Toast.LENGTH_SHORT).show()
             }
        }
        
        binding.btnDeleteCourse.setOnClickListener {
            if (currentSubjectId != null) {
                 androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Xóa môn học")
                    .setMessage("Bạn có chắc chắn muốn xóa môn học này không? Tất cả lịch học, nhiệm vụ và bài thi liên quan sẽ bị xóa.")
                    .setPositiveButton("Xóa") { _, _ ->
                        dbHelper.deleteSubject(currentSubjectId!!)
                        Toast.makeText(context, "Đã xóa môn học", Toast.LENGTH_SHORT).show()
                        
                        // Notify listener to refresh parent (e.g. subject list)
                        parentFragmentManager.setFragmentResult("edit_course_request", android.os.Bundle().apply {
                            putBoolean("refresh", true)
                        })
                        parentFragmentManager.popBackStack()
                    }
                    .setNegativeButton("Hủy", null)
                    .show()
            } else if (classItem != null) {
                // Fallback for isolated class deletion if subject ID is missing
                dbHelper.deleteClass(classItem.id)
                Toast.makeText(context, "Đã xóa lịch học", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            }
        }

        binding.btnAddMaterial.setOnClickListener {
            showAddMaterialOptionDialog()
        }
        
        // Populate Data - Only use classItem basic info if we haven't loaded fresh data yet
         if (classItem != null && currentSubjectId == null) {
            binding.tvCourseTitle.text = classItem.subjectName
            binding.tvTime.text = "${classItem.startTime} - ${classItem.endTime}"
            binding.tvLocation.text = classItem.room
            binding.tvLecturer.text = classItem.teacher
         }
        
        refreshData()
    }

    override fun onResume() {
        super.onResume()
        refreshData()
    }

    private fun refreshData() {
        if (currentSubjectId == null) return

        val subject = dbHelper.getSubjectById(currentSubjectId!!)
        if (subject != null) {
            binding.tvCourseTitle.text = subject.name
            binding.tvLecturer.text = subject.teacher
            binding.tvLocation.text = subject.room
            binding.tvTime.text = subject.schedule
            
            // Note: If classItem was provided, existing code used classItem values for Title/Time/Location.
            // But they might be stale.
            // We should prefer the FRESH subject data.
            // The only thing we lose is specific class time if the subject has multiple classes.
            // But subject.schedule shows all times.
            
            loadRelatedTasks(subject.id, subject.name)
            setupNoteSection(subject)
            
            // Set colored background for title
            try {
                val color = android.graphics.Color.parseColor(subject.color)
                val lightColor = androidx.core.graphics.ColorUtils.setAlphaComponent(color, 50) // 20% alpha
                
                val drawable = android.graphics.drawable.GradientDrawable()
                drawable.shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                drawable.cornerRadius = 16f * resources.displayMetrics.density // 16dp radius
                drawable.setColor(lightColor)
                
                binding.tvCourseTitle.background = drawable
                binding.tvCourseTitle.setPadding(
                    (12 * resources.displayMetrics.density).toInt(),
                    (8 * resources.displayMetrics.density).toInt(),
                    (12 * resources.displayMetrics.density).toInt(),
                    (8 * resources.displayMetrics.density).toInt()
                )
                binding.tvCourseTitle.setTextColor(color) // Match text color to card color for harmony
                
            } catch (e: Exception) {
                // Fallback or ignore
            }
        }
    }

    private fun loadRelatedTasks(subjectId: String, subjectName: String) {
        val tasks = dbHelper.getTasksBySubjectId(subjectId)
        
        if (tasks.isEmpty()) {
            binding.recyclerRelatedTasks.visibility = View.GONE
            binding.tvNoTasks.visibility = View.VISIBLE
        } else {
            binding.recyclerRelatedTasks.visibility = View.VISIBLE
            binding.tvNoTasks.visibility = View.GONE
            
            // Map util.TaskItem to adapter.TaskListItem
            val adapterItems = tasks.map { task ->
                com.example.quanlylichhoc.adapters.TaskListItem.TaskItem(
                    id = task.id,
                    title = task.title,
                    time = formatTaskDeadline(task.deadline),
                    subject = subjectName,
                    priority = task.priority,
                    description = task.description,
                    isCompleted = task.isCompleted,
                    isOverdue = checkIsOverdue(task.deadline) && !task.isCompleted
                )
            }
            val adapter = com.example.quanlylichhoc.adapters.TasksAdapter(
                    adapterItems,
                    onTaskLongClick = { item -> 
                        // Optional: Allow deleting/editing from here too? For now, maybe just a toast or empty
                        // Ideally we should allow similar options
                         showTaskOptions(item, subjectId, subjectName)
                    },
                    onTaskStatusChange = { item ->
                        toggleTaskStatus(item, subjectId, subjectName)
                    }
                )
            binding.recyclerRelatedTasks.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
            binding.recyclerRelatedTasks.adapter = adapter
        }
    }

    private fun toggleTaskStatus(item: com.example.quanlylichhoc.adapters.TaskListItem.TaskItem, subjectId: String, subjectName: String) {
        val newStatus = !item.isCompleted
        dbHelper.updateTaskStatus(item.id, newStatus)
        loadRelatedTasks(subjectId, subjectName) // Refresh
        val msg = if (newStatus) "Đã hoàn thành!" else "Đã đánh dấu chưa xong"
        android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
    }

    private fun showTaskOptions(item: com.example.quanlylichhoc.adapters.TaskListItem.TaskItem, subjectId: String, subjectName: String) {
        val options = arrayOf("Chỉnh sửa", "Xóa", if (item.isCompleted) "Đánh dấu chưa xong" else "Đánh dấu đã xong")
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(item.title)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> { // Edit
                        val dialog = AddTaskFragment()
                        val bundle = Bundle()
                        bundle.putString("taskId", item.id)
                        bundle.putString("taskTitle", item.title)
                        
                        val task = dbHelper.getTaskById(item.id)
                        if (task != null) {
                            bundle.putString("taskDesc", task.description)
                            bundle.putString("taskDeadline", task.deadline)
                            bundle.putString("taskPriority", task.priority)
                            bundle.putLong("taskSubjectId", task.subjectId)
                        }
                        
                        dialog.arguments = bundle
                        dialog.show(parentFragmentManager, "AddTaskFragment")
                        
                        parentFragmentManager.registerFragmentLifecycleCallbacks(object : androidx.fragment.app.FragmentManager.FragmentLifecycleCallbacks() {
                            override fun onFragmentDestroyed(fm: androidx.fragment.app.FragmentManager, f: Fragment) {
                                super.onFragmentDestroyed(fm, f)
                                if (f == dialog) {
                                    loadRelatedTasks(subjectId, subjectName) 
                                    parentFragmentManager.unregisterFragmentLifecycleCallbacks(this)
                                }
                            }
                        }, false)
                    }
                    1 -> { // Delete
                        androidx.appcompat.app.AlertDialog.Builder(requireContext())
                            .setTitle("Xóa nhiệm vụ?")
                            .setMessage("Bạn có chắc muốn xóa nhiệm vụ này không?")
                            .setPositiveButton("Xóa") { _, _ ->
                                dbHelper.deleteTask(item.id)
                                loadRelatedTasks(subjectId, subjectName)
                                Toast.makeText(context, "Đã xóa", Toast.LENGTH_SHORT).show()
                            }
                            .setNegativeButton("Hủy", null)
                            .show()
                    }
                    2 -> { // Toggle Complete
                        toggleTaskStatus(item, subjectId, subjectName)
                    }
                }
            }
            .show()
    }

    private fun showAddMaterialOptionDialog() {
        val options = arrayOf("Tải lên tệp", "Thêm liên kết", "Chụp ảnh")
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Thêm tài liệu mới")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> Toast.makeText(context, "Tính năng tải lên tệp đang phát triển", Toast.LENGTH_SHORT).show()
                    1 -> Toast.makeText(context, "Tính năng thêm liên kết đang phát triển", Toast.LENGTH_SHORT).show()
                    2 -> Toast.makeText(context, "Tính năng chụp ảnh đang phát triển", Toast.LENGTH_SHORT).show()
                }
            }
            .show()
    }
    
    private fun setupNoteSection(subject: com.example.quanlylichhoc.database.SubjectItem) {
        binding.tvNoteContent.text = if (subject.note.isNotEmpty()) subject.note else "Chưa có ghi chú"
        
        val editNoteListener = View.OnClickListener {
            showEditNoteDialog(subject)
        }
        
        binding.tvNoteContent.setOnClickListener(editNoteListener)
    }

    private fun showEditNoteDialog(subject: com.example.quanlylichhoc.database.SubjectItem) {
        val input = android.widget.EditText(context)
        val currentNote = binding.tvNoteContent.text.toString()
        input.setText(if (currentNote == "Chưa có ghi chú") "" else currentNote)
        input.hint = "Nhập ghi chú..."
        
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Ghi chú môn học")
            .setView(input)
            .setPositiveButton("Lưu") { _, _ ->
                val newNote = input.text.toString()
                dbHelper.updateSubjectNote(subject.id, newNote)
                binding.tvNoteContent.text = if (newNote.isNotEmpty()) newNote else "Chưa có ghi chú"
                Toast.makeText(context, "Đã lưu ghi chú", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun checkIsOverdue(deadline: String): Boolean {
        if (deadline.isEmpty() || deadline == "Không có hạn") return false
        
        try {
            val now = java.util.Calendar.getInstance()
            
            var dateStr = deadline
            if (dateStr.startsWith("Hôm nay")) {
                 val sdfDate = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale("vi", "VN"))
                 dateStr = dateStr.replace("Hôm nay", sdfDate.format(now.time))
            }

            val parts = dateStr.split(" ")
            val datePart = parts[0]
            val timePart = if (parts.size > 1) parts[1] else ""
            
            val sdfFull = if (timePart.isNotEmpty()) {
                java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale("vi", "VN"))
            } else {
                java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale("vi", "VN"))
            }
            
            val deadlineDate = sdfFull.parse(if (timePart.isNotEmpty()) "$datePart $timePart" else datePart) ?: return false
            
            if (timePart.isEmpty()) {
                 val deadlineCal = java.util.Calendar.getInstance()
                 deadlineCal.time = deadlineDate
                 return deadlineDate.before(removeTime(now.time))
            }

            return deadlineDate.before(now.time)
        } catch (e: Exception) {
            return false
        }
    }

    private fun removeTime(date: java.util.Date): java.util.Date {
        val cal = java.util.Calendar.getInstance()
        cal.time = date
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.time
    }

    private fun formatTaskDeadline(deadline: String): String {
        if (deadline.isEmpty()) return "Không có hạn"
        if (deadline == "Hôm nay") return "Hôm nay"
        
        try {
            val parts = deadline.split(" ")
            val datePart = parts[0]
            val timePart = if (parts.size > 1) parts[1] else ""
            
            val sdfDate = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale("vi", "VN"))
            val dateObj = sdfDate.parse(datePart) ?: return deadline
            
            val today = java.util.Calendar.getInstance()
            val target = java.util.Calendar.getInstance()
            target.time = dateObj
            
            val isSameYear = today.get(java.util.Calendar.YEAR) == target.get(java.util.Calendar.YEAR)
            val isSameDay = isSameYear && today.get(java.util.Calendar.DAY_OF_YEAR) == target.get(java.util.Calendar.DAY_OF_YEAR)
            val isTomorrow = isSameYear && today.get(java.util.Calendar.DAY_OF_YEAR) + 1 == target.get(java.util.Calendar.DAY_OF_YEAR)
            
            var displayDate = when {
                isSameDay -> "Hôm nay"
                isTomorrow -> "Ngày mai"
                else -> datePart
            }
            
            if (timePart.isNotEmpty()) {
                displayDate += ", $timePart"
            }
            
            return displayDate
        } catch (e: Exception) {
            return deadline
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dbHelper.close()
        _binding = null
    }
}
