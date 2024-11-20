package com.unh.expense_tracker.ui.account

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.unh.expense_tracker.AppData
import com.unh.expense_tracker.databinding.FragmentRecurringExpenseBinding

class RecurringExpenseFragment :Fragment() {
    private lateinit var binding: FragmentRecurringExpenseBinding
    private val db = Firebase.firestore
    private lateinit var mRecyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRecurringExpenseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {}
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
    override fun onResume() {
        super.onResume()
        loadrecuexpfromfirebase()
    }

    private fun loadrecuexpfromfirebase() {
        val userEmail = AppData.email
        val expenseRecyclerList: ArrayList<recurringexpensecard> = arrayListOf()

        mRecyclerView = binding.expenseRecurring
        mRecyclerView.setHasFixedSize(true)
        mRecyclerView.layoutManager = LinearLayoutManager(context)
        val recurringExpenseAdapter = RecurringExpenseAdapter(expenseRecyclerList, this)
        mRecyclerView.adapter = recurringExpenseAdapter

        db.collection("user_expenses")
            .whereEqualTo("email", userEmail)
            .whereEqualTo("recurring", "Yes")
            .get()
            .addOnSuccessListener { documents ->
                expenseRecyclerList.clear()
                for (document in documents) {
                    val category = document.getString("category") ?: "N/A"
                    val amount = document.getString("amount") ?: "N/A"
                    val description = document.getString("description") ?: "N/A"
                    val date = document.getString("date") ?: "N/A"
                    expenseRecyclerList.add(
                        recurringexpensecard(
                            "Name: $category",
                            "Amount: $amount",
                            "Description: $description",
                            "Last Spent Date: $date"
                        )
                    )
                }
                recurringExpenseAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.e("RecurringExpenseFragment", "Error fetching data", exception)
            }

    }
}
