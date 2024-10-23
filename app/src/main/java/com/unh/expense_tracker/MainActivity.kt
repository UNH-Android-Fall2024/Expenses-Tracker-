package com.unh.expense_tracker

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.unh.expense_tracker.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

        db.collection("App_UsersCredentials")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Toast.makeText(this, "Email does not exist", Toast.LENGTH_SHORT).show()
                } else {
                    val document = documents.first()
                    val checkpass = document.getString("password")

                    if (checkpass == password) {
                        Toast.makeText(this, "Logged in successfully", Toast.LENGTH_SHORT).show()

                        binding.editTextUserName.setText("")
                        binding.editTextPassword.setText("")

                        Log.d("MainActivity2", "MainActivity2 started successfully")
                        val intent = Intent(this, MainActivity2::class.java)
                        startActivity(intent)
                        Log.d("MainActivity2", "MainActivity2 started successfully2")
                    } else {
                        Toast.makeText(this, "Incorrect password", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error checking email: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
