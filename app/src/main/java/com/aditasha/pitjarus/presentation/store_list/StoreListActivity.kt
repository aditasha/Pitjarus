package com.aditasha.pitjarus.presentation.store_list

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.aditasha.pitjarus.R
import com.aditasha.pitjarus.databinding.ActivityStoreListBinding
import com.aditasha.pitjarus.domain.model.Result
import com.aditasha.pitjarus.domain.model.StoreEntity
import com.aditasha.pitjarus.presentation.store_visit.StoreVisitActivity
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class StoreListActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityStoreListBinding

    private lateinit var mapFragment: SupportMapFragment
    private lateinit var googleMap: GoogleMap
    private var located = false
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLocation: Location? = null
    private var boundsBuilder = LatLngBounds.builder()

    private val storeListViewModel: StoreListViewModel by viewModels()
    private var storeList = mutableListOf<StoreEntity>()
    private val listAdapter = StoreListAdapter(this::onListItemClick)

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            ) {
                getMyLastLocation()
            } else {
                val snack = Snackbar.make(
                    binding.root,
                    "Allow location permission to set address",
                    Snackbar.LENGTH_LONG
                )
                val params = snack.view.layoutParams as FrameLayout.LayoutParams
                params.gravity = Gravity.CENTER_HORIZONTAL or Gravity.TOP
                snack.view.layoutParams = params
                snack.show()
                finish()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStoreListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(this@StoreListActivity)
        mapFragment = supportFragmentManager.findFragmentById(R.id.googleMap) as SupportMapFragment
        mapFragment.getMapAsync(this)

        lifecycleScope.launchWhenStarted {
            storeListViewModel.storeListResult.collectLatest {
                binding.loading.isVisible = it is Result.Loading
                if (it is Result.Error) {
                    val snack = Snackbar.make(
                        binding.root,
                        it.localizedMessage,
                        Snackbar.LENGTH_LONG
                    )
                    val params =
                        snack.view.layoutParams as FrameLayout.LayoutParams
                    params.gravity = Gravity.CENTER_HORIZONTAL or Gravity.TOP
                    snack.setTextMaxLines(10)
                    snack.show()
                }
                if (it is Result.Success) {
                    storeList.clear()
                    storeList.addAll(it.data)
                    listAdapter.submitList(storeList.toList())
                    for (i in storeList) {
                        if (i.latitude != null && i.longitude != null) {
                            val storeLocation =
                                LatLng(i.latitude.toDouble(), i.longitude.toDouble())
                            boundsBuilder.include(storeLocation)
                            googleMap.addMarker(
                                MarkerOptions()
                                    .position(storeLocation)
                                    .title(i.storeName)
                                    .snippet(i.address)
                            )
                        }
                    }
                    val bounds = boundsBuilder.build()
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 128))
                }
            }
        }

        binding.apply {
            recycler.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(this@StoreListActivity)
                adapter = listAdapter
            }
            val date = SimpleDateFormat(
                "dd-MM-yyyy",
                Locale.getDefault()
            ).format(System.currentTimeMillis())
            titleKunjungan.text = resources.getString(R.string.list_kunjungan, date)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.uiSettings.apply {
            isCompassEnabled = true
            isMapToolbarEnabled = true
            isZoomControlsEnabled = true
        }
        getMyLastLocation()
    }

    override fun onResume() {
        super.onResume()
        if (located) {
            storeListViewModel.fetchStoreList()
        }
    }

    @SuppressLint("MissingPermission")
    private fun getMyLastLocation() {
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION) &&
            checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                currentLocation = location
                if (location != null) {
                    googleMap.apply {
                        isMyLocationEnabled = true
                        uiSettings.isMyLocationButtonEnabled = true
                    }
                    located = true
                    val latLng = LatLng(location.latitude, location.longitude)
                    boundsBuilder.include(latLng)
                    storeListViewModel.fetchStoreList()
                } else {
                    val locationRequest =
                        LocationRequest.Builder(
                            Priority.PRIORITY_HIGH_ACCURACY,
                            TimeUnit.SECONDS.toMillis(1)
                        )
                            .build()
                    val locationCallback = object : LocationCallback() {
                        override fun onLocationResult(location: LocationResult) {
                            getMyLastLocation()
                        }
                    }
                    fusedLocationClient.requestLocationUpdates(
                        locationRequest,
                        locationCallback,
                        null
                    )
                    located = false
                    Toast.makeText(
                        this@StoreListActivity,
                        "Location is not found. Try Again",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this@StoreListActivity,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun onListItemClick(roomId: Int) {
        val intent = Intent(this@StoreListActivity, StoreVisitActivity::class.java)
        intent.putExtra(StoreVisitActivity.ROOM_ID, roomId)
        startActivity(intent)
    }
}