package com.example.mybyk

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Paint
import android.os.Bundle
import android.telephony.SmsManager
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import org.json.JSONObject
import kotlin.properties.Delegates

class payment : AppCompatActivity(), PaymentResultListener {
    private lateinit var auth: FirebaseAuth
    private lateinit var paymentbtn: ImageView
    private var netTotalAmount by Delegates.notNull<Double>()
    private lateinit var phoneNumber: String
    private lateinit var packageItem: Package

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)
        auth = FirebaseAuth.getInstance()

        // Initialize Razorpay Checkout
        Checkout.preload(applicationContext)

        paymentbtn = findViewById<ImageView>(R.id.payment)

        // Set OnClickListener for payment button
        paymentbtn.setOnClickListener {
            startPayment()
        }

        val textView = findViewById<TextView>(R.id.a)
        textView.paintFlags = textView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

        // Receive package item from intent (assuming Parcelable is implemented for Package class)
        packageItem = intent.getParcelableExtra("PACKAGE_DETAILS")!!
        Log.d("PaymentActivity", "Received package item: $packageItem")

        // Use the packageItem to display details
        packageItem?.let {
            findViewById<TextView>(R.id.tvPackageName1).text = it.packageName

            val days = it.packageValidity.split(" ")[0].toIntOrNull() ?: 0
            val totalPrice = it.packagePrice * days

            val tt = it.packagePrice1
            findViewById<TextView>(R.id.tvPackagePrice1).text = "₹$totalPrice"
            findViewById<TextView>(R.id.lessprice).text = "₹$totalPrice"
            findViewById<TextView>(R.id.tvPackageValidity1).text = it.packageValidity
            findViewById<TextView>(R.id.tvPackagePriceDis1).text = "₹${it.packagePrice1}"
            findViewById<TextView>(R.id.a).text = "₹${it.packagePrice1}"

            val totaldis = tt - totalPrice
            findViewById<TextView>(R.id.dis).text = "- ₹$totaldis"
            findViewById<TextView>(R.id.f).text = "🎉🎉 Woohoo! You saved ₹${totaldis} 🎉🎉"

            val depositAmount = 500
            findViewById<TextView>(R.id.dep).text = "₹${depositAmount}"

            // Calculate netTotalAmount in rupees
            netTotalAmount = depositAmount + totalPrice
            findViewById<TextView>(R.id.netamount).text = "₹${netTotalAmount}"
        }

        // Request SMS permission if not already granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS), 1)
        }
    }

    // Method to start Razorpay payment process
    private fun startPayment() {
        val checkout = Checkout()

        // Set your Razorpay API Key here
        checkout.setKeyID("rzp_test_0gw2a1UTLIztqO") // Use your actual Razorpay API key

        // Get the current authenticated user
        val currentUser = auth.currentUser

        if (currentUser != null) {
            try {
                // Create payment options
                val options = JSONObject()
                options.put("name", "MYBYK")
                packageItem?.let {
                    options.put("description", it.packageName)
                }
                options.put("currency", "INR")

                // Convert netTotalAmount (in rupees) to paise (1 INR = 100 paise)
                val amountInPaise = (netTotalAmount * 100).toInt()
                options.put("amount", amountInPaise) // Amount in paise

                // Prefill email and contact
                val prefill = JSONObject()

                // Use the current user's email and phone number if available
                prefill.put("email", currentUser.email ?: "defaultemail@example.com")
                prefill.put("contact", currentUser.phoneNumber ?: "0000000000") // Use a default if phone number is not available

                options.put("prefill", prefill)

                // Open Razorpay checkout screen
                checkout.open(this, options)

            } catch (e: Exception) {
                Toast.makeText(this, "Error in payment: ${e.message}", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_LONG).show()
        }
    }

    // Razorpay PaymentResultListener callback methods

    // Handle successful payment
    override fun onPaymentSuccess(razorpayPaymentID: String?) {
        val paymentid = razorpayPaymentID
        val currentUser = auth.currentUser

        if (currentUser != null) {
            // User is signed in, retrieve the phone number
            val phoneNumber = currentUser.phoneNumber
            val amountInPaise1 = (netTotalAmount * 100).toInt()

            // Construct the SMS message
            val smsMessage = """
                Payment Successful!
                Transaction ID: $paymentid
                Total Amount Paid: ₹$netTotalAmount
                Thank you for using MYBYK!
            """.trimIndent()

            // Send the SMS if the phone number is available
            if (!phoneNumber.isNullOrEmpty()) {
                try {
                    val smsManager: SmsManager = SmsManager.getDefault()
                    smsManager.sendTextMessage(phoneNumber, null, smsMessage, null, null)
                    Toast.makeText(this, "$phoneNumber", Toast.LENGTH_SHORT).show()
                    Toast.makeText(this, "Payment Successful and SMS sent!", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(this, "Failed to send SMS: ${e.message}", Toast.LENGTH_LONG).show()
                    e.printStackTrace()
                }
            } else {
                Toast.makeText(this, "Phone number not available", Toast.LENGTH_SHORT).show()
            }

            // Pass the phone number to the dashboard activity
            val intent = Intent(this, mycyclebooking::class.java)
            intent.putExtra("phoneNumber", phoneNumber)
            intent.putExtra("paymentid", paymentid)
            intent.putExtra("netTotalAmount", amountInPaise1)

            startActivity(intent)
        }
    }




    // Handle payment error
    override fun onPaymentError(code: Int, response: String?) {
        Toast.makeText(this, "Payment failed: $response", Toast.LENGTH_SHORT).show()
        // Redirect to package details page after payment failure
        startActivity(Intent(this, userpackagedetails::class.java))
    }

    // Handle permission result
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "SMS permission granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "SMS permission denied", Toast.LENGTH_SHORT).show()
        }
    }
}
