package com.example.salubris.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.arm.aichat.InferenceEngine
import com.arm.aichat.InferenceEngine.State
import com.example.salubris.ui.theme.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

// ---------- Helper extension from the library ----------
val State.isModelLoaded: Boolean
    get() = this is State.ModelReady ||
            this is State.Benchmarking ||
            this is State.ProcessingSystemPrompt ||
            this is State.ProcessingUserPrompt ||
            this is State.Generating

// ---------- Message model ----------
data class ChatMessage(val text: String, val isUser: Boolean)

// ---------- Main composable ----------
@Composable
fun ChatDialog(
    engine: InferenceEngine,
    modelPath: String,
    onDismiss: () -> Unit
) {
    val engineState by engine.state.collectAsState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF121212))
        ) {
            when {
                engineState is State.Error ->
                    ErrorContent((engineState as State.Error).exception, onDismiss)

                engineState.isModelLoaded ->
                    ChatContent(engine, onDismiss)

                else ->
                    LoadingContent()
            }
        }
    }
}

// ---------- Loading screen ----------
@Composable
private fun LoadingContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(color = productColor)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Loading model...", color = Color.White, fontSize = 16.sp)
    }
}

// ---------- Error screen ----------
@Composable
private fun ErrorContent(exception: Exception, onDismiss: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Failed to load model",
            color = cancelColor,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            exception.message ?: "Unknown error",
            color = Color.LightGray,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onDismiss,
            colors = ButtonDefaults.buttonColors(containerColor = productColor)
        ) {
            Text("Close", color = Color.White)
        }
    }
}

// ---------- Actual chat UI ----------
@Composable
private fun ChatContent(engine: InferenceEngine, onDismiss: () -> Unit) {
    var userInput by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf<ChatMessage>() }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    var isGenerating by remember { mutableStateOf(false) }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(productColor)
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "AI Assistant",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, "Close", tint = Color.White)
            }
        }

        // Messages
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { msg -> ChatBubble(msg) }
        }

        // Input row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = userInput,
                onValueChange = { userInput = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type a message...", color = Color.LightGray) },
                textStyle = TextStyle(color = Color.White),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = productColor,
                    unfocusedBorderColor = Color.Gray,
                    cursorColor = productColor,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                ),
                maxLines = 4,
                enabled = !isGenerating
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    val prompt = userInput.trim()
                    if (prompt.isNotEmpty() && !isGenerating) {
                        messages.add(ChatMessage(prompt, isUser = true))
                        userInput = ""
                        isGenerating = true
                        scope.launch {
                            try {
                                engine.setSystemPrompt(
                                    "You are a helpful health assistant for the Salubris app."
                                )
                            } catch (_: Exception) {}

                            val flow: Flow<String> = engine.sendUserPrompt(prompt)
                            val responseBuilder = StringBuilder()
                            flow.catch { e ->
                                messages.add(ChatMessage("Error: ${e.message}", false))
                            }.collectLatest { token ->
                                responseBuilder.append(token)
                                val lastIndex = messages.lastIndex
                                if (lastIndex >= 0 && !messages[lastIndex].isUser) {
                                    messages[lastIndex] = ChatMessage(
                                        responseBuilder.toString(), false
                                    )
                                } else {
                                    messages.add(ChatMessage(token, false))
                                }
                            }
                            if (responseBuilder.isNotEmpty() && messages.last().isUser) {
                                messages.add(ChatMessage(responseBuilder.toString(), false))
                            }
                            isGenerating = false
                        }
                    }
                },
                enabled = userInput.trim().isNotEmpty() && !isGenerating
            ) {
                Icon(
                    Icons.Default.Send,
                    "Send",
                    tint = if (userInput.trim().isNotEmpty() && !isGenerating) productColor
                    else Color.Gray
                )
            }
        }
    }
}

// ---------- Chat bubble ----------
@Composable
private fun ChatBubble(message: ChatMessage) {
    val alignment = if (message.isUser) Alignment.CenterEnd else Alignment.CenterStart
    val bgColor = if (message.isUser) productColor else Color(50, 50, 50)

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Text(
            text = message.text,
            color = Color.White,
            modifier = Modifier
                .background(bgColor, shape = RoundedCornerShape(12.dp))
                .padding(12.dp)
                .widthIn(max = 300.dp),
            fontSize = 16.sp
        )
    }
}