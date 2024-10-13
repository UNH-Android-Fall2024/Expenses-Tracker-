package com.unh.expense_tracker

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.unh.expense_tracker.databinding.NewUserRegisterBinding

class NewUserRegister : AppCompatActivity() {
    private lateinit var binding: NewUserRegisterBinding
    private val db = Firebase.firestore
    private var TAG = "Expense_Tracker_App"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = NewUserRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val backtologinpage = findViewById<TextView>(R.id.backtologin1)
        backtologinpage.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        binding.button.setOnClickListener {
            writenewuserdetailstofirebase()
        }
    }
    private fun writenewuserdetailstofirebase() {
        val firstName = binding.editTextFirstName.text.toString().trim()
        val lastName = binding.editTextLastName.text.toString().trim()
        val email = binding.editTextEmail.text.toString().trim()
        val password = binding.editTextPassword.text.toString().trim()
        val confirmPass = binding.editTextConfirmPassword.text.toString().trim()

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPass.isEmpty()) {
            Toast.makeText(this, "Please enter all the fields", Toast.LENGTH_SHORT).show()
            return
        }
        if (password != confirmPass) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }
        db.collection("App_UsersCredentials")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {

                    Toast.makeText(this, "Email already exists", Toast.LENGTH_SHORT).show()
                } else {
                    val user = hashMapOf(
                        "firstName" to firstName,
                        "lastName" to lastName,
                        "email" to email,
                        "password" to password
                    )

                    db.collection("App_UsersCredentials")
                        .add(user)
                        .addOnSuccessListener { documentReference ->
                            Log.d(TAG, "DocumentSnapshot successfully written with ID: ${documentReference.id}")
                            Toast.makeText(this, "Successfully created account! Click 'Back to Login' to sign in.", Toast.LENGTH_LONG).show()

                            binding.editTextFirstName.setText("")
                            binding.editTextLastName.setText("")
                            binding.editTextEmail.setText("")
                            binding.editTextPassword.setText("")
                            binding.editTextConfirmPassword.setText("")
                        }
                        .addOnFailureListener { exception ->
                            Log.w(TAG, "Error adding document", exception)
                            Toast.makeText(this, "Error registering user", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error checking email", exception)
                Toast.makeText(this, "Error checking email", Toast.LENGTH_SHORT).show()
            }
    }
}
