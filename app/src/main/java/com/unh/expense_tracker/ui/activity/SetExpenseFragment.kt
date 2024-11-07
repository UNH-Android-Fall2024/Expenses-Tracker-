package com.unh.expense_tracker.ui.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.unh.expense_tracker.databinding.SetExpenseBinding


class SetExpenseFragment : Fragment() {

    private lateinit var binding: SetExpenseBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SetExpenseBinding.inflate(inflater, container, false)
        return binding.root
    }
    }