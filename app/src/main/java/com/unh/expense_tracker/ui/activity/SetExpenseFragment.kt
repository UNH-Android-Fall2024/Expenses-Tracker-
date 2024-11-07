package com.unh.expense_tracker.ui.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.unh.expense_tracker.AppData
import com.unh.expense_tracker.R
import com.unh.expense_tracker.databinding.SetExpenseBinding


class SetExpenseFragment : Fragment() {
    private val db = Firebase.firestore
    private lateinit var binding: SetExpenseBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SetExpenseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
        binding.setLimitButton.setOnClickListener {
            saveExpenseLimittofirebase()
        }
    }
    private fun saveExpenseLimittofirebase(){
        val email = AppData.email
        val totalIncome = binding.totalIncomeInput.text.toString().trim()
        val monthlyLimit = binding.monthlyLimitInput.text.toString().trim()
        val houseRent = binding.contentLayout.findViewById<EditText>(R.id.houseRentInput).text.toString().trim()
        val houseUtilities = binding.contentLayout.findViewById<EditText>(R.id.houseUtilitiesInput).text.toString().trim()
        val vehicleExpenses = binding.contentLayout.findViewById<EditText>(R.id.vehicleExpensesInput).text.toString().trim()
        val food = binding.contentLayout.findViewById<EditText>(R.id.foodInput).text.toString().trim()
        val trips = binding.contentLayout.findViewById<EditText>(R.id.tripsInput).text.toString().trim()
        val groceries = binding.contentLayout.findViewById<EditText>(R.id.groceriesInput).text.toString().trim()
        val shopping = binding.contentLayout.findViewById<EditText>(R.id.shoppingInput).text.toString().trim()
        val miscellaneous = binding.contentLayout.findViewById<EditText>(R.id.miscellaneousInput).text.toString().trim()

        val expenseData = hashMapOf(
            "email" to email,
            "totalIncome" to totalIncome,
            "monthlyLimit" to monthlyLimit,
            "houseRent" to houseRent,
            "houseUtilities" to houseUtilities,
            "vehicleExpenses" to vehicleExpenses,
            "food" to food,
            "trips" to trips,
            "groceries" to groceries,
            "shopping" to shopping,
            "miscellaneous" to miscellaneous
        )
        if(totalIncome.isEmpty() || monthlyLimit.isEmpty() || houseRent.isEmpty() || houseUtilities.isEmpty()|| vehicleExpenses.isEmpty()
            || food.isEmpty() || trips.isEmpty() || groceries.isEmpty() || shopping.isEmpty() || miscellaneous.isEmpty()){
            Toast.makeText(requireContext(), "Please fill in all fields.", Toast.LENGTH_SHORT).show()
            return
        }
        db.collection("expense_limit")
            .add(expenseData)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Expense limit saved successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to save expense limit: ${e.message}", Toast.LENGTH_SHORT).show()
            }

    }
    }