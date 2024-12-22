package com.example.mybyk

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class admin_dashboard : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        // Get admin email passed from login
        val adminEmail = intent.getStringExtra("ADMIN_EMAIL")
        val emailTextView: TextView = findViewById(R.id.admin_email)
        emailTextView.text = adminEmail

        val userDetailsCard: CardView = findViewById(R.id.card_user_details)
        val areaDetailsCard: CardView = findViewById(R.id.card_area_details)
        val packageDetailsCard: CardView = findViewById(R.id.card_package_details)

        userDetailsCard.setOnClickListener {
            startActivity(Intent(this, UserDetailsActivity::class.java))
        }

        areaDetailsCard.setOnClickListener {
            startActivity(Intent(this, addareacrud::class.java))
        }

        packageDetailsCard.setOnClickListener {
            startActivity(Intent(this, PackageDetails::class.java))
        }
    }

    // Inflate the menu for the three-dot menu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_admin_dashboard, menu)
        return true
    }

    // Handle the logout option in the menu
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                Toast.makeText(this, "Logou successfull", Toast.LENGTH_SHORT).show()
                // Perform logout logic here (e.g., clearing session or navigating to login screen)
                startActivity(Intent(this, AdminLogin::class.java))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
