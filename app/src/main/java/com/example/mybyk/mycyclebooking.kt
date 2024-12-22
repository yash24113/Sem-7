package com.example.mybyk

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class mycyclebooking : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var phonenumber:String
    private lateinit var phonenumber1:TextView
    private lateinit var paymentid: String
    private lateinit var paymentid1: TextView

    private lateinit var amount: String
    private lateinit var amount1: TextView
    private lateinit var back: ImageView
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mycyclebooking)



        phonenumber = intent.getStringExtra("phoneNumber").toString()
        phonenumber1 = findViewById(R.id.tvUserName)
        phonenumber1.text = phonenumber


        paymentid = intent.getStringExtra("paymentid").toString()
        paymentid1 = findViewById(R.id.tvPaymentId)
        paymentid1.text = paymentid


        val netTotalAmountInPaise = intent.getIntExtra("netTotalAmount", 0) // Default value is 0


        val netTotalAmountInRupees = netTotalAmountInPaise / 100.0
        amount1 = findViewById(R.id.amount)
        amount1.text = netTotalAmountInRupees.toString()



        back = findViewById(R.id.backhome)
        back.setOnClickListener {
            auth = FirebaseAuth.getInstance()
            val currentUser = auth.currentUser
            if (currentUser != null) {

                val phoneNumber = currentUser.phoneNumber


                val intent = Intent(this, dashboard::class.java)
                intent.putExtra("phoneNumber", phoneNumber)
                intent.putExtra("paymentid",paymentid)
                intent.putExtra("netTotalAmount",netTotalAmountInRupees)
                startActivity(intent)
            }
        }
        val downloadButton = findViewById<Button>(R.id.downloadButton)
        downloadButton.setOnClickListener {
            generatePDF(phonenumber, paymentid, netTotalAmountInRupees.toString())
        }
    }

    private fun generatePDF(phonenumber: String, paymentid: String, amount: String) {

        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(300, 600, 1).create()
        val page = pdfDocument.startPage(pageInfo)


        val canvas = page.canvas

        // Write details on the PDF
        val titlePaint = Paint().apply {
            color = Color.BLACK
            textSize = 18F
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val labelPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 14F
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val valuePaint = Paint().apply {
            color = Color.BLACK
            textSize = 14F
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }

        // Draw text on the PDF canvas with styles applied
        canvas.drawText("Bill Details", 80F, 50F, titlePaint)   // Styled title
        canvas.drawText("Mobile Number:", 50F, 100F, labelPaint) // Label with bold
        canvas.drawText("$phonenumber", 150F, 100F, valuePaint)        // Value with normal text
        canvas.drawText("Payment ID:", 50F, 150F, labelPaint)    // Label with bold
        canvas.drawText("$paymentid", 150F, 150F, valuePaint)       // Value with normal text
        canvas.drawText("Amount:", 50F, 200F, labelPaint)        // Label with bold
        canvas.drawText("₹$amount", 150F, 200F, valuePaint)      // Value with normal text


        // Finish the page
        pdfDocument.finishPage(page)

        // Save the PDF to a file
        val filePath = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)?.absolutePath + "/bill.pdf"
        val file = File(filePath)
        try {
            pdfDocument.writeTo(FileOutputStream(file))
            Toast.makeText(this, "Bill saved to $filePath", Toast.LENGTH_LONG).show()
           
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to generate PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }

        // Close the document
        pdfDocument.close()
    }



}