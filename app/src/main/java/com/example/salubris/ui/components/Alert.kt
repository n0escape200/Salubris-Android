package com.example.salubris.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun Alert(
    open: Boolean,
    onDismiss: () -> Unit
) {
    if (!open) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Product") },
        text = { Text("This is your modal content.\nYou can put forms or any composable here.") },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton (onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

