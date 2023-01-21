package com.aditasha.pitjarus.presentation.store_visit

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.aditasha.pitjarus.GeofenceBroadcastReceiver
import com.aditasha.pitjarus.Helper
import com.aditasha.pitjarus.R
import com.aditasha.pitjarus.databinding.ActivityStoreVisitBinding
import com.aditasha.pitjarus.domain.model.Result
import com.aditasha.pitjarus.domain.model.StoreEntity
import com.aditasha.pitjarus.presentation.store_detail.StoreDetailActivity
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


@AndroidEntryPoint
class StoreVisitActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStoreVisitBinding

    private var storeEntity: StoreEntity? = null
    private val visitViewModel: StoreVisitViewModel by viewModels()

    private lateinit var geofencingClient: GeofencingClient
    private lateinit var geofence: Geofence
    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(this@StoreVisitActivity, GeofenceBroadcastReceiver::class.java)
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        intent.action = ACTION_GEOFENCE_EVENT
        @SuppressLint("UnspecifiedImmutableFlag")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getBroadcast(
                this@StoreVisitActivity,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
        } else {
            PendingIntent.getBroadcast(
                this@StoreVisitActivity,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
    }
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val transition = intent.getIntExtra(TRANSITION, -1)
            if (transition != -1) {
                transitionListener(transition)
            }
        }
    }
    private var locationStatus = Geofence.GEOFENCE_TRANSITION_EXIT

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions[Manifest.permission.ACCESS_BACKGROUND_LOCATION] ?: false -> {
                    addGeofence()
                }
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false -> {
                    // Precise location access granted.
                    addGeofence()
                    getMyLastLocation()
                }
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false -> {
                    // Only approximate location access granted.
                    getMyLastLocation()
                }
                permissions[Manifest.permission.CAMERA] ?: false -> {
                    binding.camera.performClick()
                }
                else -> {
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
        }

    @Suppress("DEPRECATION")
    private val captureImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                processImage()
            }
        }
    private lateinit var currentPhotoPath: String
    private lateinit var photoUri: Uri
    private lateinit var prefix: String
    private lateinit var suffix: String
    private lateinit var photoFile: File
    private var photoTaken = false

    private var visitedSaved = false
    private var photoSaved = false

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStoreVisitBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        storeFlowCollector()
        storePictureCollector()
        storeVisitedCollector()

        val roomId = intent.extras?.getInt(ROOM_ID)
        if (roomId != null) {
            visitViewModel.fetchStore(roomId)
        }

        geofencingClient = LocationServices.getGeofencingClient(this@StoreVisitActivity)
        fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(this@StoreVisitActivity)

        binding.camera.setOnClickListener {
            if (checkPermission(Manifest.permission.CAMERA)) {
                val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                try {
                    photoFile = createImageFile()
                    photoUri = FileProvider.getUriForFile(
                        this@StoreVisitActivity,
                        "com.aditasha.pitjarus.provider",
                        photoFile
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                    captureImageLauncher.launch(takePictureIntent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                requestPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.CAMERA
                    )
                )
            }
        }

        binding.noVisit.setOnClickListener { finish() }
        binding.visit.setOnClickListener {
            storeEntity?.roomId?.let {
                visitViewModel.updatePicture(it, currentPhotoPath)
                visitViewModel.updateVisited(it, true, System.currentTimeMillis())
            }
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

    override fun onResume() {
        super.onResume()
        if (storeEntity != null) {
            addGeofence()
        }
        registerReceiver(receiver, IntentFilter(ACTION_GEOFENCE_TRANSITION))
    }

    override fun onPause() {
        super.onPause()
        removeGeofence()
        unregisterReceiver(receiver)
    }

    private fun storeFlowCollector() {
        lifecycleScope.launchWhenStarted {
            visitViewModel.storeResult.collectLatest { result ->
                binding.loading.isVisible = result is Result.Loading
                if (result is Result.Error) showError(result.localizedMessage)
                if (result is Result.Success) {
                    storeEntity = result.data
                    binding.apply {
                        storeEntity?.let {
                            storeName.text = it.storeName.toString()
                            storeAddress.text = it.address.toString()
                            storeSubchannel.text = resources.getString(
                                R.string.sub_channel,
                                it.subchannelName.toString()
                            )
                            storeChannel.text =
                                resources.getString(R.string.channel, it.channelName.toString())
                            storeArea.text =
                                resources.getString(R.string.area, it.areaName.toString())
                            storeRegion.text =
                                resources.getString(R.string.region, it.regionName.toString())
                            if (it.picture != null) {
                                val bitmap = BitmapFactory.decodeFile(it.picture)
                                photoViewer.setImageBitmap(bitmap)
                            }
                            if (it.lastVisited != null) {
                                val date = SimpleDateFormat(
                                    "dd-MM-yyyy",
                                    Locale.getDefault()
                                ).format(it.lastVisited)
                                lastVisit.text =
                                    resources.getString(R.string.last_visit, date.toString())
                            } else lastVisit.text = resources.getString(R.string.last_visit, "-")
                            if (it.latitude != null && it.longitude != null) {
                                geofence = Geofence.Builder()
                                    .setRequestId(GEOFENCE_ID)

                                    // Set the circular region of this geofence.
                                    .setCircularRegion(
                                        it.latitude.toDouble(),
                                        it.longitude.toDouble(),
                                        RADIUS
                                    )

                                    // Set the expiration duration of the geofence. This geofence gets automatically
                                    // removed after this period of time.
                                    .setExpirationDuration(Geofence.NEVER_EXPIRE)

                                    // Set the transition types of interest. Alerts are only generated for these
                                    // transition. We track entry and exit transitions in this sample.
                                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)

                                    // Create the geofence.
                                    .build()
                            }
                            addGeofence()
                        }
                    }
                }
            }
        }
    }

    private fun storePictureCollector() {
        lifecycleScope.launchWhenStarted {
            visitViewModel.storePictureResult.collectLatest { result ->
                binding.loading.isVisible = result is Result.Loading
                if (result is Result.Error) showError(result.localizedMessage)
                if (result is Result.Success) {
                    photoSaved = true
                    checkDataIsSaved()
                }
            }
        }
    }

    private fun storeVisitedCollector() {
        lifecycleScope.launchWhenStarted {
            visitViewModel.storeVisitedResult.collectLatest { result ->
                binding.loading.isVisible = result is Result.Loading
                if (result is Result.Error) showError(result.localizedMessage)
                if (result is Result.Success) {
                    val date = SimpleDateFormat.getDateInstance().format(result.data)
                    binding.lastVisit.text = getString(R.string.last_visit, date.toString())
                    visitedSaved = true
                    checkDataIsSaved()
                }
            }
        }
    }

    private fun showError(localizedMessage: String) {
        val snack = Snackbar.make(
            binding.root,
            localizedMessage,
            Snackbar.LENGTH_LONG
        )
        val params =
            snack.view.layoutParams as FrameLayout.LayoutParams
        params.gravity = Gravity.CENTER_HORIZONTAL or Gravity.TOP
        snack.setTextMaxLines(10)
        snack.show()
    }

    private fun checkDataIsSaved() {
        if (photoSaved && visitedSaved) {
            val intent = Intent(this@StoreVisitActivity, StoreDetailActivity::class.java)
            intent.putExtra(ROOM_ID, storeEntity?.roomId)
            startActivity(intent)
        }
    }

    private fun getGeofencingRequest(): GeofencingRequest {
        return GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofence(geofence)
        }.build()
    }

    private fun addGeofence() {
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION) &&
            checkPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        ) {
            removeGeofence()
            geofencingClient.addGeofences(getGeofencingRequest(), geofencePendingIntent)
                .addOnSuccessListener { Log.d("test", "geofence added") }
                .addOnFailureListener {
                    it.printStackTrace()
                    showError(it.localizedMessage!!)
                }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                requestPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    )
                )
            } else {
                requestPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                )
            }
        }
    }

    private fun removeGeofence() {
        geofencingClient.removeGeofences(geofencePendingIntent)
    }

    private fun transitionListener(geofenceTransition: Int) {
        when (geofenceTransition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> {
                binding.apply {
                    location.text = resources.getString(R.string.location_ok)
                    location.setTextColor(resources.getColor(R.color.primary, theme))
                }
                locationStatus = Geofence.GEOFENCE_TRANSITION_ENTER
                checkVisitButton()
            }
            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                binding.apply {
                    location.text = resources.getString(R.string.location_not_ok)
                    location.setTextColor(resources.getColor(R.color.red, theme))
                }
                locationStatus = Geofence.GEOFENCE_TRANSITION_EXIT
                checkVisitButton()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getMyLastLocation() {
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION) &&
            checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location == null) {
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
                    Toast.makeText(
                        this@StoreVisitActivity,
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
            this@StoreVisitActivity,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        prefix = "JPEG_${timeStamp}_"
        suffix = ".jpg"
        return File.createTempFile(
            prefix, /* prefix */
            suffix, /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    private fun processImage() {
        try {
            val bitmap = BitmapFactory.decodeFile(currentPhotoPath)
//            val exifAngle = Helper.getExifAngle(this@StoreVisitActivity, photoUri)
            val rotatedBitmap = Helper.rotateBitmap(bitmap, 90f)
            val fo = FileOutputStream(photoFile)
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, fo)
            fo.flush()
            fo.close()
            binding.photoViewer.setImageBitmap(rotatedBitmap)
            photoTaken = true
            checkVisitButton()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun checkVisitButton() {
        binding.visit.isEnabled = locationStatus == Geofence.GEOFENCE_TRANSITION_ENTER && photoTaken
    }

    companion object {
        const val ACTION_GEOFENCE_TRANSITION = "action transition"
        const val TRANSITION = "transition"
        const val ACTION_GEOFENCE_EVENT = "geofence"
        const val ROOM_ID = "room id"
        const val GEOFENCE_ID = "store"
        const val RADIUS = 100f
    }
}