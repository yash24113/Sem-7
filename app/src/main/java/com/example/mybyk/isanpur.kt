// dashboard.kt
package com.example.mybyk

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import android.widget.PopupMenu
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView

class isanpur : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var drawerLayout: DrawerLayout
    val data = ArrayList<Product>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_isanpur)

        // Populate data
        data.add(Product(R.drawable.logo1, "669100-01391", R.drawable.righ))
        data.add(Product(R.drawable.logo1, "669100-04822", R.drawable.righ))
        data.add(Product(R.drawable.logo1, "669100-02457", R.drawable.righ))
        data.add(Product(R.drawable.logo1, "669100-07543", R.drawable.righ))
        data.add(Product(R.drawable.logo1, "669100-01804", R.drawable.righ))

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(this)

        

        // Set up adapter with item click listener
        val adapter = ListAdapter(data) { product ->
            if(product.title == "Isanpur" )
            {
                startActivity(Intent(this, isanpur::class.java))
                Toast.makeText(this, "Clicked: ${product.title}", Toast.LENGTH_SHORT).show()
            }

        }
        recyclerView.adapter = adapter

        auth = FirebaseAuth.getInstance()

        val icon1: ImageView = findViewById(R.id.icon1)

        icon1.setOnClickListener {
            val p = PopupMenu(this, icon1)
            p.menuInflater.inflate(R.menu.homemenu, p.menu)
            p.setOnMenuItemClickListener { menuitem ->
                when (menuitem.itemId) {
                    R.id.action_logout -> showLogoutConfirmationDialog()
                }
                true
            }
            p.show()
        }

        drawerLayout = findViewById(R.id.drawer_layout)
        val menuIcon: ImageView = findViewById(R.id.menu1)

        menuIcon.setOnClickListener {
            drawerLayout.openDrawer(findViewById<NavigationView>(R.id.nav_view))
        }
    }

    private fun showLogoutConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Are you sure you want to logout app?")
            .setPositiveButton("Yes") { dialog, id ->
                // User clicked Yes button
                auth.signOut()
                Toast.makeText(this, "Logout successfully", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, login::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
                finish() // Close the current activity
            }
            .setNegativeButton("No") { dialog, id ->
                // User cancelled the dialog
                dialog.dismiss()
            }
        // Create and show the AlertDialog
        builder.create().show()
    }
}


