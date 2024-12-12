package com.unh.expense_tracker

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.unh.expense_tracker.databinding.NewUserRegisterBinding

//For some synatx like binding i have refered to the code discussedin class the link is below:
// https://github.com/UNH-Android-Fall2024/IceBreaker_Sri-Harsha-Vardhan-Yendru
class NewUserRegister : AppCompatActivity() {
    private lateinit var binding: NewUserRegisterBinding
    private val db = Firebase.firestore
    private val auth = Firebase.auth
    private var TAG = "Expense_Tracker_App"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = NewUserRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

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
// For firebase authentication: https://firebase.google.com/docs/auth/android/password-auth
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val currentUser = auth.currentUser
                    val userId = currentUser?.uid

                    if (userId != null) {
                        val user = hashMapOf(
                            "firstName" to firstName,
                            "lastName" to lastName,
                            "email" to email
                        )

                        db.collection("App_UsersCredentials")
                            .document(userId)
                            .set(user)
                            .addOnSuccessListener {
                                Log.d(TAG, "User successfully registered with ID: $userId")
                                Toast.makeText(this, "Successfully created account! Click 'Back to Login' to sign in.", Toast.LENGTH_LONG).show()
                                binding.editTextFirstName.setText("")
                                binding.editTextLastName.setText("")
                                binding.editTextEmail.setText("")
                                binding.editTextPassword.setText("")
                                binding.editTextConfirmPassword.setText("")
                            }
                            .addOnFailureListener { exception ->
                                Log.w(TAG, "Error adding document", exception)
                                //Toast Messages:https://developer.android.com/guide/topics/ui/notifiers/toasts
                                Toast.makeText(this, "Error registering user", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    task.exception?.let {
                        val errorMessage = when (it) {
                            is FirebaseAuthWeakPasswordException -> "Password is too weak."
                            is FirebaseAuthInvalidCredentialsException -> "Invalid email format."
                            is FirebaseAuthUserCollisionException -> "This email is already registered."
                            else -> "Error creating account: ${it.localizedMessage}"
                        }
                        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                        Log.w(TAG, "Error: ${it.message}", it)
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error during user creation", exception)
                Toast.makeText(this, "Error creating account", Toast.LENGTH_SHORT).show()
            }
    }
}
