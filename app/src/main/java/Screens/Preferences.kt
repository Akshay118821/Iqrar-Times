package com.example.iqrarnewscompose.profile

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
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

        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onBack() }) {
                Icon(Icons.Default.ArrowBack, null, tint = TextBlack)
            }

            Text(
                text = "Preferences",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = BrandRed
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Title
        Text(
            text = "SELECT YOUR INTERESTS",
            modifier = Modifier.padding(start = 16.dp),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = TextGray
        )

        Text(
            text = "Choose categories for your preferred news feed",
            modifier = Modifier.padding(start = 16.dp, top = 4.dp),
            fontSize = 12.sp,
            color = TextGray
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Categories List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            items(parentCategories) { category ->

                val isSelected = selectedCategories.contains(category.id)


                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable {
                            // 🔥 UPDATE: కచ్చితంగా కేటగిరీ ID ని మాత్రమే యాడ్/రిమూవ్ చేయాలి
                            if (isSelected) {
                                selectedCategories.remove(category.id ?: "")
                            } else {
                                category.id?.let { selectedCategories.add(it) }
                            }
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected)
                            BrandRed.copy(alpha = 0.1f)
                        else
                            Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = if (isSelected) 4.dp else 1.dp
                    ),
                    border = if (isSelected) {
                        BorderStroke(2.dp, BrandRed)
                    } else {
                        BorderStroke(1.dp, Color.LightGray)
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = category.name ?: "",
                            fontSize = 16.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) BrandRed else TextBlack
                        )

                        if (isSelected) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = BrandRed,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }

        // Notifications Toggle
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Push Notifications",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextBlack
                    )
                    Text(
                        "Get notified about breaking news",
                        fontSize = 12.sp,
                        color = TextGray
                    )
                }

                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = BrandRed
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Save Button
        Button(
            onClick = {
                // 🔥 UPDATE: IDs ని సెట్ ఫార్మాట్ లో సేవ్ చేస్తున్నాం
                prefs.edit()
                    .putStringSet("selected_categories", selectedCategories.toSet())
                    .putBoolean("notifications_enabled", notificationsEnabled)
                    .apply()

                Toast.makeText(context, "Preferences saved!", Toast.LENGTH_SHORT).show()
                onBack() // మళ్ళీ ప్రొఫైల్ కి వెళ్తుంది
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(55.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrandRed)
        ) {
            Text(
                "SAVE PREFERENCES",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}