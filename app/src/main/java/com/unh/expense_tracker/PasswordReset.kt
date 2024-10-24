package com.unh.expense_tracker

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.unh.expense_tracker.databinding.PasswordResetBinding

class PasswordReset : AppCompatActivity() {
    private lateinit var binding: PasswordResetBinding
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = PasswordResetBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        val backtologinpage2 = findViewById<TextView>(R.id.backtologin2)
        backtologinpage2.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        binding.button3.setOnClickListener {
            resetpasswordtofirebase()
        }
    }

    private fun resetpasswordtofirebase() {
        val email = binding.editTextText.text.toString().trim()
        val password = binding.editTextPassword2.text.toString().trim()
        val confirmPassword = binding.editTextConfirmPassword2.text.toString().trim()

        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please enter all the fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("App_UsersCredentials")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty()) {
                    Toast.makeText(this, "Email does not exist", Toast.LENGTH_SHORT).show()
                } else {
                    for (document in documents) {
                        val documentId = document.id
                        db.collection("App_UsersCredentials")
                            .document(documentId)
                            .update("password", password)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Password updated successfully. Click 'Back to Login' to sign in.", Toast.LENGTH_SHORT).show()
                                binding.editTextText.setText("")
                                binding.editTextPassword2.setText("")
                                binding.editTextConfirmPassword2.setText("")
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Error updating password: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error checking email: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
