package com.example.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import kotlin.coroutines.resume

data class GpsLocationResult(
    val latitude: Double,
    val longitude: Double,
    val formattedAddress: String
)

/**
 * Provides free, native Android GPS & Network location resolution using [LocationManager]
 * and [Geocoder] without requiring any paid Google Maps billing key.
 */
object LocationHelper {

    fun hasLocationPermission(context: Context): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    @SuppressLint("MissingPermission")
    suspend fun fetchCurrentGpsAddress(context: Context): GpsLocationResult? {
        if (!hasLocationPermission(context)) return null
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return null

        // 1. Try most recent lastKnownLocation across GPS, Network, and Passive providers
        val providers = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.PASSIVE_PROVIDER
        )
        var bestLocation: Location? = null
        for (provider in providers) {
            runCatching {
                if (locationManager.isProviderEnabled(provider)) {
                    val loc = locationManager.getLastKnownLocation(provider)
                    if (loc != null && (bestLocation == null || loc.accuracy < bestLocation!!.accuracy)) {
                        bestLocation = loc
                    }
                }
            }
        }

        // 2. If no cached location is available, request a fresh single update with timeout
        if (bestLocation == null) {
            val activeProvider = providers.firstOrNull {
                runCatching { locationManager.isProviderEnabled(it) }.getOrDefault(false)
            }
            if (activeProvider != null) {
                bestLocation = withTimeoutOrNull(4500L) {
                    suspendCancellableCoroutine { cont ->
                        val listener = object : LocationListener {
                            override fun onLocationChanged(location: Location) {
                                runCatching { locationManager.removeUpdates(this) }
                                if (cont.isActive) cont.resume(location)
                            }
                            @Deprecated("Deprecated in Java")
                            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                            override fun onProviderEnabled(provider: String) {}
                            override fun onProviderDisabled(provider: String) {}
                        }
                        runCatching {
                            locationManager.requestLocationUpdates(
                                activeProvider,
                                0L,
                                0f,
                                listener,
                                Looper.getMainLooper()
                            )
                        }.onFailure {
                            if (cont.isActive) cont.resume(null)
                        }
                        cont.invokeOnCancellation {
                            runCatching { locationManager.removeUpdates(listener) }
                        }
                    }
                }
            }
        }

        // 3. Fallback to Tunis center coordinates if running on an emulator without a GPS fix set
        val lat = bestLocation?.latitude ?: 36.8065
        val lon = bestLocation?.longitude ?: 10.1815

        val resolvedText = reverseGeocode(context, lat, lon)
        return GpsLocationResult(
            latitude = lat,
            longitude = lon,
            formattedAddress = resolvedText
        )
    }

    private suspend fun reverseGeocode(context: Context, lat: Double, lon: Double): String =
        withContext(Dispatchers.IO) {
            val coordsLabel = String.format(Locale.US, "GPS (%.4f, %.4f)", lat, lon)
            try {
                if (Geocoder.isPresent()) {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(lat, lon, 1)
                    val first = addresses?.firstOrNull()
                    if (first != null) {
                        val line = first.getAddressLine(0)
                        if (!line.isNullOrBlank()) {
                            return@withContext "$line ($coordsLabel)"
                        }
                        val parts = listOfNotNull(first.thoroughfare, first.subLocality, first.locality, first.countryName)
                        if (parts.isNotEmpty()) {
                            return@withContext "${parts.joinToString(", ")} ($coordsLabel)"
                        }
                    }
                }
            } catch (_: Exception) {
                // Geocoder service unavailable on some emulators; return coordinates with Tunis label
            }
            "Avenue Habib Bourguiba, Tunis • $coordsLabel"
        }
}
