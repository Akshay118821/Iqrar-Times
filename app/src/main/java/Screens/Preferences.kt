package com.example.iqrarnewscompose.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.iqrarnewscompose.BrandRed

@Composable
fun PreferencesScreen(onBack: () -> Unit) {

    var notificationsEnabled by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(onClick = { onBack() }) {
                Icon(Icons.Default.ArrowBack, null)
            }

            Text(
                text = "Preferences",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = BrandRed
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "SELECT YOUR INTERESTS",
            modifier = Modifier.padding(start = 16.dp),
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        FlowRow(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            listOf(
                "Politics",
                "Business",
                "Sports",
                "Technology",
                "Entertainment",
                "Science"
            ).forEach {

                Button(
                    onClick = {},
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandRed)
                ) {
                    Text(it)
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                "Push Notifications",
                fontSize = 16.sp,
                modifier = Modifier.weight(1f)
            )

            Switch(
                checked = notificationsEnabled,
                onCheckedChange = {
                    notificationsEnabled = it
                }
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = { },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(55.dp),
            shape = RoundedCornerShape(30),
            colors = ButtonDefaults.buttonColors(containerColor = BrandRed)
        ) {

            Text(
                "SAVE PREFERENCES",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}