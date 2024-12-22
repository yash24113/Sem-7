package com.example.mybyk

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.example.mybyk.R.id
import com.example.mybyk.R.layout
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_main)

        val progressBar: ProgressBar = findViewById(id.progress_bar)
        progressBar.visibility = ProgressBar.VISIBLE // Ensure the ProgressBar is visible initially

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        Handler().postDelayed({
            val currentUser = auth.currentUser
            if (currentUser != null) {
                // User is signed in, retrieve the phone number
                val phoneNumber = currentUser.phoneNumber

                // Pass the phone number to the dashboard activity
                val intent = Intent(this, dashboard::class.java)
                intent.putExtra("phoneNumber", phoneNumber)
                startActivity(intent)
            } else {
                // No user is signed in, redirect to Login
                startActivity(Intent(this, login::class.java))
            }
            finish() // close this activity

        }, 1000) // Delay of 1 second
    }
}
