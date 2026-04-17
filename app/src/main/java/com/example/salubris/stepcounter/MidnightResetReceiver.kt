package com.example.salubris.stepcounter

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat

class MidnightResetReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("MidnightReset", "Midnight reset triggered")
        val serviceIntent = Intent(context, StepService::class.java).apply {
            action = StepService.ACTION_RESET_STEPS
        }
        ContextCompat.startForegroundService(context, serviceIntent)
    }
}