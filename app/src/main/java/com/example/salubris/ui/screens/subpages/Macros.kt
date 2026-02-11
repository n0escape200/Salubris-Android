package com.example.salubris.ui.screens.subpages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.salubris.ui.components.MacroLine
import com.example.salubris.ui.theme.ContainerBackground
import com.example.salubris.ui.theme.caloriesColor
import com.example.salubris.ui.theme.carbsColor
import com.example.salubris.ui.theme.fatsColor
import com.example.salubris.ui.theme.proteinColor

@Composable
fun Macros() {
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(ContainerBackground, shape = RoundedCornerShape(10.dp)),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Assignment,
                    contentDescription = "User icon",
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text("Macros for today", color = Color.White, fontWeight = FontWeight.W600)
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp, 10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(
                    modifier = Modifier
                        .background(caloriesColor, RoundedCornerShape(5.dp))
                        .padding(10.dp, 5.dp)
                ) {
                    Text("Kcal", color = Color.White)
                    Text("2500", color = Color.White)
                }
                Column(
                    modifier = Modifier
                        .background(proteinColor, RoundedCornerShape(5.dp))
                        .padding(10.dp, 5.dp)
                ) {
                    Text("Protein", color = Color.White)
                    Text("2500", color = Color.White)
                }
                Column(
                    modifier = Modifier
                        .background(carbsColor, RoundedCornerShape(5.dp))
                        .padding(10.dp, 5.dp)
                ) {
                    Text("Carbs", color = Color.White)
                    Text("2500", color = Color.White)
                }
                Column(
                    modifier = Modifier
                        .background(fatsColor, RoundedCornerShape(5.dp))
                        .padding(10.dp, 5.dp)
                ) {
                    Text("Fats", color = Color.White)
                    Text("2500", color = Color.White)
                }
            }
        }
        Column(
            modifier = Modifier
                .background(
                    ContainerBackground,
                    shape = RoundedCornerShape(10.dp)
                )
                .padding(10.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)

        ) {
            MacroLine()
            MacroLine()
            MacroLine()
            MacroLine()
        }
    }
}