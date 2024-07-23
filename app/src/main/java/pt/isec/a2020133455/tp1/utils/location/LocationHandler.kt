package pt.isec.a2020133455.tp1.utils.location

import android.location.Location

interface LocationHandler {
    var locationEnabled : Boolean
    var onLocation : ((Location) -> Unit)?
    fun startLocationUpdates()
    fun stopLocationUpdates()
}

