package com.example.mybyk

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.chaos.view.PinView
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class otpvarificaion : AppCompatActivity() {
    private lateinit var btnSubmit: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var pinview: PinView
    private lateinit var OTP: String
    private lateinit var auth: FirebaseAuth
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var phoneNumber: String
    private lateinit var resendTV: TextView
    private lateinit var mobile: TextView
    private lateinit var number: String



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otpvarificaion)

        init()
        addTextChangeListener()

        OTP = intent.getStringExtra("OTP").toString()
        resendToken = intent.getParcelableExtra("resendToken")!!
        phoneNumber = intent.getStringExtra("phoneNumber")!!
        mobile.text = phoneNumber

        btnSubmit.setOnClickListener {
            verifyOTP()
        }

        resendTV.setOnClickListener {
            resendVerificationCode()
            resendOTPTvVisibility()
        }
    }

    private fun verifyOTP() {
        val typedOTP = pinview.text.toString()
        if (typedOTP.isNotEmpty()) {
            if (typedOTP.length == 6) {
                val credential: PhoneAuthCredential = PhoneAuthProvider.getCredential(OTP, typedOTP)
                progressBar.visibility = View.VISIBLE
                signInWithPhoneAuthCredential(credential)
            } else {
                Toast.makeText(this, "Please Enter Correct OTP", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Please Enter OTP", Toast.LENGTH_SHORT).show()
        }
    }

    private fun resendOTPTvVisibility() {
        pinview.setText("")
        resendTV.visibility = View.INVISIBLE
        resendTV.isEnabled = false

        Handler(Looper.myLooper()!!).postDelayed({
            resendTV.visibility = View.VISIBLE
            resendTV.isEnabled = true
        }, 6000)
    }

    private fun resendVerificationCode() {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)       // Phone number to verify
            .setTimeout(6L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this)                 // Activity (for callback binding)
            .setCallbacks(callbacks)
            .setForceResendingToken(resendToken) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            Log.d("OTP", "Verification completed with credential: $credential")
            // Commenting this out to ensure it doesn't automatically log in
            // signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            if (e is FirebaseAuthInvalidCredentialsException) {
                Log.d("OTP", "onVerificationFailed: ${e.message}")
            } else if (e is FirebaseTooManyRequestsException) {
                Log.d("OTP", "onVerificationFailed: ${e.message}")
            }
            progressBar.visibility = View.GONE
            Toast.makeText(this@otpvarificaion, "Verification Failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }

        override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
            OTP = verificationId
            resendToken = token
            Log.d("OTP", "Code sent: verificationId=$verificationId, token=$token")
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                progressBar.visibility = View.GONE
                if (task.isSuccessful) {
                    val user =  FirebaseAuth.getInstance().currentUser
                    user?.let {
                        // Access the user's display name here
                        val displayName = it.displayName
                       // Toast.makeText(this, "User's display name: $displayName", Toast.LENGTH_SHORT).show()
                        Log.d("FirebaseAuth", "User's display name: $displayName")
                    }
                    Log.d("OTP", "signInWithPhoneAuthCredential: success")
                    Toast.makeText(this, "Authenticated Successfully", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@otpvarificaion, dashboard::class.java)

                    //intent.putExtra("phoneNumber", number)
                    startActivity(intent)
                    // startActivity(Intent(this, ::class.java))
                    finish()
                } else {
                    Log.d("OTP", "signInWithPhoneAuthCredential: failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(this, "Invalid OTP", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Sign in failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }



    private fun init() {
        auth = FirebaseAuth.getInstance()
        progressBar = findViewById(R.id.pb)
        btnSubmit = findViewById(R.id.votp)
        resendTV = findViewById(R.id.resendTextView)
        pinview = findViewById(R.id.pinview)
        mobile = findViewById(R.id.mobile) // Initialize the mobile TextView here
    }

    private fun addTextChangeListener() {
        pinview.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                Log.d("OTP", "OTP entered: ${s.toString()}")
                if (s.toString().length == 6) {
                    verifyOTP()
                }
            }
        })
    }
}


