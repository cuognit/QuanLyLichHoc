

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
        if (isTodo) {
            binding.tabTodo.setBackgroundResource(R.drawable.bg_tab_selected)
            binding.tabTodo.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue_500)) 
            
            binding.tabDone.background = null
            binding.tabDone.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
        } else {
            binding.tabDone.setBackgroundResource(R.drawable.bg_tab_selected)
            binding.tabDone.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue_500))
            
            binding.tabTodo.background = null
            binding.tabTodo.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
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
                 TaskListItem.TaskItem(
                     id = it.id,
                     title = it.title,
                     time = it.deadline.ifEmpty { "Không có hạn" }, // Mapping deadline to time line
                     subject = it.description.split(" - ").firstOrNull() ?: "Chung", // Hacky way to get Subject name since I concatenated it in GetAllTasks
                     priority = it.priority,
                     isCompleted = it.isCompleted
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
            val adapter = TasksAdapter(displayList) { taskItem ->
                showTaskOptions(taskItem)
            }
            binding.rvTasks.layoutManager = LinearLayoutManager(context)
            binding.rvTasks.adapter = adapter
        }
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
                        // Desc?
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
                         // I need updateTaskStatus method or updateTask.
                         // For now I can use updateTask if I have full data, but I don't.
                         // Simplification: Not implemented or add updateTaskStatus to specific method
                         android.widget.Toast.makeText(context, "Tính năng đang phát triển", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dbHelper.close()
        _binding = null
    }
}
