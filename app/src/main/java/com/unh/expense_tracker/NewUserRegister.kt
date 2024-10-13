package com.unh.expense_tracker

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import androidx.appcompat.app.AppCompatActivity

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

        binding.button.setOnClickListener{
            writenewuserdetailstofirebase()
        }
    }
    private fun writenewuserdetailstofirebase(){
        val firstName=binding.editTextFirstName
        val lastName=binding.editTextLastName
        val emaild=binding.editTextEmail
        val password=binding.editTextPassword
        val confirmPass=binding.editTextConfirmPassword

        val tag = "Expense_Tracker_App"
        val msg = "Variables: $firstName $lastName $emaild $password $confirmPass"

        Log.d(tag, msg)

        val users = hashMapOf(
            "firstname" to firstName.text.toString(),
            "lastName" to lastName.text.toString(),
            "emaild" to emaild.text.toString(),
            "password" to password.text.toString(),

        )
        db.collection( "App_UsersCredentials")
            .add(users)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG,"Document Snapshot written successfully with ID: ${documentReference.id}")
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error adding Document", exception)
            }
        firstName.setText("")
        lastName.setText("")
        emaild.setText("")
        password.setText("")

    }

}
