package com.example.mybyk

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth

class menuuser : AppCompatActivity() {
    private lateinit var mobile: TextView
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menuuser)

        // Initialize FirebaseAuth
        firebaseAuth = FirebaseAuth.getInstance()

        // Initialize the TextView
        mobile = findViewById(R.id.mobile)

        // Get the current user
        val user = firebaseAuth.currentUser

        // Check if the user is not null and has a phone number
        val phoneNumber = user?.phoneNumber ?: "Phone number not available"

        // Set the phone number in the TextView
        mobile.text = phoneNumber
    }
}
