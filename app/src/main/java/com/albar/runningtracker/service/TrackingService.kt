package com.albar.runningtracker.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.albar.runningtracker.R
import com.albar.runningtracker.other.Constants.ACTION_PAUSE_SERVICE
import com.albar.runningtracker.other.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import com.albar.runningtracker.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.albar.runningtracker.other.Constants.ACTION_STOP_SERVICE
import com.albar.runningtracker.other.Constants.FASTEST_LOCATION_INTERVAL
import com.albar.runningtracker.other.Constants.LOCATION_UPDATE_INTERVAL
import com.albar.runningtracker.other.Constants.NOTIFICATION_CHANNEL_ID
import com.albar.runningtracker.other.Constants.NOTIFICATION_CHANNEL_NAME
import com.albar.runningtracker.other.Constants.NOTIFICATION_ID
import com.albar.runningtracker.other.Constants.TIMER_UPDATE_INTERVAL
import com.albar.runningtracker.other.TrackingUtility
import com.albar.runningtracker.ui.MainActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

typealias Polyline = MutableList<LatLng>
typealias Polylines = MutableList<Polyline>

class TrackingService : LifecycleService() {

    private var isFirstRun = true

    // to get location update
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    // creating live data to give time in second
    private val timeRunInSeconds = MutableLiveData<Long>()


    companion object {
        // MutableLive data to observe changes
        // <MutableList< to save list of polyline
        // <MutableList<LatLng> to save Lat Long coordinate in list
        // val pathPoints = MutableLiveData<MutableList<MutableList<LatLng>>>()

        val pathPoints = MutableLiveData<Polylines>()
        val isTracking = MutableLiveData<Boolean>()

        // creating live data to give time in millis
        val timeRunInMillis = MutableLiveData<Long>()
    }

    @SuppressLint("VisibleForTests")
    override fun onCreate() {
        super.onCreate()
        postInitialValues()
        fusedLocationProviderClient = FusedLocationProviderClient(this)

        isTracking.observe(this, Observer {
            updateLocationTracking(it)
        })
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    if (isFirstRun) {
                        startForegroundService()
                        isFirstRun = false
                    } else {
                        startTimer()
                        Timber.d("Resuming Service")
                    }
                }
                ACTION_PAUSE_SERVICE -> {
                    Timber.d("Paused service")
                    pauseService()
                }
                ACTION_STOP_SERVICE -> {
                    Timber.d("Stopped service")
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private var isTimerEnabled = false
    private var lapTime = 0L
    private var timeRun = 0L
    private var timeStarted = 0L
    private var lastSecondTimeStamp = 0L

    private fun startTimer() {
        // add empty polyline
        addEmptyPolyline()
        isTracking.postValue(true)
        timeStarted = System.currentTimeMillis()
        isTimerEnabled = true
        CoroutineScope(Dispatchers.Main).launch {
            while (isTracking.value!!) {
                // time difference between now and timeStarted
                lapTime = System.currentTimeMillis() - timeStarted
                // Post the new lapTime
                timeRunInMillis.postValue(timeRun + lapTime)
                if (timeRunInMillis.value!! >= lastSecondTimeStamp + 1000L) {
                    timeRunInSeconds.postValue(timeRunInSeconds.value!! + 1)
                    lastSecondTimeStamp += 1000L
                }
                delay(TIMER_UPDATE_INTERVAL)
            }
            timeRun += lapTime // adding each lapTime once the user stop the stopwatch
        }
    }

    private fun pauseService() {
        isTracking.postValue(false)
        isTimerEnabled = false
    }

    private fun startForegroundService() {
        startTimer()
        isTracking.postValue(true)

        // create notif manager
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        // construct our notif
        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_run)
            .setContentTitle("Running App")
            .setContentText("00:00:00")
            .setContentIntent(getMainActivityPendingIntent())

        // launch foreground service
        startForeground(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun getMainActivityPendingIntent() = PendingIntent.getActivity(
        this,
        0,
        Intent(this, MainActivity::class.java).also {
            it.action = ACTION_SHOW_TRACKING_FRAGMENT // will be send to main Activity
        },
        FLAG_UPDATE_CURRENT // if already exist it won't create a new notif instead updating it
    )

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }

    private fun postInitialValues() {
        isTracking.postValue(false) // postValue() -> update changes to background
        pathPoints.postValue(mutableListOf())
    }

    @SuppressLint("MissingPermission")
    private fun updateLocationTracking(isTracking: Boolean) {
        if (isTracking) {
            if (TrackingUtility.hasLocationPermission(this)) {
                val request = LocationRequest().apply {
                    // how often we get our location update
                    interval = LOCATION_UPDATE_INTERVAL
                    fastestInterval = FASTEST_LOCATION_INTERVAL
                    priority = PRIORITY_HIGH_ACCURACY
                }
                fusedLocationProviderClient.requestLocationUpdates(
                    request,
                    locationCallback, //error due to we use easy permission
                    Looper.getMainLooper()
                )
            }
        } else {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    // add emptyPolylines
    private fun addEmptyPolyline() = pathPoints.value?.apply {
        add(mutableListOf())
        pathPoints.postValue(this)
    }
        ?: pathPoints.postValue(mutableListOf(mutableListOf())) // inside 1 add coordinate, second mutablelistof add polyline

    // function to add coordinate on the last coordinate list
    private fun addPathPoint(location: Location?) {
        location?.let {
            val pos = LatLng(location.latitude, location.longitude)
            pathPoints.value?.apply {
                last().add(pos)
                pathPoints.postValue(this)
            }
        }
    }

    // to request location updates whenever the location changes
    // we can give the interval time as well
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            if (isTracking.value!!) {
                result.locations.let { locations ->
                    for (location in locations) {
                        addPathPoint(location)
                        Timber.d("New Location: ${location.latitude}, ${location.longitude}")
                    }
                }
            }
        }
    }
}