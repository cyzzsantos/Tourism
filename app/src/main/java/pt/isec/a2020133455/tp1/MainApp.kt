package pt.isec.a2020133455.tp1

import android.app.Application
import android.location.LocationManager
import pt.isec.a2020133455.tp1.utils.location.LocationHandler
import pt.isec.a2020133455.tp1.utils.location.LocationManagerHandler

class TP1App : Application() {

    val locationHandler: LocationHandler by lazy {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        LocationManagerHandler(locationManager)
    }

    override fun onCreate() {
        super.onCreate()
    }
}