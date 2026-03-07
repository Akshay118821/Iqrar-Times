package com.example.iqrarnewscompose.profile

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.iqrarnewscompose.CategoryItem
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items

// --- COLORS (Defined locally to ensure they appear correctly) ---
val BrandRed = Color(0xFFD32F2F)
val TextBlack = Color(0xFF111111)
val BgGrey = Color(0xFFF5F5F5)        // Light Grey Background

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    categories: List<CategoryItem>,
    onToggleHeader: (Boolean) -> Unit,
    openLogin: () -> Unit
){

    val context = LocalContext.current
    val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)

    var isLoggedIn by remember {
        mutableStateOf(prefs.getBoolean("isLoggedIn", false))
    }

    // Default View
    var localView by rememberSaveable { mutableStateOf("Main") }

    // Back Handler
    BackHandler(enabled = localView != "Main") {
        localView = "Main"
        onToggleHeader(true)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {

        when (localView) {

            // ================== MAIN MENU ==================
            "Main" -> {
                LaunchedEffect(Unit) { onToggleHeader(true) }

                if (!isLoggedIn) {
                    ProfileMenuView(
                        onNavigate = { viewName -> localView = viewName },
                        onContactClick = {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:contact@iqrartimes.com")
                            }
                            context.startActivity(intent)
                        },
                        onLoginClick = {
                            openLogin()
                        },
                    )
                } else {
                    LoggedProfileView(
                        onNavigate = { viewName -> localView = viewName }, // Added Navigation here
                        onLogout = {
                            prefs.edit().putBoolean("isLoggedIn", false).apply()
                            isLoggedIn = false
                        }
                    )
                }
            }

            // ================== PREFERENCES SCREEN (THIS WAS MISSING) ==================
            "Preferences" -> {

                LaunchedEffect(Unit) { onToggleHeader(true) }

                // -- State Variables --
                var politics by remember { mutableStateOf(true) }
                var business by remember { mutableStateOf(false) }
                var sports by remember { mutableStateOf(false) }
                var technology by remember { mutableStateOf(true) }
                var entertainment by remember { mutableStateOf(false) }
                var science by remember { mutableStateOf(false) }

                var pushEnabled by remember { mutableStateOf(true) }
                var language by remember { mutableStateOf("English") }
                val selectedCategories = remember { mutableStateListOf<String>() }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(BgGrey) // Grey BG to fix white screen issue
                        .statusBarsPadding()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {

                    Spacer(modifier = Modifier.height(10.dp))

                    // --- TITLE ---
                    Text(
                        text = "Preferences",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandRed
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // --- 1. INTERESTS ---
                    Text(
                        text = "SELECT YOUR INTERESTS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                    )

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 400.dp)
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {

                            items(categories) { category ->

                                val name = category.name ?: ""

                                val isSelected = selectedCategories.contains(name)

                                ScreenshotChip(
                                    selected = isSelected,
                                    label = name
                                ) {

                                    if (isSelected) {
                                        selectedCategories.remove(name)
                                    } else {
                                        selectedCategories.add(name)
                                    }

                                }

                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // --- 2. LANGUAGE ---
                    Text(
                        text = "LANGUAGE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                    )

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                language = if (language == "English") "Hindi" else "English"
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Translate,
                                contentDescription = null,
                                tint = BrandRed,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "Language",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextBlack
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = language,
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                            Icon(Icons.Default.ChevronRight, null, tint = Color.Gray)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // --- 3. NOTIFICATIONS ---
                    Text(
                        text = "NOTIFICATIONS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                    )

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsNone,
                                contentDescription = null,
                                tint = BrandRed,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "Push Notifications",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextBlack,
                                modifier = Modifier.weight(1f)
                            )
                            Switch(
                                checked = pushEnabled,
                                onCheckedChange = { pushEnabled = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = BrandRed,
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = Color.LightGray
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Stay updated with breaking news and personalized alerts based on your interests.",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(40.dp))

                    // --- SAVE BUTTON ---
                    Button(
                        onClick = {

                            val prefs = context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)

                            prefs.edit()
                                .putStringSet("selected_categories", selectedCategories.toSet())
                                .putBoolean("notifications", pushEnabled)
                                .putString("language", language)
                                .apply()

                            localView = "Main"
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandRed)
                    ) {
                        Text(
                            text = "SAVE PREFERENCES",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(50.dp))
                }
            }

            // ================== OTHER SCREENS ==================
            "Terms" -> {
                LocalWebViewScreen("नियम और शर्तें", "https://www.iqrartimes.com/terms-of-service") {
                    localView = "Main"
                    onToggleHeader(true)
                }
            }

            "Privacy" -> {
                LocalWebViewScreen("गोपनीयता नीति", "https://www.iqrartimes.com/privacy-policy") {
                    localView = "Main"
                    onToggleHeader(true)
                }
            }
        }
    }
}

// --- HELPER FOR CHIPS ---
@Composable
fun ScreenshotChip(
    selected: Boolean,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        color = if (selected) BrandRed else Color.White,
        shape = RoundedCornerShape(50),
        border = if (selected) null else BorderStroke(1.dp, Color(0xFFE0E0E0)),
        modifier = Modifier
            .height(36.dp)
            .clickable { onClick() }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Text(
                text = label,
                color = if (selected) Color.White else TextBlack,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// --- MENU VIEW (LOGGED OUT) ---
@Composable
fun ProfileMenuView(
    onNavigate: (String) -> Unit,
    onContactClick: () -> Unit,
    onLoginClick: () -> Unit
) {

    var isLanguageMenuExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    var selectedLanguage by remember { mutableStateOf(prefs.getString("language", "Hindi") ?: "Hindi") }
    val isEnglish = selectedLanguage == "English"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(30.dp))
        Button(
            onClick = { onLoginClick() },
            colors = ButtonDefaults.buttonColors(containerColor = BrandRed),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.width(220.dp).height(48.dp)
        ) {
            Text(if (isEnglish) "SIGN IN / SIGN UP" else "साइन इन / साइन अप", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(if (isEnglish) "Sign in to access your saved articles." else "अपने सेव किए गए लेख देखने के लिए साइन इन करें।", textAlign = TextAlign.Center, color = TextBlack, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(35.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                ProfileGridItem(Icons.Default.Language, selectedLanguage, Modifier.fillMaxWidth()) { isLanguageMenuExpanded = true }
                DropdownMenu(expanded = isLanguageMenuExpanded, onDismissRequest = { isLanguageMenuExpanded = false }) {
                    DropdownMenuItem(text = { Text("English") }, onClick = { selectedLanguage = "English"; prefs.edit().putString("language", "English").apply(); isLanguageMenuExpanded = false })
                    DropdownMenuItem(text = { Text("Hindi") }, onClick = { selectedLanguage = "Hindi"; prefs.edit().putString("language", "Hindi").apply(); isLanguageMenuExpanded = false })
                }
            }
            ProfileGridItem(Icons.Default.Notifications, if (isEnglish) "Notifications" else "सूचनाएं", Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // Updated to navigate
            ProfileGridItem(Icons.Default.Settings, if (isEnglish) "Preferences" else "प्राथमिकताएं", Modifier.weight(1f), onClick = { onNavigate("Preferences") })
            ProfileGridItem(Icons.Default.Info, if (isEnglish) "About" else "हमारे बारे में", Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(30.dp))
        ProfileListTile(Icons.Default.Security, if (isEnglish) "Privacy Policy" else "गोपनीयता नीति") { onNavigate("Privacy") }
        ProfileListTile(Icons.Default.Description, if (isEnglish) "Terms & Conditions" else "नियम और शर्तें") { onNavigate("Terms") }
        ProfileListTile(Icons.Default.Email, if (isEnglish) "Contact" else "संपर्क करें") { onContactClick() }
    }
}

// --- LOGGED IN VIEW (UPDATED TO SUPPORT NAVIGATION) ---
@Composable
fun LoggedProfileView(
    onNavigate: (String) -> Unit, // Added this parameter
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(30.dp))
        Icon(Icons.Default.AccountCircle, null, modifier = Modifier.size(110.dp), tint = Color.Gray)
        Spacer(modifier = Modifier.height(10.dp))
        Text("Kalyan Kumar", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextBlack)
        Text("Edit Profile", color = BrandRed, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(30.dp))

        ProfileListTile(Icons.Default.Language, "Language") {}
        ProfileListTile(Icons.Default.Notifications, "Notifications") {}

        // FIX: Added Navigation Click Here
        ProfileListTile(Icons.Default.Settings, "Preferences") {
            onNavigate("Preferences")
        }

        ProfileListTile(Icons.Default.Info, "About") {}
        ProfileListTile(Icons.Default.Security, "Privacy Policy") { onNavigate("Privacy") }
        ProfileListTile(Icons.Default.Description, "Terms & Conditions") { onNavigate("Terms") }
        ProfileListTile(Icons.Default.Email, "Contact") {}

        Spacer(modifier = Modifier.height(20.dp))
        ProfileListTile(Icons.Default.Logout, "Log Out") { onLogout() }
    }
}

@Composable
fun ProfileGridItem(icon: ImageVector, text: String, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    OutlinedButton(onClick = onClick, shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, Color(0xFFE0E0E0)), modifier = modifier.height(65.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, modifier = Modifier.size(22.dp), tint = Color.Black)
            Spacer(Modifier.width(10.dp))
            Text(text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
        }
    }
}

@Composable
fun ProfileListTile(icon: ImageVector, label: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).height(60.dp).clickable { onClick() },
        shape = RoundedCornerShape(10.dp), border = BorderStroke(1.dp, Color(0xFFF0F0F0)), colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, modifier = Modifier.size(22.dp), tint = Color.Black)
            Spacer(modifier = Modifier.width(18.dp))
            Text(label, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color.Black)
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, null, tint = Color.Gray)
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LocalWebViewScreen(title: String, url: String, onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Row(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = BrandRed) }
            Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(start = 8.dp), color = TextBlack)
        }
        HorizontalDivider()
        AndroidView(factory = { ctx -> WebView(ctx).apply { layoutParams = ViewGroup.LayoutParams(-1, -1); settings.javaScriptEnabled = true; webViewClient = WebViewClient(); loadUrl(url) } }, modifier = Modifier.fillMaxSize())
    }
}
//some of the changes are done in this codes today