package com.example.quanlylichhoc.ui.fragments

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import com.example.quanlylichhoc.R
import com.example.quanlylichhoc.databinding.FragmentOnboardingBinding

class OnboardingFragment : Fragment() {

    private var _binding: FragmentOnboardingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAnimations()
        setupInputLogic()
    }

    private fun setupAnimations() {
        // Floating animation for the illustration
        val floatingAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.anim_floating_up_down)
        binding.layoutIllustration.startAnimation(floatingAnim)

        // Fade In Up animations with delays
        val fadeInUp = AnimationUtils.loadAnimation(requireContext(), R.anim.anim_fade_in_up)
        
        binding.tvTitle.visibility = View.INVISIBLE
        binding.tvDescription.visibility = View.INVISIBLE
        binding.tvNameLabel.visibility = View.INVISIBLE
        binding.etName.visibility = View.INVISIBLE
        binding.btnGetStarted.visibility = View.INVISIBLE

        Handler(Looper.getMainLooper()).postDelayed({
            binding.tvTitle.visibility = View.VISIBLE
            binding.tvTitle.startAnimation(fadeInUp)
        }, 200)

        Handler(Looper.getMainLooper()).postDelayed({
            binding.tvDescription.visibility = View.VISIBLE
            binding.tvDescription.startAnimation(fadeInUp)
        }, 400)

        Handler(Looper.getMainLooper()).postDelayed({
            binding.tvNameLabel.visibility = View.VISIBLE
            binding.etName.visibility = View.VISIBLE
            binding.tvNameLabel.startAnimation(fadeInUp)
            binding.etName.startAnimation(fadeInUp)
        }, 600)

        Handler(Looper.getMainLooper()).postDelayed({
            binding.btnGetStarted.visibility = View.VISIBLE
            binding.btnGetStarted.startAnimation(fadeInUp)
        }, 800)
    }

    private fun setupInputLogic() {
        binding.etName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.btnGetStarted.isEnabled = !s.isNullOrBlank()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.btnGetStarted.setOnClickListener {
            handleStart()
        }
    }

    private fun handleStart() {
        val name = binding.etName.text.toString().trim()
        
        // Show loading
        binding.btnGetStarted.text = ""
        binding.btnGetStarted.isEnabled = false
        binding.progressLoading.visibility = View.VISIBLE

        // Simulate initialization / loading
        Handler(Looper.getMainLooper()).postDelayed({
            saveAndFinish(name)
        }, 1500)
    }

    private fun saveAndFinish(name: String) {
        val prefs = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("user_name", name)
            putBoolean("onboarding_completed", true)
            apply()
        }

        // Navigate to Home
        parentFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.anim_fade_in_up, 0)
            .replace(R.id.fragment_container, HomeFragment())
            .commit()
            
        // Show the bottom navigation in MainActivity
        (activity as? com.example.quanlylichhoc.ui.MainActivity)?.showBottomNav(true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
