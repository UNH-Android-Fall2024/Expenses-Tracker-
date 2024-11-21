package com.unh.expense_tracker.ui.activity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.unh.expense_tracker.AppData
import com.unh.expense_tracker.R
import com.unh.expense_tracker.databinding.SetGoalsBinding

class SetGoalFragment : Fragment() {

    private lateinit var binding: SetGoalsBinding
    private val db = Firebase.firestore
    private var goal1DocumentId: String? = null
    private var goal2DocumentId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SetGoalsBinding.inflate(inflater, container, false)
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

        checkIfDataExists(1)
        checkIfDataExists(2)

        setupFieldListeners()

        binding.btnSetGoal1.setOnClickListener {
            saveGoalToFirebase(1)
        }

        binding.btnEditGoal1.setOnClickListener {
            enableFields(1)
        }

        binding.btnDeleteGoal1.setOnClickListener {
            deleteGoalFromFirebase(1)
        }

        binding.btnSetGoal2.setOnClickListener {
            saveGoalToFirebase(2)
        }

        binding.btnEditGoal2.setOnClickListener {
            enableFields(2)
        }

        binding.btnDeleteGoal2.setOnClickListener {
            deleteGoalFromFirebase(2)
        }
    }

    private fun checkIfDataExists(goalNumber: Int) {
        val email = AppData.email
        val collectionName = if (goalNumber == 1) "Goalsetting1" else "Goalsetting2"

        db.collection(collectionName)
            .whereEqualTo("email", email)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Toast.makeText(
                        requireContext(),
                        "Failed to listen for data: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@addSnapshotListener
                }

                if (snapshots != null && !snapshots.isEmpty) {
                    val document = snapshots.documents[0]
                    if (goalNumber == 1) {
                        goal1DocumentId = document.id
                        binding.inputGoalName1.setText(document.getString("goalName") ?: "")
                        binding.inputTotalAmount1.setText(document.getString("totalAmount") ?: "")
                        binding.inputAmountSaved1.setText(document.getString("amountSaved") ?: "")
                        binding.inputRemainingAmount1.setText(document.getString("remainingAmount") ?: "")
                        disableFields(1)
                    } else {
                        goal2DocumentId = document.id
                        binding.inputGoalName2.setText(document.getString("goalName") ?: "")
                        binding.inputTotalAmount2.setText(document.getString("totalAmount") ?: "")
                        binding.inputAmountSaved2.setText(document.getString("amountSaved") ?: "")
                        binding.inputRemainingAmount2.setText(document.getString("remainingAmount") ?: "")
                        disableFields(2)
                    }
                } else {
                    if (goalNumber == 1) goal1DocumentId = null else goal2DocumentId = null
                    enableFields(goalNumber)
                }
            }
    }

    private fun saveGoalToFirebase(goalNumber: Int) {
        val email = AppData.email ?: return
        val collectionName = if (goalNumber == 1) "Goalsetting1" else "Goalsetting2"

        val goalName = if (goalNumber == 1) binding.inputGoalName1.text.toString()
        else binding.inputGoalName2.text.toString()
        val totalAmount = if (goalNumber == 1) binding.inputTotalAmount1.text.toString().toDoubleOrNull()
        else binding.inputTotalAmount2.text.toString().toDoubleOrNull()
        val amountSaved = if (goalNumber == 1) binding.inputAmountSaved1.text.toString().toDoubleOrNull()
        else binding.inputAmountSaved2.text.toString().toDoubleOrNull()

        if (goalName.isEmpty() || totalAmount == null || amountSaved == null) {
            Toast.makeText(requireContext(), "All fields are required!", Toast.LENGTH_SHORT).show()
            return
        }

        val remainingAmount = totalAmount - amountSaved

        val goalData = hashMapOf(
            "email" to email,
            "goalName" to goalName,
            "totalAmount" to totalAmount.toString(),
            "amountSaved" to amountSaved.toString(),
            "remainingAmount" to remainingAmount.toString()
        )

        val documentId = if (goalNumber == 1) goal1DocumentId else goal2DocumentId

        if (documentId != null) {
            db.collection(collectionName).document(documentId)
                .set(goalData)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Goal $goalNumber updated successfully!", Toast.LENGTH_SHORT).show()
                    disableFields(goalNumber)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Failed to update Goal $goalNumber: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            db.collection(collectionName)
                .add(goalData)
                .addOnSuccessListener { doc ->
                    Toast.makeText(requireContext(), "Goal $goalNumber saved successfully!", Toast.LENGTH_SHORT).show()
                    if (goalNumber == 1) goal1DocumentId = doc.id else goal2DocumentId = doc.id
                    disableFields(goalNumber)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Failed to save Goal $goalNumber: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun deleteGoalFromFirebase(goalNumber: Int) {
        val collectionName = if (goalNumber == 1) "Goalsetting1" else "Goalsetting2"
        val documentId = if (goalNumber == 1) goal1DocumentId else goal2DocumentId

        if (documentId != null) {
            db.collection(collectionName).document(documentId)
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Goal $goalNumber deleted successfully!", Toast.LENGTH_SHORT).show()
                    clearFields(goalNumber)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Failed to delete Goal $goalNumber: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(requireContext(), "No data to delete for Goal $goalNumber!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun clearFields(goalNumber: Int) {
        if (goalNumber == 1) {
            binding.inputGoalName1.text.clear()
            binding.inputTotalAmount1.text.clear()
            binding.inputAmountSaved1.text.clear()
            binding.inputRemainingAmount1.text.clear()
            enableFields(1)
            goal1DocumentId = null
        } else {
            binding.inputGoalName2.text.clear()
            binding.inputTotalAmount2.text.clear()
            binding.inputAmountSaved2.text.clear()
            binding.inputRemainingAmount2.text.clear()
            enableFields(2)
            goal2DocumentId = null
        }
    }

    private fun enableFields(goalNumber: Int) {
        if (goalNumber == 1) {
            binding.inputGoalName1.isEnabled = true
            binding.inputTotalAmount1.isEnabled = true
            binding.inputAmountSaved1.isEnabled = true
            binding.inputRemainingAmount1.isEnabled = false
        } else {
            binding.inputGoalName2.isEnabled = true
            binding.inputTotalAmount2.isEnabled = true
            binding.inputAmountSaved2.isEnabled = true
            binding.inputRemainingAmount2.isEnabled = false
        }
    }

    private fun disableFields(goalNumber: Int) {
        if (goalNumber == 1) {
            binding.inputGoalName1.isEnabled = false
            binding.inputTotalAmount1.isEnabled = false
            binding.inputAmountSaved1.isEnabled = false
            binding.inputRemainingAmount1.isEnabled = false
        } else {
            binding.inputGoalName2.isEnabled = false
            binding.inputTotalAmount2.isEnabled = false
            binding.inputAmountSaved2.isEnabled = false
            binding.inputRemainingAmount2.isEnabled = false
        }
    }

    private fun setupFieldListeners() {
        setupFieldListener(1)
        setupFieldListener(2)
    }

    private fun setupFieldListener(goalNumber: Int) {
        val totalAmountField = if (goalNumber == 1) binding.inputTotalAmount1 else binding.inputTotalAmount2
        val amountSavedField = if (goalNumber == 1) binding.inputAmountSaved1 else binding.inputAmountSaved2
        totalAmountField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateRemainingAmount(goalNumber)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        amountSavedField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateRemainingAmount(goalNumber)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun updateRemainingAmount(goalNumber: Int) {
        val totalAmount = if (goalNumber == 1) binding.inputTotalAmount1.text.toString().toDoubleOrNull()
        else binding.inputTotalAmount2.text.toString().toDoubleOrNull()
        val amountSaved = if (goalNumber == 1) binding.inputAmountSaved1.text.toString().toDoubleOrNull()
        else binding.inputAmountSaved2.text.toString().toDoubleOrNull()

        val remainingAmount = if (totalAmount != null && amountSaved != null) {
            totalAmount - amountSaved
        } else {
            null
        }

        if (goalNumber == 1) {
            binding.inputRemainingAmount1.setText(remainingAmount?.toString() ?: "")
        } else {
            binding.inputRemainingAmount2.setText(remainingAmount?.toString() ?: "")
        }
    }
}
