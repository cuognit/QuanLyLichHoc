

package com.example.quanlylichhoc.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quanlylichhoc.R
import com.example.quanlylichhoc.adapters.TaskListItem
import com.example.quanlylichhoc.adapters.TasksAdapter
import com.example.quanlylichhoc.databinding.FragmentTasksBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TasksFragment : Fragment() {

    private var _binding: FragmentTasksBinding? = null
    private val binding get() = _binding!!

    private lateinit var dbHelper: com.example.quanlylichhoc.database.DatabaseHelper
    private var allTasks: List<com.example.quanlylichhoc.utils.TaskItem> = emptyList()
    private var isShowingTodo = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTasksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        parentFragmentManager.setFragmentResultListener("task_updated", this) { _, bundle ->
            if (bundle.getBoolean("refresh")) {
                loadTasks()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbHelper = com.example.quanlylichhoc.database.DatabaseHelper(requireContext())

        setupHeader()
        setupTabs()
        loadTasks()
    }
    
    override fun onResume() {
        super.onResume()
        loadTasks()
    }

    private fun setupHeader() {
        // Set dynamic date
        val dateFormat = SimpleDateFormat("EEEE, d MMMM", Locale("vi", "VN"))
        binding.tvHeaderDate.text = dateFormat.format(Date())

        binding.btnAddTask.setOnClickListener {
            val dialog = AddTaskFragment()
            dialog.show(parentFragmentManager, "AddTaskFragment")
            parentFragmentManager.registerFragmentLifecycleCallbacks(object : androidx.fragment.app.FragmentManager.FragmentLifecycleCallbacks() {
                override fun onFragmentDestroyed(fm: androidx.fragment.app.FragmentManager, f: Fragment) {
                    super.onFragmentDestroyed(fm, f)
                    if (f == dialog) {
                        loadTasks() // Refresh after dialog close
                        parentFragmentManager.unregisterFragmentLifecycleCallbacks(this)
                    }
                }
            }, false)
        }
    }

    private fun setupTabs() {
        binding.tabTodo.setOnClickListener {
            updateTabState(isTodo = true)
        }
        binding.tabDone.setOnClickListener {
            updateTabState(isTodo = false)
        }
    }

    private fun updateTabState(isTodo: Boolean) {
        isShowingTodo = isTodo
        
        val padding8dp = (8 * resources.displayMetrics.density).toInt()

        if (isTodo) {
            binding.tabTodo.setBackgroundResource(R.drawable.bg_tab_selected)
            binding.tabTodo.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue_500)) 
            binding.tabTodo.setPadding(0, padding8dp, 0, padding8dp)
            
            binding.tabDone.setBackgroundResource(R.drawable.bg_tab_unselected)
            binding.tabDone.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
            binding.tabDone.setPadding(0, padding8dp, 0, padding8dp)
        } else {
            binding.tabDone.setBackgroundResource(R.drawable.bg_tab_selected)
            binding.tabDone.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue_500))
            binding.tabDone.setPadding(0, padding8dp, 0, padding8dp)
            
            binding.tabTodo.setBackgroundResource(R.drawable.bg_tab_unselected)
            binding.tabTodo.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
            binding.tabTodo.setPadding(0, padding8dp, 0, padding8dp)
        }
        filterAndDisplayTasks()
    }

    private fun loadTasks() {
        allTasks = dbHelper.getAllTasks()
        filterAndDisplayTasks()
    }
    
    private fun filterAndDisplayTasks() {
        val filtered = if (isShowingTodo) {
            allTasks.filter { !it.isCompleted }
        } else {
            allTasks.filter { it.isCompleted }
        }
        
        // Group by Date or something? For now simple list
        val displayList = ArrayList<TaskListItem>()
        
        // Simple grouping logic: header for everything for now or just items
         if (filtered.isNotEmpty()) {
             displayList.add(TaskListItem.Header(if (isShowingTodo) "Cần làm" else "Đã hoàn thành"))
             displayList.addAll(filtered.map { 
                 val subjectRawName = if (it.subjectId != -1L) {
                     dbHelper.getSubjectById(it.subjectId.toString())?.name ?: "Khác"
                 } else {
                     "Khác"
                 }
                 TaskListItem.TaskItem(
                     id = it.id,
                     title = it.title,
                     time = formatTaskDeadline(it.deadline),
                     subject = subjectRawName,
                     priority = it.priority,
                     description = it.description,
                     isCompleted = it.isCompleted,
                     isOverdue = checkIsOverdue(it.deadline) && !it.isCompleted
                 )
             })
         }
        
        if (displayList.isEmpty()) {
            binding.rvTasks.visibility = View.GONE
            binding.tvEmptyTasks.visibility = View.VISIBLE
            binding.tvEmptyTasks.text = if (isShowingTodo) "Không có nhiệm vụ nào cần làm" else "Chưa có nhiệm vụ hoàn thành"
        } else {
            binding.rvTasks.visibility = View.VISIBLE
            binding.tvEmptyTasks.visibility = View.GONE
            val adapter = TasksAdapter(displayList, 
                onTaskLongClick = { taskItem -> showTaskOptions(taskItem) },
                onTaskStatusChange = { taskItem -> toggleTaskStatus(taskItem) }
            )
            binding.rvTasks.layoutManager = LinearLayoutManager(context)
            binding.rvTasks.adapter = adapter
        }
    }
    
    private fun toggleTaskStatus(item: TaskListItem.TaskItem) {
        val newStatus = !item.isCompleted
        dbHelper.updateTaskStatus(item.id, newStatus)
        loadTasks() // Refresh list to move item to correct tab
        
        val msg = if (newStatus) "Đã hoàn thành!" else "Đã đánh dấu chưa xong"
        android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
    }
    
    private fun showTaskOptions(item: TaskListItem.TaskItem) {
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
                        // Pass other details for pre-filling
                        // We need the original TaskItem to get description and full deadline if possible.
                        // The adapter item has: id, title, time (formatted), subject, priority.
                        // It DOES NOT have description or raw deadline easily accessible in the formatted "time" field.
                        // We might need to fetch the full task from DB or enrich the adapter item.
                        // Let's fetch from DB to be safe and accurate.
                        val task = dbHelper.getTaskById(item.id)
                        if (task != null) {
                            bundle.putString("taskDesc", task.description)
                            bundle.putString("taskDeadline", task.deadline)
                            bundle.putString("taskPriority", task.priority)
                            bundle.putLong("taskSubjectId", task.subjectId)
                        }
                        
                        dialog.arguments = bundle
                        dialog.show(parentFragmentManager, "AddTaskFragment")
                        // Reload listener logic...
                    }
                    1 -> { // Delete
                        androidx.appcompat.app.AlertDialog.Builder(requireContext())
                            .setTitle("Xóa nhiệm vụ?")
                            .setMessage("Bạn có chắc muốn xóa nhiệm vụ này không?")
                            .setPositiveButton("Xóa") { _, _ ->
                                dbHelper.deleteTask(item.id)
                                loadTasks()
                                android.widget.Toast.makeText(context, "Đã xóa", android.widget.Toast.LENGTH_SHORT).show()
                            }
                            .setNegativeButton("Hủy", null)
                            .show()
                    }
                    2 -> { // Toggle Complete
                         toggleTaskStatus(item)
                    }
                }
            }
            .show()
    }

    private fun checkIsOverdue(deadline: String): Boolean {
        if (deadline.isEmpty() || deadline == "Không có hạn") return false
        
        try {
            val now = java.util.Calendar.getInstance()
            
            // Handle "Hôm nay" special case if it exists in DB without date
            var dateStr = deadline
            if (dateStr.startsWith("Hôm nay")) {
                 val sdfDate = SimpleDateFormat("dd/MM/yyyy", Locale("vi", "VN"))
                 dateStr = dateStr.replace("Hôm nay", sdfDate.format(now.time))
            }

            val parts = dateStr.split(" ")
            val datePart = parts[0]
            val timePart = if (parts.size > 1) parts[1] else ""
            
            val sdfFull = if (timePart.isNotEmpty()) {
                SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("vi", "VN"))
            } else {
                SimpleDateFormat("dd/MM/yyyy", Locale("vi", "VN"))
            }
            
            val deadlineDate = sdfFull.parse(if (timePart.isNotEmpty()) "$datePart $timePart" else datePart) ?: return false
            
            // If only date is present, check if day is BEFORE today (yesterday or older). 
            // If today, it's not overdue yet unless time is specified? 
            // Usually "Due today" implies due by end of day, so not overdue until tomorrow.
            if (timePart.isEmpty()) {
                 val deadlineCal = java.util.Calendar.getInstance()
                 deadlineCal.time = deadlineDate
                 // Reset time to end of day? Or just check if deadline < today (ignoring time)?
                 // Let's say strictly overdue if date < today.
                 return deadlineDate.before(removeTime(now.time))
            }

            return deadlineDate.before(now.time)
        } catch (e: Exception) {
            return false
        }
    }

    private fun removeTime(date: Date): Date {
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
        if (deadline == "Hôm nay") return "Hôm nay" // Legacy handling
        
        try {
            // Deadline might be "dd/MM/yyyy" or "dd/MM/yyyy HH:mm"
            val parts = deadline.split(" ")
            val datePart = parts[0]
            val timePart = if (parts.size > 1) parts[1] else ""
            
            val sdfDate = SimpleDateFormat("dd/MM/yyyy", Locale("vi", "VN"))
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
