package com.example.iqrarnewscompose

import VideoArticle
import VideoItemCard
import VideosScreen
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import coil.compose.rememberAsyncImagePainter
import com.example.iqrarnewscompose.api.RetrofitInstance
import com.example.iqrarnewscompose.api.SendOtpRequest
import com.example.iqrarnewscompose.api.VerifyOtpRequest
import com.example.iqrarnewscompose.api.CommonResponse
import com.example.iqrarnewscompose.ui.theme.IqrarNewsComposeTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.content.Context
import android.widget.Toast
import androidx.compose.material.icons.filled.PlayCircleFilled
import com.example.iqrarnewscompose.profile.ProfileMenuView
import androidx.compose.runtime.Composable
import com.google.firebase.messaging.FirebaseMessaging
import android.util.Log
import androidx.compose.runtime.saveable.rememberSaveable
import com.example.iqrarnewscompose.profile.LoggedProfileView
import com.example.iqrarnewscompose.profile.PreferencesScreen
import kotlinx.coroutines.Dispatchers
import androidx.core.content.FileProvider
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import android.graphics.drawable.BitmapDrawable
import java.io.File
import java.io.FileOutputStream

// Data Model
data class NewsArticle(
    val id: String,
    val title: String,
    val image: String,
    val date: String,
    val author: String,
    val content: String
)

class MainActivity : ComponentActivity() {

    private val viewModel: NewsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {

        //  1. Install Splash Screen (Must be first)
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                1
            )
        }

        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    Log.d("FCM_TOKEN", token)
                }
            }

        viewModel.loadCategories("HINDI")

        var isDataLoaded = false

        // Start loading news immediately
        viewModel.loadNews("", "HINDI") {
            isDataLoaded = true
        }

        val startTime = System.currentTimeMillis()


        splashScreen.setKeepOnScreenCondition {

            val isTakingTooLong = (System.currentTimeMillis() - startTime) > 1000
            (!isDataLoaded && !isTakingTooLong)
        }

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
                        var selectedScreen = "Profile"
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
                        val tkn = result.token ?: result.data?.token ?: result.data?.accessToken ?: result.data?.auth_token ?: ""
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
    onOpenDrawer: () -> Unit,
    openLogin: () -> Unit,
    openLoginDialog: () -> Unit
) {

    var selectedScreen by remember { mutableStateOf("Home") }
    var currentLanguage by remember { mutableStateOf("Hindi") }
    var showLanguageMenu by remember { mutableStateOf(false) }
    var isMainHeaderVisible by remember { mutableStateOf(true) }
    var selectedArticle by remember { mutableStateOf<NewsArticle?>(null) }

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

    BackHandler(enabled = selectedArticle != null || isFlipMode) {
        if (selectedArticle != null) {
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
                                IconButton(onClick = {
                                    isFlipMode = true
                                    tempBarsVisible = true
                                }) {
                                    Image(painter = painterResource(id = R.drawable.header_badge), contentDescription = "Header Badge", modifier = Modifier.height(28.dp), contentScale = ContentScale.Fit)
                                }
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
                        selectedScreen = screen
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
            if (selectedArticle != null) {
                NewsDetailScreen(article = selectedArticle!!, isLoggedIn = isLoggedIn, onOpenLoginDialog = openLoginDialog)
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
                            content = apiNews.content ?: ""
                        )
                    },
                    onScreenTap = {
                        tempBarsVisible = !tempBarsVisible
                    }
                )
            }
            else {
                when (selectedScreen) {
                    "Home" -> HomeScreen(lang = currentLanguage, onNavigate = { selectedScreen = it }, onNewsClick = { selectedArticle = it }, categories = viewModel.categories, viewModel = viewModel)
                    "Profile" -> com.example.iqrarnewscompose.profile.ProfileScreen(categories = viewModel.categories, onToggleHeader = { isMainHeaderVisible = it }, openLogin = { openLogin() })
                    "Live TV" -> LiveTVScreen(currentLanguage, viewModel)
                    "E-Paper" -> EPaperNativeScreen(viewModel, currentLanguage)
                    "Videos" -> VideosScreen(currentLanguage, viewModel)
                    else -> CategoryNewsScreen(catId = selectedScreen, lang = currentLanguage, onNavigate = { selectedScreen = it }, onNewsClick = { selectedArticle = it }, categories = viewModel.categories, viewModel = viewModel)
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
    onViewProfileClick: () -> Unit // 🔥 Navigation callback
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

            DrawerMenuItem(icon = Icons.Outlined.Settings, text = "Settings")
            DrawerMenuItem(icon = Icons.Outlined.Security, text = "Privacy Policy")
            DrawerMenuItem(icon = Icons.Outlined.Description, text = "Terms & Conditions")
            DrawerMenuItem(icon = Icons.Outlined.HelpOutline, text = "Contact Us")

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
fun DrawerMenuItem(icon: ImageVector, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .clickable { /* Handle Click */ },
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
                    contentScale = ContentScale.Fit
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
                                        val tkn = body?.token ?: body?.data?.token ?: body?.data?.accessToken ?: body?.data?.auth_token ?: ""
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


// -------------------------------------------------------------------------
// EXISTING CODE (UNCHANGED BELOW THIS LINE, EXCEPT NEEDED REUSABLE FUNCS)
// -------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsDetailScreen(article: NewsArticle, isLoggedIn: Boolean, onOpenLoginDialog: () -> Unit) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    val token = prefs.getString("token", "") ?: ""

    var showCommentBox by remember { mutableStateOf(false) }
    var commentText by remember { mutableStateOf("") }
    val viewModel: NewsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val scope = rememberCoroutineScope()

    LaunchedEffect(article.id) {
        viewModel.loadComments(article.id)
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

            Text(
                text = article.title,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextBlack
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = android.text.Html
                    .fromHtml(article.content, android.text.Html.FROM_HTML_MODE_LEGACY)
                    .toString(),
                fontSize = 16.sp,
                color = TextBlack
            )

            Spacer(modifier = Modifier.height(24.dp))

            // val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
            // val isLoggedIn = prefs.getBoolean("isLoggedIn", false)

            // 🔴 ACTION ROW (Share + Bookmark + Comment)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {

                // SHARE
                // SHARE
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        // 🔥 IKKADA KOTHA LOGIC ADD CHESAM
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
                            showCommentBox = !showCommentBox
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
                    LoggedProfileView(
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
        }
    }
}


@Composable
fun LoggedInProfileUI(onLogout: () -> Unit) {

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

            ProfileGridItem(Icons.Default.Info,"About",Modifier.weight(1f))

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
fun ProfileGridItem(icon: ImageVector, text: String, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = { },
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
    val epapers = viewModel.epaperList
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
                    contentScale = ContentScale.Fit
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
                                        news.content ?: ""
                                    )
                                )
                            }
                        )
                    }
                }

                item {
                    SectionHeader(if (lang == "Hindi") "लेटेस्ट न्यूज़" else "Latest News", lang, onViewAllClick = { onNavigate("World") })
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
                                    news.content ?: ""
                                )
                            )
                        }
                    )
                }

                item {
                    SectionHeader(if (lang == "Hindi") "विश्व समाचार" else "World News", lang, onViewAllClick = { onNavigate("World") })
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
                                    news.content ?: ""
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
                        img = "https://cdn-icons-png.flaticon.com/512/3669/3669968.png"
                    ) { onNavigate("Live TV") }

                    SpecialBanner(
                        tit = if (lang == "Hindi") "ई-पेपर" else "E-Paper",
                        des = if (lang == "Hindi") "आज का संस्करण" else "Today's Edition",
                        btn = if (lang == "Hindi") "अभी पढ़ें" else "Read Now",
                        img = "https://cdn-icons-png.flaticon.com/512/2537/2537926.png"
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
                                    item.content ?: ""
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
fun SmallNewsCard(img: String, tit: String, date: String, auth: String, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(84.dp)
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.size(90.dp, 60.dp)
            ) {
                Image(
                    rememberAsyncImagePainter(img),
                    null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {

                Text(
                    tit,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = TextBlack,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.DateRange, null, tint = TextGray, modifier = Modifier.size(10.dp))
                    Text(" ${formatDate(date)}", fontSize = 9.sp, color = TextGray)

                    Spacer(modifier = Modifier.weight(1f))

                    Icon(Icons.Default.PersonOutline, null, tint = TextGray, modifier = Modifier.size(11.dp))
                    Text(" $auth", fontSize = 9.sp, color = TextGray)
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
fun SpecialBanner(tit: String, des: String, btn: String, img: String, onClick: () -> Unit) {
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