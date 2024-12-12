package com.unh.expense_tracker

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.unh.expense_tracker.databinding.ActivityMainBinding
import java.util.concurrent.Executor

//For some synatx like binding i have refered to the code discussedin class the link is below:
// https://github.com/UNH-Android-Fall2024/IceBreaker_Sri-Harsha-Vardhan-Yendru
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
            val email = binding.editTextUserName.text.toString().trim()
            val password = binding.editTextPassword.text.toString().trim()
            logintoapp(email,password)
        }
        binding.buttonFingerprintLogin.setOnClickListener{
            authenticateWithBiometric()
        }
    }
    //For biometric to get overview we used this reference https://developer.android.com/identity/sign-in/biometric-auth
    //For the below syntax we used chatgpt
    private fun authenticateWithBiometric() {
        val biometricManager = BiometricManager.from(this)
        if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) != BiometricManager.BIOMETRIC_SUCCESS) {
            Toast.makeText(this, "Biometric authentication is not available", Toast.LENGTH_SHORT).show()
            return
        }

        val executor: Executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                Toast.makeText(this@MainActivity, "Biometric Authentication Successful", Toast.LENGTH_SHORT).show()
                val sharedPreferences = getSharedPreferences("UserCredentials", Context.MODE_PRIVATE)
                val savedEmail = sharedPreferences.getString("email", null)
                val savedPassword = sharedPreferences.getString("password", null)
                if (savedEmail != null && savedPassword != null) {
                    logintoapp(savedEmail, savedPassword)
                    }
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                //Toast Messages:https://developer.android.com/guide/topics/ui/notifiers/toasts
                Toast.makeText(this@MainActivity, "Authentication failed", Toast.LENGTH_SHORT).show()
            }
        })
//For biometric to get overview we used this reference https://developer.android.com/identity/sign-in/biometric-auth
    //For the below syntax we used chatgpt
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Login with Biometric")
            .setSubtitle("Authenticate using fingerprint or face recognition")
            .setNegativeButtonText("Cancel")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
    private fun saveUserlogindetails(email: String, password: String) {
        val sharedPreferences = getSharedPreferences("UserCredentials", Context.MODE_PRIVATE)
        val editpref = sharedPreferences.edit()
        editpref.putString("email", email)
        editpref.putString("password", password)
        editpref.apply()
    }
    private fun logintoapp(email:String,password:String) {
        //val email = binding.editTextUserName.text.toString().trim()
        //val password = binding.editTextPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            //Toast Messages:https://developer.android.com/guide/topics/ui/notifiers/toasts
            Toast.makeText(this, "Please enter all the fields", Toast.LENGTH_SHORT).show()
            return
        }
// For firebase authentication: https://firebase.google.com/docs/auth/android/password-auth
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    saveUserlogindetails(email,password)
                    Toast.makeText(this, "Logged in successfully", Toast.LENGTH_SHORT).show()
                    binding.editTextUserName.setText("")
                    binding.editTextPassword.setText("")
                    //for navigating to another activity https://sebhastian.com/android-putextra/?
                    val intent = Intent(this, MainActivity2::class.java)
                    intent.putExtra("email", email)
                    startActivity(intent)
                    Log.d("MainActivity2", "MainActivity2 started successfully")
                } else {
                    //Toast Messages:https://developer.android.com/guide/topics/ui/notifiers/toasts
                    Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
