package com.example.salubris.ai

import android.content.Context
import com.arm.aichat.AiChat
import com.arm.aichat.InferenceEngine
import kotlinx.coroutines.flow.Flow

class LlmController(private val context: Context) {

    lateinit var engine: InferenceEngine
        private set

    var isReady = false
        private set

    suspend fun init() {
        engine = AiChat.getInferenceEngine(context)
    }

    suspend fun loadModel(path: String) {
        engine.loadModel(path)
        isReady = true
    }

    fun chat(prompt: String): Flow<String> {
        return engine.sendUserPrompt(prompt)
    }

    fun destroy() {
        if (::engine.isInitialized) {
            engine.destroy()
        }
    }
}