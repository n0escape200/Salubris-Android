package com.example.salubris.utils

import android.content.Context
import java.io.File

fun copyModel(context: Context, fileName: String): String {
    val outFile = File(context.filesDir, fileName)

    if (!outFile.exists()) {
        context.assets.open(fileName).use { input ->
            outFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }

    return outFile.absolutePath
}