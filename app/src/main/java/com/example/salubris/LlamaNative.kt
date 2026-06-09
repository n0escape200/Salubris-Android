package com.example.salubris

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class LlamaNative {
    private var modelLoaded = false
    private val TAG = "LlamaNative"

    init {
        try {
            System.loadLibrary("llama")
            Log.d(TAG, "Native library loaded")
        } catch (e: UnsatisfiedLinkError) {
            Log.e(TAG, "Failed to load native library 'llama'", e)
        }
    }

    // Native methods (declared in C++)
    private external fun nativeLoadModel(modelPath: String): Boolean
    private external fun nativeGenerate(prompt: String): String

    suspend fun loadModel(modelPath: String): Boolean = withContext(Dispatchers.IO) {
        Log.d(TAG, "Loading model from: $modelPath")
        val file = File(modelPath)
        if (!file.exists()) {
            Log.e(TAG, "Model file not found at $modelPath")
            return@withContext false
        }
        val success = try {
            nativeLoadModel(modelPath)
        } catch (e: Exception) {
            Log.e(TAG, "Exception during nativeLoadModel", e)
            false
        }
        modelLoaded = success
        if (success) Log.d(TAG, "Model loaded successfully")
        else Log.e(TAG, "Model loading failed")
        success
    }

    suspend fun generate(prompt: String): String = withContext(Dispatchers.IO) {
        if (!modelLoaded) {
            Log.e(TAG, "Generate called but model not loaded")
            return@withContext "LLM not loaded. Please wait for model initialization."
        }
        try {
            val result = nativeGenerate(prompt)
            Log.d(TAG, "Generated response length: ${result.length}")
            result
        } catch (e: Exception) {
            Log.e(TAG, "Exception during nativeGenerate", e)
            "Error generating response: ${e.message}"
        }
    }
}