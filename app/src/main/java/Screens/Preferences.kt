package com.example.iqrarnewscompose.profile

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.iqrarnewscompose.BrandRed
import com.example.iqrarnewscompose.CategoryItem
import com.example.iqrarnewscompose.TextBlack
import com.example.iqrarnewscompose.TextGray
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.iqrarnewscompose.R

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PreferencesScreen(
    categories: List<CategoryItem>,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    // SharedPreferences
    val prefs = context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)

    // Load saved categories
    val savedCategories = prefs.getStringSet("selected_categories", emptySet()) ?: emptySet()

    // Selected state
    val selectedCategories = remember { mutableStateSetOf<String>() }

    // Notifications
    var notificationsEnabled by remember {
        mutableStateOf(prefs.getBoolean("notifications_enabled", true))
    }

    // Load on start
    LaunchedEffect(Unit) {
        selectedCategories.clear()
        selectedCategories.addAll(savedCategories)
    }

    // Parent categories only
    val parentCategories = categories.filter {
        it.parent_id == "0" || it.parent_id == null || it.parent_id.isEmpty()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Sub-Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 8.dp, end = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onBack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = BrandRed)
                }

                Text(
                    text = "Preferences",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandRed
                )
            }

        Spacer(modifier = Modifier.height(16.dp))

        // INTERESTS SECTION
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                text = "SELECT YOUR INTERESTS",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFEEEEEE))
            ) {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    parentCategories.forEach { category ->
                        val isSelected = selectedCategories.contains(category.id)
                        
                        CategoryChip(
                            label = category.name ?: "",
                            isSelected = isSelected,
                            onClick = {
                                if (isSelected) {
                                    selectedCategories.remove(category.id ?: "")
                                } else {
                                    category.id?.let { selectedCategories.add(it) }
                                }
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // LANGUAGE SECTION
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                text = "LANGUAGE",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth().clickable { /* Open Language Dialog */ },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFEEEEEE))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Language,
                            contentDescription = null,
                            tint = BrandRed,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Language",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextBlack
                        )
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "English", // This can be dynamic
                            fontSize = 15.sp,
                            color = Color.Gray
                        )
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // NOTIFICATIONS SECTION
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                text = "NOTIFICATIONS",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFEEEEEE))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = null,
                            tint = BrandRed,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Push Notifications",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextBlack
                        )
                    }
                    
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { notificationsEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = BrandRed,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color.LightGray
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Stay updated with breaking news and personalized alerts based on your interests.",
                fontSize = 12.sp,
                color = Color.Gray,
                lineHeight = 18.sp
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Save Button
        Button(
            onClick = {
                prefs.edit()
                    .putStringSet("selected_categories", selectedCategories.toSet())
                    .putBoolean("notifications_enabled", notificationsEnabled)
                    .apply()

                Toast.makeText(context, "Preferences saved!", Toast.LENGTH_SHORT).show()
                onBack()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(56.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrandRed),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Text(
                "SAVE PREFERENCES",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                letterSpacing = 0.5.sp
            )
        }

        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun CategoryChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        border = if (isSelected) null else BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.6f)),
        color = if (isSelected) BrandRed else Color.White
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else Color.Black.copy(alpha = 0.7f)
        )
    }
}