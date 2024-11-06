package com.unh.expense_tracker.ui.activity

import android.R
import android.app.DatePickerDialog
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.unh.expense_tracker.AppData
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
        val categories = listOf(
            "House Rent",
            "House Utilities",
            "Vehicle Expenses",
            "Food",
            "Trips",
            "Groceries",
            "Shopping",
            "Miscellaneous"
        )
        val adapter = ArrayAdapter(requireContext(), R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter

        binding.ivCalendar.setOnClickListener {
            showDatePickerDialog()
        }

        addExpenseViewModel.selectedDate.observe(viewLifecycleOwner, Observer { date ->
            binding.tvSelectedDate.text = date
        })

        binding.btnAddExpense.setOnClickListener {
            saveaddExpenseToFirestore()
        }

        binding.cbRecurringYes.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.cbRecurringNo.isChecked = false
            }
        }

        binding.cbRecurringNo.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.cbRecurringYes.isChecked = false
            }
        }
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // abstract method
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    android.R.id.home -> {
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val minDate = Calendar.getInstance()
        minDate.set(Calendar.DAY_OF_MONTH, 1)

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
        datePickerDialog.datePicker.minDate = minDate.timeInMillis
        datePickerDialog.datePicker.maxDate = calendar.timeInMillis
        datePickerDialog.show()
    }

    private fun saveaddExpenseToFirestore() {
        val amountString = binding.etAmount.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val category = binding.spinnerCategory.selectedItem.toString()
        val date = binding.tvSelectedDate.text.toString().trim()
        val email = AppData.email
        val isRecurring = if (binding.cbRecurringYes.isChecked) "Yes" else "No"

        Log.d("AddExpenseFragment", "Email received: $email")

        if (description.isEmpty() || category.isEmpty() || date.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill in all fields.", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountString.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            Toast.makeText(requireContext(), "Enter an amount greater than 0.", Toast.LENGTH_SHORT).show()
            return
        }

        val expense = hashMapOf(
            "amount" to amountString,
            "description" to description,
            "category" to category,
            "date" to date,
            "email" to email,
            "recurring" to isRecurring
        )

        db.collection("user_expenses")
            .add(expense)
            .addOnSuccessListener {
                Log.d("AddExpenseFragment", "Expense added successfully")
                Toast.makeText(requireContext(), "Expense added successfully.", Toast.LENGTH_SHORT).show()
                clearInputFields()


                findNavController().previousBackStackEntry?.savedStateHandle?.set("expense_added", true)
            }
            .addOnFailureListener { e ->
                Log.w("AddExpenseFragment", "Failed to add expense - ${e.message}")
                Toast.makeText(requireContext(), "Failed to add expense: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun clearInputFields() {
        binding.etAmount.text.clear()
        binding.etDescription.text.clear()
        binding.spinnerCategory.setSelection(0)
        binding.tvSelectedDate.text = "DD/MM/YYYY"
        binding.cbRecurringYes.isChecked = false
        binding.cbRecurringNo.isChecked = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
