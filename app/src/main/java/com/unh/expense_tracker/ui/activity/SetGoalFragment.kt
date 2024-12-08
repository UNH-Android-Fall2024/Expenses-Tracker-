package com.unh.expense_tracker.ui.activity

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
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
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
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
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // abstract method
            }

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
                    Toast.makeText(requireContext(), "Failed to listen for data: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshots != null && !snapshots.isEmpty) {
                    val document = snapshots.documents[0]
                    val goalName = document.getString("goalName") ?: "Goal $goalNumber"
                    if (goalNumber == 1) {
                        goal1DocumentId = document.id
                        binding.inputGoalName1.setText(goalName)
                        binding.inputTotalAmount1.setText(document.getString("totalAmount") ?: "")
                        binding.inputAmountSaved1.setText(document.getString("amountSaved") ?: "")
                        binding.inputRemainingAmount1.setText(document.getString("remainingAmount") ?: "")
                        disableFields(1)
                    } else {
                        goal2DocumentId = document.id
                        binding.inputGoalName2.setText(goalName)
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
                    Toast.makeText(requireContext(), "Goal '$goalName' updated successfully!", Toast.LENGTH_SHORT).show()
                    disableFields(goalNumber)
                    if (remainingAmount == 0.0) {
                        notifyGoalAchieved(goalName)
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Failed to update Goal '$goalName': ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            db.collection(collectionName)
                .add(goalData)
                .addOnSuccessListener { doc ->
                    Toast.makeText(requireContext(), "Goal '$goalName' saved successfully!", Toast.LENGTH_SHORT).show()
                    if (goalNumber == 1) goal1DocumentId = doc.id else goal2DocumentId = doc.id
                    disableFields(goalNumber)
                    if (remainingAmount == 0.0) {
                        notifyGoalAchieved(goalName)
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Failed to save Goal '$goalName': ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }


    private fun deleteGoalFromFirebase(goalNumber: Int) {
        val collectionName = if (goalNumber == 1) "Goalsetting1" else "Goalsetting2"
        val documentId = if (goalNumber == 1) goal1DocumentId else goal2DocumentId

        val goalName = if (goalNumber == 1) binding.inputGoalName1.text.toString() else binding.inputGoalName2.text.toString()

        if (documentId != null) {
            db.collection(collectionName).document(documentId)
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Goal '$goalName' deleted successfully!", Toast.LENGTH_SHORT).show()
                    clearFields(goalNumber)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Failed to delete Goal '$goalName': ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(requireContext(), "No data to delete for Goal '$goalName'!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun notifyGoalAchieved(goalName: String) {
        createNotificationChannel()

        val intent = Intent(requireContext(), requireActivity()::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(requireContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val builder = NotificationCompat.Builder(requireContext(), "CHANNEL_ID")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle("Hurray!!")
            .setContentText("You have reached your goal: '$goalName'!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(requireContext())) {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
                return
            }
            notify(102, builder.build())
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Goal Notification"
            val descriptionText = "Notifications for goal achievements"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("CHANNEL_ID", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
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

    private fun updateRemainingAmount(goalNumber: Int) {
        val totalAmount = if (goalNumber == 1) binding.inputTotalAmount1.text.toString().toDoubleOrNull()
        else binding.inputTotalAmount2.text.toString().toDoubleOrNull()
        val amountSaved = if (goalNumber == 1) binding.inputAmountSaved1.text.toString().toDoubleOrNull()
        else binding.inputAmountSaved2.text.toString().toDoubleOrNull()

        if (totalAmount == null || amountSaved == null) {
            // Clear remaining amount if either field is empty or invalid
            if (goalNumber == 1) {
                binding.inputRemainingAmount1.setText("")
            } else {
                binding.inputRemainingAmount2.setText("")
            }
            return
        }

        if (amountSaved > totalAmount) {
            Toast.makeText(requireContext(), "Amount Saved cannot be greater than Total Amount!", Toast.LENGTH_SHORT).show()
            if (goalNumber == 1) {
                binding.inputAmountSaved1.setText(totalAmount.toString())
            } else {
                binding.inputAmountSaved2.setText(totalAmount.toString())
            }
            return
        }

        val remainingAmount = totalAmount - amountSaved

        if (goalNumber == 1) {
            binding.inputRemainingAmount1.setText(remainingAmount.toString())
        } else {
            binding.inputRemainingAmount2.setText(remainingAmount.toString())
        }

        if (remainingAmount == 0.0) {
            val goalName = if (goalNumber == 1) binding.inputGoalName1.text.toString() else binding.inputGoalName2.text.toString()
            notifyGoalAchieved(goalName)
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
}
