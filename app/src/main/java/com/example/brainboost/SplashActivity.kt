package com.example.brainboost

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.brainboost.databinding.ActivitySplashBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.*

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    private lateinit var auth: FirebaseAuth
    private val splashTime = 2000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // 🔥 Logo animation
        binding.ivLogo.animate()
            .scaleX(2.2f).scaleY(2.2f)
            .setDuration(500)
            .withEndAction {
                binding.ivLogo.animate()
                    .scaleX(0.1f).scaleY(0.1f)
                    .setDuration(500)
                    .withEndAction {
                        binding.ivLogo.animate()
                            .scaleX(1.0f).scaleY(1.0f)
                            .setDuration(500)
                            .start()
                    }.start()
            }.start()

        CoroutineScope(Dispatchers.Main).launch {
            delay(splashTime)

            val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
            val showOnboarding = prefs.getBoolean("show_onboarding", true)
            val currentUser = auth.currentUser

            if (currentUser != null) {
                // User logged in → Go to main
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            } else {
                if (showOnboarding) {
                    // First install or after logout
                    startActivity(Intent(this@SplashActivity, OnboardingActivity::class.java))
                } else {
                    // User already saw onboarding → go to login
                    startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
                }
            }

            finish()
        }
    }
}
