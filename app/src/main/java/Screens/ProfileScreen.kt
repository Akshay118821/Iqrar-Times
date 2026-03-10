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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.iqrarnewscompose.CategoryItem
import com.example.iqrarnewscompose.BrandRed
import com.example.iqrarnewscompose.TextBlack

@Composable
fun ProfileScreen(
    categories: List<CategoryItem>,
    onToggleHeader: (Boolean) -> Unit,
    openLogin: () -> Unit
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
                            if (it != "Main") onToggleHeader(false) // 🔥 WebView open ayyeppudu main header hide chesthunnam
                        },
                        onLoginClick = { openLogin() },
                        onContactClick = {
                            val intent = Intent(Intent.ACTION_SENDTO).apply { data = Uri.parse("mailto:contact@iqrartimes.com") }
                            context.startActivity(intent)
                        }
                    )
                } else {
                    LoggedProfileView(
                        onNavigate = {
                            localView = it
                            if (it != "Main") onToggleHeader(false) // 🔥 WebView open ayyeppudu main header hide chesthunnam
                        },
                        onLogout = {
                            prefs.edit().putBoolean("isLoggedIn", false).apply()
                        }
                    )
                }
            }

            "Preferences" -> {
                PreferencesScreen(
                    categories = categories,
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
        }
    }
}

@Composable
fun LoggedProfileView(onNavigate: (String) -> Unit, onLogout: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(30.dp))

        Box(contentAlignment = Alignment.BottomEnd) {
            Icon(Icons.Default.AccountCircle, null, modifier = Modifier.size(110.dp), tint = Color.LightGray)
            Surface(modifier = Modifier.size(30.dp), shape = CircleShape, color = BrandRed, border = BorderStroke(2.dp, Color.White)) {
                Icon(Icons.Default.Edit, null, tint = Color.White, modifier = Modifier.padding(6.dp))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text("Kalyan Kumar", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextBlack)
        Text("Edit Profile", color = BrandRed, fontSize = 14.sp)

        Spacer(modifier = Modifier.height(30.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            ProfileGridItem(Icons.Default.Language, "Language", Modifier.weight(1f))
            Spacer(modifier = Modifier.width(12.dp))
            ProfileGridItem(Icons.Default.Notifications, "Notifications", Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            ProfileGridItem(Icons.Default.Settings, "Preferences", Modifier.weight(1f)) { onNavigate("Preferences") }
            Spacer(modifier = Modifier.width(12.dp))
            ProfileGridItem(Icons.Default.Info, "About", Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 🔥 Logic works for both LoggedIn and Guest users
        ProfileListTile(Icons.Default.Security, "Privacy Policy") { onNavigate("Privacy") }
        ProfileListTile(Icons.Default.Description, "Terms & Conditions") { onNavigate("Terms") }
        ProfileListTile(Icons.Default.Email, "Contact") { }

        Spacer(modifier = Modifier.height(12.dp))
        ProfileListTile(Icons.Default.Logout, "Log Out", isLogout = true) { onLogout() }
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
fun ProfileMenuView(onNavigate: (String) -> Unit, onLoginClick: () -> Unit, onContactClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(30.dp))
        Button(onClick = onLoginClick, colors = ButtonDefaults.buttonColors(containerColor = BrandRed), shape = RoundedCornerShape(8.dp), modifier = Modifier.width(220.dp).height(48.dp)) {
            Text("SIGN IN / SIGN UP", fontWeight = FontWeight.Bold, color = Color.White)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("Sign in to access your saved articles.", textAlign = TextAlign.Center, color = TextBlack, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(35.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ProfileGridItem(Icons.Default.Language, "Language", Modifier.weight(1f))
            ProfileGridItem(Icons.Default.Notifications, "Notifications", Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ProfileGridItem(Icons.Default.Settings, "Preferences", Modifier.weight(1f)) { onLoginClick() }
            ProfileGridItem(Icons.Default.Info, "About", Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(30.dp))

        // 🔥 GUEST MODE NAVIGATION TO WEBVIEW
        ProfileListTile(Icons.Default.Security, "Privacy Policy") { onNavigate("Privacy") }
        ProfileListTile(Icons.Default.Description, "Terms & Conditions") { onNavigate("Terms") }
        ProfileListTile(Icons.Default.Email, "Contact") { onContactClick() }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LocalWebViewScreen(title: String, url: String, onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Row(
            modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, null, tint = BrandRed)
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
                        databaseEnabled = true
                        useWideViewPort = true         // Mobile view fix
                        loadWithOverviewMode = true    // Content fitting fix
                        javaScriptCanOpenWindowsAutomatically = true
                    }

                    webViewClient = android.webkit.WebViewClient()
                    webChromeClient = android.webkit.WebChromeClient() // Smooth loading kosam

                    loadUrl(url)
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}