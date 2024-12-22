package com.example.mybyk

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter

class CycleListActivity : AppCompatActivity() {

    private lateinit var qrIV: ImageView
    private lateinit var message: String
    private lateinit var areaName: String
    private lateinit var areaDocumentId: String
    private lateinit var recyclerView: RecyclerView
    private lateinit var cycleAdapter: CycleAdapter
    private lateinit var hub: ImageView
    private val cycleList = mutableListOf<Cycle>()

    private lateinit var auth: FirebaseAuth

    private lateinit var drawerLayout: DrawerLayout
    private val SCAN_REQUEST_CODE = 1001

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cycle_list)




        areaDocumentId = intent.getStringExtra("areaDocumentId").toString()
        getCyclesForArea(areaDocumentId)
        areaName = intent.getStringExtra("areaName").toString()

        auth = FirebaseAuth.getInstance()

        // Get the current user and phone number
        val user = auth.currentUser
        val phoneNumber = user?.phoneNumber

        // Inflate the custom layout (item_no_cycle)
       // val parentLayout = findViewById<LinearLayout>(R.id.linearLayoutSection) // The layout where you want to add item_no_cycle

        val view = LayoutInflater.from(this).inflate(R.layout.item_no_cycle, null, true)

// Now the hub ImageView is part of the activity layout
        hub = view.findViewById(R.id.hub)
        hub.setOnClickListener {
            Toast.makeText(this@CycleListActivity, "Navigating to Dashboard", Toast.LENGTH_SHORT).show()
            val intent = Intent(this@CycleListActivity, dashboard::class.java)
            startActivity(intent)
        }



        recyclerView = findViewById(R.id.rvCycleList)
        cycleAdapter = CycleAdapter(cycleList) { cycle ->
            // Handle scan button click
            val documentId = cycle.documentId

            if (documentId != null) {
                handleScannedData(documentId)
            } else {
                Toast.makeText(this, "No document ID provided", Toast.LENGTH_SHORT).show()
            }

        }




        recyclerView.adapter = cycleAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Set the mobile number and username in the navigation header
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        val headerView = navigationView.getHeaderView(0)
        val mobileTextView = headerView.findViewById<TextView>(R.id.nav_header_subtitle)
        val usernameTextView = headerView.findViewById<TextView>(R.id.nav_header_title)

        if (!phoneNumber.isNullOrEmpty()) {
            mobileTextView.text = phoneNumber
            // Fetch and display the username
            fetchUsername(phoneNumber, usernameTextView)
        } else {
           // Toast.makeText(this, "Phone number not available", Toast.LENGTH_SHORT).show()
        }

        val areaName = intent.getStringExtra("areaName")
        val areaNameTextView: TextView = findViewById(R.id.textView4)
        areaNameTextView.text = areaName

        val areaDocumentId = intent.getStringExtra("areaDocumentId") ?: return
        getCyclesForArea(areaDocumentId)

        val icon1: ImageView = findViewById(R.id.icon1)
        icon1.setOnClickListener {
            val p = PopupMenu(this, icon1)
            p.menuInflater.inflate(R.menu.homemenu, p.menu)
            p.setOnMenuItemClickListener { menuitem ->
                when (menuitem.itemId) {
                    R.id.action_logout -> {
                        showLogoutConfirmationDialog()
                        true
                    }
                    R.id.admin -> {
                        startActivity(Intent(this, AdminLogin::class.java))
                        true
                    }
                    else -> false
                }
            }
            p.show()
        }

        val menuIcon: ImageView = findViewById(R.id.menu1)
        drawerLayout = findViewById(R.id.drawer_layout)
        menuIcon.setOnClickListener {
            drawerLayout.openDrawer(findViewById<NavigationView>(R.id.nav_view))
        }
    }

    private fun fetchUsername(phoneNumber: String, usernameTextView: TextView) {
        val db = FirebaseFirestore.getInstance()
        db.collection("Users").whereEqualTo("phoneNumber", phoneNumber).get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val document = documents.documents[0]
                    val username = document.getString("username")
                    usernameTextView.text = username ?: "Username not found"
                }
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error fetching username", e)
                Toast.makeText(this, "Error fetching username", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getCyclesForArea(areaDocumentId: String) {
        val cyclesCollection = FirebaseFirestore.getInstance()
            .collection("Areas")
            .document(areaDocumentId)
            .collection("Cycle")

        cyclesCollection.get()
            .addOnSuccessListener { documents ->
                cycleList.clear()
                for (document in documents) {
                    val cycleLockNumber = document.getString("CycleLockNumber") ?: ""
                    cycleList.add(Cycle(document.id, cycleLockNumber))
                }
                cycleAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error getting cycles for areaDocumentId: $areaDocumentId", e)
            }
    }

    private fun showLogoutConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Are you sure you want to logout app?")
            .setPositiveButton("Yes") { _, _ ->
                auth.signOut()
                Toast.makeText(this, "Logout successfully", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, login::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
                finish()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
        builder.create().show()
    }
    private fun handleScannedData(scannedData: String) {
        //Toast.makeText(this, "Scanned Data: $scannedData", Toast.LENGTH_SHORT).show()

        val db = FirebaseFirestore.getInstance()

        val areaDocumentId = intent.getStringExtra("areaDocumentId") ?: return

        // Query the "Cycle" collection under the specific area document ID
        db.collection("Areas")
            .document(areaDocumentId) // Replace with the correct areaDocumentId if needed
            .collection("Cycle")
            .document(scannedData) // Querying the document directly using its ID (scannedData)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Log success message
                    // Toast.makeText(this, "Cycle document found for ID: $scannedData", Toast.LENGTH_LONG).show()

                    // Document exists, extract the data
                    val dd = document.getString("CycleLockNumber") ?: ""
                    // Toast.makeText(this, "Cycle Lock Number: $dd", Toast.LENGTH_LONG).show()

//                    val dd1 = document.getString("areaId") ?: ""
//                    Toast.makeText(this, "area Id: $dd1", Toast.LENGTH_LONG).show()

                    //                 val CycleLockNumber = document.getString("CycleLockNumber") ?: ""
                    //    val areaId = document.getString("areaId") ?: ""


                    // Fetch area name using areaId
                    db.collection("Areas").document(areaDocumentId).get()
                        .addOnSuccessListener { areaDoc ->
                            val areaName = areaDoc.getString("areaName") ?: "Unknown"
                            Log.d("CycleScanner", "Area Name: $areaName")
                            //  Toast.makeText(this, "Area Name is: $areaName", Toast.LENGTH_LONG).show()
                            // Call showCycleDetails only when data is ready
                            showCycleDetails(scannedData, dd, areaName)

                            // Redirect to dashboard activity after displaying details
                            //  redirectToDashboard()
                        }
                        .addOnFailureListener { e ->
                            Log.w("CycleScanner", "Error fetching area name", e)
                            Toast.makeText(this, "Error fetching area name", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    // Document does not exist
                    Log.w("CycleScanner", "Cycle document not found for ID: $scannedData")
                    Toast.makeText(this, "Cycle not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.w("CycleScanner", "Error fetching cycle details", e)
                Toast.makeText(this, "Error fetching cycle details", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showCycleDetails(documentId: String, cycleLockNumber: String, areaName: String) {
        // Inflate the custom layout
        val dialogView = layoutInflater.inflate(R.layout.dialog_cycle_details, null)


        // Set the details in the TextView
        val tvDetails = dialogView.findViewById<TextView>(R.id.tvCycleDetails)
        message = "Cycle ID: $documentId\nLock Number: $cycleLockNumber\nArea Name: $areaName\nStatus: Available\nOwner: Yash Khalas"
        tvDetails.text = message

        // Set the logo in ImageView if needed
        val imgLogo = dialogView.findViewById<ImageView>(R.id.imgCycleLogo)
        imgLogo.setImageResource(R.drawable.logo1) // Replace with the actual logo resource

        qrIV = dialogView.findViewById<ImageView>(R.id.idIVQrcode)
        generateQRCode(message)
        // Create and show the AlertDialog with the custom view
        AlertDialog.Builder(this)

            .setView(dialogView)
            .setPositiveButton("OK", null)
            .show()
    }


    private fun generateQRCode(text: String) {
        val qrCodeWriter = QRCodeWriter()
        try {
            val bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, 150, 200)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
                }
            }
            qrIV.setImageBitmap(bitmap)
            //Toast.makeText(this, "QR Code Generated Successfully", Toast.LENGTH_SHORT).show()
            // findViewById<EditText>(R.id.idEdt).text.clear()
        } catch (e: WriterException) {
            e.printStackTrace()
            Toast.makeText(this, "Error generating QR Code", Toast.LENGTH_SHORT).show()
        }
    }
}

