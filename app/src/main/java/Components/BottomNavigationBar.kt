package com.example.iqrarnewscompose.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.iqrarnewscompose.BrandRed
import com.example.iqrarnewscompose.NotoSansFont
import com.example.iqrarnewscompose.TextBlack

@Composable
fun IqrarBottomBar(currentScreen: String, onNavigate: (String) -> Unit) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 10.dp
    ) {
        val items = listOf("न्यूज़", "वीडियोज़", "लाइव टीवी", "ई-पेपर", "प्रोफ़ाइल")
        val icons = listOf(
            Icons.Default.Newspaper,
            Icons.Default.PlayCircle,
            Icons.Default.LiveTv,
            Icons.Default.Description,
            Icons.Default.Person
        )

        items.forEachIndexed { index, item ->
            val isSelected = (currentScreen == "Home" && item == "News") || currentScreen == item

            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = icons[index],
                        contentDescription = item,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = item,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        // ERROR FIX: NotoSans badulu NotoSansFont vadanu
                        fontFamily = NotoSansFont
                    )
                },
                selected = isSelected,
                onClick = {
                    if (item == "News") onNavigate("Home") else onNavigate(item)
                },

                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = BrandRed,
                    unselectedIconColor = TextBlack,
                    selectedTextColor = BrandRed,
                    unselectedTextColor = TextBlack,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}