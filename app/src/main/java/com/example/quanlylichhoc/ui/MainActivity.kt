package com.example.quanlylichhoc.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.quanlylichhoc.R
import com.example.quanlylichhoc.databinding.ActivityMainBinding
import com.example.quanlylichhoc.ui.fragments.ExamsFragment
import com.example.quanlylichhoc.ui.fragments.HomeFragment
import com.example.quanlylichhoc.ui.fragments.OnboardingFragment
import com.example.quanlylichhoc.ui.fragments.ProfileFragment
import com.example.quanlylichhoc.ui.fragments.ScheduleFragment
import com.example.quanlylichhoc.ui.fragments.TasksFragment
import android.content.Context
import android.view.View

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        val prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val currentTheme = prefs.getString("app_theme", "purple")
        when (currentTheme) {
            "blue" -> setTheme(R.style.Theme_QuanLyLichHoc_Blue)
            "pink" -> setTheme(R.style.Theme_QuanLyLichHoc_Pink)
            "orange" -> setTheme(R.style.Theme_QuanLyLichHoc_Orange)
            "green" -> setTheme(R.style.Theme_QuanLyLichHoc_Green)
            "teal" -> setTheme(R.style.Theme_QuanLyLichHoc_Teal)
            else -> setTheme(R.style.Theme_QuanLyLichHoc)
        }
        
        super.onCreate(savedInstanceState)
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Load default fragment or Onboarding
        if (savedInstanceState == null) {
            val isOnboardingCompleted = prefs.getBoolean("onboarding_completed", false)

            if (!isOnboardingCompleted) {
                showBottomNav(false)
                loadFragment(OnboardingFragment())
            } else {
                showBottomNav(true)
                loadFragment(HomeFragment())
            }
        }

        // Initialize Database and verify data
        val dbHelper = com.example.quanlylichhoc.database.DatabaseHelper(this)
        val classes = dbHelper.getAllClasses()
        val tasks = dbHelper.getAllTasks()
        val exams = dbHelper.getAllExams()
        
        android.util.Log.d("DB_CHECK", "Classes found: ${classes.size}")
        android.util.Log.d("DB_CHECK", "Tasks found: ${tasks.size}")
        android.util.Log.d("DB_CHECK", "Exams found: ${exams.size}")

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.nav_home -> HomeFragment()
                R.id.nav_schedule -> ScheduleFragment()
                R.id.nav_tasks -> TasksFragment()
                R.id.nav_exams -> ExamsFragment()
                R.id.nav_profile -> ProfileFragment()
                else -> HomeFragment()
            }
            loadFragment(fragment)
            true
        }

        requestNotificationPermission()
        com.example.quanlylichhoc.utils.NotificationHelper.scheduleDailySummary(this)
    }

    private fun requestNotificationPermission() {
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    fun showBottomNav(show: Boolean) {
        binding.bottomNavigation.visibility = if (show) View.VISIBLE else View.GONE
    }
}
