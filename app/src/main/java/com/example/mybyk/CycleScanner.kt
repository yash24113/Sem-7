package com.example.mybyk

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter

class CycleScanner : AppCompatActivity() {
    private lateinit var qrIV: ImageView
    private lateinit var message: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cycle_scanner)


        val documentId = intent.getStringExtra("documentId")
        if (documentId != null) {
            handleScannedData(documentId)
        } else {
            Toast.makeText(this, "No document ID provided", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleScannedData(scannedData: String) {
        //Toast.makeText(this, "Scanned Data: $scannedData", Toast.LENGTH_SHORT).show()

        val db = FirebaseFirestore.getInstance()

        // Query the "Cycle" collection under the specific area document ID
        db.collection("Areas")
            .document("ecrNeC92rpjto49WF3s4") // Replace with the correct areaDocumentId if needed
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
                    db.collection("Areas").document("ecrNeC92rpjto49WF3s4").get()
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
