package com.example.salubris.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.salubris.ui.theme.caloriesColor
import com.example.salubris.ui.theme.carbsColor
import com.example.salubris.ui.theme.fatsColor
import com.example.salubris.ui.theme.proteinColor

@Composable
fun MacroLine() {
    Row(
        modifier = Modifier
            .background(Color(60, 60, 60), RoundedCornerShape(10.dp))
            .padding(10.dp)
            .fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text("Title", color = Color.White, fontWeight = FontWeight.W600, fontSize = 27.sp)
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(caloriesColor, RoundedCornerShape(5.dp))
                        .padding(5.dp, 2.dp)
                ) { Text("K: 3124", color = Color.White) }
                Box(
                    modifier = Modifier
                        .background(proteinColor, RoundedCornerShape(5.dp))
                        .padding(5.dp, 2.dp)
                ) { Text("P: 3124", color = Color.White) }
                Box(
                    modifier = Modifier
                        .background(carbsColor, RoundedCornerShape(5.dp))
                        .padding(5.dp, 2.dp)
                ) { Text("C: 3124", color = Color.White) }
                Box(
                    modifier = Modifier
                        .background(fatsColor, RoundedCornerShape(5.dp))
                        .padding(5.dp, 2.dp)
                ) { Text("F: 3124", color = Color.White) }
            }
        }
    }
}