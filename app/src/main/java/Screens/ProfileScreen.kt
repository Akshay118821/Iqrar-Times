package com.example.iqrarnewscompose.profile

import android.annotation.SuppressLint
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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


val BrandRed = Color(0xFFD32F2F)
val TextBlack = Color(0xFF1A1A1A)
val TextGray = Color(0xFF666666)

@Composable
fun ProfileScreen(onToggleHeader: (Boolean) -> Unit) {
    val context = LocalContext.current
    var localView by remember { mutableStateOf("Main") }

    // Handle Hardware Back Button
    BackHandler(enabled = localView != "Main") {
        localView = "Main"
        onToggleHeader(true)
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        when (localView) {
            "Main" -> {

                LaunchedEffect(Unit) { onToggleHeader(true) }

                ProfileMenuView(
                    onNavigate = { viewName ->
                        localView = viewName
                        onToggleHeader(false)
                    },
                    onContactClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:contact@iqrartimes.com")
                        }
                        try { context.startActivity(intent) } catch (e: Exception) { }
                    }
                )
            }

            "Terms" -> LocalWebViewScreen(
                title = "Terms & Conditions",
                url = "https://www.iqrartimes.com/terms-of-service",
                onBack = {
                    localView = "Main"
                    onToggleHeader(true)
                }
            )

            "Privacy" -> LocalWebViewScreen(
                title = "Privacy Policy",
                url = "https://www.iqrartimes.com/privacy-policy",
                onBack = {
                    localView = "Main"
                    onToggleHeader(true)
                }
            )
        }
    }
}

@Composable
fun ProfileMenuView(onNavigate: (String) -> Unit, onContactClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(30.dp))
        Button(onClick = { }, colors = ButtonDefaults.buttonColors(containerColor = BrandRed), shape = RoundedCornerShape(8.dp), modifier = Modifier.width(220.dp).height(48.dp)) {
            Text("SIGN IN / SIGN UP", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Sign in to access your saved articles and personalized recommendations.", textAlign = TextAlign.Center, color = TextBlack, fontSize = 14.sp)

        Spacer(modifier = Modifier.height(35.dp))

        // GRID SECTION
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ProfileGridItem(Icons.Default.Language, "Language", Modifier.weight(1f))
            ProfileGridItem(Icons.Default.Notifications, "Notifications", Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ProfileGridItem(Icons.Default.Settings, "Preferences", Modifier.weight(1f))
            ProfileGridItem(Icons.Default.Info, "About", Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(30.dp))

        // LIST SECTION
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
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = BrandRed)
            }
            Text(text = title, fontWeight = FontWeight.Bold, color = TextBlack, fontSize = 18.sp, modifier = Modifier.padding(start = 8.dp))
        }
        Divider(color = Color(0xFFEEEEEE), thickness = 1.dp)

        AndroidView(factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                webViewClient = WebViewClient()
                loadUrl(url)
            }
        }, modifier = Modifier.fillMaxSize())
    }
}

@Composable
fun ProfileGridItem(icon: ImageVector, text: String, modifier: Modifier = Modifier) {
    OutlinedButton(onClick = { }, shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, Color(0xFFE0E0E0)), modifier = modifier.height(65.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = TextBlack)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, modifier = Modifier.size(22.dp), tint = TextBlack)
            Spacer(Modifier.width(10.dp))
            Text(text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun ProfileListTile(icon: ImageVector, label: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).height(60.dp).clickable { onClick() },
        shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(1.dp), border = BorderStroke(1.dp, Color(0xFFF0F0F0))
    ) {
        Row(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = TextBlack, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(18.dp))
            Text(label, color = TextBlack, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, null, tint = Color.Gray)
        }
    }
}