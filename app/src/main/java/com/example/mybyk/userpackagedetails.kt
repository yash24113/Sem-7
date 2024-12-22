package com.example.mybyk

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class userpackagedetails : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var packageAdapter: UserPackageAdapter
    private lateinit var packageList: MutableList<Package>  // Mutable list for packages
    private val firestore = FirebaseFirestore.getInstance()  // Firestore instance
    private val packageCollection = firestore.collection("Package")  // Firestore collection reference

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_userpackagedetails)

        // Get the passed areaName from CycleListActivity
        val areaName = intent.getStringExtra("areaName")

        recyclerView = findViewById(R.id.recyclerviewpackage)
        val b1 = findViewById<ImageView>(R.id.back)

        // Handle back button to return to CycleListActivity
        b1.setOnClickListener {
            val intent = Intent(this, CycleListActivity::class.java)
            intent.putExtra("areaName", areaName)  // Pass the area name back
            startActivity(intent)
        }

        // Initialize the package list and adapter
        packageList = mutableListOf()  // Initialize the list of packages
        packageAdapter = UserPackageAdapter(packageList)

        recyclerView.adapter = packageAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Fetch the list of packages from Firestore
        fetchPackages()
    }



    private fun fetchPackages() {
        packageCollection.get()
            .addOnSuccessListener { documents ->
                packageList.clear()  // Clear the list to avoid duplicates
                for (document in documents) {
                    Log.d("Firestore", "Document data: ${document.data}")  // Log document data
                    try {
                        val packageItem = document.toObject(Package::class.java)
                        packageList.add(packageItem)  // Add each package to the list
                    } catch (e: Exception) {
                        Log.e("Firestore", "Error deserializing package: ${e.message}")
                    }
                }
                packageAdapter.notifyDataSetChanged()  // Notify the adapter of data changes
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error fetching packages", e)
                Toast.makeText(this, "Failed to fetch packages", Toast.LENGTH_SHORT).show()
            }
    }

}
