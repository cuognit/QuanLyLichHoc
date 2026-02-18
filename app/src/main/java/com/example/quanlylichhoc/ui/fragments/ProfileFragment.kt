
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

        setupRows()
        setupListeners()
    }

    private fun setupRows() {
        // Account Section
        bindRow(binding.rowPersonalInfo, R.drawable.ic_user_info, "Thông tin cá nhân")
        bindRow(binding.rowChangePassword, R.drawable.ic_lock, "Đổi mật khẩu")

        // App Section
        bindRow(binding.rowNotifications, R.drawable.ic_notification_purple, "Thông báo")
        
        bindRow(binding.rowDarkMode, R.drawable.ic_moon, "Chế độ tối")
        binding.rowDarkMode.switchToggle.visibility = View.VISIBLE
        binding.rowDarkMode.imgChevron.visibility = View.GONE
        binding.rowDarkMode.switchToggle.isChecked = false // Default off

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
        binding.btnLogout.setOnClickListener {
            Toast.makeText(context, "Đăng xuất thành công!", Toast.LENGTH_SHORT).show()
        }

        // Example click listeners
        binding.rowPersonalInfo.root.setOnClickListener { 
            Toast.makeText(context, "Thông tin cá nhân", Toast.LENGTH_SHORT).show() 
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
