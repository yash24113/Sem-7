package com.example.mybyk

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class addareacrud : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var areaAdapter: AreaAdapter
    private lateinit var mainContent: LinearLayout
    private lateinit var bikeAnimationView: LottieAnimationView
    private val areaList = mutableListOf<Area>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_addareacrud)

       bikeAnimationView = findViewById(R.id.bikeAnimationView)
        mainContent = findViewById(R.id.mainContent)

        // Show loader initially
        bikeAnimationView.visibility = ProgressBar.VISIBLE
        mainContent.visibility = ConstraintLayout.GONE

        loadData()

        recyclerView = findViewById(R.id.rvAreaList)
        areaAdapter = AreaAdapter(areaList, object : AreaAdapter.OnItemClickListener {
            override fun onUpdateClick(area: Area) {
                showUpdateDialog(area)
            }

            override fun onDeleteClick(area: Area) {
                deleteArea(area.documentId)
            }
        }, showIcons = true)

        recyclerView.adapter = areaAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Fetch and display areas initially
        getAreas()

        // Add area button listener
        findViewById<Button>(R.id.btnAddArea).setOnClickListener {
            val areaName = findViewById<EditText>(R.id.etAreaName).text.toString()
            if (areaName.isNotEmpty()) {
                addArea(areaName)
            }
        }
    }

    private fun loadData() {
        // Simulate a delay for data loading
        mainContent.postDelayed({
            // Hide loader and show content after loading
            bikeAnimationView.visibility = ProgressBar.GONE
            mainContent.visibility = ConstraintLayout.VISIBLE
        }, 3000) // 2-second delay for demonstration purposes
    }

    private fun addArea(areaName: String) {
        val areasCollection = FirebaseFirestore.getInstance().collection("Areas")

        areasCollection.orderBy("areaId", Query.Direction.DESCENDING).limit(1).get()
            .addOnSuccessListener { documents ->
                var newAreaId = 1
                if (documents.size() > 0) {
                    val lastArea = documents.documents[0]
                    val maxId = lastArea.getLong("areaId")
                    if (maxId != null) {
                        newAreaId = maxId.toInt() + 1
                    }
                }
                val areaData = hashMapOf(
                    "areaId" to newAreaId,
                    "areaName" to areaName
                )
                areasCollection.add(areaData)
                    .addOnSuccessListener { documentReference ->
                        Log.d("Firestore", "Area added with ID: ${documentReference.id}")
                        Toast.makeText(this, "Area added successfully", Toast.LENGTH_SHORT).show()

                        // Clear the EditText field after adding the area
                        findViewById<EditText>(R.id.etAreaName).text.clear()

                        // Refresh the area list
                        getAreas()
                    }
                    .addOnFailureListener { e ->
                        Log.w("Firestore", "Error adding area", e)
                    }
            }
    }

    private fun getAreas() {
        val areasCollection = FirebaseFirestore.getInstance().collection("Areas")
        val query = areasCollection.orderBy("areaId", Query.Direction.ASCENDING)

        query.get()
            .addOnSuccessListener { documents ->
                areaList.clear()
                for (document in documents) {
                    val areaId = document.getLong("areaId")?.toInt() ?: 0
                    val areaName = document.getString("areaName") ?: ""
                    areaList.add(Area(document.id, areaId, areaName))
                }
                areaAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error getting areas", e)
            }
    }

    private fun updateArea(documentId: String, newAreaName: String) {
        val areasCollection = FirebaseFirestore.getInstance().collection("Areas")
        areasCollection.document(documentId).update("areaName", newAreaName)
            .addOnSuccessListener {
                Log.d("Firestore", "Area updated successfully")
                Toast.makeText(this, "Area updated successfully", Toast.LENGTH_SHORT).show()
                getAreas()
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error updating area", e)
            }
    }

    private fun deleteArea(documentId: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete Area")
        builder.setMessage("Are you sure you want to delete this area?")

        builder.setPositiveButton("Yes") { dialog, _ ->
            val areasCollection = FirebaseFirestore.getInstance().collection("Areas")
            areasCollection.document(documentId).delete()
                .addOnSuccessListener {
                    Log.d("Firestore", "Area deleted successfully")
                    Toast.makeText(this, "Area removed successfully", Toast.LENGTH_SHORT).show()
                    getAreas()
                }
                .addOnFailureListener { e ->
                    Log.w("Firestore", "Error deleting area", e)
                }
            dialog.dismiss()
        }

        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }

    private fun showUpdateDialog(area: Area) {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.dialog_update_area, null)
        val editText = dialogLayout.findViewById<EditText>(R.id.etUpdateAreaName)

        editText.setText(area.areaName)

        with(builder) {
            setTitle("Update Area")
            setPositiveButton("Update") { dialog, _ ->
                val newAreaName = editText.text.toString()
                updateArea(area.documentId, newAreaName)
            }
            setNegativeButton("Cancel") { dialog, _ ->
            }
            setView(dialogLayout)
            show()
        }
    }
}
