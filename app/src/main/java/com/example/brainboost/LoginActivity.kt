package com.example.brainboost

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.graphics.Typeface
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import com.example.brainboost.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var b: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(b.root)

        auth = FirebaseAuth.getInstance()

        // ---------------------------
        //  ANIMATIONS
        // ---------------------------
        val shakeAnim = AnimationUtils.loadAnimation(this, R.anim.shake)
        val fadeSlide = AnimationUtils.loadAnimation(this, R.anim.fade_slide_up)

        b.ivLogo.startAnimation(fadeSlide.apply { startOffset = 0 })
        b.tvWelcome.startAnimation(fadeSlide.apply { startOffset = 100 })
        b.tvSubtitle.startAnimation(fadeSlide.apply { startOffset = 200 })
        b.emailInputLayout.startAnimation(fadeSlide.apply { startOffset = 300 })
        b.passwordInputLayout.startAnimation(fadeSlide.apply { startOffset = 400 })
        b.btnLogin.startAnimation(fadeSlide.apply { startOffset = 500 })
        b.tvForgotPassword.startAnimation(fadeSlide.apply { startOffset = 600 })
        b.tvSignup.startAnimation(fadeSlide.apply { startOffset = 700 })

        // ---------------------------
        //  SIGN UP PARTIAL COLOR TEXT
        // ---------------------------
        b.tvSignup.text = HtmlCompat.fromHtml(
            "Don't have an account? <b><font color='#063f9c'>Sign Up</font></b>",
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )

        // Go to Signup
        b.tvSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        // ---------------------------
        //  LOGIN BUTTON
        // ---------------------------
        b.btnLogin.setOnClickListener {
            val email = b.etEmail.text.toString().trim()
            val pass = b.etPassword.text.toString().trim()

            if (email.isEmpty() || pass.isEmpty()) {
                b.emailInputLayout.error = "Required"
                b.passwordInputLayout.error = "Required"
                b.emailInputLayout.startAnimation(shakeAnim)
                b.passwordInputLayout.startAnimation(shakeAnim)
                return@setOnClickListener
            }

            b.emailInputLayout.error = null
            b.passwordInputLayout.error = null

            b.btnLogin.isEnabled = false
            b.btnLogin.alpha = 0.6f

            auth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener { task ->
                    b.btnLogin.isEnabled = true
                    b.btnLogin.alpha = 1f

                    if (task.isSuccessful) {

                        Toast.makeText(this, "Login successfully!", Toast.LENGTH_SHORT).show()

                        startActivity(Intent(this, MainActivity::class.java))
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                        finish()
                    }
                    else {
                        b.passwordInputLayout.startAnimation(shakeAnim)
                        b.passwordInputLayout.error = "Incorrect email or password"

                        Toast.makeText(
                            this,
                            task.exception?.localizedMessage ?: "Login failed",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }

        // ---------------------------
        //  FORGOT PASSWORD DIALOG
        // ---------------------------
        b.tvForgotPassword.setOnClickListener {
            val email = b.etEmail.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this, "Enter your email first", Toast.LENGTH_SHORT).show()
                b.emailInputLayout.startAnimation(shakeAnim)
                return@setOnClickListener
            }

            AlertDialog.Builder(this)
                .setTitle("Reset Password")
                .setMessage("Send password reset email to $email?")
                .setPositiveButton("Yes") { _, _ ->
                    auth.sendPasswordResetEmail(email)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(
                                    this,
                                    "Reset email sent to $email",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(
                                    this,
                                    task.exception?.localizedMessage ?: "Failed to send email",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                }
                .setNegativeButton("Cancel", null)
                .create().show()
        }
    }

    // Back button transition
    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}
