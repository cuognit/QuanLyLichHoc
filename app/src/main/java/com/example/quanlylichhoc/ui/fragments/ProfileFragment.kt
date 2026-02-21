
package com.example.quanlylichhoc.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.quanlylichhoc.R
import com.example.quanlylichhoc.databinding.FragmentProfileBinding
import com.example.quanlylichhoc.databinding.ItemSettingRowBinding
import com.example.quanlylichhoc.ui.MainActivity

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

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
