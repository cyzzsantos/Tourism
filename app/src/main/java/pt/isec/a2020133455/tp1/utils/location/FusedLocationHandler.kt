package pt.isec.a2020133455.tp1.utils.location

import android.annotation.SuppressLint
import android.location.Location
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority.*

class FusedLocationHandler(private val locationProvider: FusedLocationProviderClient) :
    LocationHandler {
    override var locationEnabled = false
    override var onLocation: ((Location) -> Unit)? = null

    @SuppressLint("MissingPermission")
    override fun startLocationUpdates() {
        if (locationEnabled)
            return

        val notify = onLocation ?: return

        notify(Location(null).apply {
            latitude = 0.0
            longitude = 0.0
        })

        locationProvider.lastLocation
            .addOnSuccessListener { location ->
                location?.let(notify)
            }
        val locationRequest =
            LocationRequest.Builder(PRIORITY_BALANCED_POWER_ACCURACY, 6000)
                .setMinUpdateDistanceMeters(100f)
                .setMinUpdateIntervalMillis(3000)
                .setMaxUpdateDelayMillis(10000)
                .setPriority(PRIORITY_BALANCED_POWER_ACCURACY)
                .setIntervalMillis(6000)
                .build()
        locationProvider.requestLocationUpdates(locationRequest,locationCallback, Looper.myLooper())

        locationEnabled = true
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            result.locations.forEach(onLocation)
        }
    }
    override fun stopLocationUpdates() {
        if (!locationEnabled)
            return
        locationProvider.removeLocationUpdates(locationCallback)
        locationEnabled = false
    }

}