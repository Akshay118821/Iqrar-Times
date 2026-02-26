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
import com.example.iqrarnewscompose.BrandRed
import com.example.iqrarnewscompose.TextBlack

// ... Colors remain the same ...

@Composable
fun ProfileScreen(onToggleHeader: (Boolean) -> Unit) {
    val context = LocalContext.current
    var localView by remember { mutableStateOf("Main") }

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
                        try { context.startActivity(intent) } catch (_: Exception) {}
                    }
                )
            }
            // ... Terms and Privacy cases remain the same ...
            "Terms" -> LocalWebViewScreen("Terms & Conditions", "https://www.iqrartimes.com/terms-of-service") { localView = "Main"; onToggleHeader(true) }
            "Privacy" -> LocalWebViewScreen("Privacy Policy", "https://www.iqrartimes.com/privacy-policy") { localView = "Main"; onToggleHeader(true) }
        }
    }
}

@Composable
fun ProfileMenuView(onNavigate: (String) -> Unit, onContactClick: () -> Unit) {

    // State for Language Dropdown
    var isLanguageMenuExpanded by remember { mutableStateOf(false) }
    var selectedLanguage by remember { mutableStateOf("English") }

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(30.dp))
        Button(
            onClick = { },
            colors = ButtonDefaults.buttonColors(containerColor = BrandRed),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.width(220.dp).height(48.dp)
        ) {
            Text("SIGN IN / SIGN UP", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Sign in to access your saved articles and personalized recommendations.",
            textAlign = TextAlign.Center,
            color = TextBlack,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(35.dp))

        // --- ROW 1: Language (With Dropdown) & Notifications ---
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {

            // Box is needed to anchor the DropdownMenu to the Button
            Box(modifier = Modifier.weight(1f)) {
                ProfileGridItem(
                    icon = Icons.Default.Language,
                    text = selectedLanguage, // Show selected language
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { isLanguageMenuExpanded = true } // Open Menu
                )

                DropdownMenu(
                    expanded = isLanguageMenuExpanded,
                    onDismissRequest = { isLanguageMenuExpanded = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    DropdownMenuItem(
                        text = { Text("English", color = TextBlack) },
                        onClick = {
                            selectedLanguage = "English"
                            isLanguageMenuExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Hindi (हिंदी)", color = TextBlack) },
                        onClick = {
                            selectedLanguage = "Hindi"
                            isLanguageMenuExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Telugu (తెలుగు)", color = TextBlack) },
                        onClick = {
                            selectedLanguage = "Telugu"
                            isLanguageMenuExpanded = false
                        }
                    )
                }
            }

            ProfileGridItem(Icons.Default.Notifications, "Notifications", Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- ROW 2: Preferences & About ---
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ProfileGridItem(Icons.Default.Settings, "Preferences", Modifier.weight(1f))
            ProfileGridItem(Icons.Default.Info, "About", Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(30.dp))

        ProfileListTile(Icons.Default.Security, "Privacy Policy") { onNavigate("Privacy") }
        ProfileListTile(Icons.Default.Description, "Terms & Conditions") { onNavigate("Terms") }
        ProfileListTile(Icons.Default.Email, "Contact") { onContactClick() }
    }
}

@Composable
fun ProfileGridItem(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    OutlinedButton(
        onClick = onClick, // Use the parameter here
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
        modifier = modifier.height(65.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextBlack)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, modifier = Modifier.size(22.dp), tint = TextBlack)
            Spacer(Modifier.width(10.dp))
            // Limit text length so it fits in the button
            Text(
                text,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}

// ... Rest of the code (LocalWebViewScreen, ProfileListTile) remains the same ...
// (I have omitted them to save space, but you should keep them)

@Composable
fun ProfileListTile(icon: ImageVector, label: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).height(60.dp).clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp),
        border = BorderStroke(1.dp, Color(0xFFF0F0F0))
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

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LocalWebViewScreen(title: String, url: String, onBack: () -> Unit) {
    // ... (Your existing WebView Code) ...
    // Just putting placeholders here to complete the file structure
    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Row(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = BrandRed) }
            Text(title, fontWeight = FontWeight.Bold, color = TextBlack, fontSize = 18.sp, modifier = Modifier.padding(start = 8.dp))
        }
        Divider(color = Color(0xFFEEEEEE), thickness = 1.dp)
        AndroidView(factory = { ctx -> WebView(ctx).apply { loadUrl(url) } }, modifier = Modifier.fillMaxSize())
    }
}