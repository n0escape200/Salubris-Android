package com.example.salubris.stepcounter

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorManager
import android.util.Log
import androidx.core.content.ContextCompat

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Boot completed, checking step sensor")
            val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
            if (stepSensor != null) {
                Log.d("BootReceiver", "Step sensor present, starting service")
                ContextCompat.startForegroundService(context, Intent(context, StepService::class.java))
            } else {
                Log.d("BootReceiver", "No step sensor, skipping service start")
            }
        }
    }
}