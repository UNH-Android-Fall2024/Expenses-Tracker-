package com.unh.expense_tracker.ui.activity

import android.app.DatePickerDialog
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.unh.expense_tracker.R
import java.util.Locale

class AddExpenseFragment : Fragment() {
    private lateinit var tvSelectedDate: TextView
    private lateinit var ivCalendar: ImageView

    private val addExpenseViewModel: AddExpenseViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_addexpense, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tvSelectedDate = view.findViewById(R.id.tvSelectedDate)
        ivCalendar = view.findViewById(R.id.ivCalendar)
        ivCalendar.setOnClickListener {
            showDatePickerDialog()
        }
        addExpenseViewModel.selectedDate.observe(viewLifecycleOwner, Observer { date ->
            tvSelectedDate.text = date
        })

    }
    private fun showDatePickerDialog(){
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

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

        datePickerDialog.show()
    }
}