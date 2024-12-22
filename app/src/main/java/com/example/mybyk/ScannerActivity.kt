package com.example.mybyk

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class ScannerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanner)

        val documentId = intent.getStringExtra("documentId")
        if (documentId != null) {
            fetchCycleDetails(documentId)
        } else {
            Toast.makeText(this, "No document ID provided", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchCycleDetails(documentId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collectionGroup("Cycle")
            .whereEqualTo("documentId", documentId)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val document = documents.documents[0]
                    val cycleLockNumber = document.getString("CycleLockNumber") ?: ""
                    val areaId = document.getString("areaId") ?: ""

                    // Fetch area name
                    db.collection("Areas").document(areaId).get()
                        .addOnSuccessListener { areaDoc ->
                            val areaName = areaDoc.getString("areaName") ?: "Unknown"
                            displayCycleDetails(documentId, cycleLockNumber, areaName)
                        }
                        .addOnFailureListener { e ->
                            Log.w("Firestore", "Error fetching area name", e)
                            Toast.makeText(this, "Error fetching area name", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "Cycle not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error fetching cycle details", e)
                Toast.makeText(this, "Error fetching cycle details", Toast.LENGTH_SHORT).show()
            }
    }

    private fun displayCycleDetails(documentId: String, lockNumber: String, areaName: String) {
        val message = "Cycle ID: $documentId\nLock Number: $lockNumber\nArea Name: $areaName"
        AlertDialog.Builder(this)

            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
}
