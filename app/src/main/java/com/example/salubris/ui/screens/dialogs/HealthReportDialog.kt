package com.example.salubris.ui.screens.dialogs

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.salubris.utils.Vocabulary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

@Composable
fun HealthReportDialog(
    generatePayload: suspend () -> String,
    onDismiss: () -> Unit,
    onSuccess: (String) -> Unit = {}
) {
    var isLoading by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf(false) }

    // Response data
    var mobileReport by remember { mutableStateOf("") }
    var currentHealth by remember { mutableStateOf("") }
    var advice by remember { mutableStateOf("") }
    var potentialConditions by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF121212))
        ) {
            // Close button
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp)
                    .padding(top = 64.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title with icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "🏥 ",
                        fontSize = 28.sp
                    )
                    Text(
                        text = Vocabulary.get().healthReport,
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Content area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(
                            Color(0xFF1E1E1E),
                            shape = MaterialTheme.shapes.medium
                        )
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    when {
                        isLoading -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                CircularProgressIndicator(
                                    color = Color(0xFF4CAF50),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Generating your health report...\nThis may take a few minutes.",
                                    color = Color.White,
                                    textAlign = TextAlign.Center,
                                    fontSize = 16.sp
                                )
                            }
                        }

                        isSuccess -> {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Show the mobile-formatted report
                                if (mobileReport.isNotEmpty()) {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color(0xFF2A2A2A)
                                        ),
                                        shape = MaterialTheme.shapes.small
                                    ) {
                                        Text(
                                            text = mobileReport,
                                            color = Color.White,
                                            fontSize = 15.sp,
                                            lineHeight = 24.sp,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp)
                                        )
                                    }
                                } else {
                                    // Fallback: show individual fields if mobile is missing
                                    if (currentHealth.isNotEmpty()) {
                                        Text(
                                            text = "Current Health",
                                            color = Color(0xFF4CAF50),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                        Text(
                                            text = currentHealth,
                                            color = Color.White,
                                            fontSize = 14.sp
                                        )
                                    }
                                    if (advice.isNotEmpty()) {
                                        Text(
                                            text = "Advice",
                                            color = Color(0xFF4CAF50),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                        Text(
                                            text = advice,
                                            color = Color.White,
                                            fontSize = 14.sp
                                        )
                                    }
                                    if (potentialConditions.isNotEmpty()) {
                                        Text(
                                            text = "Potential Conditions",
                                            color = Color(0xFF4CAF50),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                        Text(
                                            text = potentialConditions,
                                            color = Color.White,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }

                        isError -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    text = "❌ Error",
                                    color = Color.Red,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = statusMessage ?: "An unknown error occurred.",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        else -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    text = "Press the button below to generate your personalized health report.",
                                    color = Color.Gray,
                                    fontSize = 16.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action button
                Button(
                    onClick = {
                        if (!isLoading) {
                            isLoading = true
                            isError = false
                            isSuccess = false
                            statusMessage = null
                            mobileReport = ""
                            currentHealth = ""
                            advice = ""
                            potentialConditions = ""

                            scope.launch {
                                try {
                                    val payload = generatePayload()
                                    Log.d("HealthReport", "Payload generated: $payload")

                                    val result = withContext(Dispatchers.IO) {
                                        submitReport(payload)
                                    }

                                    if (result.isSuccess) {
                                        val responseBody = result.getOrNull() ?: ""
                                        try {
                                            val json = JSONObject(responseBody)
                                            // Parse all fields
                                            mobileReport = json.optString("mobile", "")
                                            currentHealth = json.optString("currentHealth", "")
                                            advice = json.optString("advice", "")
                                            potentialConditions =
                                                json.optString("potentialConditions", "")

                                            // If mobile is empty, build it from individual fields
                                            if (mobileReport.isEmpty()) {
                                                mobileReport = buildString {
                                                    if (currentHealth.isNotEmpty()) {
                                                        appendLine("Current health:")
                                                        appendLine(currentHealth)
                                                        appendLine()
                                                    }
                                                    if (advice.isNotEmpty()) {
                                                        appendLine("Advice:")
                                                        appendLine(advice)
                                                        appendLine()
                                                    }
                                                    if (potentialConditions.isNotEmpty()) {
                                                        appendLine("Potential conditions:")
                                                        appendLine(potentialConditions)
                                                    }
                                                }
                                            }

                                            isSuccess = true
                                            statusMessage = "Report received"
                                            onSuccess(responseBody)
                                        } catch (e: Exception) {
                                            isError = true
                                            statusMessage = "Invalid response format: ${e.message}"
                                            Log.e("HealthReport", "Failed to parse response", e)
                                        }
                                    } else {
                                        val exception = result.exceptionOrNull()
                                        isError = true
                                        statusMessage = when (exception) {
                                            is UnknownHostException -> "Cannot reach the server. Check your internet connection."
                                            is ConnectException -> "Connection refused. Is the server running?"
                                            is SocketTimeoutException -> "Connection timed out. The server took too long to respond."
                                            is IOException -> "Network error: ${exception.message}"
                                            else -> exception?.message ?: "Unknown error"
                                        }
                                        Log.e("HealthReport", "Request failed", exception)
                                    }
                                } catch (e: Exception) {
                                    isError = true
                                    statusMessage = e.message ?: "Unexpected error"
                                    Log.e("HealthReport", "Unexpected error", e)
                                } finally {
                                    isLoading = false
                                }
                            }
                        }
                    },
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSuccess) Color(0xFF4CAF50) else Color(0xFF2E7D32)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        when {
                            isLoading -> "Submitting..."
                            isSuccess -> "Generate New Report"
                            else -> "Generate Health Report"
                        },
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }

                if (isError) {
                    TextButton(onClick = {
                        isError = false
                        statusMessage = null
                    }) {
                        Text("Dismiss", color = Color.Gray)
                    }
                }
            }
        }
    }
}

// Suspending function to perform the POST request
private suspend fun submitReport(payload: String): Result<String> {
    return try {
        val client = OkHttpClient.Builder()
            .connectTimeout(300, TimeUnit.SECONDS)
            .writeTimeout(300, TimeUnit.SECONDS)
            .readTimeout(300, TimeUnit.SECONDS)
            .build()

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = payload.toRequestBody(mediaType)

        // Update this endpoint as needed
        val endpoint = "http://192.168.1.130:3000/report"
        Log.d("HealthReport", "Sending POST to $endpoint")

        val request = Request.Builder()
            .url(endpoint)
            .post(body)
            .addHeader("Accept", "application/json")
            .build()

        client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string() ?: ""
            if (response.isSuccessful) {
                Log.d("HealthReport", "Response: $responseBody")
                Result.success(responseBody)
            } else {
                val errorMsg = "Server error: ${response.code} - $responseBody"
                Log.e("HealthReport", errorMsg)
                Result.failure(IOException(errorMsg))
            }
        }
    } catch (e: Exception) {
        Log.e("HealthReport", "Network exception", e)
        Result.failure(e)
    }
}