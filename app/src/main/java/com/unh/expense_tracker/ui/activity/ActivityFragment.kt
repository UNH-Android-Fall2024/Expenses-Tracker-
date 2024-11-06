package com.unh.expense_tracker.ui.activity

import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.unh.expense_tracker.databinding.FragmentActivityBinding
import java.util.Locale
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.unh.expense_tracker.AppData

class ActivityFragment : Fragment() {

    private lateinit var binding: FragmentActivityBinding
    private val db = Firebase.firestore
    private lateinit var mRecyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val activityViewModel = ViewModelProvider(this).get(ActivityViewModel::class.java)
        binding = FragmentActivityBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentDate = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        val formattedDate = "Today, ${dateFormat.format(currentDate)}"
        binding.textCurrentDate.text = formattedDate

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>("expense_added")
            ?.observe(viewLifecycleOwner) { result ->
                if (result) {
                    loadTotalExpense()
                    loadExpenseDataFromFirebase()
                }
            }

        binding.fabAdd.setOnClickListener {
            findNavController().navigate(ActivityFragmentDirections.actionNavigationActivityToAddExpenseFragment())
        }
        binding.btnTransactions.setOnClickListener{
            findNavController().navigate(ActivityFragmentDirections.actionNavigationActivityToExpenseStatistics())
        }

        loadTotalExpense()
        loadExpenseDataFromFirebase()
    }

    override fun onResume() {
        super.onResume()
        loadTotalExpense()
        loadExpenseDataFromFirebase()
    }
    private fun loadExpenseDataFromFirebase() {
        val userEmail = AppData.email
        val expenseRecyclerList: ArrayList<expensecard> = arrayListOf()

        val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)

        mRecyclerView = binding.recyclerExpenseChild
        mRecyclerView.setHasFixedSize(true)
        mRecyclerView.layoutManager = LinearLayoutManager(context)
        val expenseAdapter = ExpenseAdapter(expenseRecyclerList, this)
        mRecyclerView.adapter = expenseAdapter

        db.collection("user_expenses")
            .whereEqualTo("email", userEmail)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.e("ActivityFragment", "Error loading expenses", error)
                    return@addSnapshotListener
                }

                if (snapshots == null || snapshots.isEmpty) {
                    Log.d("ActivityFragment", "No data available for user: $userEmail")
                    return@addSnapshotListener
                }

                Log.d("ActivityFragment", "Data found for user: $userEmail with ${snapshots.size()} entries")

                expenseRecyclerList.clear()

                for (document in snapshots.documents) {
                    val amount = document.getString("amount") ?: "0"
                    val transactionDate = document.getString("date") ?: "N/A"
                    val description = document.getString("description") ?: "N/A"
                    val category = document.getString("category") ?: "N/A"
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val date = dateFormat.parse(transactionDate)

                    if (date != null) {
                        val calendar = Calendar.getInstance()
                        calendar.time = date
                        val expenseMonth = calendar.get(Calendar.MONTH)
                        val expenseYear = calendar.get(Calendar.YEAR)

                        // Check if the expense date matches the current month and year
                        if (expenseMonth == currentMonth && expenseYear == currentYear) {
                            expenseRecyclerList.add(
                                expensecard(
                                    "Amount Spent: $amount",
                                    "Transaction Date: $transactionDate",
                                    "Category: $category",
                                    "Description: $description"
                                )
                            )
                        }
                    }
                }

                Log.d("ActivityFragment", "RecyclerView updated with ${expenseRecyclerList.size} items")
                expenseAdapter.notifyDataSetChanged()
            }
    }

    private fun loadTotalExpense() {
        val userEmail = AppData.email
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)

        db.collection("user_expenses")
            .whereEqualTo("email", userEmail)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.e("ActivityFragment", "Listen failed.", error)
                    return@addSnapshotListener
                }

                var totalExpense = 0.0
                if (snapshots != null && !snapshots.isEmpty) {
                    for (document in snapshots.documents) {
                        val expenseString = document.getString("amount")
                        val transactionDate = document.getString("date") ?: "N/A"
                        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        val date = dateFormat.parse(transactionDate)
                        if (date != null) {
                            val calendar = Calendar.getInstance()
                            calendar.time = date
                            val expenseMonth = calendar.get(Calendar.MONTH)
                            val expenseYear = calendar.get(Calendar.YEAR)

                            if (expenseMonth == currentMonth && expenseYear == currentYear) {
                                val expense = expenseString?.toDoubleOrNull() ?: 0.0
                                totalExpense += expense
                            }
                        }
                    }
                }

                val formattedTotal = String.format("$%.2f", totalExpense)
                binding.spendSoFarText.text = "Amount Spent this Month\n$formattedTotal"
                Log.d("ActivityFragment", "Total Expense Updated: $formattedTotal")
            }
    }
}
