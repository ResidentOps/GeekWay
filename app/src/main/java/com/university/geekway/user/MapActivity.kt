package com.university.geekway.user

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.PopupMenu
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.university.geekway.AppPermissions
import com.university.geekway.BaseActivity
import com.university.geekway.R
import com.university.geekway.databinding.ActivityMapBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException

class MapActivity : BaseActivity(), OnMapReadyCallback {

    private var mGoogleMap: GoogleMap? = null
    private lateinit var appPermission: AppPermissions
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private var permissionToRequest = mutableListOf<String>()
    private var isLocationPermissionOk = false
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private lateinit var currentLocation: Location
    private var currentMarker: Marker? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityMapBinding
    public lateinit var databaseRef: DatabaseReference
    private lateinit var db: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val mapFragment = supportFragmentManager.findFragmentById(com.university.geekway.R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        db = FirebaseDatabase.getInstance()
        databaseRef = Firebase.database.reference
        databaseRef.addValueEventListener(placeListener)

        auth = Firebase.auth
        appPermission = AppPermissions()

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            isLocationPermissionOk = permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true
                    && permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true
            if (isLocationPermissionOk)
                setUpGoogleMap()
            else
                Snackbar.make(binding.root, R.string.error_gps_not_allow, Snackbar.LENGTH_SHORT).show()
        }

        binding.buttonMapCancel.setOnClickListener {
            onBackPressed()
        }

        binding.buttonCurrentLocation.setOnClickListener {
            getCurrentLocation()
        }

        binding.buttonMapType.setOnClickListener {
            val popupMenu = PopupMenu(this, it)
            popupMenu.apply {
                menuInflater.inflate(com.university.geekway.R.menu.map_type_menu, popupMenu.menu)
                setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        com.university.geekway.R.id.btnNormal -> mGoogleMap?.mapType = GoogleMap.MAP_TYPE_NORMAL
                        com.university.geekway.R.id.btnSatellite -> mGoogleMap?.mapType = GoogleMap.MAP_TYPE_SATELLITE
                    }
                    true
                }
                show()
            }
        }
    }

    private var placeId = ""
    override fun onMapReady(googleMap: GoogleMap) {
        googleMap.setMapStyle(this?.let { MapStyleOptions.loadRawResourceStyle(it, com.university.geekway.R.raw.map_style) })
        mGoogleMap = googleMap
        mGoogleMap?.clear()
        initSearch()
        when {
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                isLocationPermissionOk = true
                setUpGoogleMap()
            }
            this.shouldShowRequestPermissionRationale(

                Manifest.permission.ACCESS_FINE_LOCATION
            ) -> {
                AlertDialog.Builder(this)
                    .setTitle(R.string.text_gps_allowing)
                    .setTitle(R.string.text_gpsApp_allowing)
                    .setPositiveButton(R.string.text_gpsApp_allow_OK) { _, _ ->
                        requestLocation()
                    }.create().show()
            } else ->{
            requestLocation()
        }
        }
        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.uiSettings.isCompassEnabled = false
        googleMap.uiSettings.isMyLocationButtonEnabled = false
    }

    private fun initSearch() {
        CoroutineScope(Dispatchers.Main).launch{
            binding.textSearch?.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
                if(actionId == EditorInfo.IME_ACTION_DONE
                    || actionId == EditorInfo.IME_ACTION_SEARCH
                    || event.action == KeyEvent.ACTION_DOWN
                    || event.action == KeyEvent.KEYCODE_ENTER){
                    //println("initSearch Action is called")
                    //search method
                    locateSearch()
                    v.hideKeyboard()
                    true
                } else {
                    false
                }
            })
        }
    }

    fun View.hideKeyboard() {
        val inputManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(windowToken, 0)
    }

    private fun requestLocation() {
        permissionToRequest.add(android.Manifest.permission.ACCESS_FINE_LOCATION)
        permissionToRequest.add(android.Manifest.permission.ACCESS_COARSE_LOCATION)
        permissionLauncher.launch(permissionToRequest.toTypedArray())
    }

    private fun setUpGoogleMap() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            isLocationPermissionOk = false
            return
        }
        mGoogleMap?.isMyLocationEnabled = true
        mGoogleMap?.uiSettings?.isTiltGesturesEnabled = true
        setUpLocationUpdate()
    }

    private fun setUpLocationUpdate() {
        locationRequest = LocationRequest.create()
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)
                for (location in locationResult?.locations!!) {
                    Log.d("TAG", "onLocationResult: ${location.longitude} ${location.latitude}")
                }
            }
        }
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        startLocationUpdates()
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            isLocationPermissionOk = false
            return
        }
        fusedLocationProviderClient?.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
            }
        }

        val bundle = intent.extras
        if (bundle != null) {
            var intentAddress = bundle!!.getString("placeaddress")
            var geocoder = Geocoder(this)
            var list: List<Address> = emptyList()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    list = geocoder.getFromLocationName(intentAddress, 1)
                } catch (e: IOException) {
                    Log.e("TAG", "locate: IOException")
                }
                if (list.size > 0) {
                    var address = list.get(0)
                    Log.e("TAG", "locate: IOException")
                    moveCameraIntent(LatLng(address.latitude, address.longitude), 15F, address.getAddressLine(0))
                }
            }
        } else
        getCurrentLocation()
    }

    private fun moveCameraIntent(latlng : LatLng, zoom : Float, title : String){
        CoroutineScope(Dispatchers.Main).launch {
            mGoogleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, zoom))
        }
    }

    private fun getCurrentLocation() {
        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            isLocationPermissionOk = false
            return
        }
        fusedLocationProviderClient.lastLocation.addOnSuccessListener {
            currentLocation = it
            moveCameraToLocation(currentLocation)
        }
    }

    private fun moveCameraToLocation(location: Location) {
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(
            LatLng(
                location.latitude,
                location.longitude
            ), 15f
        )
        mGoogleMap?.animateCamera(cameraUpdate)
    }

    private fun stopLocationUpdates() {
        fusedLocationProviderClient?.removeLocationUpdates(locationCallback)
        Log.d("TAG", "stopLocationUpdates: Location Update Stop")
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    override fun onResume() {
        super.onResume()
        if (fusedLocationProviderClient != null) {
            startLocationUpdates()
            currentMarker?.remove()
        }
    }

    private fun locateSearch() {
        var searchString = binding.textSearch?.text.toString()
        var geocoder = Geocoder(this)
        var list: List<Address> = emptyList()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                list = geocoder.getFromLocationName(searchString, 1)
            } catch (e: IOException) {
                Log.e("TAG", "locate: IOException")
            }
            if (list.size > 0) {
                var address = list.get(0)
                Log.e("TAG", "locate: IOException")
                moveCamera(LatLng(address.latitude, address.longitude), 15F, address.getAddressLine(0))
            }
        }
    }

    private fun moveCamera(latlng : LatLng, zoom : Float, title : String){
        CoroutineScope(Dispatchers.Main).launch {
            mGoogleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, zoom))
        }
    }

    private var latD = 0.0
    private var lngD = 0.0
    val placeListener = object : ValueEventListener {
        override fun onCancelled(error: DatabaseError) {
        }
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            if (dataSnapshot.exists()) {
                val ref = FirebaseDatabase.getInstance().getReference("Places")
                ref.child(placeId)
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                for (ds in snapshot.children) {
                                    val name = "${ds.child("placename").value}"
                                    val lat = "${ds.child("placeLat").value}"
                                    val lng = "${ds.child("placeLng").value}"

                                    try {
                                        latD = lat.toDouble()
                                    } catch(e: Exception){
                                        println(e.message)
                                    }

                                    try {
                                        lngD = lng.toDouble()
                                    } catch(e: Exception){
                                        println(e.message)
                                    }

                                    if (latD != 0.0 && lngD != 0.0) {
                                        val placeLoc = LatLng(latD, lngD)
                                        val markerOptions = MarkerOptions().position(placeLoc).title(name)
                                        mGoogleMap?.addMarker(markerOptions)
                                    }
                                }
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {
                        }
                    })
            }
        }
    }
}