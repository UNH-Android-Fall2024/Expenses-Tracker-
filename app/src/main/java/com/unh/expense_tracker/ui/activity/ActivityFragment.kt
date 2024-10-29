package com.unh.expense_tracker.ui.activity

import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.unh.expense_tracker.databinding.FragmentActivityBinding
import java.util.Locale
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class ActivityFragment : Fragment() {

    private lateinit var binding: FragmentActivityBinding
    private val db = Firebase.firestore

    // This property is only valid between onCreateView and
    // onDestroyView.
//    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val activityViewModel =
            ViewModelProvider(this).get(ActivityViewModel::class.java)

        binding = FragmentActivityBinding.inflate(inflater, container, false)
        val root: View = binding.root

        //val textView: TextView = binding.textTitle
        activityViewModel.text.observe(viewLifecycleOwner) {
         //   textView.text = it
          //  Log.d("test", it)
        }
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentDate = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        val formattedDate = "Today, ${dateFormat.format(currentDate)}"

        loadTotalExpense()

        binding.textCurrentDate.text = formattedDate
        binding.fabAdd.setOnClickListener {
           findNavController().navigate(ActivityFragmentDirections.actionNavigationActivityToAddExpenseFragment())
        }
    }

    private fun loadTotalExpense() {
        val userEmail = "h@gmail.com"
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
                        Log.w("test", "${expense}")
                        totalExpense += expense
                    }
                }
                Log.w("test", "total - ${totalExpense}")
                val formattedTotal = String.format("$%.2f", totalExpense)
                Log.w("test", "formatted total - $formattedTotal")
                binding.spendSoFarText.text = "Amount Spent this Month\n$formattedTotal"
            }
    }


//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
}