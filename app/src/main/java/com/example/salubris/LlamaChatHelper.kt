package com.example.salubris

import android.content.ContentResolver
import android.net.Uri
import com.github.ljcamargo.llamacpp.LlamaHelper as CoreLlamaHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch

class LlamaChatHelper(private val contentResolver: ContentResolver) {
    private var coreHelper: CoreLlamaHelper? = null
    private val _response = MutableStateFlow("")
    val response = _response.asStateFlow()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val _eventFlow = MutableSharedFlow<CoreLlamaHelper.LLMEvent>()
    private var modelLoaded = false

    suspend fun loadModel(modelPath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val uri = Uri.parse("file://$modelPath")
            coreHelper = CoreLlamaHelper(contentResolver, scope, _eventFlow)
            coreHelper?.loadModel(uri, nCtx = 512, nThreads = 4)
            modelLoaded = true
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun generate(prompt: String): String = withContext(Dispatchers.IO) {
        if (!modelLoaded) return@withContext "Model not loaded"
        _response.value = ""
        coreHelper?.predict(prompt)

        // Collect tokens until "Done" is received
        _eventFlow.collect { event ->
            when (event) {
                is CoreLlamaHelper.LLMEvent.Ongoing -> {
                    _response.value += event.word
                }
                is CoreLlamaHelper.LLMEvent.Done -> {
                    // This return@withContext works if it's the last statement in collect
                    // but collect is a terminal operator that doesn't return normally.
                    // We need a way to break out of collect.
                }
            }
        }
        _response.value
    }
}