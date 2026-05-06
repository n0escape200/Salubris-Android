package com.example.salubris.utils

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.salubris.ui.components.Input

@Composable
fun RenderFormFields(fields:  SnapshotStateList<FormData>, ){
    Column {
        fields.forEachIndexed { index, field ->
            when (field.type) {
                FieldType.STRING -> {
                    Input(
                        label = field.name,
                        value = field.value as? String ?: "",
                        onChange = { newValue ->
                            fields[index] = field.copy(value = newValue)
                        }
                    )
                }
                FieldType.NUMBER -> {
                    Input(
                        label = field.name,
                        value = (field.value as? Number ?: "").toString(),
                        onChange = { newValue ->
                            val numberValue = newValue.toIntOrNull() ?: ""
                            fields[index] = field.copy(value = numberValue)
                        },
                        keyboardType = KeyboardType.Number
                    )
                }
                FieldType.BOOLEAN -> {
                    // You can add a Checkbox or Switch component here
                    Text(
                        text = "${field.name}: ${field.value}",
                        color = Color.White
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}