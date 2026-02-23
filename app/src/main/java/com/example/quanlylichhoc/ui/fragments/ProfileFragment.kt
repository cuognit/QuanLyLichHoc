
package com.example.quanlylichhoc.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.quanlylichhoc.R
import com.example.quanlylichhoc.database.CalendarSyncManager
import com.example.quanlylichhoc.databinding.FragmentProfileBinding
import com.example.quanlylichhoc.databinding.ItemSettingRowBinding
import com.example.quanlylichhoc.ui.MainActivity

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val requestPermissionLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val readGranted = permissions[android.Manifest.permission.READ_CALENDAR] ?: false
        val writeGranted = permissions[android.Manifest.permission.WRITE_CALENDAR] ?: false

        if (readGranted && writeGranted) {
            // Người dùng đã cho phép -> Chạy hàm đồng bộ
            performSync()
        } else {
            Toast.makeText(requireContext(), "Bạn cần cấp quyền Lịch để đồng bộ!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadUserData()
        setupRows()
        setupListeners()
    }

    private fun loadUserData() {
        val prefs = requireContext().getSharedPreferences("AppPrefs", android.content.Context.MODE_PRIVATE)
        val userName = prefs.getString("user_name", "Người dùng")
        binding.tvUserName.text = userName
    }

    private fun setupRows() {
        val prefs = requireContext().getSharedPreferences("AppPrefs", android.content.Context.MODE_PRIVATE)
        
        // App Section
        bindRow(binding.rowTheme, R.drawable.ic_palette, "Chủ đề giao diện")
        
        bindRow(binding.rowDarkMode, R.drawable.ic_moon, "Chế độ tối") 
        val isDarkMode = prefs.getBoolean("dark_mode", false)
        binding.rowDarkMode.switchToggle.visibility = View.VISIBLE
        binding.rowDarkMode.imgChevron.visibility = View.GONE
        binding.rowDarkMode.switchToggle.isChecked = isDarkMode

        bindRow(binding.rowLanguage, R.drawable.ic_language, "Ngôn ngữ")
        binding.rowLanguage.tvValue.visibility = View.VISIBLE
        binding.rowLanguage.tvValue.text = "Tiếng Việt"

        // Data Section
        bindRow(binding.rowBackup, R.drawable.ic_backup, "Sao lưu và đồng bộ")

        // Support Section
        bindRow(binding.rowContact, R.drawable.ic_headset, "Liên hệ")
        bindRow(binding.rowPolicy, R.drawable.ic_policy, "Điều khoản")
    }

    private fun bindRow(rowBinding: ItemSettingRowBinding, iconRes: Int, title: String) {
        rowBinding.imgIcon.setImageResource(iconRes)
        rowBinding.tvTitle.text = title
    }

    private fun setupListeners() {
        val prefs = requireContext().getSharedPreferences("AppPrefs", android.content.Context.MODE_PRIVATE)

        binding.rowDarkMode.switchToggle.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("dark_mode", isChecked).apply()
            if (isChecked) {
                androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO)
            }
            
            // Manual restart to avoid platform recreate() bug during night mode change
            val intent = android.content.Intent(requireActivity(), MainActivity::class.java)
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            activity?.finish()
            activity?.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        binding.rowTheme.root.setOnClickListener {
            showThemePickerDialog(prefs)
        }
        binding.rowBackup.root.setOnClickListener {
            // Gọi hộp thoại xin quyền
            requestPermissionLauncher.launch(arrayOf(
                android.Manifest.permission.READ_CALENDAR,
                android.Manifest.permission.WRITE_CALENDAR
            ))
        }
    }
    private fun performSync() {
        val context = requireContext()

        // Khởi tạo Database và SyncManager
        val dbHelper = com.example.quanlylichhoc.database.DatabaseHelper(context)
        val syncManager = com.example.quanlylichhoc.database.CalendarSyncManager(context)

        // Lấy dữ liệu Lịch Học và Lịch Thi từ DB mới
        val listClasses = dbHelper.getAllClasses()
        val listExams = dbHelper.getAllExams()

        if (listClasses.isEmpty() && listExams.isEmpty()) {
            Toast.makeText(context, "Chưa có Lịch học hoặc Lịch thi nào để đồng bộ!", Toast.LENGTH_SHORT).show()
            return
        }

        var successCount = 0
        val sdfDate = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())

        // ==========================================
        // 1. ĐỒNG BỘ LỊCH HỌC (Classes)
        // ==========================================
        for (classItem in listClasses) {
            android.util.Log.d("SyncDebug", "Đang xử lý môn: ${classItem.subjectName}") // Sửa lại đúng tên biến của bạn

            try {
                if (classItem.startDate.isEmpty() || classItem.endDate.isEmpty()) {
                    android.util.Log.e("SyncDebug", "-> BỎ QUA: Môn này chưa nhập Ngày bắt đầu / Ngày kết thúc")
                    continue
                }

                val startDate = sdfDate.parse(classItem.startDate)
                val endDate = sdfDate.parse(classItem.endDate)

                if (startDate == null || endDate == null) {
                    android.util.Log.e("SyncDebug", "-> BỎ QUA: Lỗi parse ngày tháng (Có thể sai định dạng dd/MM/yyyy)")
                    continue
                }

                val calendar = java.util.Calendar.getInstance()
                calendar.time = startDate

                val timePartsStart = classItem.startTime.split(":")
                val hStart = timePartsStart[0].toInt()
                val mStart = timePartsStart[1].toInt()

                val timePartsEnd = classItem.endTime.split(":")
                val hEnd = timePartsEnd[0].toInt()
                val mEnd = timePartsEnd[1].toInt()

                var isSynced = false
                while (!calendar.time.after(endDate)) {
                    if (calendar.get(java.util.Calendar.DAY_OF_WEEK) == classItem.dayOfWeek) { // Sửa classItem.day cho đúng

                        val eventStart = calendar.clone() as java.util.Calendar
                        eventStart.set(java.util.Calendar.HOUR_OF_DAY, hStart)
                        eventStart.set(java.util.Calendar.MINUTE, mStart)
                        eventStart.set(java.util.Calendar.SECOND, 0)

                        val eventEnd = calendar.clone() as java.util.Calendar
                        eventEnd.set(java.util.Calendar.HOUR_OF_DAY, hEnd)
                        eventEnd.set(java.util.Calendar.MINUTE, mEnd)
                        eventEnd.set(java.util.Calendar.SECOND, 0)

                        val title = "Học: \${classItem.subjectName}"
                        val isSuccess = syncManager.syncScheduleToCalendar(
                            subjectName = title,
                            room = classItem.room ?: "",
                            startTimeInMillis = eventStart.timeInMillis,
                            endTimeInMillis = eventEnd.timeInMillis
                        )

                        if (isSuccess) {
                            isSynced = true
                        } else {
                            android.util.Log.e("SyncDebug", "-> LỖI: Hàm syncScheduleToCalendar trả về false!")
                        }
                    }
                    calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
                }

                if (isSynced) {
                    successCount++
                    android.util.Log.d("SyncDebug", "-> THÀNH CÔNG: Đã đẩy môn này lên Lịch!")
                }

            } catch (e: Exception) {
                android.util.Log.e("SyncDebug", "-> LỖI CODE: \${e.message}")
                e.printStackTrace()
            }
        }

        // ==========================================
        // 2. ĐỒNG BỘ LỊCH THI (Exams)
        // ==========================================
        for (exam in listExams) {
            try {
                val examDate = sdfDate.parse(exam.date) ?: continue
                val timeParts = exam.time.split(":")
                val hStart = timeParts[0].toInt()
                val mStart = timeParts[1].toInt()

                // Tạo thời gian Bắt đầu thi
                val eventStart = java.util.Calendar.getInstance()
                eventStart.time = examDate
                eventStart.set(java.util.Calendar.HOUR_OF_DAY, hStart)
                eventStart.set(java.util.Calendar.MINUTE, mStart)
                eventStart.set(java.util.Calendar.SECOND, 0)

                // Tạo thời gian Kết thúc (Bằng giờ bắt đầu + số phút làm bài)
                val eventEnd = eventStart.clone() as java.util.Calendar
                eventEnd.add(java.util.Calendar.MINUTE, exam.duration)

                val title = "Thi ${exam.type}: ${exam.subjectName}"
                val roomDesc = if (exam.room.isNotEmpty()) "Phòng: ${exam.room} | SBD: ${exam.sbd}" else ""

                val isSuccess = syncManager.syncScheduleToCalendar(
                    subjectName = title,
                    room = roomDesc,
                    startTimeInMillis = eventStart.timeInMillis,
                    endTimeInMillis = eventEnd.timeInMillis
                )
                if (isSuccess) successCount++

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Thông báo hoàn thành
        Toast.makeText(context, "Đã đồng bộ thành công $successCount sự kiện lên Lịch!", Toast.LENGTH_LONG).show()
    }

    private fun showThemePickerDialog(prefs: android.content.SharedPreferences) {
        val themes = listOf(
            ThemeOption("purple", "Tím Hào Hoa", "#FF6200EE"),
            ThemeOption("blue", "Xanh Thanh Lịch", "#3B82F6"),
            ThemeOption("pink", "Hồng Quyến Rũ", "#EC4899"),
            ThemeOption("orange", "Cam Năng Động", "#F59E0B"),
            ThemeOption("green", "Lục Tươi Mới", "#10B981"),
            ThemeOption("teal", "Ngọc Lam Dịu Dàng", "#14B8A6")
        )

        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.fragment_my_subjects, null)
        // Reusing fragment_my_subjects structure just for the recycler/title
        // Better to create a simple custom layout or just use a ListView with custom adapter
        
        val listView = android.widget.ListView(requireContext())
        val adapter = object : android.widget.ArrayAdapter<ThemeOption>(requireContext(), R.layout.item_theme_option, themes) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_theme_option, parent, false)
                val item = getItem(position)!!
                
                val tvName = view.findViewById<android.widget.TextView>(R.id.tv_theme_name)
                val viewColor = view.findViewById<View>(R.id.view_color_preview)
                
                tvName.text = item.name
                viewColor.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor(item.colorCode))
                
                return view
            }
        }
        listView.adapter = adapter
        
        val dialog = android.app.AlertDialog.Builder(requireContext())
            .setTitle("Chọn chủ đề giao diện")
            .setView(listView)
            .create()

        listView.setOnItemClickListener { _, _, position, _ ->
            val selected = themes[position]
            prefs.edit().putString("app_theme", selected.id).apply()
            dialog.dismiss()
            
            // Manual restart to avoid platform recreate() bug
            val intent = android.content.Intent(requireActivity(), MainActivity::class.java)
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            activity?.finish()
            activity?.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        dialog.show()
    }

    data class ThemeOption(val id: String, val name: String, val colorCode: String)

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
