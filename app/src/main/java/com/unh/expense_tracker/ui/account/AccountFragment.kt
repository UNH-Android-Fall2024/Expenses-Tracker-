package com.unh.expense_tracker.ui.account

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.unh.expense_tracker.AppData
import com.unh.expense_tracker.databinding.FragmentAccountBinding


class AccountFragment : Fragment() {

    private var _binding: FragmentAccountBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val db = Firebase.firestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val accountViewModel =
            ViewModelProvider(this).get(AccountViewModel::class.java)

        _binding = FragmentAccountBinding.inflate(inflater, container, false)
        val root: View = binding.root
        loadUserDetails()
        return root
    }

    private fun loadUserDetails() {

        val userEmail = AppData.email
        Log.d("name","$userEmail")

        db.collection("App_UsersCredentials")
            .whereEqualTo("email", userEmail)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    for (document in documents.documents) {
                        val firstName = document.getString("firstName") ?: ""
                        Log.d("name", firstName)
                        val lastName = document.getString("lastName") ?: ""
                        val fullName = "$firstName $lastName"
                        Log.d("name", fullName)
                        binding.name.text = fullName
                        break
                    }
                } else {
                    Log.e("AccountFragment", "No user found with email: $userEmail")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("AccountFragment", "Error fetching user details", exception)
            }
    }

}