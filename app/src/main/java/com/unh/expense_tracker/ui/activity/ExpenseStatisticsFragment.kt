package com.unh.expense_tracker.ui.activity

import android.graphics.Color
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
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.unh.expense_tracker.AppData
import com.unh.expense_tracker.databinding.ExpenseStatisticsBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class ExpenseStatisticsFragment : Fragment() {

    private lateinit var binding: ExpenseStatisticsBinding
    private val db = Firebase.firestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ExpenseStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBarChart()
        fetchExpenseData()
//https://www.droidcon.com/2023/08/27/menuprovider-api-android/?
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
//Syntax taken from chatgpt
    private fun setupBarChart() {
        binding.barChart.apply {
            description.isEnabled = false
            setFitBars(true)
            animateY(1500)
            setExtraOffsets(0f, 0f, 0f, 30f)

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                textSize = 8f
                labelRotationAngle = -45f
                setLabelCount(8, false)
            }

            axisLeft.axisMinimum = 0f
            axisRight.isEnabled = false
            legend.isEnabled = false
        }
    }


    private fun fetchExpenseData() {
        val userEmail = AppData.email
        val categoryTotals = mutableMapOf<String, Float>()
        val allCategories = listOf(
            "House Rent", "House Utilities", "Food", "Trips",
            "Vehicle Expenses", "Groceries", "Shopping", "Miscellaneous"
        )

        allCategories.forEach { category -> categoryTotals[category] = 0f }


        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH) + 1
        val currentYear = calendar.get(Calendar.YEAR)

        db.collection("user_expenses")
            .whereEqualTo("email", userEmail)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.e("ExpenseStatisticsFragment", "Error fetching data", error)
                    return@addSnapshotListener
                }

                snapshots?.documents?.forEach { document ->
                    val dateStr = document.getString("date") ?: return@forEach
                    val category = document.getString("category") ?: return@forEach
                    val amount = document.getString("amount")?.toFloatOrNull() ?: 0f

                    val sdf = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
                    val date = sdf.parse(dateStr)

                    val expenseCalendar = Calendar.getInstance().apply { time = date }
                    val expenseMonth = expenseCalendar.get(Calendar.MONTH) + 1
                    val expenseYear = expenseCalendar.get(Calendar.YEAR)

                    if (expenseMonth == currentMonth && expenseYear == currentYear) {
                        categoryTotals[category] = categoryTotals.getOrDefault(category, 0f) + amount
                    }
                }

                updateBarChart(categoryTotals, allCategories)
            }
    }


    private fun updateBarChart(categoryTotals: Map<String, Float>, allCategories: List<String>) {
        val entries = allCategories.mapIndexed { index, category ->
            BarEntry(index.toFloat(), categoryTotals[category] ?: 0f)
        }
//https://www.truiton.com/2015/04/android-chart-example-mp-android-chart-library/
        val dataSet = BarDataSet(entries, "Expenses").apply {
            colors = listOf(
                Color.parseColor("#FF6384"),
                Color.parseColor("#36A2EB"),
                Color.parseColor("#FFCE56"),
                Color.parseColor("#4BC0C0"),
                Color.parseColor("#9966FF"),
                Color.parseColor("#FF9F40"),
                Color.parseColor("#FF6384"),
                Color.parseColor("#36A2EB")
            )
            setValueTextSize(10f)
            setDrawValues(true)
        }

        val barData = BarData(dataSet)
        barData.barWidth = 0.7f

        binding.barChart.apply {
            data = barData

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawLabels(true)
                labelRotationAngle = -45f
                setLabelCount(allCategories.size, false)
                valueFormatter = IndexAxisValueFormatter(allCategories)
            }

            axisLeft.axisMinimum = 0f
            axisRight.isEnabled = false
            legend.isEnabled = false

            setFitBars(true)
            invalidate()
        }
    }
}
