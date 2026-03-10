package com.example.iqrarnewscompose

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HeaderWithToggle(
    isPreferredMode: Boolean,
    onToggleChange: (Boolean) -> Unit,
    onSettingsClick: () -> Unit,
    lang: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Title
        Text(
            text = if (isPreferredMode) {
                if (lang == "Hindi") "पसंदीदा समाचार" else "Preferred News"
            } else {
                if (lang == "Hindi") "सभी समाचार" else "All News"
            },
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextBlack
        )

        // Toggle + Settings
        Row(verticalAlignment = Alignment.CenterVertically) {

            Text(
                text = if (lang == "Hindi") "पसंदीदा" else "My Feed",
                fontSize = 13.sp,
                color = if (isPreferredMode) BrandRed else TextGray
            )

            Spacer(modifier = Modifier.width(8.dp))

            Switch(
                checked = isPreferredMode,
                onCheckedChange = onToggleChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = BrandRed,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color.LightGray
                )
            )

            Spacer(modifier = Modifier.width(4.dp))

            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "Preferences",
                    tint = BrandRed,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}