package com.example.salubris.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.salubris.ui.theme.Purple80
import com.example.salubris.ui.theme.submitColor

data class GoalOptionUi(
    val title: String,
    val subtitle: String,
    val isSelected: Boolean,
    val onSelect: () -> Unit
)

@Composable
fun GoalSelector(options: List<GoalOptionUi>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, option ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { option.onSelect() }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = option.isSelected,
                    onClick = option.onSelect,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = option.title,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    // Styled subtitle: highlight calorie number and weight change
                    Text(
                        text = buildAnnotatedString {
                            // Split subtitle into parts: e.g., "1800 kcal/day · +0.5 kg/week"
                            val parts = option.subtitle.split("·")
                            if (parts.size == 2) {
                                withStyle(style = SpanStyle(color = submitColor, fontWeight = FontWeight.Bold)) {
                                    append(parts[0].trim())
                                }
                                append(" · ")
                                withStyle(style = SpanStyle(color = Purple80, fontWeight = FontWeight.Bold)) {
                                    append(parts[1].trim())
                                }
                            } else {
                                append(option.subtitle)
                            }
                        },
                        fontSize = 14.sp
                    )
                }
            }
            // Separator line
            if (index < options.lastIndex) {
                HorizontalDivider(
                    color = Color.Gray.copy(alpha = 0.3f),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}