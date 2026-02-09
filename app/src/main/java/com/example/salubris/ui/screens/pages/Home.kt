package com.example.salubris.ui.screens.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.salubris.ui.theme.ContainerBackground
import com.example.salubris.ui.theme.MainContainerBorder


@Composable
fun Header() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(color = ContainerBackground, shape = MainContainerBorder)
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Row {
            Box(
                modifier = Modifier
                    .border(1.dp, Color(77, 184, 255), shape = RoundedCornerShape(50))
                    .padding(7.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "User icon",
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier
                .padding(0.dp,5.dp,0.dp,0.dp)
            ) {
                Text("Welcome back", color = Color.White)
                Text("User", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
            }
        }
    }
}


@Composable
fun TotalIntake(){
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = ContainerBackground, shape = MainContainerBorder)
            .padding(10.dp)
    ) {

    }
}

@Composable
fun Home(navController: NavController) {
    Column(modifier = Modifier
        .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Header()
        TotalIntake()
    }
}