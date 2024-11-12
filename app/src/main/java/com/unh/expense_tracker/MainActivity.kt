package com.unh.expense_tracker

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.unh.expense_tracker.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        val createAccountTextView = findViewById<TextView>(R.id.textViewCreateAccount)
        val resetPassword = findViewById<TextView>(R.id.textViewForgotPassword)

        createAccountTextView.setOnClickListener {
            val intent = Intent(this, NewUserRegister::class.java)
            startActivity(intent)
        }
        resetPassword.setOnClickListener {
            val intent = Intent(this, PasswordReset::class.java)
            startActivity(intent)
        }

        binding.buttonLogin.setOnClickListener {
            logintoapp()
        }
    }

    private fun logintoapp() {
        val email = binding.editTextUserName.text.toString().trim()
        val password = binding.editTextPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter all the fields", Toast.LENGTH_SHORT).show()
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Logged in successfully", Toast.LENGTH_SHORT).show()
                    binding.editTextUserName.setText("")
                    binding.editTextPassword.setText("")
                    val intent = Intent(this, MainActivity2::class.java)
                    intent.putExtra("email", email)
                    startActivity(intent)
                    Log.d("MainActivity2", "MainActivity2 started successfully")
                } else {
                    Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
