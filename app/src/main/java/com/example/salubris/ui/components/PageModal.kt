package com.example.salubris.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.salubris.ui.theme.ContainerBackground

@Composable
fun PageModal(tabs: Map<String, @Composable () -> Unit>) {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(ContainerBackground, shape = RoundedCornerShape(5.dp))
                .padding(5.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            tabs.forEach { (key, _) ->
                Box(
                    modifier = Modifier
                        .background(Color(40, 40, 40), shape = RoundedCornerShape(5.dp))
                        .padding(5.dp)
                ) {
                    Text(key, color = Color.White)
                }
            }
        }
        Column {
            tabs.forEach { (_, content) ->
                content()
            }
        }
    }
}