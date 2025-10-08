package ru.wb.mapkit.mapcompose.demoapp.utils

import android.app.PendingIntent
import android.content.Context
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import ru.wb.mapkit.mapcompose.location.LocationCallback
import ru.wb.mapkit.mapcompose.location.LocationEngine
import ru.wb.mapkit.mapcompose.location.LocationPriority
import ru.wb.mapkit.mapcompose.location.LocationRequestProperties
import ru.wb.mapkit.mapcompose.location.LocationResult
import com.google.android.gms.location.LocationCallback as GoogleLocationCallback
import com.google.android.gms.location.LocationResult as GoogleLocationResult
import java.util.concurrent.ConcurrentHashMap

/**
 * Реализация LocationEngine с использованием FusedLocationProviderClient
 */
class FusedLocationEngine(context: Context) : LocationEngine {

    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    // Хранилище активных callback'ов и их соответствующих Google Location Callback'ов
    private val callbackMap = ConcurrentHashMap<LocationCallback, GoogleLocationCallback>()

    override fun getLastLocation(callback: LocationCallback) {
        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    val result = if (location != null) {
                        LocationResult.create(location)
                    } else {
                        LocationResult.create(emptyList())
                    }

                    callback.onLocationResult(result)
                }
                .addOnFailureListener { exception ->
                    callback.onLocationFailure(exception)
                }
        } catch (securityException: SecurityException) {
            callback.onLocationFailure(securityException)
            throw securityException
        }
    }

    override fun requestLocationUpdates(request: LocationRequestProperties, callback: LocationCallback, looper: Looper?) {
        val googleLocationRequest = convertToGoogleLocationRequest(request)

        val googleCallback = object : GoogleLocationCallback() {
            override fun onLocationResult(locationResult: GoogleLocationResult) {
                callback.onLocationResult(LocationResult.create(locationResult.locations))
            }

            override fun onLocationAvailability(locationAvailability: LocationAvailability) {
                // no op
            }
        }

        callbackMap[callback] = googleCallback

        try {
            val targetLooper = looper ?: Looper.getMainLooper()
            fusedLocationClient.requestLocationUpdates(googleLocationRequest, googleCallback, targetLooper)
                .addOnFailureListener { exception ->
                    callbackMap.remove(callback)
                    callback.onLocationFailure(exception)
                }
        } catch (securityException: SecurityException) {
            callbackMap.remove(callback)
            callback.onLocationFailure(securityException)
            throw securityException
        }
    }

    override fun requestLocationUpdates(request: LocationRequestProperties, pendingIntent: PendingIntent) {
        val googleLocationRequest = convertToGoogleLocationRequest(request)

        try {
            fusedLocationClient.requestLocationUpdates(googleLocationRequest, pendingIntent)
        } catch (securityException: SecurityException) {
            throw securityException
        }
    }

    override fun removeLocationUpdates(callback: LocationCallback) {
        callbackMap.remove(callback)?.let { googleCallback -> fusedLocationClient.removeLocationUpdates(googleCallback) }
    }

    override fun removeLocationUpdates(pendingIntent: PendingIntent) {
        fusedLocationClient.removeLocationUpdates(pendingIntent)
    }

    fun cleanUp() {
        callbackMap.values.forEach { googleCallback -> fusedLocationClient.removeLocationUpdates(googleCallback) }
        callbackMap.clear()
    }

    private fun convertToGoogleLocationRequest(request: LocationRequestProperties): LocationRequest {
        val priority = when (request.priority) {
            LocationPriority.PRIORITY_HIGH_ACCURACY -> Priority.PRIORITY_HIGH_ACCURACY
            LocationPriority.PRIORITY_BALANCED_POWER_ACCURACY -> Priority.PRIORITY_BALANCED_POWER_ACCURACY
            LocationPriority.PRIORITY_LOW_POWER -> Priority.PRIORITY_LOW_POWER
            LocationPriority.PRIORITY_NO_POWER -> Priority.PRIORITY_PASSIVE
        }

        return LocationRequest.Builder(request.interval)
            .setPriority(priority)
            .setMinUpdateDistanceMeters(request.displacement)
            .setMinUpdateIntervalMillis(request.fastestInterval)
            .build()
    }
}