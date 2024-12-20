package com.unh.expense_tracker

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.unh.expense_tracker.databinding.PasswordResetBinding

//For some synatx like binding i have refered to the code discussed in class the link is below:
// https://github.com/UNH-Android-Fall2024/IceBreaker_Sri-Harsha-Vardhan-Yendru
class PasswordReset : AppCompatActivity() {
    private lateinit var binding: PasswordResetBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = PasswordResetBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()


        val backToLoginPage = findViewById<TextView>(R.id.backtologin2)
        backToLoginPage.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        binding.button3.setOnClickListener {
            resetPassword()
        }
    }

    private fun resetPassword() {
        val email = binding.editTextText.text.toString().trim()

        if (email.isEmpty()) {
            //Toast Messages:https://developer.android.com/guide/topics/ui/notifiers/toasts
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
            return
        }
// For firebase authentication: https://firebase.google.com/docs/auth/android/password-auth
        Firebase.auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        this,
                        "Password reset email sent. Please check your email.",
                        Toast.LENGTH_SHORT
                    ).show()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(
                        this,
                        "Error: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
}
