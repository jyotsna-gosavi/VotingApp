package com.example.votingapp

import android.content.Intent
import android.os.Bundle
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.votingapp.databinding.ActivityVotingBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore

class VotingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVotingBinding
    private val database = FirebaseDatabase.getInstance().reference
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityVotingBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        binding.voteButton.setOnClickListener {
            val selectedRadioButtonId = binding.candidateRadioGroup.checkedRadioButtonId
            if (selectedRadioButtonId == -1) {
                Toast.makeText(this, "Please select a candidate.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            val selectedRadioButton = findViewById<RadioButton>(selectedRadioButtonId)
            val selectedCandidate = selectedRadioButton.text.toString()
            val userId = auth.currentUser?.uid ?: return@setOnClickListener

            // Check if the user has already voted
            firestore.collection("votes").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        // User has already voted
                        Toast.makeText(this, "You have already voted.", Toast.LENGTH_SHORT).show()
                    } else {
                        // User has not voted, allow voting
                        val voteData = hashMapOf(
                            "candidate" to selectedCandidate
                        )

                        firestore.collection("votes").document(userId).set(voteData)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Vote cast successfully.", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { exception ->
                                Toast.makeText(this, "Failed to cast vote: $exception", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Error checking vote status: $exception", Toast.LENGTH_SHORT).show()
                }
        }

        // Logout Button

        binding.logoutButton.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    }
