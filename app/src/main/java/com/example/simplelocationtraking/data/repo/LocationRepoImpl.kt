package com.example.simplelocationtraking.data.repo

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import android.util.Log
import com.example.simplelocationtraking.data.error.HandlePermission
import com.example.simplelocationtraking.data.error.Result
import com.example.simplelocationtraking.domain.repo.LocationRepo
import com.example.simplelocationtraking.presentance.utils.hasPermissionRequired
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.channels.onSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class LocationRepoImpl(
    private val context: Context,
    private val fusedLocationProviderClient: FusedLocationProviderClient
) : LocationRepo {
    @SuppressLint("MissingPermission")
    override fun getTrackingLocation(interval:Long): Flow<Result<Location, HandlePermission>> {
        return callbackFlow {
            try {
                if (!context.hasPermissionRequired()) {
                    trySend(Result.Error(HandlePermission.PERMISSION_NOT_DENIED))
                    close(cause = Exception(HandlePermission.PERMISSION_NOT_DENIED.name))
                    return@callbackFlow
                }

                val locationManager =
                    context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

                val gpsEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                val networkEnable =
                    locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

                if (!gpsEnable && !networkEnable) {
                    trySend(Result.Error(HandlePermission.INTERNET_OR_GPS_NOT_DENIED))
                    close(cause = Exception(HandlePermission.INTERNET_OR_GPS_NOT_DENIED.name))
                    return@callbackFlow
                }


                val locationRequest = LocationRequest.Builder(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    interval
                ).build()

                val locationCallback = object : LocationCallback() {
                    override fun onLocationResult(result: LocationResult) {
                        super.onLocationResult(result)
                        result.locations.lastOrNull()?.let { location ->
                            trySend(Result.Success(location)).onFailure {
                                Log.e("t1", "onLocationResult: $it", )
                            }.onSuccess {
                                Log.d("t1", "Succeess")
                            }
                        }
                    }

                    override fun onLocationAvailability(result: LocationAvailability) {
                        super.onLocationAvailability(result)
                        if (!result.isLocationAvailable) {
                            trySend(Result.Error(HandlePermission.CANT_GET_LOCATION))
                        }

                    }
                }

                fusedLocationProviderClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )
                awaitClose {
                    fusedLocationProviderClient.removeLocationUpdates(locationCallback)
                }
            }catch (e:Exception){
                trySend(Result.Error(HandlePermission.PERMISSION_NOT_DENIED)).onFailure {
                    throw Exception(it)
                }
                close(e)
            }

        }
    }
}