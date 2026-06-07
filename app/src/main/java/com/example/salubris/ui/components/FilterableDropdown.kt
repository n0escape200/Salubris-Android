package com.example.salubris.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.window.PopupProperties
import kotlinx.coroutines.delay

@Composable
fun <T> FilterableDropdown(
    options: List<T>,
    selectedItem: T?,
    onItemSelected: (T?) -> Unit,
    label: String,
    displayText: (T) -> String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    textFieldColors: TextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        focusedBorderColor = Color.White,
        unfocusedBorderColor = Color.White.copy(alpha = 0.7f),
        focusedLabelColor = Color.White,
        unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
        cursorColor = Color.White
    )
) {
    var expanded by remember { mutableStateOf(false) }
    var text by remember { mutableStateOf(selectedItem?.let(displayText) ?: "") }
    var textFieldSize by remember { mutableStateOf(Size.Zero) }

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(selectedItem) {
        text = selectedItem?.let(displayText) ?: ""
    }

    val filteredOptions = remember(text, options) {
        if (text.isBlank()) {
            options
        } else {
            options.filter {
                displayText(it).contains(text, ignoreCase = true)
            }
        }
    }

    Box(modifier = modifier) {

        OutlinedTextField(
            value = text,
            onValueChange = { newText ->
                text = newText

                expanded = filteredOptions.isNotEmpty()

                if (newText.isBlank()) {
                    onItemSelected(null)
                }
            },
            enabled = enabled,
            label = { Text(label) },
            singleLine = true,
            keyboardActions = KeyboardActions(
                onDone = {
                    expanded = false
                    keyboardController?.hide()
                }
            ),
            trailingIcon = {
                IconButton(
                    onClick = {
                        expanded = !expanded

                        if (expanded) {
                            focusRequester.requestFocus()
                        }
                    }
                ) {
                    Icon(
                        imageVector =
                            if (expanded)
                                Icons.Default.KeyboardArrowUp
                            else
                                Icons.Default.KeyboardArrowDown,
                        contentDescription = null
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .onGloballyPositioned {
                    textFieldSize = it.size.toSize()
                },
            colors = textFieldColors
        )

        DropdownMenu(
            expanded = expanded && filteredOptions.isNotEmpty(),
            onDismissRequest = {
                expanded = false
            },
            properties = PopupProperties(
                focusable = false
            ),
            modifier = Modifier
                .width(
                    with(LocalDensity.current) {
                        textFieldSize.width.toDp()
                    }
                )
                .heightIn(max = 250.dp)
                .background(Color.DarkGray),
            containerColor = Color.DarkGray,
            shape = RoundedCornerShape(8.dp)
        ) {

            filteredOptions.forEachIndexed { index, option ->

                DropdownMenuItem(
                    text = {
                        Text(
                            text = displayText(option),
                            color = Color.White
                        )
                    },
                    onClick = {
                        text = displayText(option)
                        expanded = false

                        onItemSelected(option)

                        keyboardController?.hide()
                    },
                    colors = MenuDefaults.itemColors(
                        textColor = Color.White
                    ),
                    contentPadding = PaddingValues(
                        horizontal = 16.dp,
                        vertical = 12.dp
                    )
                )

                if (index < filteredOptions.lastIndex) {
                    HorizontalDivider(
                        color = Color.Gray,
                        thickness = 0.5.dp
                    )
                }
            }
        }
    }
}