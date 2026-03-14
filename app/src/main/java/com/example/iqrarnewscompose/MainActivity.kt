package com.example.iqrarnewscompose

import VideoArticle
import VideoItemCard
import VideosScreen
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import coil.compose.rememberAsyncImagePainter
import com.example.iqrarnewscompose.api.CommonResponse
import com.example.iqrarnewscompose.api.RetrofitInstance
import com.example.iqrarnewscompose.api.SendOtpRequest
import com.example.iqrarnewscompose.api.VerifyOtpRequest
import com.example.iqrarnewscompose.profile.PreferencesScreen
import com.example.iqrarnewscompose.profile.ProfileGridItem
import com.example.iqrarnewscompose.profile.ProfileMenuView
import com.example.iqrarnewscompose.ui.theme.IqrarNewsComposeTheme
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Data Model
data class NewsArticle(
    val id: String,
    val title: String,
    val image: String,
    val date: String,
    val author: String,
    val content: String,
    val viewCount: String
)

class MainActivity : ComponentActivity() {

    private val viewModel: NewsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {

        // 1) Install Splash Screen
        installSplashScreen()

        super.onCreate(savedInstanceState)

        // 2) Notification permission - okkasari matrame adige logic
        val permissionPrefs = getSharedPreferences("permission_prefs", Context.MODE_PRIVATE)
        val hasRequestedNotification =
            permissionPrefs.getBoolean("has_requested_notification", false)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            val isAlreadyGranted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!isAlreadyGranted && !hasRequestedNotification) {
                requestPermissions(
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1
                )

                // okkasari adigam ani remember cheskodaniki
                permissionPrefs.edit()
                    .putBoolean("has_requested_notification", true)
                    .apply()
            }
        }

        // 3) FCM Logic (same)
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("FCM_TOKEN", task.result)
                }
            }

        // 4) Data load (same) - app open avvadaniki wait cheyyadu
        viewModel.loadCategories("HINDI")
        viewModel.loadNews("", "HINDI") { }

        // 5) UI
        setContent {
            IqrarNewsComposeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainApp(viewModel = viewModel)
                }
            }
        }
    }
}



// Colors & Fonts
val BrandRed = Color(0xFFD32F2F)
val TextBlack = Color(0xFF1A1A1A)
val TextGray = Color(0xFF666666)
val NotoSansFont = FontFamily.SansSerif


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(viewModel: NewsViewModel) {

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var authStep by remember { mutableStateOf(0) }

    val context = LocalContext.current
    val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)

    var isLoggedIn by remember {
        mutableStateOf(prefs.getBoolean("isLoggedIn", false))
    }

    var userEmail by remember { mutableStateOf("") }
    var showLoginDialog by remember { mutableStateOf(false) }
    var selectedScreen by remember { mutableStateOf("Home") }

    BackHandler(enabled = authStep > 0 || drawerState.isOpen) {
        if (drawerState.isOpen) {
            scope.launch { drawerState.close() }
        } else if (authStep == 2) {
            authStep = 1
        } else if (authStep == 1) {
            authStep = 0
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color.White,
                modifier = Modifier.width(300.dp)
            ) {

                SideMenuDrawer(
                    isLoggedIn = isLoggedIn,
                    userName = "Kalyan Kumar", // 🔥 Profile స్క్రీన్ లో ఉన్న పేరు ఇక్కడ ఇస్తున్నాం
                    onLoginClick = {
                        scope.launch { drawerState.close() }
                        authStep = 1
                    },
                    onLogoutClick = {
                        prefs.edit().putBoolean("isLoggedIn", false).apply()
                        isLoggedIn = false
                        scope.launch { drawerState.close() }
                    },
                    // 🔥 క్లిక్ చేస్తే ప్రొఫైల్ కి వెళ్ళాలి
                    onViewProfileClick = {
                        selectedScreen = "Profile"
                        scope.launch { drawerState.close() }
                    },
                    onNavigate = { screen ->
                        selectedScreen = screen
                        scope.launch { drawerState.close() }
                    },
                    onContactClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply { data = Uri.parse("mailto:contact@iqrartimes.com") }
                        context.startActivity(intent)
                        scope.launch { drawerState.close() }
                    }
                )
            }
        }
    ) {

        Box(modifier = Modifier.fillMaxSize()) {

            MainContent(
                viewModel = viewModel,
                isLoggedIn = isLoggedIn,
                selectedScreen = selectedScreen,
                onNavigate = { selectedScreen = it },
                onOpenDrawer = {
                    scope.launch { drawerState.open() }
                },
                openLogin = {
                    authStep = 1
                },
                openLoginDialog = {
                    showLoginDialog = true
                }
            )

            if (showLoginDialog) {
                LoginDialog(
                    onDismiss = { showLoginDialog = false },
                    onLoginSuccess = { result ->
                        isLoggedIn = true

                        // 🔥 UPDATED TOKEN EXTRACTION
                        val tkn = result.data?.access          // Try 'access' first
                            ?: result.data?.token
                            ?: result.data?.accessToken
                            ?: result.data?.auth_token
                            ?: result.token
                            ?: ""

                        Log.d("TOKEN_SAVE", "Saving Token: ${tkn.take(50)}")

                        prefs.edit()
                            .putBoolean("isLoggedIn", true)
                            .putString("token", tkn)
                            .apply()

                        showLoginDialog = false
                    }
                )
            }

            // LOGIN SCREEN
            if (authStep == 1) {

                LoginScreen(

                    onGetOtpClick = { email ->

                        userEmail = email
                        authStep = 2

                    },

                    onBackClick = {
                        authStep = 0
                    }

                )
            }

            // OTP SCREEN
            if (authStep == 2) {

                OtpScreen(

                    email = userEmail,

                    onVerifyClick = {

                        // login success
                        isLoggedIn = true
                        authStep = 0
                    },

                    onBackClick = {
                        authStep = 1
                    }
                )
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(
    viewModel: NewsViewModel,
    isLoggedIn: Boolean,
    selectedScreen: String,
    onNavigate: (String) -> Unit,
    onOpenDrawer: () -> Unit,
    openLogin: () -> Unit,
    openLoginDialog: () -> Unit
) {

    var currentLanguage by remember { mutableStateOf("Hindi") }
    var showLanguageMenu by remember { mutableStateOf(false) }
    var isMainHeaderVisible by remember { mutableStateOf(true) }
    var selectedArticle by remember { mutableStateOf<NewsArticle?>(null) }
    var showCommentsFor by remember { mutableStateOf<NewsArticle?>(null) }

    var isFlipMode by remember { mutableStateOf(false) }
    var currentFlipPage by remember { mutableIntStateOf(0) }
    var tempBarsVisible by remember { mutableStateOf(false) }

    // 🔥 TIMER: 2 seconds taruvatha bars hide avvali
    LaunchedEffect(tempBarsVisible) {
        if (tempBarsVisible) {
            delay(2000)
            tempBarsVisible = false
        }
    }

    LaunchedEffect(currentLanguage) {
        val langParam = if (currentLanguage == "Hindi") "HINDI" else "ENGLISH"
        viewModel.loadCategories(langParam)
    }

    BackHandler(enabled = selectedArticle != null || isFlipMode || showCommentsFor != null) {
        if (showCommentsFor != null) {
            showCommentsFor = null // Comments close chestundi
        } else if (selectedArticle != null) {
            selectedArticle = null
            if (!isFlipMode) isMainHeaderVisible = true
        } else if (isFlipMode) {
            isFlipMode = false
            currentFlipPage = 0
            isMainHeaderVisible = true
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),

        topBar = {
            // Logic: Flip mode lo unnappudu tap chesthene Header ravali
            val shouldShowHeader = !isFlipMode || (isFlipMode && tempBarsVisible)

            if (shouldShowHeader && selectedArticle == null) {
                if (isMainHeaderVisible) {
                    TopAppBar(
                        title = {
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Image(painter = painterResource(id = R.drawable.logo), contentDescription = "Logo", modifier = Modifier.height(32.dp), contentScale = ContentScale.Fit)
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = {
                                onOpenDrawer()
                                if(isFlipMode) tempBarsVisible = true
                            }) {
                                Icon(Icons.Default.Menu, null, tint = Color.White)
                            }
                        },
                        actions = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box {
                                    IconButton(onClick = {
                                        showLanguageMenu = true
                                        if(isFlipMode) tempBarsVisible = true
                                    }) {
                                        Icon(Icons.Default.Translate, null, tint = Color.White)
                                    }
                                    DropdownMenu(
                                        expanded = showLanguageMenu,
                                        onDismissRequest = { showLanguageMenu = false },
                                        modifier = Modifier.background(Color.White)
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("English", color = TextBlack) },
                                            onClick = {
                                                currentLanguage = "English"
                                                showLanguageMenu = false
                                                isFlipMode = false // 🔥 Close Flip Screen
                                                viewModel.loadCategories("ENGLISH")
                                                viewModel.loadNews("", "ENGLISH") {}
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("हिंदी", color = TextBlack) },
                                            onClick = {
                                                currentLanguage = "Hindi"
                                                showLanguageMenu = false
                                                isFlipMode = false // 🔥 Close Flip Screen
                                                viewModel.loadCategories("HINDI")
                                                viewModel.loadNews("", "HINDI") {}
                                            }
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                CustomToggleButton(
                                    checked = isFlipMode,
                                    onCheckedChange = { 
                                        isFlipMode = it
                                        if (it) tempBarsVisible = true
                                    }
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandRed)
                    )
                }
            } else if (selectedArticle != null) {
                // 🔥 కేటగిరీ పేరుని వెతికే లాజిక్
                val categoryName = viewModel.categories
                    .find { it.id.toString() == selectedScreen }
                    ?.name ?: if (selectedScreen == "Home") "Iqrar Times" else "News"

                TopAppBar(
                    title = {
                        Text(
                            text = categoryName,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            selectedArticle = null

                            if (!isFlipMode) isMainHeaderVisible = true
                        }) {
                            Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandRed)
                )
            }
        },

        bottomBar = {

            val shouldShowBottomBar = (!isFlipMode && isMainHeaderVisible && selectedArticle == null) || (isFlipMode && tempBarsVisible)

            if (shouldShowBottomBar) {
                IqrarBottomBar(
                    selectedScreen,
                    currentLanguage,
                    onNavigate = { screen ->
                        // 🔥 THE FIX: Navigate away from Flip Screen
                        onNavigate(screen)
                        isFlipMode = false
                        currentFlipPage = 0
                        isMainHeaderVisible = true
                    }
                )
            }
        }

    ) { innerPadding ->

        // Overlay Logic: FlipMode lo unnapudu images resize avvadu (Floating bars)
        val boxModifier = if (isFlipMode) Modifier.fillMaxSize() else Modifier.padding(innerPadding)

        Box(modifier = boxModifier) {
            if (showCommentsFor != null) {
                //  Idhi kotha screen ni chupisthundi
                CommentsSectionScreen(
                    article = showCommentsFor!!,
                    viewModel = viewModel,
                    onBack = { showCommentsFor = null }
                )
            } else if (selectedArticle != null) {
                //  News Detail screen ki kotha parameter pass chesthunnam
                NewsDetailScreen(
                    article = selectedArticle!!,
                    isLoggedIn = isLoggedIn,
                    onOpenLoginDialog = openLoginDialog,
                    onCommentClick = { showCommentsFor = it } // Idhi click chesthe state marusthundi
                )
            }
            else if (isFlipMode) {
                FlipNewsScreen(
                    viewModel = viewModel,
                    initialPage = currentFlipPage,
                    onBack = { isFlipMode = false; currentFlipPage = 0 },
                    onPageChange = { page -> currentFlipPage = page },
                    onNewsClick = { apiNews ->
                        selectedArticle = NewsArticle(
                            id = apiNews.id ?: "",
                            title = apiNews.name ?: "",
                            image = apiNews.icon ?: "",
                            date = apiNews.date ?: "",
                            author = apiNews.author ?: "Admin",
                            content = apiNews.content ?: "",
                            viewCount = apiNews.viewcount?.toString() ?: "0"
                        )
                    },
                    onScreenTap = {
                        tempBarsVisible = !tempBarsVisible
                    }
                )
            }
            else {
                when (selectedScreen) {
                    "Home" -> HomeScreen(lang = currentLanguage, onNavigate = { onNavigate(it) }, onNewsClick = { selectedArticle = it }, categories = viewModel.categories, viewModel = viewModel)
                    "Profile" -> com.example.iqrarnewscompose.profile.ProfileScreen(categories = viewModel.categories, onToggleHeader = { isMainHeaderVisible = it }, openLogin = { openLogin() })
                    "Live TV" -> LiveTVScreen(currentLanguage, viewModel)
                    "E-Paper" -> EPaperNativeScreen(viewModel, currentLanguage)
                    "Videos" -> VideosScreen(currentLanguage, viewModel)
                    "Preferences" -> com.example.iqrarnewscompose.profile.PreferencesScreen(
                        categories = viewModel.categories,
                        onBack = { 
                            onNavigate("Home")
                            isMainHeaderVisible = true 
                        }
                    )
                    "Privacy" -> com.example.iqrarnewscompose.profile.LocalWebViewScreen(
                        title = "Privacy Policy",
                        url = "https://www.iqrartimes.com/privacy-policy",
                        onBack = { 
                            onNavigate("Home")
                            isMainHeaderVisible = true 
                        }
                    )
                    "Terms" -> com.example.iqrarnewscompose.profile.LocalWebViewScreen(
                        title = "Terms & Conditions",
                        url = "https://www.iqrartimes.com/terms-of-service",
                        onBack = { 
                            onNavigate("Home")
                            isMainHeaderVisible = true 
                        }
                    )
                    else -> CategoryNewsScreen(catId = selectedScreen, lang = currentLanguage, onNavigate = { onNavigate(it) }, onNewsClick = { selectedArticle = it }, categories = viewModel.categories, viewModel = viewModel)
                }
            }
        }
    }
}
// ------------------------------------------------------------
// ✅ NEW COMPOSABLES: Drawer, Login Screen, OTP Screen
// ------------------------------------------------------------

@Composable
fun SideMenuDrawer(
    isLoggedIn: Boolean,
    userName: String, // 🔥 Profile Screen లో ఉన్న పేరు ఇక్కడ పాస్ చేస్తాం
    onLoginClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onViewProfileClick: () -> Unit, // 🔥 Navigation callback
    onNavigate: (String) -> Unit,
    onContactClick: () -> Unit
){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // --- 1. HEADER SECTION ---
        if (isLoggedIn) {
            // RED HEADER (Sync with Profile Screen)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BrandRed)
                    .padding(24.dp)
                    .padding(top = 20.dp)
            ) {
                Column {
                    // Profile Icon (Same as Profile Screen)
                    Surface(
                        modifier = Modifier.size(65.dp),
                        shape = androidx.compose.foundation.shape.CircleShape,
                        color = Color.White.copy(alpha = 0.9f)
                    ) {
                        Icon(
                            Icons.Default.AccountCircle,
                            contentDescription = null,
                            modifier = Modifier.padding(2.dp),
                            tint = Color.Gray
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Name (Same as Profile Screen: "Kalyan Kumar")
                    Text(
                        text = userName,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(15.dp))

                    // View Profile Button
                    Button(
                        onClick = onViewProfileClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(50),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                        modifier = Modifier.height(36.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                    ) {
                        Text("View Profile", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            // BEFORE LOGIN HEADER
            Text(
                text = "Sign in to your account",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(24.dp).padding(top = 30.dp, bottom = 20.dp)
            )
        }

        // --- 2. MENU ITEMS (No changes here) ---
        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp)) {

            // Language Item
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.Language, null, tint = Color.Black)
                Spacer(modifier = Modifier.width(16.dp))
                Text("Language", modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold, color = TextBlack)
                Box(modifier = Modifier.border(1.dp, Color.LightGray, RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("English", fontSize = 12.sp, color = TextBlack)
                        Icon(Icons.Default.KeyboardArrowDown, null, modifier = Modifier.size(16.dp))
                    }
                }
            }

            DrawerMenuItem(icon = Icons.Outlined.Settings, text = "Preferences", onClick = { onNavigate("Preferences") })
            DrawerMenuItem(icon = Icons.Outlined.Security, text = "Privacy Policy", onClick = { onNavigate("Privacy") })
            DrawerMenuItem(icon = Icons.Outlined.Description, text = "Terms & Conditions", onClick = { onNavigate("Terms") })
            DrawerMenuItem(icon = Icons.Outlined.HelpOutline, text = "Contact Us", onClick = { onContactClick() })

            Spacer(modifier = Modifier.height(20.dp))
            Text(text = "Version 1.2.4", color = Color.Gray, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(30.dp))

            // Login / Logout Button
            Button(
                onClick = { if (isLoggedIn) onLogoutClick() else onLoginClick() },
                colors = ButtonDefaults.buttonColors(containerColor = BrandRed),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text(text = if (isLoggedIn) "Logout" else "Login", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginDialog(
    onDismiss: () -> Unit,
    onLoginSuccess: (CommonResponse) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var step by remember { mutableStateOf(1) } // 1: Email, 2: OTP
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = {},
        containerColor = Color.White,
        modifier = Modifier.padding(16.dp),
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (step == 1) {
                    Text(
                        text = "Login with Email",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextBlack
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = { Text("Enter Email") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = {
                            if (email.isBlank()) return@Button
                            scope.launch {
                                isLoading = true
                                try {
                                    val response = RetrofitInstance.api.sendEmailOtp(SendOtpRequest(email))
                                    if (response.isSuccessful) {
                                        step = 2
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                                isLoading = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandRed),
                        modifier = Modifier.fillMaxWidth().height(45.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Get OTP", color = Color.White)
                        }
                    }
                } else {
                    Text(
                        text = "Verify OTP",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextBlack
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Sent to $email",
                        fontSize = 12.sp,
                        color = TextGray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = otp,
                        onValueChange = { if (it.length <= 4) otp = it },
                        placeholder = { Text("Enter 4-digit OTP") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = {
                            if (otp.length < 4) return@Button
                            scope.launch {
                                isLoading = true
                                try {
                                    val response = RetrofitInstance.api.verifyEmailOtp(VerifyOtpRequest(email, otp))
                                    if (response.isSuccessful) {
                                        onLoginSuccess(response.body()!!)
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                                isLoading = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandRed),
                        modifier = Modifier.fillMaxWidth().height(45.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Verify", color = Color.White)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = TextGray)
                }
            }
        }
    )
}

@Composable
fun DrawerMenuItem(icon: ImageVector, text: String, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = text, tint = Color.Black)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextBlack)
    }
}

// --- SCREEN 1: LOGIN (Image 3) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onGetOtpClick: (String) -> Unit,
    onBackClick: () -> Unit
) {

    var email by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {

        Column(modifier = Modifier.fillMaxSize()) {

            // RED TOP AREA
            Box(
                modifier = Modifier
                    .weight(0.4f)
                    .fillMaxWidth()
                    .background(BrandRed),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo",
                    modifier = Modifier.width(200.dp),

                    )
            }

            // WHITE AREA
            Box(
                modifier = Modifier
                    .weight(0.6f)
                    .fillMaxWidth()
                    .background(Color.White)
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Login with Email",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextBlack
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Enter your Email Id to receive a\nverification code",
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        color = TextGray
                    )

                    Spacer(modifier = Modifier.height(30.dp))

                    // EMAIL FIELD
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = { Text("Enter Email") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF0F0F0), RoundedCornerShape(8.dp)),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // GET OTP BUTTON
                    Button(
                        onClick = {

                            if (email.isBlank()) return@Button

                            scope.launch {

                                isLoading = true

                                try {

                                    val response =
                                        RetrofitInstance.api.sendEmailOtp(
                                            SendOtpRequest(email)
                                        )

                                    if (response.isSuccessful) {

                                        onGetOtpClick(email)

                                    }

                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }

                                isLoading = false
                            }

                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {

                        if (isLoading) {

                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )

                        } else {

                            Text(
                                "Get OTP",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )

                        }

                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        "Login with Email",
                        color = TextGray,
                        fontSize = 12.sp
                    )

                    Text(
                        "Continue as Guest",
                        color = TextGray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}
// --- SCREEN 2: OTP (Image 4) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpScreen(
    email: String,
    onVerifyClick: () -> Unit,
    onBackClick: () -> Unit
) {

    var otp by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    val context = LocalContext.current
    val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)

    Box(modifier = Modifier.fillMaxSize()) {

        Column(modifier = Modifier.fillMaxSize()) {

            // RED TOP AREA
            Box(
                modifier = Modifier
                    .weight(0.4f)
                    .fillMaxWidth()
                    .background(BrandRed),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo",
                    modifier = Modifier.width(200.dp),
                    contentScale = ContentScale.Fit
                )
            }

            // WHITE BOTTOM AREA
            Box(
                modifier = Modifier
                    .weight(0.6f)
                    .fillMaxWidth()
                    .background(Color.White)
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Verify your email",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextBlack
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "We sent a 4-digit code to your email address",
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        color = TextGray
                    )

                    Spacer(modifier = Modifier.height(30.dp))

                    // OTP INPUT FIELD
                    OutlinedTextField(
                        value = otp,
                        onValueChange = {
                            if (it.length <= 4) otp = it
                        },
                        placeholder = { Text("Enter OTP") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(40.dp))

                    // VERIFY BUTTON
                    Button(
                        onClick = {

                            if (otp.length < 4) return@Button

                            scope.launch {

                                isLoading = true

                                try {

                                    val response =
                                        RetrofitInstance.api.verifyEmailOtp(
                                            VerifyOtpRequest(
                                                email = email,
                                                otp = otp
                                            )
                                        )

                                    if (response.isSuccessful) {
                                        val body = response.body()
                                        val tkn = body?.data?.access
                                            ?: body?.data?.token
                                            ?: body?.data?.accessToken
                                            ?: body?.data?.auth_token
                                            ?: body?.token
                                            ?: ""

                                        Log.d("OTP_TOKEN", "Extracted Token: ${tkn.take(50)}")
                                        // Save login status and token
                                        prefs.edit()
                                            .putBoolean("isLoggedIn", true)
                                            .putString("token", tkn)
                                            .apply()

                                        onVerifyClick()
                                    }

                                } catch (_: Exception) {
                                }

                                isLoading = false
                            }

                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {

                        if (isLoading) {

                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )

                        } else {

                            Text(
                                text = "Verify",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Resend code",
                        color = TextGray,
                        fontSize = 12.sp
                    )

                    Text(
                        text = "Change email address",
                        color = TextGray,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .clickable { onBackClick() }
                            .padding(top = 4.dp)
                    )
                }
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsDetailScreen(
    article: NewsArticle,
    isLoggedIn: Boolean,
    onOpenLoginDialog: () -> Unit,
    onCommentClick: (NewsArticle) -> Unit
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    val token = prefs.getString("token", "") ?: ""

    var showCommentBox by remember { mutableStateOf(false) }
    var commentText by remember { mutableStateOf("") }
    val viewModel: NewsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val scope = rememberCoroutineScope()

    LaunchedEffect(article.id) {
        viewModel.loadComments(token, article.id)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(Color.White)
    ) {

        Image(
            painter = rememberAsyncImagePainter(article.image),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
            contentScale = ContentScale.Crop
        )

        Column(modifier = Modifier.padding(16.dp)) {

            // 1. TITLE
            Text(
                text = article.title,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextBlack
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 🔥🔥🔥 KOTHA CODE: DATE & TIME ADD CHESAM 🔥🔥🔥
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp) // Kinda content ki gap
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange, // Calendar Icon
                    contentDescription = null,
                    tint = TextGray,
                    modifier = Modifier.size(14.dp)
                )

                Spacer(modifier = Modifier.width(6.dp))

                // Date & Time Text
                Text(
                    text = formatDate(article.date), // "12 Mar 2024 • 10:30 AM"
                    fontSize = 12.sp,
                    color = TextGray
                )

                Spacer(modifier = Modifier.width(16.dp))

                // View Count Icon
                Icon(
                    imageVector = Icons.Default.LiveTv, // Generic Eye-like or views
                    contentDescription = null,
                    tint = TextGray,
                    modifier = Modifier.size(14.dp)
                )

                Spacer(modifier = Modifier.width(6.dp))

                // View Count Text
                Text(
                    text = "${article.viewCount} Views",
                    fontSize = 12.sp,
                    color = TextGray
                )
            }
            // 🔥🔥🔥 END OF NEW CODE 🔥🔥🔥

            // 2. CONTENT (Existing Code)
            Text(
                text = android.text.Html
                    .fromHtml(article.content, android.text.Html.FROM_HTML_MODE_LEGACY)
                    .toString(),
                fontSize = 16.sp,
                color = TextBlack
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 🔴 ACTION ROW (Share + Bookmark + Comment)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {

                // SHARE
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        shareNewsDetailWithImage(
                            context = context,
                            imageUrl = article.image,
                            title = article.title,
                            scope = scope
                        )
                    }
                ) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = null,
                        tint = BrandRed
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Share",
                        color = BrandRed,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // BOOKMARK
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        Toast.makeText(
                            context,
                            "Article Bookmarked",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                ) {
                    Icon(
                        Icons.Default.BookmarkBorder,
                        contentDescription = null,
                        tint = BrandRed
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Bookmark",
                        color = BrandRed,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // COMMENT
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        if (!isLoggedIn) {
                            onOpenLoginDialog()
                        } else {
                            onCommentClick(article)
                        }
                    }
                ){
                    Icon(
                        Icons.Default.Comment,
                        contentDescription = null,
                        tint = BrandRed
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Comment",
                        color = BrandRed,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (showCommentBox) {
                Spacer(modifier = Modifier.height(20.dp))
                Text("Comments", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(10.dp))

                // Comment Input Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        OutlinedTextField(
                            value = commentText,
                            onValueChange = { commentText = it },
                            modifier = Modifier.fillMaxWidth().height(100.dp),
                            placeholder = { Text("Write your thoughts...") },
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BrandRed,
                                unfocusedBorderColor = Color.LightGray,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                cursorColor = BrandRed
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                if (commentText.isNotBlank()) {
                                    val latestToken = context.getSharedPreferences("auth", android.content.Context.MODE_PRIVATE).getString("token", "") ?: ""
                                    viewModel.postComment(latestToken, article.id, commentText) { success ->
                                        if (success) {
                                            commentText = ""
                                            android.widget.Toast.makeText(context, "Comment posted successfully!", android.widget.Toast.LENGTH_SHORT).show()
                                        } else {
                                            android.widget.Toast.makeText(context, "Failed to post comment. Please try again.", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.align(Alignment.End),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandRed),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Post Comment", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Comments List
                viewModel.commentsList.forEach { comment ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(comment.user_name ?: "Anonymous", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(comment.comment ?: "", fontSize = 14.sp)
                            Text(
                                text = formatDate(comment.created_at ?: ""),
                                fontSize = 10.sp,
                                color = TextGray
                            )
                        }
                    }
                }
            }
        }
    }
}

// ... (Pina unna imports anni same unchu) ...

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    categories: List<CategoryItem>,
    onToggleHeader: (Boolean) -> Unit,
    openLogin: () -> Unit
){
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)

    var isLoggedIn by remember { mutableStateOf(prefs.getBoolean("isLoggedIn", false)) }

    // Instant Update Listener
    DisposableEffect(prefs) {
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { p, key ->
            if (key == "isLoggedIn") { isLoggedIn = p.getBoolean("isLoggedIn", false) }
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
                    // 🔥 ERROR FIXED HERE: Parameters ni correct ga pass chesam
                    ProfileMenuView(
                        onNavigate = { viewName -> localView = viewName },
                        onLoginClick = { openLogin() }, // pass openLogin directly
                        onContactClick = {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:contact@iqrartimes.com")
                            }
                            context.startActivity(intent)
                        }
                    )
                } else {
                    LoggedInProfileUI(
                        onNavigate = { viewName -> localView = viewName },
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

            "Terms" -> LocalWebViewScreen("Terms & Conditions", "https://www.iqrartimes.com/terms-of-service") { localView = "Main" }
            "Privacy" -> LocalWebViewScreen("Privacy Policy", "https://www.iqrartimes.com/privacy-policy") { localView = "Main" }
            "About" -> LocalWebViewScreen("About Us", "https://www.iqrartimes.com/about") { localView = "Main" }
        }
    }
}


@Composable
fun LoggedInProfileUI(onNavigate: (String) -> Unit, onLogout: () -> Unit) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(40.dp))

        Image(
            painter = painterResource(id = R.drawable.ic_profile),
            contentDescription = null,
            modifier = Modifier.size(120.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Kalyan Kumar",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Edit Profile",
            color = BrandRed
        )

        Spacer(modifier = Modifier.height(30.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {

            ProfileGridItem(Icons.Default.Language,"Language",Modifier.weight(1f))

            ProfileGridItem(Icons.Default.Notifications,"Notifications",Modifier.weight(1f))

        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {

            ProfileGridItem(Icons.Default.Settings,"Preferences",Modifier.weight(1f))

            ProfileGridItem(Icons.Default.Info, "About", Modifier.weight(1f)) {
                onNavigate("About")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        ProfileListTile(Icons.Default.Security,"Privacy Policy"){}

        ProfileListTile(Icons.Default.Description,"Terms & Conditions"){}

        ProfileListTile(Icons.Default.Email,"Contact"){}

        ProfileListTile(Icons.Default.Logout,"Log Out") {
            onLogout()
        }

    }
}

@Composable
fun ProfileGridItem(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {} // 🔥 Idhi kothaga add chesam
) {
    OutlinedButton(
        onClick = onClick, // 🔥 Ikkada empty badulu 'onClick' pettu
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
        modifier = modifier.height(65.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextBlack)
    ) {
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .height(60.dp)
            .clickable { onClick() },
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

        // WebView implementation with all necessary fixes
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )

                    // 🔥 Ee settings valla blank screen raadu, website mobile screen ki fit avthundi
                    settings.apply {
                        javaScriptEnabled = true      // JavaScript run avvadaniki
                        domStorageEnabled = true       // Modern websites load avvadaniki kachitamga undali
                        databaseEnabled = true
                        useWideViewPort = true         // Screen size auto adjust avvadaniki
                        loadWithOverviewMode = true    // Mobile view correctly raavadaniki
                        javaScriptCanOpenWindowsAutomatically = true
                    }

                    // Website handling settings
                    webViewClient = android.webkit.WebViewClient()   // Links open avvadaniki
                    webChromeClient = android.webkit.WebChromeClient() // Website scripts fast ga run avvadaniki

                    loadUrl(url)
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

data class LiveItem(
    val thumbUrl: String,
    val titleEn: String,
    val titleHi: String,
    val metaEn: String,
    val metaHi: String
)

@Composable
fun LiveTVScreen(
    lang: String,
    viewModel: NewsViewModel
) {

    // ✅ State Variables
    var showLoader by remember { mutableStateOf(true) }
    var selectedItem by remember { mutableStateOf<com.example.iqrarnewscompose.api.ApiNewsArticle?>(null) }
    var isUserSelected by remember { mutableStateOf(false) } // Track if user clicked manually

    // Data list
    val liveNews = remember { mutableStateListOf<com.example.iqrarnewscompose.api.ApiNewsArticle>() }

    // ✅ 1. Initial Load Logic (Handles Language Change & Loader)
    LaunchedEffect(lang) {
        showLoader = true // 🔥 Start Loader
        liveNews.clear()  // Clear old data immediately
        selectedItem = null
        isUserSelected = false

        val langParam = if (lang == "Hindi") "HINDI" else "ENGLISH"

        // Load specific Live TV Category ID (Use your actual Category ID here)
        viewModel.loadNews("84a69d51-4e22-4d76-b700-0d51aee23e37", langParam) {
            liveNews.clear()
            liveNews.addAll(viewModel.newsList)

            // Set first item as default playing
            if (liveNews.isNotEmpty()) {
                selectedItem = liveNews.first()
            }
            showLoader = false // 🔥 Stop Loader only when data arrives
        }
    }

    // ✅ 2. Auto-Refresh Logic (Runs every 30 seconds)
    LaunchedEffect(lang) {
        while (true) {
            delay(30_000L) // Wait 30 Seconds
            val langParam = if (lang == "Hindi") "HINDI" else "ENGLISH"

            viewModel.loadNews("84a69d51-4e22-4d76-b700-0d51aee23e37", langParam) {
                liveNews.clear()
                liveNews.addAll(viewModel.newsList)

                // Only update the Big Card automatically if the USER HAS NOT CLICKED anything
                if (!isUserSelected && liveNews.isNotEmpty()) {
                    selectedItem = liveNews.first()
                }
            }
        }
    }

    // ✅ MAIN UI
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F2F2)) // Light Gray Background
    ) {

        if (showLoader) {
            // 🌀 LOADER STATE: Only Circular Loader, NO TEXT
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = BrandRed
            )
        } else {
            // 📰 CONTENT STATE
            Column(modifier = Modifier.fillMaxSize()) {

                // Title
                Text(
                    text = if (lang == "Hindi") "लाइव न्यूज़" else "Live News",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextBlack,
                    modifier = Modifier.padding(16.dp)
                )

                // 📺 BIG CARD (Video Player or Image)
                Card(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Black)
                ) {
                    selectedItem?.let { item ->

                        // Check for YouTube URL or Video URL
                        val videoUrl = when {
                            !item.youtube_url.isNullOrEmpty() && item.youtube_url.first().isNotEmpty() ->
                                item.youtube_url.first()
                            else -> item.video?.firstOrNull() ?: ""
                        }

                        // Force recomposition when item changes
                        key(item.id) {
                            if (videoUrl.isNotEmpty()) {
                                // ▶️ SHOW VIDEO PLAYER
                                VideoItemCard(
                                    video = VideoArticle(
                                        title = item.name ?: "",
                                        description = item.content ?: "",
                                        thumbUrl = item.icon ?: "",
                                        videoUrl = videoUrl,
                                        date = formatDate(item.date ?: ""),
                                        views = "1K"
                                    ),
                                    isPlaying = true,
                                    onPlayClick = {}
                                )
                            } else {
                                // 🖼️ SHOW IMAGE FALLBACK (No Text)
                                Box(contentAlignment = Alignment.Center) {
                                    Image(
                                        painter = rememberAsyncImagePainter(item.icon),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    // Overlay Play Button
                                    Icon(
                                        imageVector = Icons.Default.PlayCircleFilled,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.8f),
                                        modifier = Modifier.size(50.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 📝 HEADLINE (For Big Card)
                selectedItem?.let {
                    Column(Modifier.padding(horizontal = 16.dp)) {
                        Text(
                            text = it.name ?: "",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextBlack,
                            maxLines = 2
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Watch Live", color = BrandRed, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(BrandRed, RoundedCornerShape(50))
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 📜 SMALL CARDS LIST (Scrollable)
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(liveNews) { item ->
                        // Don't show the currently playing item in the list below
                        if (item.id != selectedItem?.id) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        // Update Big Card & Stop Auto-Refresh Override
                                        selectedItem = item
                                        isUserSelected = true
                                    }
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Thumbnail
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.size(100.dp, 70.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.LightGray)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Image(
                                            painter = rememberAsyncImagePainter(item.icon),
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        Icon(
                                            imageVector = Icons.Default.PlayCircleFilled,
                                            contentDescription = null,
                                            tint = BrandRed
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                // Text Info
                                Column {
                                    Text(
                                        text = item.name ?: "",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = TextBlack,
                                        maxLines = 2,
                                        lineHeight = 18.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = formatDate(item.date ?: ""),
                                        fontSize = 12.sp,
                                        color = TextGray
                                    )
                                }
                            }
                            // Divider
                            Divider(
                                color = Color.LightGray,
                                thickness = 0.5.dp,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                    item { Spacer(modifier = Modifier.height(20.dp)) }
                }
            }
        }
    }
}

@Composable
fun EPaperNativeScreen(viewModel: NewsViewModel, lang: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val epapers = viewModel.epaperList.reversed()  // 🔥 Just add .reversed() here

    var currentPageIndex by remember { mutableIntStateOf(0) }

    // 1. DATE STATE
    val calendar = java.util.Calendar.getInstance()
    val formatter = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
    var selectedDate by remember { mutableStateOf(formatter.format(calendar.time)) }

    val S3_BASE_URL = "https://iqrar-times.s3.ap-south-1.amazonaws.com/"

    // 🔥 DATE LOGIC: Date select chesinappudu patha data clear ayyi kothadi load avthundi
    LaunchedEffect(selectedDate, lang) {
        viewModel.loadEPaper(lang, selectedDate)
        currentPageIndex = 0 // Reset index to first card
    }

    val datePickerDialog = android.app.DatePickerDialog(
        context, { _, year, month, dayOfMonth ->
            val cal = java.util.Calendar.getInstance()
            cal.set(year, month, dayOfMonth)
            selectedDate = formatter.format(cal.time)
        },
        calendar.get(java.util.Calendar.YEAR),
        calendar.get(java.util.Calendar.MONTH),
        calendar.get(java.util.Calendar.DAY_OF_MONTH)
    )

    // 🔥 MAIN UI: Column ki vertical scroll ledhu kabatti page fix ga untundi
    Column(
        modifier = Modifier.fillMaxSize().background(Color.White)
    ) {
        // --- 1. HEADER (Calendar & Actions) ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp).clickable { datePickerDialog.show() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.CalendarMonth, null, tint = BrandRed, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = selectedDate, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))

            IconButton(onClick = {
                if (epapers.isNotEmpty()) {
                    val imgPath = epapers[currentPageIndex].image ?: ""
                    val finalUrl = if (imgPath.startsWith("http")) imgPath else S3_BASE_URL + imgPath
                    downloadAndSaveImage(context, finalUrl, scope, isShare = false)
                }
            }) { Icon(Icons.Default.FileDownload, null, modifier = Modifier.size(22.dp)) }

            Spacer(modifier = Modifier.width(4.dp))

            IconButton(onClick = {
                if (epapers.isNotEmpty()) {
                    val imgPath = epapers[currentPageIndex].image ?: ""
                    val finalUrl = if (imgPath.startsWith("http")) imgPath else S3_BASE_URL + imgPath
                    downloadAndSaveImage(context, finalUrl, scope, isShare = true)
                }
            }) { Icon(Icons.Default.Share, null, modifier = Modifier.size(22.dp)) }
        }

        HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFEEEEEE))

        // --- 2. BIG NEWS IMAGE (Height Penchaanu mama) ---
        Box(
            modifier = Modifier
                .weight(1f) // 🔥 Idhi middle space motham teesukuni height ni penchuthundi
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 5.dp), // Margins thaggincham size peruguthundi
            contentAlignment = Alignment.TopCenter
        ) {
            if (epapers.isEmpty()) {
                Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = BrandRed)
                    Text("Finding news for $selectedDate", modifier = Modifier.padding(top = 10.dp), fontSize = 14.sp)
                }
            } else {
                val currentItem = epapers[currentPageIndex]
                val imgPath = currentItem.image ?: ""
                val finalImageUrl = if (imgPath.startsWith("http")) imgPath else S3_BASE_URL + imgPath

                Image(
                    painter = rememberAsyncImagePainter(model = finalImageUrl),
                    contentDescription = "News Card",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp),

                )
            }
        }

        // --- 3. NAVIGATION BUTTONS (Tab bar daggara & Distance pencham) ---
        if (epapers.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 15.dp, top = 5.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { if (currentPageIndex > 0) currentPageIndex-- },
                    enabled = currentPageIndex > 0,
                    modifier = Modifier.width(110.dp).height(38.dp),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.2.dp, if (currentPageIndex > 0) BrandRed else Color.LightGray)
                ) {
                    Text("Previous", fontSize = 13.sp, color = Color.Black, fontWeight = FontWeight.SemiBold)
                }

                // 🔥 DISTANCE: Prev/Next madhya gap ni 100dp ki pencha (Far ga undadaniki)
                Spacer(modifier = Modifier.width(100.dp))

                Button(
                    onClick = { if (currentPageIndex < epapers.size - 1) currentPageIndex++ },
                    enabled = currentPageIndex < epapers.size - 1,
                    modifier = Modifier.width(110.dp).height(38.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    border = BorderStroke(1.2.dp, if (currentPageIndex < epapers.size - 1) BrandRed else Color.LightGray)
                ) {
                    Text("Next", fontSize = 13.sp, color = Color.Black, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp)) // Bottom Nav space
    }
}

// 🔥 HELPER FUNCTION: To handle Download & Share
fun downloadAndSaveImage(context: android.content.Context, url: String, scope: kotlinx.coroutines.CoroutineScope, isShare: Boolean) {
    val loader = coil.ImageLoader(context)
    val request = coil.request.ImageRequest.Builder(context).data(url).allowHardware(false).build()

    scope.launch(kotlinx.coroutines.Dispatchers.IO) {
        val result = (loader.execute(request) as? coil.request.SuccessResult)?.drawable
        val bitmap = (result as? android.graphics.drawable.BitmapDrawable)?.bitmap

        if (bitmap != null) {
            if (isShare) {
                shareImage(context, bitmap)
            } else {
                saveImageToGallery(context, bitmap)
            }
        }
    }
}

// 🔥 SAVE TO GALLERY LOGIC
fun saveImageToGallery(context: android.content.Context, bitmap: android.graphics.Bitmap) {
    val filename = "IqrarEPaper_${System.currentTimeMillis()}.jpg"
    val contentValues = android.content.ContentValues().apply {
        put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, filename)
        put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_PICTURES)
    }

    val uri = context.contentResolver.insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
    uri?.let {
        context.contentResolver.openOutputStream(it).use { stream ->
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 100, stream!!)
        }
        // Show success toast on main thread
        (context as? android.app.Activity)?.runOnUiThread {
            android.widget.Toast.makeText(context, "Saved to Gallery!", android.widget.Toast.LENGTH_SHORT).show()
        }
    }
}

// 🔥 SHARE LOGIC
fun shareImage(context: android.content.Context, bitmap: android.graphics.Bitmap) {
    try {
        // 1. Image ni cache lo save chestunnam
        val cachePath = java.io.File(context.cacheDir, "images")
        cachePath.mkdirs() // Directory lekapothe srustisthundi
        val file = java.io.File(cachePath, "shared_news.jpg")
        val stream = java.io.FileOutputStream(file)
        bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 100, stream)
        stream.close()

        // 2. 🔥 IMPORTANT: Authority kachithamga Manifest lo unnattu ".provider" undali
        val contentUri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider", // <--- IDHI MANIFEST THO MATCH AVVALI
            file
        )

        if (contentUri != null) {
            val shareIntent = android.content.Intent().apply {
                action = android.content.Intent.ACTION_SEND
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION) // Permission ivvali
                setDataAndType(contentUri, context.contentResolver.getType(contentUri))
                putExtra(android.content.Intent.EXTRA_STREAM, contentUri)
                type = "image/jpeg"
            }
            context.startActivity(android.content.Intent.createChooser(shareIntent, "Share News Card"))
        }
    } catch (e: Exception) {
        // App crash avvakunda error ni log chesthundi
        android.util.Log.e("SHARE_ERROR", "Sharing failed: ${e.message}")
    }
}



@Composable
fun HomeScreen(
    lang: String,
    onNavigate: (String) -> Unit,
    onNewsClick: (NewsArticle) -> Unit,
    categories: List<CategoryItem>,
    viewModel: NewsViewModel
) {
    val context = LocalContext.current
    var showPreferencesScreen by remember { mutableStateOf(false) }
    var isPreferredMode by remember { mutableStateOf(false) }
    val prefs = context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
    val savedCategories = prefs.getStringSet("selected_categories", emptySet()) ?: emptySet()
    // Local loader state
    var showLoader by remember { mutableStateOf(true) }

    LaunchedEffect(lang) {
        showLoader = true
        val langParam = if (lang == "Hindi") "HINDI" else "ENGLISH"
        viewModel.loadNews("", langParam) {
            showLoader = false
        }
    }

    val newsList = viewModel.newsList
    val filteredNews = if (isPreferredMode && savedCategories.isNotEmpty()) {
        newsList.filter { news ->
            news.categories?.any { it in savedCategories } == true
        }
    } else {
        newsList
    }
    val latestNews = filteredNews.firstOrNull()

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {

        // ✅ CHANGE: List empty unte Text kaadhu, ONLY LOADER vastundi
        if (newsList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = BrandRed)
            }
        } else {
            // Data vachaka List chupistundi
            LazyColumn(modifier = Modifier.fillMaxSize().background(Color.White)) {

                item {
                    DynamicCategorySection(
                        categories = categories,
                        selected = "Home",
                        onClick = onNavigate
                    )
                }

                item {
                    latestNews?.let { news ->
                        FeaturedNewsCard(
                            img = news.icon,
                            tit = news.name ?: "",
                            meta = news.date ?: "",
                            onClick = {
                                onNewsClick(
                                    NewsArticle(
                                        news.id ?: "",
                                        news.name ?: "",
                                        news.icon,
                                        news.date ?: "",
                                        news.author ?: "",
                                        news.content ?: "",
                                        news.viewcount?.toString() ?: "0"
                                    )
                                )
                            }
                        )
                    }
                }

                item {
                    SectionHeader(if (lang == "Hindi") "लेटेस्ट न्यूज़" else "Latest News", lang, onViewAllClick = { onNavigate("") })
                }

                items(viewModel.newsList.take(5)) { news ->
                    SmallNewsCard(
                        img = news.icon,
                        tit = news.name ?: "",
                        date = news.date ?: "",
                        auth = news.author ?: "Admin",
                        onClick = {
                            onNewsClick(
                                NewsArticle(
                                    news.id ?: "",
                                    news.name ?: "",
                                    news.icon,
                                    news.date ?: "",
                                    news.author ?: "",
                                    news.content ?: "",
                                    news.viewcount?.toString() ?: "0"
                                )
                            )
                        }
                    )
                }

                item {
                    SectionHeader(if (lang == "Hindi") "विश्व समाचार" else "World News", lang, onViewAllClick = { onNavigate("") })
                }

                items(viewModel.newsList.take(5)) { news ->
                    SmallNewsCard(
                        img = news.icon,
                        tit = news.name ?: "",
                        date = news.date ?: "",
                        auth = news.author ?: "Admin",
                        onClick = {
                            onNewsClick(
                                NewsArticle(
                                    news.id ?: "",
                                    news.name ?: "",
                                    news.icon,
                                    news.date ?: "",
                                    news.author ?: "",
                                    news.content ?: "",
                                    news.viewcount?.toString() ?: "0"
                                )
                            )
                        }
                    )
                }

                item {
                    SpecialBanner(
                        tit = if (lang == "Hindi") "लाइव टीवी" else "Live TV",
                        des = if (lang == "Hindi") "अपडेट रहें" else "Stay updated",
                        btn = if (lang == "Hindi") "अभी देखें" else "Watch Now",
                        img = R.drawable.livetv
                    ) { onNavigate("Live TV") }

                    SpecialBanner(
                        tit = if (lang == "Hindi") "ई-पेपर" else "E-Paper",
                        des = if (lang == "Hindi") "आज का संस्करण" else "Today's Edition",
                        btn = if (lang == "Hindi") "अभी पढ़ें" else "Read Now",
                        img = R.drawable.epaper
                    ) { onNavigate("E-Paper") }
                }
            }
        }
    }
}

@Composable
fun CategoryNewsScreen(
    catId: String,
    lang: String,
    onNavigate: (String) -> Unit,
    onNewsClick: (NewsArticle) -> Unit,
    categories: List<CategoryItem>,
    viewModel: NewsViewModel
) {
    var showLoader by remember { mutableStateOf(true) }

    LaunchedEffect(catId, lang) {
        showLoader = true
        val langParam = if (lang == "Hindi") "HINDI" else "ENGLISH"
        viewModel.loadNews(catId, langParam) {
            showLoader = false
        }
    }

    val list = viewModel.newsList

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {

        // ✅ CHANGE: Only Loader if list is empty
        if (list.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = BrandRed)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            ) {

                item {
                    DynamicCategorySection(
                        categories = categories,
                        selected = catId,
                        onClick = onNavigate
                    )
                }

                items(viewModel.newsList) { item ->
                    SmallNewsCard(
                        img = item.icon,
                        tit = item.name ?: "",
                        date = item.date ?: "",
                        auth = item.author ?: "Admin",
                        onClick = {
                            onNewsClick(
                                NewsArticle(
                                    item.id ?: "",
                                    item.name ?: "",
                                    item.image?.firstOrNull() ?: "",
                                    item.date ?: "",
                                    item.author ?: "",
                                    item.content ?: "",
                                    item.viewcount?.toString() ?: "0"
                                )
                            )
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun DynamicCategorySection(
    categories: List<CategoryItem>,
    selected: String,

    onClick: (String) -> Unit
) {
    val parentCategories = categories
        .filter { it.parent_id == "0" || it.parent_id == null }


    val allCategories = listOf("Home" to "Home") +
            parentCategories.map { it.id to it.name }

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(allCategories) { pair ->
            val id = pair.first
            val name = pair.second
            val isSelected = selected.equals(id, true)
            val contentColor = if (isSelected) BrandRed else TextBlack

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(end = 24.dp)
                    .clickable { onClick(id.toString()) }
            ) {

                // 🔥 LOGIC CHANGE: Home ayithe Image, ledante Text
                if (id == "Home") {
                    Image(
                        // ⚠️ NOTE: Make sure 'ic_home_red' exists in drawable
                        painter = painterResource(id = R.drawable.ic_home_red),
                        contentDescription = "Home",
                        modifier = Modifier
                            .size(24.dp)
                            .padding(bottom = 2.dp),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Text(
                        text = name ?: "",
                        color = contentColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                // Red Underline Indicator
                if (isSelected) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .width(30.dp)
                            .height(3.dp)
                            .background(BrandRed, RoundedCornerShape(2.dp))
                    )
                } else {
                    Spacer(modifier = Modifier.height(7.dp))
                }
            }
        }
    }
}

@Composable
fun FeaturedNewsCard(img: String, tit: String, meta: String, onClick: () -> Unit = {}) {
    Column(modifier = Modifier.padding(16.dp).clickable { onClick() }) {
        Card(shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth().height(200.dp)) {
            Image(rememberAsyncImagePainter(img), null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(tit, color = TextBlack, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(formatDate(meta), color = TextGray, fontSize = 12.sp)
    }
}

@Composable
fun SmallNewsCard(
    img: String,
    tit: String,
    date: String,
    auth: String,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp) // 🔥 Height konchem pencham (Details clear ga undadaniki)
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp), // Shadow konchem pencham
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // 1. IMAGE CARD
            Card(
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.size(100.dp, 75.dp) // Image size set chesam
            ) {
                Image(
                    painter = rememberAsyncImagePainter(img),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 2. TEXT DETAILS
            Column(modifier = Modifier.weight(1f)) {

                // Title
                Text(
                    text = tit,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = TextBlack,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.weight(1f)) // Kinda ki push chestundi

                // 🔥 DATE & TIME ROW 🔥
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Clock Icon
                    Icon(
                        imageVector = Icons.Default.AccessTime, // 🕒 Time Icon
                        contentDescription = null,
                        tint = TextGray,
                        modifier = Modifier.size(12.dp)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    // Date & Time Text
                    // (formatDate function Date inka Time ni kalipi istundi)
                    Text(
                        text = formatDate(date),
                        fontSize = 11.sp, // Chinna font size fit avvadaniki
                        color = TextGray,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Icon(
                        imageVector = Icons.Default.Person, // Author Icon
                        contentDescription = null,
                        tint = BrandRed,
                        modifier = Modifier.size(12.dp)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = auth,
                        fontSize = 11.sp,
                        color = BrandRed,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

fun formatDate(input: String): String {
    return try {
        val parser = java.text.SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            java.util.Locale.getDefault()
        )
        parser.timeZone = java.util.TimeZone.getTimeZone("UTC")

        val date = parser.parse(input)

        val output = java.text.SimpleDateFormat(
            "dd MMM yyyy  h:mm a",
            java.util.Locale.getDefault()
        )

        output.format(date!!)
    } catch (e: Exception) {
        input
    }
}

@Composable
fun SectionHeader(tit: String, lang: String, show: Boolean = true, onViewAllClick: () -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(tit, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextBlack)
        if (show) {
            Text(
                text = if (lang == "Hindi") "सभी देखें" else "View All",
                color = BrandRed,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onViewAllClick() }
            )
        }
    }
}

@Composable
fun SpecialBanner(tit: String, des: String, btn: String, img: Any, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp, 8.dp).clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(tit, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextBlack)
                Text(des, fontSize = 11.sp, color = TextGray)
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(onClick = onClick, border = BorderStroke(1.dp, BrandRed), modifier = Modifier.height(32.dp)) {
                    Text(btn, fontSize = 11.sp, color = BrandRed, fontWeight = FontWeight.Bold)
                }
            }
            Image(rememberAsyncImagePainter(img), null, modifier = Modifier.size(80.dp, 60.dp), contentScale = ContentScale.Fit)
        }
    }
}

@Composable
fun IqrarBottomBar(sel: String, lang: String, onNavigate: (String) -> Unit) {
    NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
        val items = listOf("News", "Videos", "Live TV", "E-Paper", "Profile")
        val icons = listOf(Icons.Default.Newspaper, Icons.Default.PlayCircle, Icons.Default.LiveTv, Icons.Default.Description, Icons.Default.Person)

        items.forEachIndexed { i, item ->
            val isSelected = (sel == "Home" && item == "News") || sel == item
            val dispLabel = if (lang == "Hindi") {
                when (item) {
                    "News" -> "समाचार"
                    "Videos" -> "वीडियो"
                    "Live TV" -> "लाइव टीवी"
                    "E-Paper" -> "ई-पेपर"
                    "Profile" -> "प्रोफ़ाइल"
                    else -> item
                }
            } else item

            NavigationBarItem(
                icon = { Icon(icons[i], null, modifier = Modifier.size(24.dp)) },
                label = {
                    Text(
                        text = dispLabel,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontFamily = NotoSansFont
                    )
                },
                selected = isSelected,
                onClick = { if (item == "News") onNavigate("Home") else onNavigate(item) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = BrandRed,
                    selectedTextColor = BrandRed,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}

@Composable
fun ToggleNewsScreen(
    viewModel: NewsViewModel,
    onBack: () -> Unit
) {

    val context = LocalContext.current

    val prefs = context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)

    val selectedCategories =
        prefs.getStringSet("selected_categories", emptySet()) ?: emptySet()

    val newsList = viewModel.newsList

    val filteredNews = newsList.filter { news ->
        selectedCategories.contains(news.category_name)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(onClick = { onBack() }) {
                Icon(Icons.Default.ArrowBack, null)
            }

            Text(
                text = "Your News",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        LazyColumn {

            items(filteredNews) { news ->

                SmallNewsCard(
                    img = news.icon,
                    tit = news.name ?: "",
                    date = news.date ?: "",
                    auth = news.author ?: "Admin"
                )

            }

        }

    }
}

// 🔥 Idhi file chivarlo (bayata) add chey mama
fun shareNewsDetailWithImage(
    context: android.content.Context,
    imageUrl: String,
    title: String,
    scope: kotlinx.coroutines.CoroutineScope
) {
    val loader = coil.ImageLoader(context)
    val request = coil.request.ImageRequest.Builder(context)
        .data(imageUrl)
        .allowHardware(false)
        .build()

    scope.launch(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val result = (loader.execute(request) as? coil.request.SuccessResult)?.drawable
            val bitmap = (result as? android.graphics.drawable.BitmapDrawable)?.bitmap

            if (bitmap != null) {
                // 1. Image ni cache lo save chestunnam
                val cachePath = java.io.File(context.cacheDir, "images")
                cachePath.mkdirs()
                val file = java.io.File(cachePath, "shared_news_detail.jpg")
                val stream = java.io.FileOutputStream(file)
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 100, stream)
                stream.close()

                // 2. URI create chesthunnam (Manifest lo unnattu ".provider" undali)
                val contentUri = androidx.core.content.FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider", // Manifest authority ki match chesam
                    file
                )

                // 3. Share sheet open chesthunnam (Image + Text)
                val shareIntent = android.content.Intent().apply {
                    action = android.content.Intent.ACTION_SEND
                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    setDataAndType(contentUri, "image/jpeg")
                    putExtra(android.content.Intent.EXTRA_STREAM, contentUri) // Image stream
                    putExtra(android.content.Intent.EXTRA_TEXT, title)        // 🔥 News Title Text
                    type = "image/jpeg"
                }
                context.startActivity(android.content.Intent.createChooser(shareIntent, "Share News"))
            }
        } catch (e: Exception) {
            android.util.Log.e("SHARE_ERROR", "Sharing failed: ${e.message}")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsSectionScreen(
    article: NewsArticle,
    viewModel: NewsViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var commentText by remember { mutableStateOf("") }
    var isPosting by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    //  Refresh trigger - KEY PART
    var refreshKey by remember { mutableIntStateOf(0) }

    //  Comments load - refreshKey change ayithe reload
    LaunchedEffect(article.id, refreshKey) {
        isLoading = true
        val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
        val token = prefs.getString("token", "") ?: ""
        viewModel.loadComments(token, article.id)
        delay(500)
        isLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        // --- 1. HEADER ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.Black
                )
            }

            Text(
                text = "Comments",
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            // 🔥 REFRESH BUTTON - Other users comments load cheyadaniki
            IconButton(onClick = { refreshKey++ }) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = BrandRed
                )
            }
        }

        // --- 2. ARTICLE TITLE ---
        Text(
            text = article.title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(horizontal = 20.dp),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "${viewModel.commentsList.size} Comments",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        HorizontalDivider(color = Color(0xFFEEEEEE))

        // --- 3. COMMENTS LIST ---
        when {
            isLoading -> {
                // Loading State
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = BrandRed)
                }
            }

            viewModel.commentsList.isEmpty() -> {
                // Empty State
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.ChatBubbleOutline,
                            contentDescription = null,
                            tint = Color.LightGray,
                            modifier = Modifier.size(70.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No comments yet",
                            color = Color.Gray,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Be the first to comment!",
                            color = Color.LightGray,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            else -> {
                // Comments List
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 20.dp)
                ) {
                    items(viewModel.commentsList) { comment ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 14.dp)
                        ) {
                            // Avatar with first letter
                            Surface(
                                modifier = Modifier.size(42.dp),
                                shape = CircleShape,
                                color = BrandRed.copy(alpha = 0.1f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = (comment.user_name?.firstOrNull() ?: 'U')
                                            .uppercase()
                                            .toString(),
                                        color = BrandRed,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = comment.user_name ?: "Anonymous",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color.Black
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "• ${getTimeAgo(comment.created_at)}",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = comment.comment ?: "",
                                    fontSize = 14.sp,
                                    color = Color(0xFF444444),
                                    lineHeight = 20.sp
                                )
                            }
                        }

                        HorizontalDivider(color = Color(0xFFF5F5F5))
                    }

                    // Bottom spacing
                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }
        }

        // --- 4. COMMENT INPUT BOX (Bottom Fixed) ---
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 12.dp,
            color = Color.White
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Text Input
                OutlinedTextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    placeholder = {
                        Text(
                            "Write a comment...",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 50.dp, max = 120.dp),
                    shape = RoundedCornerShape(25.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF5F5F5),
                        unfocusedContainerColor = Color(0xFFF5F5F5),
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = BrandRed.copy(alpha = 0.5f),
                        cursorColor = BrandRed
                    ),
                    maxLines = 4,
                    singleLine = false
                )

                Spacer(modifier = Modifier.width(12.dp))

                // 🔥 SEND BUTTON with Loading
                Button(
                    onClick = {
                        if (commentText.isNotBlank() && !isPosting) {
                            isPosting = true
                            val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
                            val token = prefs.getString("token", "") ?: ""

                            // 🔥 DEBUG LOGS - Idhi add chesanu (functionality touch kaaledu)
                            Log.d("COMMENT_DEBUG", "========== COMMENT POST START ==========")
                            Log.d("COMMENT_DEBUG", "Token Empty: ${token.isEmpty()}")
                            Log.d("COMMENT_DEBUG", "Token Length: ${token.length}")
                            Log.d("COMMENT_DEBUG", "Token First 30 chars: ${token.take(30)}")
                            Log.d("COMMENT_DEBUG", "Article ID: ${article.id}")
                            Log.d("COMMENT_DEBUG", "Comment Text: $commentText")
                            Log.d("COMMENT_DEBUG", "============================================")

                            viewModel.postComment(token, article.id, commentText) { success ->
                                isPosting = false

                                // 🔥 DEBUG LOG - Success status
                                Log.d("COMMENT_DEBUG", "POST Result - Success: $success")

                                if (success) {
                                    commentText = ""
                                    refreshKey++ // 🔥 Idhi GET API call trigger chesthundi
                                    Toast.makeText(
                                        context,
                                        "Comment posted!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Failed! Try again",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    },
                    enabled = commentText.isNotBlank() && !isPosting,
                    modifier = Modifier.size(52.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandRed,
                        disabledContainerColor = Color.LightGray
                    ),
                    contentPadding = PaddingValues(0.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp
                    )
                ) {
                    if (isPosting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            Icons.Default.Send,
                            contentDescription = "Send",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}
//Updates are done in this code .
