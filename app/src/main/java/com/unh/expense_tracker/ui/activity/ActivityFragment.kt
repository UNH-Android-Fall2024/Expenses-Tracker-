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


class ActivityFragment : Fragment() {

    private var _binding: FragmentActivityBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val activityViewModel =
            ViewModelProvider(this).get(ActivityViewModel::class.java)

        _binding = FragmentActivityBinding.inflate(inflater, container, false)
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

        // Current Date and Day
        val currentDate = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        val formattedDate = "Today, ${dateFormat.format(currentDate)}"


        binding.textCurrentDate.text = formattedDate
        binding.fabAdd.setOnClickListener {
           findNavController().navigate(ActivityFragmentDirections.actionNavigationActivityToAddExpenseFragment())
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}