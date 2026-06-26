package com.example.brainboost

import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import com.example.brainboost.databinding.ActivitySignupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class SignupActivity : AppCompatActivity() {

    private lateinit var b: ActivitySignupBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(b.root)

        auth = FirebaseAuth.getInstance()

        // Animations
        val shakeAnim = AnimationUtils.loadAnimation(this, R.anim.shake)
        val fadeSlide = AnimationUtils.loadAnimation(this, R.anim.fade_slide_up)

        // 🔥 Staggered animations for professional UI
        b.ivLogo.startAnimation(fadeSlide.apply { startOffset = 0 })
        b.tvTitle.startAnimation(fadeSlide.apply { startOffset = 100 })
        b.tvSubtitle.startAnimation(fadeSlide.apply { startOffset = 200 })
        b.nameInputLayout.startAnimation(fadeSlide.apply { startOffset = 300 })
        b.emailInputLayout.startAnimation(fadeSlide.apply { startOffset = 400 })
        b.passwordInputLayout.startAnimation(fadeSlide.apply { startOffset = 500 })
        b.btnSignup.startAnimation(fadeSlide.apply { startOffset = 600 })
        b.tvLogin.startAnimation(fadeSlide.apply { startOffset = 700 })


        // ---------------------------
        //  SIGN UP PARTIAL COLOR TEXT
        // ---------------------------
        b.tvLogin.text = HtmlCompat.fromHtml(
            "Already have an account? <b><font color='#063f9c'>Log In</font></b>",
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )


        // Go to Signup
        b.tvLogin.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        // Signup button click
        b.btnSignup.setOnClickListener {
            val name = b.etName.text.toString().trim()
            val email = b.etEmail.text.toString().trim()
            val password = b.etPassword.text.toString().trim()

            // Clear previous errors
            b.nameInputLayout.error = null
            b.emailInputLayout.error = null
            b.passwordInputLayout.error = null

            // Validation with shake
            when {
                name.isEmpty() -> {
                    b.nameInputLayout.error = "Enter your full name"
                    b.nameInputLayout.startAnimation(shakeAnim)
                    return@setOnClickListener
                }
                email.isEmpty() -> {
                    b.emailInputLayout.error = "Email is required"
                    b.emailInputLayout.startAnimation(shakeAnim)
                    return@setOnClickListener
                }
                password.isEmpty() -> {
                    b.passwordInputLayout.error = "Password required"
                    b.passwordInputLayout.startAnimation(shakeAnim)
                    return@setOnClickListener
                }
                password.length < 6 -> {
                    b.passwordInputLayout.error = "Minimum 6 characters"
                    b.passwordInputLayout.startAnimation(shakeAnim)
                    return@setOnClickListener
                }
            }

            // Disable button while processing
            b.btnSignup.isEnabled = false
            b.btnSignup.alpha = 0.6f

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    b.btnSignup.isEnabled = true
                    b.btnSignup.alpha = 1f

                    if (task.isSuccessful) {
                        val user = auth.currentUser

                        // ✨ Save user display name
                        val updates = UserProfileChangeRequest.Builder()
                            .setDisplayName(name)
                            .build()

                        user?.updateProfile(updates)?.addOnCompleteListener {
                            Toast.makeText(this, "Register Successfully!", Toast.LENGTH_SHORT).show()

                            startActivity(Intent(this, MainActivity::class.java))
                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                            finish()
                        }

                    } else {
                        val errorMsg = task.exception?.localizedMessage ?: "Signup failed"
                        Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
                    }
                }
        }

        // Navigate to Login
        b.tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            finish()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}
