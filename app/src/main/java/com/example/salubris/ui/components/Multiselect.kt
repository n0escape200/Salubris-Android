package com.example.salubris.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun Multiselect(
    valueList: Array<String>,
    selectedIndex: Int? = null,
    onItemSelected: ((Int) -> Unit)? = null
) {
    Column {
        valueList.forEachIndexed { index, item ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = selectedIndex == index,
                    onCheckedChange = { isChecked ->
                        if (isChecked) {
                            onItemSelected?.invoke(index)
                        }
                    }
                )
                Text(
                    text = item,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = if (selectedIndex == index) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}