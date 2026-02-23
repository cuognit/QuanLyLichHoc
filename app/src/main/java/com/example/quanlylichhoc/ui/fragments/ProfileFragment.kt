package com.example.quanlylichhoc.ui.fragments

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.quanlylichhoc.R
import com.example.quanlylichhoc.database.CalendarSyncManager
import com.example.quanlylichhoc.database.DatabaseHelper
import com.example.quanlylichhoc.databinding.FragmentProfileBinding
import com.example.quanlylichhoc.databinding.ItemSettingRowBinding
import com.example.quanlylichhoc.ui.MainActivity
import java.text.SimpleDateFormat
import java.util.*

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    // 1. Khai báo trình xin quyền (Phải để ở cấp độ Class như thế này mới không bị crash)
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val readGranted = permissions[Manifest.permission.READ_CALENDAR] ?: false
        val writeGranted = permissions[Manifest.permission.WRITE_CALENDAR] ?: false

        if (readGranted && writeGranted) {
            performSync() // Nếu người dùng đồng ý, bắt đầu đồng bộ
        } else {
            Toast.makeText(requireContext(), "Cần quyền Lịch để đồng bộ!", Toast.LENGTH_SHORT).show()
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

            val intent = android.content.Intent(requireActivity(), MainActivity::class.java)
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            activity?.finish()
            activity?.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        binding.rowTheme.root.setOnClickListener {
            showThemePickerDialog(prefs)
        }

        // 2. Lắng nghe sự kiện click vào dòng Sao lưu và đồng bộ
        binding.rowBackup.root.setOnClickListener {
            requestPermissionLauncher.launch(arrayOf(
                Manifest.permission.READ_CALENDAR,
                Manifest.permission.WRITE_CALENDAR
            ))
        }
    }

    // 3. Hàm xử lý logic đồng bộ dữ liệu từ DatabaseHelper sang CalendarSyncManager
    private fun performSync() {
        val context = requireContext()
        val dbHelper = DatabaseHelper(context)
        val syncManager = CalendarSyncManager(context)

        // 1. Lấy hoặc Tạo Lịch App
        val calendarId = syncManager.getOrCreateAppCalendar()
        if (calendarId == -1L) {
            Toast.makeText(context, "Lỗi khởi tạo Lịch!", Toast.LENGTH_SHORT).show()
            return
        }

        // 2. QUAN TRỌNG: Xóa sạch mọi thứ cũ trước khi nạp cái mới
        syncManager.clearAllEvents(calendarId)

        // 3. Lấy dữ liệu từ Database (Đảm bảo lấy bản mới nhất)
        val listClasses = dbHelper.getAllClasses()
        val listExams = dbHelper.getAllExams()

        var successCount = 0
        val sdfDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        // 4. Vòng lặp nạp Lịch Học
        for (classItem in listClasses) {
            try {
                val startPeriod = sdfDate.parse(classItem.startDate) ?: continue
                val endPeriod = sdfDate.parse(classItem.endDate) ?: continue

                val calendar = Calendar.getInstance()
                calendar.time = startPeriod

                val timeS = classItem.startTime.split(":")
                val timeE = classItem.endTime.split(":")

                while (!calendar.time.after(endPeriod)) {
                    if (calendar.get(Calendar.DAY_OF_WEEK) == classItem.dayOfWeek) {
                        val start = (calendar.clone() as Calendar).apply {
                            set(Calendar.HOUR_OF_DAY, timeS[0].toInt())
                            set(Calendar.MINUTE, timeS[1].toInt())
                        }
                        val end = (calendar.clone() as Calendar).apply {
                            set(Calendar.HOUR_OF_DAY, timeE[0].toInt())
                            set(Calendar.MINUTE, timeE[1].toInt())
                        }

                        if (syncManager.syncScheduleToCalendar(
                                "Học: ${classItem.subjectName}",
                                classItem.room ?: "",
                                start.timeInMillis,
                                end.timeInMillis,
                                calendarId
                            )) successCount++
                    }
                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                }
            } catch (e: Exception) { e.printStackTrace() }
        }

        // 5. Vòng lặp nạp Lịch Thi
        for (exam in listExams) {
            try {
                val examDate = sdfDate.parse(exam.date) ?: continue
                val t = exam.time.split(":")
                val start = Calendar.getInstance().apply {
                    time = examDate
                    set(Calendar.HOUR_OF_DAY, t[0].toInt())
                    set(Calendar.MINUTE, t[1].toInt())
                }
                val end = (start.clone() as Calendar).apply { add(Calendar.MINUTE, exam.duration) }

                if (syncManager.syncScheduleToCalendar(
                        "Thi ${exam.type}: ${exam.subjectName}",
                        exam.room,
                        start.timeInMillis,
                        end.timeInMillis,
                        calendarId
                    )) successCount++
            } catch (e: Exception) { e.printStackTrace() }
        }

        Toast.makeText(context, "Đã cập nhật $successCount sự kiện mới nhất!", Toast.LENGTH_LONG).show()
    }

    // ... (Giữ nguyên showThemePickerDialog và ThemeOption từ file cũ) ...

    private fun showThemePickerDialog(prefs: android.content.SharedPreferences) {
        val themes = listOf(
            ThemeOption("purple", "Tím Hào Hoa", "#FF6200EE"),
            ThemeOption("blue", "Xanh Thanh Lịch", "#3B82F6"),
            ThemeOption("pink", "Hồng Quyến Rũ", "#EC4899"),
            ThemeOption("orange", "Cam Năng Động", "#F59E0B"),
            ThemeOption("green", "Lục Tươi Mới", "#10B981"),
            ThemeOption("teal", "Ngọc Lam Dịu Dàng", "#14B8A6")
        )
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
            val intent = android.content.Intent(requireActivity(), MainActivity::class.java)
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            activity?.finish()
        }
        dialog.show()
    }

    data class ThemeOption(val id: String, val name: String, val colorCode: String)

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}