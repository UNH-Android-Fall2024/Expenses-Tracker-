package com.unh.expense_tracker.ui.activity

import android.app.DatePickerDialog
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.unh.expense_tracker.databinding.FragmentAddexpenseBinding
import java.util.Locale

class AddExpenseFragment : Fragment() {
    private val db = Firebase.firestore
    private var _binding: FragmentAddexpenseBinding? = null
    private val binding get() = _binding!!

    private val addExpenseViewModel: AddExpenseViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddexpenseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ivCalendar.setOnClickListener {
            showDatePickerDialog()
        }

        addExpenseViewModel.selectedDate.observe(viewLifecycleOwner, Observer { date ->
            binding.tvSelectedDate.text = date
        })

        binding.btnAddExpense.setOnClickListener {
            saveaddExpenseToFirestore()
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->

                val selectedDate = Calendar.getInstance()
                selectedDate.set(selectedYear, selectedMonth, selectedDay)

                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                addExpenseViewModel.setSelectedDate(dateFormat.format(selectedDate.time))
            },
            year,
            month,
            day
        )

        datePickerDialog.show()
    }

    private fun saveaddExpenseToFirestore() {
        val amount = binding.etAmount.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val category = binding.etCategory.text.toString().trim()
        val date = binding.tvSelectedDate.text.toString().trim()
        val email = "h@gmail.com"

        if (amount.isEmpty() || description.isEmpty() || category.isEmpty() || date.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill in all fields.", Toast.LENGTH_SHORT).show()
            return
        }
        if (amount <=0){

        }

        val expense = hashMapOf(
            "amount" to amount,
            "description" to description,
            "category" to category,
            "date" to date,
            "email" to email
        )

        db.collection("user_expenses")
            .add(expense)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Expense added successfully.", Toast.LENGTH_SHORT).show()
                binding.etAmount.text.clear()
                binding.etDescription.text.clear()
                binding.etCategory.text.clear()
                binding.tvSelectedDate.text = "DD/MM/YYYY"
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to add expense: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
