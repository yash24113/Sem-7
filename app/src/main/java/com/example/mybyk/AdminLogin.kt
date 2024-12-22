package com.example.mybyk

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AdminLogin : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_login)

        val email: EditText = findViewById(R.id.email)
        val pass: EditText = findViewById(R.id.pass)
        val btn: Button = findViewById(R.id.adlogin)

        btn.setOnClickListener {
            val emailInput = email.text.toString()
            val passInput = pass.text.toString()

            if (emailInput.isEmpty()) {
                Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show()
            } else if (passInput.isEmpty()) {
                Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show()
            }
            else {
            if (emailInput == "yashkharva506@gmail.com" && passInput == "2411") {
                val intent = Intent(this, admin_dashboard::class.java)
                intent.putExtra("ADMIN_EMAIL", emailInput)
                startActivity(intent)
                Toast.makeText(this, "You are Developer...!", Toast.LENGTH_LONG).show()
                finish();
            } else {
                Toast.makeText(this, "Incorrect email or password", Toast.LENGTH_SHORT).show()
            }
            }
        }
    }
}
