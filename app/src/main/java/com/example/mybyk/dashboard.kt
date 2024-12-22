package com.example.mybyk

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.example.mybyk.R.id
import com.example.mybyk.R.layout
import com.example.mybyk.R.menu
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class dashboard : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var auth: FirebaseAuth
    private lateinit var phoneNumber: String
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var areaAdapter: AreaAdapter
    private lateinit var bikeAnimationView: LottieAnimationView
    private lateinit var scanner1: LottieAnimationView
    private lateinit var mainContent: ConstraintLayout
    private val areaList = mutableListOf<Area>()
    private val areaListMap = mutableListOf<Area>()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var googleMap: GoogleMap
    private val LOCATION_PERMISSION_REQUEST_CODE = 1



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_dashboard)

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Initialize the map fragment
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Initialize views
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        val headerView = navigationView.getHeaderView(0)
        val navHeaderSubtitle = headerView.findViewById<TextView>(R.id.nav_header_subtitle)


        val book = headerView.findViewById<TextView>(R.id.booking)

        book.setOnClickListener {
            startActivity(Intent(this,mycyclebooking::class.java))
        }

        bikeAnimationView = findViewById(R.id.bikeAnimationView)
        mainContent = findViewById(R.id.mainContent)

        recyclerView = findViewById(R.id.recyclerview)
        drawerLayout = findViewById(id.drawer_layout)

        // Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        areaAdapter = AreaAdapter(areaList, object : AreaAdapter.OnItemClickListener {
            override fun onUpdateClick(area: Area) {
                showUpdateDialog(area)
            }

            override fun onDeleteClick(area: Area) {
                deleteArea(area.documentId)
            }
        }, showIcons = false)
        recyclerView.adapter = areaAdapter

        // Mobile number handling
        phoneNumber = intent.getStringExtra("phoneNumber") ?: ""
        if (phoneNumber.isNotEmpty()) {
            navHeaderSubtitle.text = phoneNumber


        } else {
           // Toast.makeText(this, "Phone number not available", Toast.LENGTH_SHORT).show()
        }

        // Show loader initially
        bikeAnimationView.visibility = LottieAnimationView.VISIBLE
        mainContent.visibility = ConstraintLayout.GONE

        // Load data after delay
        mainContent.postDelayed({
            bikeAnimationView.visibility = LottieAnimationView.GONE
            mainContent.visibility = ConstraintLayout.VISIBLE
        }, 3000) // 3-second delay

        // Populate data
        getAreasForMap()

        // Scanner button click listener


        // Menu icon click listener
        val menuIcon: ImageView = findViewById(id.menu1)
        menuIcon.setOnClickListener {
            drawerLayout.openDrawer(findViewById<NavigationView>(id.nav_view))
        }

        // Popup menu for other options
        val icon1: ImageView = findViewById(id.icon1)
        icon1.setOnClickListener {

            val p = PopupMenu(this, icon1)
            p.menuInflater.inflate(menu.homemenu, p.menu)
            p.setOnMenuItemClickListener { menuitem ->
                when (menuitem.itemId) {
                    id.action_logout -> {
                        showLogoutConfirmationDialog()
                        true
                    }
                    id.admin -> {
                        startActivity(Intent(this, AdminLogin::class.java))
                        true
                    }
                    else -> false
                }
            }
            p.show()
        }
    }
    private fun resizeBitmap(resourceId: Int, width: Int, height: Int): Bitmap {
        val imageBitmap = BitmapFactory.decodeResource(resources, resourceId)
        return Bitmap.createScaledBitmap(imageBitmap, width, height, false)
    }

    private fun getAreasForMap() {
        val areasCollection = FirebaseFirestore.getInstance().collection("Areas")
        val query = areasCollection.orderBy("areaId", Query.Direction.ASCENDING)

        query.get()
            .addOnSuccessListener { documents ->
                areaList.clear()
                googleMap.clear() // Clear previous markers on the map
                val tempAreaList = mutableListOf<AreaDistance>()
                val tempAreaListMap = mutableListOf<Area>()
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // Request location permission if not granted
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        LOCATION_PERMISSION_REQUEST_CODE
                    )
                    return@addOnSuccessListener
                }

                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        val currentLatLng = LatLng(location.latitude, location.longitude)

                        for (document in documents) {
                            val areaId = document.getLong("areaId")?.toInt() ?: 0
                            val areaName = document.getString("areaName") ?: ""
                            val latitude = document.getDouble("latitude") ?: 0.0
                            val longitude = document.getDouble("longitude") ?: 0.0
                            val areaLocation = LatLng(latitude, longitude)

                            // Calculate distance from current location
                            val distance = FloatArray(1)
                            Location.distanceBetween(
                                currentLatLng.latitude, currentLatLng.longitude,
                                areaLocation.latitude, areaLocation.longitude, distance
                            )

                            // Add to temporary list with distance
                            tempAreaList.add(AreaDistance(document.id, areaId, areaName, distance[0]))

                            tempAreaListMap.add(Area(document.id, areaId, areaName))
                            // Add marker to the map
                            val cycleIcon = BitmapDescriptorFactory.fromBitmap(resizeBitmap(R.drawable.logo1, 100, 100))
                            googleMap.addMarker(
                                MarkerOptions()
                                    .position(areaLocation)
                                    .title("$areaName")
                                    .icon(cycleIcon)
                            )
                        }

                        // Sort areas by distance
                        tempAreaList.sortBy { it.distance }

                        // Add the nearest four areas to the areaList for RecyclerView
                        // lj
                        areaList.addAll(tempAreaList.take(3).map { Area(it.documentId, it.areaId, it.areaName) })
                        areaListMap.addAll(tempAreaListMap.map { Area(it.documentId, it.areaId, it.areaName) })
                        //areaListMap.addAll( Area(it.documentId, it.areaId, it.areaName))
                       // areaList.add(Area(document.id, areaId, areaName))
                        areaAdapter.notifyDataSetChanged()  // Update the RecyclerView
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error getting areas", e)
            }
    }


    // Data class to store area along with distance
    data class AreaDistance(
        val documentId: String,
        val areaId: Int,
        val areaName: String,
        val distance: Float
    )



    private fun showUpdateDialog(area: Area) {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.dialog_update_area, null)
        val editText = dialogLayout.findViewById<EditText>(R.id.etUpdateAreaName)

        editText.setText(area.areaName)

        with(builder) {
            setTitle("Update Area")
            setPositiveButton("Update") { dialog, _ ->
                val newAreaName = editText.text.toString()
                updateArea(area.documentId, newAreaName)
            }
            setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            setView(dialogLayout)
            show()
        }
    }

    private fun updateArea(documentId: String, newAreaName: String) {
        val areasCollection = FirebaseFirestore.getInstance().collection("Areas")
        areasCollection.document(documentId).update("areaName", newAreaName)
            .addOnSuccessListener {
                Log.d("Firestore", "Area updated successfully")
                Toast.makeText(this, "Area updated successfully", Toast.LENGTH_SHORT).show()
                getAreasForMap()
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error updating area", e)
            }
    }

    private fun deleteArea(documentId: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete Area")
        builder.setMessage("Are you sure you want to delete this area?")
        builder.setPositiveButton("Yes") { dialog, _ ->
            val areasCollection = FirebaseFirestore.getInstance().collection("Areas")
            areasCollection.document(documentId).delete()
                .addOnSuccessListener {
                    Log.d("Firestore", "Area deleted successfully")
                    Toast.makeText(this, "Area removed successfully", Toast.LENGTH_SHORT).show()
                    getAreasForMap()
                }
                .addOnFailureListener { e ->
                    Log.w("Firestore", "Error deleting area", e)
                }
            dialog.dismiss()
        }
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    private fun showLogoutConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Are you sure you want to logout app?")
            .setPositiveButton("Yes") { _, _ ->
                auth.signOut()
                Toast.makeText(this, "Logout successfully", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, login::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
                finish()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
        builder.create().show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.d_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            id.action_logout -> {
                showLogoutConfirmationDialog()
                true
            }
            id.admin -> {
                startActivity(Intent(this, AdminLogin::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }



    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap

        // Request location permission and move the camera to the current location
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap.isMyLocationEnabled = true
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 10f))
                }
            }
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }

        // Handle marker clicks
        googleMap.setOnMarkerClickListener { marker ->
            // Retrieve the area name from the marker title
            //val title = marker.title
           // val areaName = title?.split(":")?.firstOrNull() ?: ""

                        val title = marker.title

            val parts = title?.split(":") ?: listOf()

           // Toast.makeText(this, "$parts", Toast.LENGTH_SHORT).show()

            val areaName = if (parts.isNotEmpty()) parts[0] else ""

          //  Toast.makeText(this, "$areaName", Toast.LENGTH_SHORT).show()
            // Get the Area object associated with this marker
            //lj
            val selectedArea = areaListMap.find { it.areaName == areaName }



           // Toast.makeText(this, "$selectedArea", Toast.LENGTH_SHORT).show()
            // Open CycleActivity and pass area details
            selectedArea?.let {
                val intent = Intent(this, CycleListActivity::class.java)
                intent.putExtra("areaDocumentId", it.documentId)
                intent.putExtra("areaName", it.areaName)
                startActivity(intent)
            }

            true
        }
    }
}
