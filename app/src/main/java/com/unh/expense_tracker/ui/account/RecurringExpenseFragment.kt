package com.unh.expense_tracker.ui.account

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.app.NotificationCompat
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.unh.expense_tracker.AppData
import com.unh.expense_tracker.databinding.FragmentRecurringExpenseBinding
import java.text.SimpleDateFormat
import java.util.*

class RecurringExpenseFragment : Fragment() {
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
        loadRecurringExpensesFromFirebase()
    }

    private fun loadRecurringExpensesFromFirebase() {
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
                var nearestExpense: Pair<String, Int>? = null
                var earliestDueDate: Date? = null

                for (document in documents) {
                    val category = document.getString("category") ?: "N/A"
                    val amount = document.getString("amount") ?: "N/A"
                    val description = document.getString("description") ?: "N/A"
                    val lastSpentDate = document.getString("date") ?: "N/A"
                    val recurrencePeriod = document.getLong("recurrence_period")?.toInt() ?: 30

                    // Calculate next due date
                    val nextDueDate = calculateNextDueDate(lastSpentDate, recurrencePeriod)
                    if (nextDueDate != null) {
                        if (earliestDueDate == null || nextDueDate.before(earliestDueDate)) {
                            earliestDueDate = nextDueDate
                            val today = Calendar.getInstance().time
                            val daysUntilDue = ((nextDueDate.time - today.time) / (1000 * 60 * 60 * 24)).toInt()
                            nearestExpense = Pair(category, daysUntilDue)
                        }
                    }

                    expenseRecyclerList.add(
                        recurringexpensecard(
                            "Name: $category",
                            "Amount: $amount",
                            "Description: $description",
                            "Last Spent Date: $lastSpentDate"
                        )
                    )
                }

                // Notify user for the nearest due expense
                nearestExpense?.let {
                    sendNotification(it.first, it.second)
                }

                recurringExpenseAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.e("RecurringExpenseFragment", "Error fetching data", exception)
            }
    }

    private fun calculateNextDueDate(lastSpentDate: String, recurrencePeriod: Int): Date? {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return try {
            val date = dateFormat.parse(lastSpentDate)
            val calendar = Calendar.getInstance()
            calendar.time = date!!
            calendar.add(Calendar.DAY_OF_YEAR, recurrencePeriod) // Add recurrence period in days
            calendar.time
        } catch (e: Exception) {
            null
        }
    }

    private fun sendNotification(expenseName: String, daysUntilDue: Int) {
        val notificationManager =
            requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "recurring_expenses_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Recurring Expenses Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(requireContext(), channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Upcoming Expense Due")
            .setContentText("$expenseName is due in ${if (daysUntilDue == 0) "today" else "$daysUntilDue days"}.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(expenseName.hashCode(), notification)
    }
}
