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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.edit
import com.example.iqrarnewscompose.CategoryItem
import com.example.iqrarnewscompose.BrandRed
import com.example.iqrarnewscompose.TextBlack
import com.example.iqrarnewscompose.api.RetrofitInstance
import com.google.firebase.messaging.FirebaseMessaging

@Composable
fun ProfileScreen(
    categories: List<CategoryItem>,
    onToggleHeader: (Boolean) -> Unit,
    openLogin: () -> Unit,
    currentLanguage: String = "Hindi",
    onLanguageChange: (String) -> Unit = {}
){
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)

    var isLoggedIn by remember { mutableStateOf(prefs.getBoolean("isLoggedIn", false)) }

    DisposableEffect(prefs) {
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { p, key ->
            if (key == "isLoggedIn") {
                isLoggedIn = p.getBoolean("isLoggedIn", false)
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    var localView by rememberSaveable { mutableStateOf("Main") }

    BackHandler(enabled = localView != "Main") {
        localView = "Main"
        onToggleHeader(true)
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        when (localView) {
            "Main" -> {
                LaunchedEffect(Unit) { onToggleHeader(true) }

                if (!isLoggedIn) {
                    ProfileMenuView(
                        onNavigate = {
                            localView = it
                            if (it != "Main") onToggleHeader(false)
                        },
                        onLoginClick = { openLogin() },
                        onContactClick = {
                            val intent = Intent(Intent.ACTION_SENDTO).apply { data = Uri.parse("mailto:contact@iqrartimes.com") }
                            context.startActivity(intent)
                        },
                        currentLanguage = currentLanguage,
                        onLanguageChange = onLanguageChange
                    )
                } else {
                    LoggedProfileView(
                        onNavigate = {
                            localView = it
                            if (it != "Main") onToggleHeader(false)
                        },
                        onLogout = {
                            prefs.edit { putBoolean("isLoggedIn", false) }
                        },
                        currentLanguage = currentLanguage,
                        onLanguageChange = onLanguageChange
                    )
                }
            }

            "Preferences" -> {
                PreferencesScreen(
                    categories = categories,
                    currentLanguage = currentLanguage,
                    onLanguageChange = onLanguageChange,
                    onBack = { localView = "Main"; onToggleHeader(true) }
                )
            }

            // 🔥 UPDATED: TERMS & CONDITIONS LINK
            "Terms" -> LocalWebViewScreen(
                title = "Terms & Conditions",
                url = "https://www.iqrartimes.com/terms-of-service",
                onBack = { localView = "Main"; onToggleHeader(true) }
            )

            // 🔥 UPDATED: PRIVACY POLICY LINK
            "Privacy" -> LocalWebViewScreen(
                title = "Privacy Policy",
                url = "https://www.iqrartimes.com/privacy-policy",
                onBack = { localView = "Main"; onToggleHeader(true) }
            )

            // 🔥 ABOUT PAGE WEBVIEW
            "About" -> LocalWebViewScreen(
                title = "About Us",
                url = "https://www.iqrartimes.com/about",
                onBack = { localView = "Main"; onToggleHeader(true) }
            )

            // 🔥 NOTIFICATION SETTINGS SCREEN
            "Notifications" -> NotificationSettingsScreen(
                onBack = { localView = "Main"; onToggleHeader(true) }
            )
        }
    }
}

@Composable
fun LoggedProfileView(
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit,
    currentLanguage: String = "Hindi",
    onLanguageChange: (String) -> Unit = {}
) {
    var showLanguageDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // 🔥 Backend nunchi save chesina email read chestunam
    val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    val userEmail = prefs.getString("userEmail", "") ?: ""
    val displayName = if (userEmail.isNotEmpty()) "ID: $userEmail" else "User"

    if (showLanguageDialog) {
        LanguagePickerDialog(
            currentLanguage = currentLanguage,
            onSelect = { lang -> onLanguageChange(lang); showLanguageDialog = false },
            onDismiss = { showLanguageDialog = false }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(30.dp))

        Box(contentAlignment = Alignment.BottomEnd) {
            Surface(
                modifier = Modifier.size(110.dp),
                shape = CircleShape,
                color = Color.LightGray
            ) {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = com.example.iqrarnewscompose.R.drawable.ic_user_profile),
                    contentDescription = "User Profile",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            }
            Surface(modifier = Modifier.size(30.dp), shape = CircleShape, color = BrandRed, border = BorderStroke(2.dp, Color.White)) {
                Icon(Icons.Default.Edit, null, tint = Color.White, modifier = Modifier.padding(6.dp))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(displayName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextBlack, textAlign = TextAlign.Center)
        Text(if (currentLanguage == "Hindi") "प्रोफ़ाइल संपादित करें" else "Edit Profile", color = BrandRed, fontSize = 14.sp)

        Spacer(modifier = Modifier.height(30.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            ProfileGridItem(Icons.Default.Language, if (currentLanguage == "Hindi") "भाषा" else "Language", Modifier.weight(1f)) { showLanguageDialog = true }
            Spacer(modifier = Modifier.width(12.dp))
            ProfileGridItem(Icons.Default.Notifications, if (currentLanguage == "Hindi") "सूचनाएं" else "Notifications", Modifier.weight(1f)) { onNavigate("Notifications") }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            ProfileGridItem(Icons.Default.Settings, if (currentLanguage == "Hindi") "प्राथमिकताएं" else "Preferences", Modifier.weight(1f)) { onNavigate("Preferences") }
            Spacer(modifier = Modifier.width(12.dp))
            ProfileGridItem(Icons.Default.Info, if (currentLanguage == "Hindi") "हमारे बारे में" else "About", Modifier.weight(1f)) { onNavigate("About") }
        }

        Spacer(modifier = Modifier.height(24.dp))

        ProfileListTile(Icons.Default.Security, if (currentLanguage == "Hindi") "गोपनीयता नीति" else "Privacy Policy") { onNavigate("Privacy") }
        ProfileListTile(Icons.Default.Description, if (currentLanguage == "Hindi") "नियम व शर्तें" else "Terms & Conditions") { onNavigate("Terms") }
        ProfileListTile(Icons.Default.Email, if (currentLanguage == "Hindi") "हमसे संपर्क करें" else "Contact") {
            val intent = Intent(Intent.ACTION_SENDTO).apply { data = Uri.parse("mailto:contact@iqrartimes.com") }
            context.startActivity(intent)
        }

        Spacer(modifier = Modifier.height(12.dp))
        ProfileListTile(Icons.Default.Logout, if (currentLanguage == "Hindi") "लॉग आउट" else "Log Out", isLogout = true) { onLogout() }
        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun ProfileGridItem(icon: ImageVector, text: String, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Card(
        modifier = modifier.height(75.dp).clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFEEEEEE))
    ) {
        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(icon, null, modifier = Modifier.size(24.dp), tint = Color.Black)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
        }
    }
}

@Composable
fun ProfileListTile(icon: ImageVector, label: String, isLogout: Boolean = false, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).height(60.dp).clickable { onClick() },
        shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, Color(0xFFF5F5F5)), colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, modifier = Modifier.size(22.dp), tint = if (isLogout) BrandRed else Color.Black)
            Spacer(modifier = Modifier.width(18.dp))
            Text(label, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = if (isLogout) BrandRed else Color.Black)
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, null, tint = Color.Gray)
        }
    }
}

@Composable
fun ProfileMenuView(
    onNavigate: (String) -> Unit,
    onLoginClick: () -> Unit,
    onContactClick: () -> Unit,
    currentLanguage: String = "Hindi",
    onLanguageChange: (String) -> Unit = {}
) {
    var showLanguageDialog by remember { mutableStateOf(false) }

    if (showLanguageDialog) {
        LanguagePickerDialog(
            currentLanguage = currentLanguage,
            onSelect = { lang -> onLanguageChange(lang); showLanguageDialog = false },
            onDismiss = { showLanguageDialog = false }
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(30.dp))
        Button(onClick = onLoginClick, colors = ButtonDefaults.buttonColors(containerColor = BrandRed), shape = RoundedCornerShape(8.dp), modifier = Modifier.width(220.dp).height(48.dp)) {
            Text(if (currentLanguage == "Hindi") "साइन इन / साइन अप" else "SIGN IN / SIGN UP", fontWeight = FontWeight.Bold, color = Color.White)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(if (currentLanguage == "Hindi") "अपने सहेजे गए लेखों तक पहुंचने के लिए साइन इन करें।" else "Sign in to access your saved articles.", textAlign = TextAlign.Center, color = TextBlack, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(35.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ProfileGridItem(Icons.Default.Language, if (currentLanguage == "Hindi") "भाषा" else "Language", Modifier.weight(1f)) { showLanguageDialog = true }
            ProfileGridItem(Icons.Default.Notifications, if (currentLanguage == "Hindi") "सूचनाएं" else "Notifications", Modifier.weight(1f)) { onNavigate("Notifications") }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ProfileGridItem(Icons.Default.Settings, if (currentLanguage == "Hindi") "प्राथमिकताएं" else "Preferences", Modifier.weight(1f)) { onLoginClick() }
            ProfileGridItem(Icons.Default.Info, if (currentLanguage == "Hindi") "हमारे बारे में" else "About", Modifier.weight(1f)) { onNavigate("About") }
        }
        Spacer(modifier = Modifier.height(30.dp))

        ProfileListTile(Icons.Default.Security, if (currentLanguage == "Hindi") "गोपनीयता नीति" else "Privacy Policy") { onNavigate("Privacy") }
        ProfileListTile(Icons.Default.Description, if (currentLanguage == "Hindi") "नियम व शर्तें" else "Terms & Conditions") { onNavigate("Terms") }
        ProfileListTile(Icons.Default.Email, if (currentLanguage == "Hindi") "हमसे संपर्क करें" else "Contact") { onContactClick() }
    }
}

// 🔥 LANGUAGE PICKER DIALOG
@Composable
fun LanguagePickerDialog(
    currentLanguage: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val options = listOf("Hindi" to "हिंदी", "English" to "English")
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp),
        title = {
            Text("Select Language", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextBlack)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                options.forEach { (key, label) ->
                    val isSelected = currentLanguage == key
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { onSelect(key) },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) BrandRed else Color(0xFFF5F5F5)
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Language,
                                contentDescription = null,
                                tint = if (isSelected) Color.White else Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                label,
                                fontSize = 15.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else TextBlack
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = BrandRed)
            }
        }
    )
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LocalWebViewScreen(title: String, url: String, onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = BrandRed)
            }
            Text(
                title,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(start = 8.dp),
                color = TextBlack
            )
        }
        HorizontalDivider()

        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )

                    // 🔥 EE SETTINGS KACHITAMGA UNDALI BLANK SCREEN RAKUNDA
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true       // Idi lekapothe blank vastundi
                        @Suppress("DEPRECATION")
                        databaseEnabled = true
                        useWideViewPort = true         // Mobile view fix
                        loadWithOverviewMode = true    // Content fitting fix
                        javaScriptCanOpenWindowsAutomatically = true
                    }

                    webViewClient = WebViewClient()
                    webChromeClient = android.webkit.WebChromeClient() // Smooth loading kosam

                    loadUrl(url)
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun NotificationSettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("app_settings", Context.MODE_PRIVATE) }
    
    // Default to true if not set
    var notificationsEnabled by remember { 
        mutableStateOf(prefs.getBoolean("notifications_enabled", true)) 
    }

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = BrandRed)
            }
            Text(
                "Notifications",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(start = 8.dp),
                color = TextBlack
            )
        }
        
        HorizontalDivider(color = Color(0xFFEEEEEE))

        Column(modifier = Modifier.padding(20.dp)) {
            // App Description
            Text(
                text = "Push Notifications",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextBlack
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Stay updated with the latest news, breaking alerts, and daily highlights directly on your device. We only send notifications for important updates.",
                fontSize = 14.sp,
                color = Color.Gray,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(30.dp))

            // Toggle Row
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
                border = BorderStroke(1.dp, Color(0xFFEEEEEE))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "News Notifications",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = TextBlack
                        )
                        Text(
                            if (notificationsEnabled) "You will receive news alerts" else "Notifications are turned off",
                            fontSize = 12.sp,
                            color = if (notificationsEnabled) BrandRed else Color.Gray
                        )
                    }
                    
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { isEnabled ->
                            notificationsEnabled = isEnabled
                            // Save to prefs
                            prefs.edit { putBoolean("notifications_enabled", isEnabled) }
                            
                            // FCM Logic
                            if (isEnabled) {
                                FirebaseMessaging.getInstance().subscribeToTopic("news")
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            android.widget.Toast.makeText(context, "Notifications Enabled", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    }
                            } else {
                                FirebaseMessaging.getInstance().unsubscribeFromTopic("news")
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            android.widget.Toast.makeText(context, "Notifications Disabled", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    }
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = BrandRed,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color.LightGray
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Text(
                "Note: You can also manage notification permissions in your device system settings.",
                fontSize = 12.sp,
                color = Color.Gray,
                fontStyle = FontStyle.Italic
            )
        }
    }
}