package com.example.quanlylichhoc.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.quanlylichhoc.R
import com.example.quanlylichhoc.databinding.ActivityMainBinding
import com.example.quanlylichhoc.ui.fragments.ExamsFragment
import com.example.quanlylichhoc.ui.fragments.HomeFragment
import com.example.quanlylichhoc.ui.fragments.ProfileFragment
import com.example.quanlylichhoc.ui.fragments.ScheduleFragment
import com.example.quanlylichhoc.ui.fragments.TasksFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Load default fragment
        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
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
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
