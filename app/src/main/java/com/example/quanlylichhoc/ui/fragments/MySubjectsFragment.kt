package com.example.quanlylichhoc.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlylichhoc.R
import com.example.quanlylichhoc.adapters.SubjectsAdapter
import com.example.quanlylichhoc.database.DatabaseHelper
import com.example.quanlylichhoc.database.SubjectItem
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MySubjectsFragment : Fragment() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var adapter: SubjectsAdapter
    private var allSubjects: List<SubjectItem> = ArrayList()
    private var filteredSubjects: List<SubjectItem> = ArrayList()
    
    // Filter States
    private var currentFilterType = FILTER_ALL
    private var currentSearchQuery = ""

    companion object {
        private const val FILTER_ALL = 0
        private const val FILTER_ACTIVE = 1
        private const val FILTER_ENDED = 2
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_my_subjects, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dbHelper = DatabaseHelper(requireContext())
        allSubjects = dbHelper.getAllSubjects()
        filteredSubjects = ArrayList(allSubjects)

        // Back Button
        view.findViewById<View>(R.id.btn_back).setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // RecyclerView
        val rvSubjects = view.findViewById<RecyclerView>(R.id.rv_subjects)
        rvSubjects.layoutManager = LinearLayoutManager(context)
        adapter = SubjectsAdapter(filteredSubjects) { subject ->
            // On Item Click - Navigate to Course Detail
            val fragment = CourseDetailFragment.newInstance(subject.id)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
        rvSubjects.adapter = adapter

        // Search
        val etSearch = view.findViewById<EditText>(R.id.et_search)
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentSearchQuery = s.toString()
                applyFilters()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Tabs
        setupTabs(view)
        
        // Initial Filter
        applyFilters()
    }

    private fun setupTabs(view: View) {
        val tabAll = view.findViewById<TextView>(R.id.tab_all)
        val tabActive = view.findViewById<TextView>(R.id.tab_active)
        val tabEnded = view.findViewById<TextView>(R.id.tab_ended)

        val activeBg = R.drawable.button_primary
        val inactiveBg = R.drawable.bg_chip_unselected
        val activeColor = ContextCompat.getColor(requireContext(), R.color.white)
        val inactiveColor = ContextCompat.getColor(requireContext(), R.color.text_secondary)

        fun updateTabStyles(selected: Int) {
            currentFilterType = selected
            
            tabAll.setBackgroundResource(if (selected == FILTER_ALL) activeBg else inactiveBg)
            tabAll.setTextColor(if (selected == FILTER_ALL) activeColor else inactiveColor)

            tabActive.setBackgroundResource(if (selected == FILTER_ACTIVE) activeBg else inactiveBg)
            tabActive.setTextColor(if (selected == FILTER_ACTIVE) activeColor else inactiveColor)

            tabEnded.setBackgroundResource(if (selected == FILTER_ENDED) activeBg else inactiveBg)
            tabEnded.setTextColor(if (selected == FILTER_ENDED) activeColor else inactiveColor)
            
            applyFilters()
        }

        tabAll.setOnClickListener { updateTabStyles(FILTER_ALL) }
        tabActive.setOnClickListener { updateTabStyles(FILTER_ACTIVE) }
        tabEnded.setOnClickListener { updateTabStyles(FILTER_ENDED) }
    }

    private fun applyFilters() {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("vi", "VN"))
        val today = Calendar.getInstance().time

        filteredSubjects = allSubjects.filter { subject ->
            // Search Filter
            val matchSearch = subject.name.contains(currentSearchQuery, ignoreCase = true) ||
                              subject.teacher.contains(currentSearchQuery, ignoreCase = true)

            // Tab Filter
            val matchTab = when (currentFilterType) {
                FILTER_ALL -> true
                FILTER_ACTIVE -> {
                    if (subject.endDate.isNotEmpty()) {
                        try {
                            val end = dateFormat.parse(subject.endDate)
                            end != null && (end.after(today) || end == today)
                        } catch (e: Exception) { true } // Keep if date invalid
                    } else true
                }
                FILTER_ENDED -> {
                    if (subject.endDate.isNotEmpty()) {
                        try {
                            val end = dateFormat.parse(subject.endDate)
                            end != null && end.before(today)
                        } catch (e: Exception) { false }
                    } else false
                }
                else -> true
            }

            matchSearch && matchTab
        }

        adapter.updateData(filteredSubjects)
        
        // Empty State
        view?.findViewById<View>(R.id.tv_empty)?.visibility = 
            if (filteredSubjects.isEmpty()) View.VISIBLE else View.GONE
    }
    
    override fun onResume() {
        super.onResume()
        // Reload data in case it changed
        allSubjects = dbHelper.getAllSubjects()
        applyFilters()
    }
}
