package com.unh.expense_tracker.ui.splitshare

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.unh.expense_tracker.AppData
import com.unh.expense_tracker.databinding.FragmentSplitshareBinding
import java.util.Calendar

class SplitshareFragment : Fragment() {

    private lateinit var binding: FragmentSplitshareBinding
    private val db = Firebase.firestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSplitshareBinding.inflate(inflater, container, false)

        binding.buttonAddSplit.setOnClickListener{
            checkEmailAndAddData()
        }
        binding.splitIvCalendar.setOnClickListener {
            showDatePickerDialog()
        }
        return binding.root
    }
    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)


        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->

                val date = "${selectedDay}/${selectedMonth + 1}/${selectedYear}"
                binding.editSpentDate.setText(date)
            },
            year,
            month,
            day
        )
        datePickerDialog.datePicker.maxDate = calendar.timeInMillis
        datePickerDialog.show()
    }
    private fun checkEmailAndAddData() {
        val userName = binding.editUserName.text.toString().trim()
        val userEmail = AppData.email

        if (userName == userEmail) {
            Toast.makeText(context, "Entered email cannot be the same as your email", Toast.LENGTH_SHORT).show()
            return
        }
        db.collection("App_UsersCredentials")
            .whereEqualTo("email", userName)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val document = documents.first()
                    val firstName = document.getString("firstName") ?: ""
                    val lastName = document.getString("lastName") ?: ""
                    val concatenatedName = "$firstName $lastName".trim()

                    addsplitdatatofirebase(concatenatedName)
                } else {
                    Toast.makeText(context, "Email Not Registered with Expense Tracker", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
    private fun addsplitdatatofirebase(fullName:String){
        val userEmail = AppData.email
        val userName = binding.editUserName.text.toString().trim()
        val splitAmount = binding.editSplitAmount.text.toString().toDoubleOrNull()
        val description = binding.editDescription.text.toString().trim()
        val spentDate = binding.editSpentDate.text.toString().trim()
        val concatenatedName=fullName

        if (userName.isEmpty() || splitAmount == null || description.isEmpty() || spentDate.isEmpty()) {

            Toast.makeText(context, "Please fill all the fields", Toast.LENGTH_SHORT).show()
            return
        }
        val splitData = hashMapOf(
            "concatenatedName" to concatenatedName,
            "userName" to userName,
            "splitAmount" to splitAmount,
            "description" to description,
            "spentDate" to spentDate,
            "userEmail" to userEmail
        )

        db.collection("Users_splits")
            .add(splitData)
            .addOnSuccessListener {
                Toast.makeText(context, "Split Added", Toast.LENGTH_SHORT).show()

                binding.editUserName.text.clear()
                binding.editSplitAmount.text.clear()
                binding.editDescription.text.clear()
                binding.editSpentDate.text.clear()
            }
            .addOnFailureListener { e ->

                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()

    }
}
