package com.example.simplelocationtraking.data.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.simplelocationtraking.MyApplication.Companion.CHANNEL_ID
import com.example.simplelocationtraking.R
import com.example.simplelocationtraking.data.error.Result
import com.example.simplelocationtraking.data.repo.LocationRepoImpl
import com.example.simplelocationtraking.domain.repo.LocationRepo
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

class LocationService : Service() {

    private lateinit var locationRepo: LocationRepo

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val coroutineExpHandle = CoroutineExceptionHandler { _, throwable ->
       println(throwable.message)
    }

    override fun onCreate() {
        super.onCreate()
        locationRepo = LocationRepoImpl(
            applicationContext,
            LocationServices.getFusedLocationProviderClient(applicationContext)
        )
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        when (intent?.action) {
            START -> {
                start(intent = intent)
            }

            STOP -> {
                stop()
            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
        stopSelf()

    }


    private fun start(intent: Intent) {
        val actionIntent = Intent(intent).also {
            it.action = "stop"
        }

        val pendingIntent = PendingIntent.getService(
            applicationContext,
            1,
            actionIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


        val notification = NotificationCompat.Builder(
            applicationContext,
            CHANNEL_ID
        )
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Tracking")
            .setContentText("Location")
            .addAction(R.drawable.ic_launcher_foreground, "Cancel", pendingIntent)


        scope.launch(coroutineExpHandle) {
            supervisorScope {
                locationRepo.getTrackingLocation(2000)
                    .catch {
                        launch {
                            throw IllegalArgumentException(it)
                        }

                    }.onEach { result ->
                        when (result) {
                            is Result.Error -> {
                                Log.e("t1", "start: ${result.error}", )
                            }

                            is Result.Success -> {
                                val latitude = result.success.latitude
                                val longitude = result.success.longitude

                                val updateNotification = notification
                                    .setContentText("Location: $latitude/$longitude")
                                notificationManager.notify(1, updateNotification.build())
                            }
                        }
                    }.launchIn(scope)
            }
            startForeground(1, notification.build())
        }
    }

    private fun stop() {
        scope.cancel()
        stopSelf()
        stopForeground(STOP_FOREGROUND_REMOVE)
    }


    companion object {
        const val START = "start"
        const val STOP = "stop"
    }

}