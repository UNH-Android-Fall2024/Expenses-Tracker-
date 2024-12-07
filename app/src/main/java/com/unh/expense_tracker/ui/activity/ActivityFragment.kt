package com.unh.expense_tracker.ui.activity

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
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
import com.unh.expense_tracker.R
import java.util.jar.Manifest

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
        binding.btnTransactions.setOnClickListener {
            findNavController().navigate(ActivityFragmentDirections.actionNavigationActivityToExpenseStatistics())
        }
        binding.btnLimit.setOnClickListener {
            findNavController().navigate(ActivityFragmentDirections.actionNavigationActivityToSetExpenseFragment())
        }
        binding.btnGoal.setOnClickListener{
            findNavController().navigate(ActivityFragmentDirections.actionNavigationActivityToSetGoalFragment())
        }

        //loadTotalExpense()
        //loadExpenseDataFromFirebase()
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
    fun deleteExpense(expenseitem :expensecard){
        val userEmail = AppData.email
        //Log.d("deleteexp","inside deleteexpense function")
        db.collection("user_expenses")
            .whereEqualTo("email", userEmail)
            .whereEqualTo("amount", expenseitem.text1.removePrefix("Amount Spent: "))
            .whereEqualTo("category", expenseitem.text3.removePrefix("Category: "))
            .whereEqualTo("description", expenseitem.text4.removePrefix("Description: "))
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    db.collection("user_expenses").document(document.id).delete()
                        .addOnSuccessListener {
                            Toast.makeText(context, "Expense Deleted Successfully.", Toast.LENGTH_SHORT).show()
                            loadExpenseDataFromFirebase()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
    private fun loadTotalExpense() {
        val userEmail = AppData.email
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)

        db.collection("expense_limit")
            .whereEqualTo("email", userEmail)
            .get()
            .addOnSuccessListener { querySnapshot ->
                var monthlyLimit: Double? = null

                if (!querySnapshot.isEmpty) {
                    val document = querySnapshot.documents[0]
                    val monthlyLimitString = document.getString("monthlyLimit") ?: "0"
                    monthlyLimit = monthlyLimitString.toDoubleOrNull()
                }

                db.collection("user_expenses")
                    .whereEqualTo("email", userEmail)
                    .addSnapshotListener { snapshots, error ->
                        if (error != null) {
                            Log.e("ActivityFragment", "Error fetching user expenses.", error)
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

                        if (monthlyLimit != null && totalExpense > monthlyLimit) {
                            showExpenseLimitExceededNotification()
                        }
                    }
            }
            .addOnFailureListener { exception ->
                Log.e("ActivityFragment", "Failed to fetch expense limit.", exception)
            }
    }

    private fun showExpenseLimitExceededNotification(){
        createNotificationChannel()

        val intent = Intent(requireContext(), requireActivity()::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(requireContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE)
        var builder = NotificationCompat.Builder(requireContext(), "CHANNEL_ID")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle("Limit Exceeded")
            .setContentText("Your expenses has crossed the limit")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(requireContext())) {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                // ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                // public fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                //                                        grantResults: IntArray)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.

                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
            // notificationId is a unique int for each notification that you must define.
            notify(101, builder.build())
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.w("test", "$requestCode $resultCode")
    }
    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is not in the Support Library.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = ("channel_name")
            val descriptionText ="Desc"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("CHANNEL_ID", name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system.
            val notificationManager: NotificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

}