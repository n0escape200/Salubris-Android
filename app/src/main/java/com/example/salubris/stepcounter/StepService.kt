package com.example.salubris.stepcounter

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object StepRepository {
    private val _steps = MutableStateFlow(0)
    val steps: StateFlow<Int> = _steps

    fun update(value: Int) {
        _steps.value = value
    }
}

class StepService : Service(), SensorEventListener {

    companion object {
        const val CHANNEL_ID = "step_channel"
        const val NOTIFICATION_ID = 1001
        private const val TAG = "StepService"
    }

    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null
    private var baseSteps = -1f
    private var currentSteps = 0

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")
        createNotificationChannel()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        if (stepSensor == null) {
            Log.e(TAG, "No step counter sensor! Stopping service.")
            stopSelf()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand")
        startForeground(NOTIFICATION_ID, buildNotification(currentSteps))
        stepSensor?.let {
            if (baseSteps < 0) {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
                Log.d(TAG, "Sensor registered")
            }
        }
        return START_STICKY
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type != Sensor.TYPE_STEP_COUNTER) return
        val totalSteps = event.values[0]
        if (baseSteps < 0) {
            baseSteps = totalSteps
            Log.d(TAG, "Base steps set to $baseSteps")
        }
        currentSteps = (totalSteps - baseSteps).toInt().coerceAtLeast(0)
        StepRepository.update(currentSteps)
        updateNotification(currentSteps)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        sensorManager.unregisterListener(this)
        stopForeground(true)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Step Tracking",
                NotificationManager.IMPORTANCE_LOW   // LOW makes it non-intrusive, but still ongoing & non-dismissible
            ).apply {
                description = "Shows your daily step count – cannot be dismissed while tracking"
                setShowBadge(false)
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
            Log.d(TAG, "Notification channel created")
        }
    }

    private fun buildNotification(steps: Int): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Replace with your own icon if desired
            .setContentTitle("Walking Tracker")
            .setContentText("Steps today: $steps")
            .setOngoing(true)               // Prevents swipe-to-dismiss
            .setCategory(Notification.CATEGORY_SERVICE)   // Marks as a service notification
            .setOnlyAlertOnce(true)         // Only alert once when created
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification(steps: Int) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification(steps))
        Log.d(TAG, "Notification updated: $steps steps")
    }
}