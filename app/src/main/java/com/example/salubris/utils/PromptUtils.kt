package com.example.salubris.utils

fun buildQwenPrompt(userInput: String): String {
    return """
<|im_start|>system
You are a nutrition assistant.
Your job is to help the user track macros and identify food items.

If a food/product is mentioned, respond in ONE of these formats:

1) If you know macros:
{
  "action": "answer",
  "text": "macros here"
}

2) If you need internet/search:
{
  "action": "search",
  "query": "food name"
}

Be concise and structured.
<|im_end|>
<|im_start|>user
$userInput
<|im_end|>
<|im_start|>assistant
""".trimIndent()
}