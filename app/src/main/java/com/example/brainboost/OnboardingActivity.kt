package com.example.brainboost

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.brainboost.databinding.ActivityOnboardingBinding

class OnboardingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOnboardingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val items = listOf(
            OnboardItem("Train your brain", "Play daily puzzles to improve memory", R.drawable.on1),
            OnboardItem("Fun challenges", "Quizzes and mini games to keep you sharp", R.drawable.onboard2),
            OnboardItem("Track progress", "See your improvement with charts and badges", R.drawable.onboard3)
        )

        val adapter = OnboardAdapter(items)
        binding.viewPager.adapter = adapter

        // 🔄 Change button text on last page
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                binding.btnNext.text = if (position == items.size - 1) "Start" else "Next"
            }
        })

        // ⏭ Skip → Login
        binding.btnSkip.setOnClickListener {
            finishOnboarding()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // ➡ Next / Start
        binding.btnNext.setOnClickListener {
            val current = binding.viewPager.currentItem

            if (current == items.size - 1) {
                // Last page → Go to SIGNUP
                finishOnboarding()
                startActivity(Intent(this, SignupActivity::class.java))
                finish()
            } else {
                // Next slide
                binding.viewPager.currentItem = current + 1
            }
        }
    }

    // 🔥 Save preference: onboarding completed
    private fun finishOnboarding() {
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        prefs.edit().putBoolean("show_onboarding", false).apply()
    }
}
