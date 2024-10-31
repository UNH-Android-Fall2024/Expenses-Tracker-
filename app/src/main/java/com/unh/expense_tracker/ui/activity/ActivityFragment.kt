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
                }
            }

        binding.fabAdd.setOnClickListener {
            findNavController().navigate(ActivityFragmentDirections.actionNavigationActivityToAddExpenseFragment())
        }

        loadTotalExpense()

        val ExpenseRecyclerList : ArrayList<expensecard> = arrayListOf()
        for (expenses in expenselist){
            ExpenseRecyclerList.add(
                expensecard(
                    "Amount Spent: "+expenses.amount,
                    "Transaction Date: "+expenses.Transactiondate,
                    "Category: "+expenses.category,
                    "Description: "+expenses.desc
                )
            )
        }
        mRecyclerView=binding.recyclerExpenseChild
        mRecyclerView.setHasFixedSize(true)
        mRecyclerView.layoutManager=LinearLayoutManager(context)
        mRecyclerView.adapter=ExpenseAdapter(ExpenseRecyclerList,this)
    }

    override fun onResume() {
        super.onResume()
        loadTotalExpense()
    }

    private fun loadTotalExpense() {
        val userEmail = AppData.email
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
                        val expense = expenseString?.toDoubleOrNull() ?: 0.0
                        totalExpense += expense
                    }
                }

                val formattedTotal = String.format("$%.2f", totalExpense)
                binding.spendSoFarText.text = "Amount Spent this Month\n$formattedTotal"
                Log.d("ActivityFragment", "Total Expense Updated: $formattedTotal")
            }
    }
}
