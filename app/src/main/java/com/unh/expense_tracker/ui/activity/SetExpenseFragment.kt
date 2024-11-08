package com.unh.expense_tracker.ui.activity

import android.app.AlertDialog
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
    private var isDataExists = false
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

        checkIfDataExists()
        binding.setLimitButton.setOnClickListener {
            saveExpenseLimittofirebase()
        }

        binding.editLimitButton.setOnClickListener {
            if (isDataExists) {
                showEditConfirmationDialog()
            } else {
                Toast.makeText(requireContext(), "No data available to edit.", Toast.LENGTH_SHORT).show()
            }
        }
        binding.deleteLimitButton.setOnClickListener{
            deleteDataFromFirebase()
        }

    }

    private fun showEditConfirmationDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Edit Expense")
        builder.setMessage("Do you want to edit your expense?")

        builder.setPositiveButton("Yes") { _, _ ->
            enableeditFields()
        }

        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }

        builder.create().show()
    }
    private fun checkIfDataExists() {
        val email = AppData.email

        db.collection("expense_limit")
            .whereEqualTo("email", email)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Toast.makeText(requireContext(), "Failed to listen for data: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshots != null && !snapshots.isEmpty) {
                    val document = snapshots.documents[0]
                    binding.totalIncomeInput.setText(document.getString("totalIncome"))
                    binding.monthlyLimitInput.setText(document.getString("monthlyLimit"))
                    binding.contentLayout.findViewById<EditText>(R.id.houseRentInput).setText(document.getString("houseRent"))
                    binding.contentLayout.findViewById<EditText>(R.id.houseUtilitiesInput).setText(document.getString("houseUtilities"))
                    binding.contentLayout.findViewById<EditText>(R.id.vehicleExpensesInput).setText(document.getString("vehicleExpenses"))
                    binding.contentLayout.findViewById<EditText>(R.id.foodInput).setText(document.getString("food"))
                    binding.contentLayout.findViewById<EditText>(R.id.tripsInput).setText(document.getString("trips"))
                    binding.contentLayout.findViewById<EditText>(R.id.groceriesInput).setText(document.getString("groceries"))
                    binding.contentLayout.findViewById<EditText>(R.id.shoppingInput).setText(document.getString("shopping"))
                    binding.contentLayout.findViewById<EditText>(R.id.miscellaneousInput).setText(document.getString("miscellaneous"))
                    disableFields()
                    isDataExists = true
                } else {
                    enableFields()
                    isDataExists = false
                }
            }
    }

private fun enableeditFields(){
    binding.totalIncomeInput.isEnabled = true
    binding.monthlyLimitInput.isEnabled = true
    binding.contentLayout.findViewById<EditText>(R.id.houseRentInput).isEnabled = true
    binding.contentLayout.findViewById<EditText>(R.id.houseUtilitiesInput).isEnabled = true
    binding.contentLayout.findViewById<EditText>(R.id.vehicleExpensesInput).isEnabled = true
    binding.contentLayout.findViewById<EditText>(R.id.foodInput).isEnabled = true
    binding.contentLayout.findViewById<EditText>(R.id.tripsInput).isEnabled = true
    binding.contentLayout.findViewById<EditText>(R.id.groceriesInput).isEnabled = true
    binding.contentLayout.findViewById<EditText>(R.id.shoppingInput).isEnabled = true
    binding.contentLayout.findViewById<EditText>(R.id.miscellaneousInput).isEnabled = true
}

    private fun enableFields() {
        binding.totalIncomeInput.text.clear()
        binding.monthlyLimitInput.text.clear()
        binding.contentLayout.findViewById<EditText>(R.id.houseRentInput).text.clear()
        binding.contentLayout.findViewById<EditText>(R.id.houseUtilitiesInput).text.clear()
        binding.contentLayout.findViewById<EditText>(R.id.vehicleExpensesInput).text.clear()
        binding.contentLayout.findViewById<EditText>(R.id.foodInput).text.clear()
        binding.contentLayout.findViewById<EditText>(R.id.tripsInput).text.clear()
        binding.contentLayout.findViewById<EditText>(R.id.groceriesInput).text.clear()
        binding.contentLayout.findViewById<EditText>(R.id.shoppingInput).text.clear()
        binding.contentLayout.findViewById<EditText>(R.id.miscellaneousInput).text.clear()


        binding.totalIncomeInput.isEnabled = true
        binding.monthlyLimitInput.isEnabled = true
        binding.contentLayout.findViewById<EditText>(R.id.houseRentInput).isEnabled = true
        binding.contentLayout.findViewById<EditText>(R.id.houseUtilitiesInput).isEnabled = true
        binding.contentLayout.findViewById<EditText>(R.id.vehicleExpensesInput).isEnabled = true
        binding.contentLayout.findViewById<EditText>(R.id.foodInput).isEnabled = true
        binding.contentLayout.findViewById<EditText>(R.id.tripsInput).isEnabled = true
        binding.contentLayout.findViewById<EditText>(R.id.groceriesInput).isEnabled = true
        binding.contentLayout.findViewById<EditText>(R.id.shoppingInput).isEnabled = true
        binding.contentLayout.findViewById<EditText>(R.id.miscellaneousInput).isEnabled = true
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
        if (isDataExists) {
            db.collection("expense_limit")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        val documentId = querySnapshot.documents[0].id
                        db.collection("expense_limit").document(documentId)
                            .set(expenseData)
                            .addOnSuccessListener {
                                Toast.makeText(requireContext(), "Expense limit updated successfully!", Toast.LENGTH_SHORT).show()
                                disableFields()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(requireContext(), "Failed to update expense limit: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Failed to fetch document: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {

            if (totalIncome.isEmpty() || monthlyLimit.isEmpty() || houseRent.isEmpty() || houseUtilities.isEmpty() ||
                vehicleExpenses.isEmpty() || food.isEmpty() || trips.isEmpty() || groceries.isEmpty() ||
                shopping.isEmpty() || miscellaneous.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in all fields.", Toast.LENGTH_SHORT).show()
                return
            }
            db.collection("expense_limit")
                .add(expenseData)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Expense limit saved successfully!", Toast.LENGTH_SHORT).show()
                    disableFields()
                    isDataExists = true
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Failed to save expense limit: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
    private fun disableFields() {
        binding.totalIncomeInput.isEnabled = false
        binding.monthlyLimitInput.isEnabled = false
        binding.contentLayout.findViewById<EditText>(R.id.houseRentInput).isEnabled = false
        binding.contentLayout.findViewById<EditText>(R.id.houseUtilitiesInput).isEnabled = false
        binding.contentLayout.findViewById<EditText>(R.id.vehicleExpensesInput).isEnabled = false
        binding.contentLayout.findViewById<EditText>(R.id.foodInput).isEnabled = false
        binding.contentLayout.findViewById<EditText>(R.id.tripsInput).isEnabled = false
        binding.contentLayout.findViewById<EditText>(R.id.groceriesInput).isEnabled = false
        binding.contentLayout.findViewById<EditText>(R.id.shoppingInput).isEnabled = false
        binding.contentLayout.findViewById<EditText>(R.id.miscellaneousInput).isEnabled = false
    }

    private fun deleteDataFromFirebase() {
        val email = AppData.email

        db.collection("expense_limit")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    for (document in querySnapshot.documents) {
                        db.collection("expense_limit").document(document.id)
                            .delete()
                            .addOnSuccessListener {
                                Toast.makeText(requireContext(), "Data deleted successfully!", Toast.LENGTH_SHORT).show()
                                clearFields()
                                isDataExists = false
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(requireContext(), "Failed to delete data: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    Toast.makeText(requireContext(), "No data found to delete.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to fetch data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun clearFields() {
        binding.totalIncomeInput.text.clear()
        binding.monthlyLimitInput.text.clear()
        binding.contentLayout.findViewById<EditText>(R.id.houseRentInput).text.clear()
        binding.contentLayout.findViewById<EditText>(R.id.houseUtilitiesInput).text.clear()
        binding.contentLayout.findViewById<EditText>(R.id.vehicleExpensesInput).text.clear()
        binding.contentLayout.findViewById<EditText>(R.id.foodInput).text.clear()
        binding.contentLayout.findViewById<EditText>(R.id.tripsInput).text.clear()
        binding.contentLayout.findViewById<EditText>(R.id.groceriesInput).text.clear()
        binding.contentLayout.findViewById<EditText>(R.id.shoppingInput).text.clear()
        binding.contentLayout.findViewById<EditText>(R.id.miscellaneousInput).text.clear()

        enableeditFields()
    }

}