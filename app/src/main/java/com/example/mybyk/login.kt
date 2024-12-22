package com.example.mybyk

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mybyk.R.layout
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class login : AppCompatActivity() {

    private lateinit var etPhoneNumber: EditText
    private lateinit var btnSubmit: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var auth: FirebaseAuth
    private lateinit var number: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_login)

        init()



        btnSubmit.setOnClickListener {
            number = etPhoneNumber.text.trim().toString()

            if (number.isEmpty()) {
                Toast.makeText(this, "Please enter a mobile number", Toast.LENGTH_SHORT).show()
            } else if (number.length != 10 || !number.startsWith("6") && !number.startsWith("7") && !number.startsWith("8") && !number.startsWith("9")) {
                Toast.makeText(this, "Please enter a valid mobile number", Toast.LENGTH_SHORT).show()
            } else {
                // Add country code if needed
                number = "+91$number" // Change to your country code
                Toast.makeText(this, "$number", Toast.LENGTH_SHORT).show()
                progressBar.visibility = ProgressBar.VISIBLE

                // Test phone number check (Firebase predefined number)
                val testNumber = "8347727949"
                if (number == "8347727949") { // Firebase test number
                    // Directly authenticate with test credentials
                    val testCredential = PhoneAuthProvider.getCredential("TEST_OTP", "123456") // Test OTP code
                    signInWithPhoneAuthCredential(testCredential)
                } else {
                    // Proceed with normal phone authentication flow
                    val options = PhoneAuthOptions.newBuilder(auth)
                        .setPhoneNumber(number)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(callbacks)           // OnVerificationStateChangedCallbacks
                        .build()
                    PhoneAuthProvider.verifyPhoneNumber(options)
                }
            }
        }

    }

    private fun init() {
        progressBar = findViewById(R.id.progress_bar)
        progressBar.visibility = View.INVISIBLE
        btnSubmit = findViewById(R.id.btn_submit)
        etPhoneNumber = findViewById(R.id.et_phone_number)
        auth = FirebaseAuth.getInstance()
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    sendToMain()
                    Toast.makeText(this, "Authenticated Successfully", Toast.LENGTH_SHORT).show()

                } else {
                    Log.d("TAG", "signInWithPhoneAuthCredential: ${task.exception.toString()}")
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                        Toast.makeText(this, "Invalid Verification Code", Toast.LENGTH_SHORT).show()
                    }
                }
                progressBar.visibility = ProgressBar.GONE
            }
    }

    private fun sendToMain() {
        val intent = Intent(this@login, dashboard::class.java)
        intent.putExtra("phoneNumber", number) // Passing the phone number
        startActivity(intent)
        finish()
    }


    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            Log.d("TAG", "onVerificationFailed: ${e.toString()}")
            if (e is FirebaseAuthInvalidCredentialsException) {
                Toast.makeText(this@login, "Invalid Request", Toast.LENGTH_SHORT).show()
            } else if (e is FirebaseTooManyRequestsException) {
                Toast.makeText(this@login, "Today SMS Quota Exceeded", Toast.LENGTH_SHORT).show()
            }
//            else{
//                Toast.makeText(this@login, "Something Went Wrong.Please Try again.", Toast.LENGTH_SHORT).show()
//            }
            progressBar.visibility = ProgressBar.GONE  
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            val intent = Intent(this@login, otpvarificaion::class.java)
            intent.putExtra("OTP", verificationId)
            intent.putExtra("resendToken", token)
            intent.putExtra("phoneNumber", number)
            startActivity(intent)
            progressBar.visibility = ProgressBar.GONE
        }

    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}






