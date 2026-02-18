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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCourseDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    private lateinit var dbHelper: com.example.quanlylichhoc.database.DatabaseHelper

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

        binding.btnEditCourse.setOnClickListener {
            Toast.makeText(context, "Tính năng sửa đang phát triển", Toast.LENGTH_SHORT).show()
        }
        
        binding.btnDeleteCourse.setOnClickListener {
            if (classItem != null) {
                dbHelper.deleteClass(classItem.id)
                Toast.makeText(context, "Đã xóa lịch học", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            } else if (subjectId != null) {
                // Delete entire subject? Maybe not implemented yet or need confirmation
                Toast.makeText(context, "Chức năng xóa môn học chưa khả dụng ở đây", Toast.LENGTH_SHORT).show()
            }
        }
        
        // Populate Data
        if (classItem != null) {
            binding.tvCourseTitle.text = classItem.subjectName
            binding.tvTime.text = "${classItem.startTime} - ${classItem.endTime}"
            binding.tvLocation.text = classItem.room
            binding.tvLecturer.text = classItem.teacher
        } else if (subjectId != null) {
            val subject = dbHelper.getSubjectById(subjectId)
            if (subject != null) {
                binding.tvCourseTitle.text = subject.name
                binding.tvLecturer.text = subject.teacher
                binding.tvLocation.text = subject.room
                binding.tvTime.text = subject.schedule
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dbHelper.close()
        _binding = null
    }
}
