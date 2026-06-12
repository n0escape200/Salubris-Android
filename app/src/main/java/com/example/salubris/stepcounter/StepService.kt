package com.example.salubris.stepcounter

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
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
import java.text.SimpleDateFormat
import java.util.*

object StepRepository {
    private val _steps = MutableStateFlow(0)
    val steps: StateFlow<Int> = _steps

    private val _sensorAvailable = MutableStateFlow(true)
    val sensorAvailable: StateFlow<Boolean> = _sensorAvailable

    fun updateSteps(value: Int) {
        _steps.value = value
    }

    fun setSensorAvailable(available: Boolean) {
        _sensorAvailable.value = available
    }
}

class StepService : Service(), SensorEventListener {

    companion object {
        const val ACTION_RESET_STEPS = "com.example.salubris.RESET_STEPS"
        const val CHANNEL_ID = "step_channel"
        const val NOTIFICATION_ID = 1001
        private const val TAG = "StepService"
        private const val ALARM_REQUEST_CODE = 2001
        private const val RESTART_REQUEST_CODE = 2002
    }

    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null
    private var baseSteps = -1f
    private var currentSteps = 0
    private var lastResetDate: String? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")
        createNotificationChannel()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand action: ${intent?.action}")

        // If no step sensor, show a dummy notification and stop gracefully
        if (stepSensor == null) {
            Log.e(TAG, "No step counter sensor. Showing notification and stopping.")
            StepRepository.setSensorAvailable(false)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    NOTIFICATION_ID,
                    buildDummyNotification(),
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH
                )
            } else {
                startForeground(NOTIFICATION_ID, buildDummyNotification())
            }
            stopSelf()
            return START_NOT_STICKY
        }

        // Sensor exists, now start foreground properly
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                buildNotification(currentSteps),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH
            )
        } else {
            startForeground(NOTIFICATION_ID, buildNotification(currentSteps))
        }
        StepRepository.setSensorAvailable(true)

        when (intent?.action) {
            ACTION_RESET_STEPS -> resetSteps()
        }

        if (baseSteps < 0) {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL)
            Log.d(TAG, "Sensor registered")
            scheduleMidnightReset()
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

        val today = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        if (lastResetDate != null && lastResetDate != today) {
            baseSteps = totalSteps
            Log.d(TAG, "Date changed, new base set to $baseSteps")
        }
        lastResetDate = today

        currentSteps = (totalSteps - baseSteps).toInt().coerceAtLeast(0)
        StepRepository.updateSteps(currentSteps)
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

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        val restartIntent = Intent(this, StepService::class.java)
        val pendingIntent = PendingIntent.getService(
            this,
            RESTART_REQUEST_CODE,
            restartIntent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.set(
            AlarmManager.RTC,
            System.currentTimeMillis() + 100,
            pendingIntent
        )
        Log.d(TAG, "Task removed, scheduling service restart")
    }

    private fun resetSteps() {
        baseSteps = -1f
        currentSteps = 0
        StepRepository.updateSteps(0)
        updateNotification(0)
        Log.d(TAG, "Steps manually reset")
    }

    private fun scheduleMidnightReset() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, MidnightResetReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val triggerTime = calendar.timeInMillis

        try {
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
            Log.d(TAG, "Midnight reset scheduled for ${Date(triggerTime)}")
        } catch (e: SecurityException) {
            Log.e(TAG, "Exact alarm permission missing. Fallback to date check only.")
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Step Tracking",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Shows your daily step count"
                setShowBadge(false)
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
            Log.d(TAG, "Notification channel created")
        }
    }

    private fun buildNotification(steps: Int): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Walking Tracker")
            .setContentText("Steps today: $steps")
            .setOngoing(true)
            .setAutoCancel(false)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    private fun buildDummyNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Step Tracker Unavailable")
            .setContentText("Your device does not have a step counter sensor.")
            .setOngoing(false)
            .build()
    }

    private fun updateNotification(steps: Int) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification(steps))
    }
}