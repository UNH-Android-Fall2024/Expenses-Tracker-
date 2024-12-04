package com.unh.expense_tracker.ui.account

import android.content.Intent 
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.unh.expense_tracker.AppData
import com.unh.expense_tracker.MainActivity
import com.unh.expense_tracker.databinding.FragmentAccountBinding

class AccountFragment : Fragment() {

    private var _binding: FragmentAccountBinding? = null

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.spentStatistics.setOnClickListener {
            findNavController().navigate(AccountFragmentDirections.actionNavigationAccountToExpenseStatistics())
        }


        binding.recurringExpenses.setOnClickListener {
            findNavController().navigate(AccountFragmentDirections.actionNavigationAccountToRecurringExpenseFragment())
        }


        binding.signOut.setOnClickListener {
            AppData.email = ""
            val intent = Intent(requireActivity(), MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            requireActivity().finish()
        }
    }

    private fun loadUserDetails() {
        val userEmail = AppData.email
        Log.d("name", "$userEmail")

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
