package com.example.mybyk

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class PackageDetails : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var packageAdapter: PackageAdapter

    private val packageList = mutableListOf<Package>()
    private val firestore = FirebaseFirestore.getInstance()
    private val packageCollection = firestore.collection("Package")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_package_details)

        recyclerView = findViewById(R.id.rvPackageList)

        // Initialize the PackageAdapter with delete and edit listeners
        packageAdapter = PackageAdapter(packageList, object : PackageAdapter.OnItemClickListener {
            override fun onDeleteClick(packageItem: Package) {
                deletePackage(packageItem)
            }

            override fun onEditClick(packageItem: Package) {
                showAddPackageDialog(packageItem)
            }
        })

        recyclerView.adapter = packageAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Add package button listener to show the dialog for adding a new package
        findViewById<Button>(R.id.btnAddPackage).setOnClickListener {
            showAddPackageDialog(null) // Passing null means it's a new package
        }

        // Fetch and display packages initially
        fetchPackages()
    }

    // Modified showAddPackageDialog function to handle both add and edit actions
    @SuppressLint("MissingInflatedId")
    private fun showAddPackageDialog(packageItem: Package?) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_package, null)

        // Pre-fill fields if packageItem is not null (i.e., we're editing)
        val etPackageId = dialogView.findViewById<EditText>(R.id.etDialogPackageId)
        val etPackageName = dialogView.findViewById<EditText>(R.id.etDialogPackageName)
        val etPackageDesc = dialogView.findViewById<EditText>(R.id.etDialogPackageDesc)
        val etPackagePrice = dialogView.findViewById<EditText>(R.id.etDialogPackagePrice)
        val etPackagePrice1 = dialogView.findViewById<EditText>(R.id.etDialogPackagePrice1)
        val etPackageValidity = dialogView.findViewById<EditText>(R.id.etDialogPackageValidity)

        if (packageItem != null) {
            etPackageId.setText(packageItem.packageId)
            etPackageName.setText(packageItem.packageName)
            etPackageDesc.setText(packageItem.packageDesc)
            etPackagePrice.setText(packageItem.packagePrice.toString())
            etPackagePrice1.setText(packageItem.packagePrice1.toString())
            etPackageValidity.setText(packageItem.packageValidity)
            etPackageId.isEnabled = false // Don't allow editing the package ID
        }

        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle(if (packageItem == null) "Add Cycle Package" else "Edit Cycle Package")
            .setPositiveButton(if (packageItem == null) "Save" else "Update") { dialog, _ ->
                val packageId = etPackageId.text.toString()
                val packageName = etPackageName.text.toString()
                val packageDesc = etPackageDesc.text.toString()
               // val packagePrice = etPackagePrice.text.toString()
                val packageValidity = etPackageValidity.text.toString()
                val packagePrice = etPackagePrice.text.toString().toDoubleOrNull() // Handle parsing
                val packagePrice1 = etPackagePrice1.text.toString().toDoubleOrNull() // Handle parsing
                if (packageId.isNotEmpty() && packageName.isNotEmpty() && packagePrice != null && packagePrice1 != null) {
                    if (packageItem == null) {
                        // Adding a new package
                        addPackage(packageId, packageName, packageDesc, packagePrice, packagePrice1,packageValidity)
                    } else {
                        // Updating an existing package
                        updatePackage(packageId, packageName, packageDesc, packagePrice,packagePrice1, packageValidity)
                    }
                } else {
                    Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }

        dialogBuilder.create().show()
    }

    private fun addPackage(packageId: String, packageName: String, packageDesc: String, packagePrice: Double, packagePrice1: Double,packageValidity: String) {
        val packageItem = Package(packageId, packageName, packageDesc, packagePrice,packagePrice1, packageValidity)

        packageCollection.document(packageId).set(packageItem)
            .addOnSuccessListener {
                Log.d("Firestore", "Package added successfully")
                Toast.makeText(this, "Package added successfully", Toast.LENGTH_SHORT).show()
                fetchPackages()
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error adding package", e)
                Toast.makeText(this, "Failed to add package", Toast.LENGTH_SHORT).show()
            }
    }

    // New function to update package details
    private fun updatePackage(packageId: String, packageName: String, packageDesc: String, packagePrice: Double, packagePrice1: Double,packageValidity: String) {
        val updatedPackage = Package(packageId, packageName, packageDesc, packagePrice, packagePrice1,packageValidity)

        packageCollection.document(packageId).set(updatedPackage)
            .addOnSuccessListener {
                Log.d("Firestore", "Package updated successfully")
                Toast.makeText(this, "Package updated successfully", Toast.LENGTH_SHORT).show()
                fetchPackages()
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error updating package", e)
                Toast.makeText(this, "Failed to update package", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchPackages() {
        packageCollection.get()
            .addOnSuccessListener { documents ->
                packageList.clear()
                for (document in documents) {
                    val packageItem = document.toObject(Package::class.java)
                    packageList.add(packageItem)
                }
                packageAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error fetching packages", e)
                Toast.makeText(this, "Failed to fetch packages", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deletePackage(packageItem: Package) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete Package")
        builder.setMessage("Are you sure you want to delete this package?")

        builder.setPositiveButton("Yes") { dialog, _ ->
            packageCollection.document(packageItem.packageId).delete()
                .addOnSuccessListener {
                    Log.d("Firestore", "Package deleted successfully")
                    Toast.makeText(this, "Package deleted successfully", Toast.LENGTH_SHORT).show()
                    fetchPackages()
                }
                .addOnFailureListener { e ->
                    Log.w("Firestore", "Error deleting package", e)
                    Toast.makeText(this, "Failed to delete package", Toast.LENGTH_SHORT).show()
                }
            dialog.dismiss()
        }

        builder.setNegativeButton("No") { dialog, _ -> dialog.dismiss() }

        builder.create().show()
    }
}
