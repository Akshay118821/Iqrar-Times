package com.example.iqrarnewscompose

import VideoArticle
import VideoItemCard
import VideosScreen
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
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
import com.example.iqrarnewscompose.ui.theme.IqrarNewsComposeTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.content.Context
import android.widget.Toast
import androidx.compose.material.icons.filled.PlayCircleFilled
import com.example.iqrarnewscompose.profile.ProfileMenuView
import com.example.iqrarnewscompose.NewsViewModel
import androidx.compose.runtime.Composable

// Data Model
data class NewsArticle(
    val title: String,
    val image: String,
    val date: String,
    val author: String,
    val content: String
)

class MainActivity : ComponentActivity() {

    private val viewModel: NewsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {

        // ✅ 1. Install Splash Screen (Must be first)
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        // ✅ 2. Pre-load Data Logic (Fix for Blank Screen)
        // Default language: Hindi
        viewModel.loadCategories("HINDI")

        var isDataLoaded = false

        // Start loading news immediately
        viewModel.loadNews("", "HINDI") {
            isDataLoaded = true
        }

        val startTime = System.currentTimeMillis()

        // ✅ 3. Keep Splash Screen until Data is Ready
        splashScreen.setKeepOnScreenCondition {
            // Keep splash if data NOT loaded AND time is less than 3 seconds
            val isTakingTooLong = (System.currentTimeMillis() - startTime) > 3000
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

                    onLoginClick = {
                        scope.launch { drawerState.close() }
                        authStep = 1
                    },

                    onLogoutClick = {

                        // clear login
                        prefs.edit().putBoolean("isLoggedIn", false).apply()
                        isLoggedIn = false

                        // close drawer
                        scope.launch { drawerState.close() }
                    }
                )
            }
        }
    ) {

        Box(modifier = Modifier.fillMaxSize()) {

            MainContent(
                viewModel = viewModel,
                onOpenDrawer = {
                    scope.launch { drawerState.open() }
                },
                openLogin = {
                    authStep = 1
                }
            )

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
                        prefs.edit().putBoolean("isLoggedIn", true).apply()

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
    onOpenDrawer: () -> Unit,
    openLogin: () -> Unit
) {

    var selectedScreen by remember { mutableStateOf("Home") }
    var currentLanguage by remember { mutableStateOf("Hindi") }

    var showLanguageMenu by remember { mutableStateOf(false) }
    var isMainHeaderVisible by remember { mutableStateOf(true) }

    var selectedArticle by remember { mutableStateOf<NewsArticle?>(null) }
    var isFlipMode by remember { mutableStateOf(false) }

    LaunchedEffect(currentLanguage) {
        val langParam = if (currentLanguage == "Hindi") "HINDI" else "ENGLISH"
        viewModel.loadCategories(langParam)
    }

    BackHandler(enabled = selectedArticle != null) {
        selectedArticle = null
        isMainHeaderVisible = true
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),

        topBar = {
            if (isFlipMode) {

                ToggleNewsScreen(
                    viewModel = viewModel,
                    onBack = {
                        isFlipMode = false
                    }
                )

            }
            else if (selectedArticle != null) {

                NewsDetailScreen(article = selectedArticle!!)

            }
            if (selectedArticle != null) {

                val categoryName = viewModel.categories
                    .find { it.id.toString() == selectedScreen }
                    ?.name ?: selectedScreen

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
                            isMainHeaderVisible = true
                        }) {
                            Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandRed)
                )

            } else if (isMainHeaderVisible) {

                TopAppBar(

                    title = {

                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {

                            Image(
                                painter = painterResource(id = R.drawable.logo),
                                contentDescription = "Logo",
                                modifier = Modifier.height(32.dp),
                                contentScale = ContentScale.Fit
                            )

                        }

                    },

                    navigationIcon = {

                        IconButton(onClick = { onOpenDrawer() }) {
                            Icon(Icons.Default.Menu, null, tint = Color.White)
                        }

                    },

                    actions = {

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Box {

                                IconButton(onClick = { showLanguageMenu = true }) {
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

                                            viewModel.loadCategories("ENGLISH")

                                            if (selectedScreen != "Home") {
                                                viewModel.loadNews(selectedScreen, "ENGLISH") {}
                                            } else {
                                                viewModel.loadNews("", "ENGLISH") {}
                                            }

                                        }
                                    )

                                    DropdownMenuItem(
                                        text = { Text("हिंदी", color = TextBlack) },
                                        onClick = {

                                            currentLanguage = "Hindi"
                                            showLanguageMenu = false

                                            viewModel.loadCategories("HINDI")

                                            if (selectedScreen != "Home") {
                                                viewModel.loadNews(selectedScreen, "HINDI") {}
                                            } else {
                                                viewModel.loadNews("", "HINDI") {}
                                            }

                                        }
                                    )
                                }

                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            Image(
                                painter = painterResource(id = R.drawable.header_badge),
                                contentDescription = "Header Badge",
                                modifier = Modifier
                                    .height(28.dp)
                                    .padding(end = 12.dp),
                                contentScale = ContentScale.Fit
                            )

                        }

                    },

                    colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandRed)

                )
            }
        },

        bottomBar = {

            if (isMainHeaderVisible && selectedArticle == null) {

                IqrarBottomBar(
                    selectedScreen,
                    currentLanguage,
                    onNavigate = {

                        selectedScreen = it
                        isMainHeaderVisible = true

                    }
                )
            }

        }

    ) { innerPadding ->

        Box(modifier = Modifier.padding(innerPadding)) {

            if (selectedArticle != null) {

                NewsDetailScreen(article = selectedArticle!!)


            } else {

                val context = LocalContext.current
                val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
                val isLoggedIn = prefs.getBoolean("isLoggedIn", false)

                when (selectedScreen) {

                    "Home" -> HomeScreen(
                        lang = currentLanguage,
                        onNavigate = { selectedScreen = it },
                        onNewsClick = { selectedArticle = it },
                        categories = viewModel.categories,
                        viewModel = viewModel
                    )

                    "Profile" -> com.example.iqrarnewscompose.profile.ProfileScreen(
                        categories = viewModel.categories,
                        onToggleHeader = { isMainHeaderVisible = it },
                        openLogin = { openLogin() }
                    )

                    "Live TV" -> LiveTVScreen(currentLanguage, viewModel)

                    "E-Paper" -> EPaperScreen(
                        "https://www.iqrartimes.com/epaper/delhi?page=1"
                    )

                    "Videos" -> VideosScreen(currentLanguage, viewModel)

                    else -> CategoryNewsScreen(
                        catId = selectedScreen,
                        lang = currentLanguage,
                        onNavigate = { selectedScreen = it },
                        onNewsClick = { selectedArticle = it },
                        categories = viewModel.categories,
                        viewModel = viewModel
                    )
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
    onLoginClick: () -> Unit,
    onLogoutClick: () -> Unit
){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .padding(top = 20.dp)
    ) {
        // --- Header Title ---
        Text(
            text = "Sign in to your account",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 30.dp)
        )

        // --- Menu Items ---

        // 1. Language Item
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Language, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = "Language", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextBlack)
            }
            Box(
                modifier = Modifier
                    .border(1.dp, Color.LightGray, RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("English", fontSize = 12.sp, color = TextBlack)
                    Icon(Icons.Outlined.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(16.dp), tint = TextBlack)
                }
            }
        }

        // 2. Settings
        DrawerMenuItem(icon = Icons.Outlined.Settings, text = "Settings")

        // 3. Privacy Policy
        DrawerMenuItem(icon = Icons.Outlined.Security, text = "Privacy Policy")

        // 4. Terms & Conditions
        DrawerMenuItem(icon = Icons.Outlined.Description, text = "Terms & Conditions")

        // 5. Contact Us
        DrawerMenuItem(icon = Icons.Outlined.HelpOutline, text = "Contact Us")

        Spacer(modifier = Modifier.height(20.dp))

        // --- Version Info ---
        Text(
            text = "Version 1.2.4",
            color = Color.Gray,
            fontSize = 14.sp,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        // --- Login Button ---
        Button(
            onClick = {
                if (isLoggedIn) {
                    onLogoutClick()
                } else {
                    onLoginClick()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF2424)), // Red Color
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(
                text = if (isLoggedIn) "Logout" else "Login",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
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

                                        // Save login status
                                        prefs.edit()
                                            .putBoolean("isLoggedIn", true)
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

@Composable
fun NewsDetailScreen(article: NewsArticle) {

    val context = LocalContext.current

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

            val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
            val isLoggedIn = prefs.getBoolean("isLoggedIn", false)

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

                        val cleanContent = android.text.Html
                            .fromHtml(article.content, android.text.Html.FROM_HTML_MODE_LEGACY)
                            .toString()

                        val shareText = article.title + "\n\n" + cleanContent

                        val intent = Intent(Intent.ACTION_SEND)
                        intent.type = "text/plain"
                        intent.putExtra(Intent.EXTRA_TEXT, shareText)

                        context.startActivity(
                            Intent.createChooser(intent, "Share via")
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

                            Toast.makeText(
                                context,
                                "Please login to comment",
                                Toast.LENGTH_SHORT
                            ).show()

                        } else {

                            Toast.makeText(
                                context,
                                "Open comment box",
                                Toast.LENGTH_SHORT
                            ).show()

                        }

                    }
                ) {

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

        }
    }
}

@Composable
fun ProfileScreen(
    lang: String,
    isLoggedIn: Boolean,
    onHeaderVisibilityChange: (Boolean) -> Unit,
    openLogin: () -> Unit
)
{
    val context = LocalContext.current
    var localView by remember { mutableStateOf("Main") }

    BackHandler(enabled = localView != "Main") {
        localView = "Main"
        onHeaderVisibilityChange(true)
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {

        when (localView) {

            "Main" -> {

                LaunchedEffect(Unit) {
                    onHeaderVisibilityChange(true)
                }

                if (isLoggedIn) {

                    // LOGIN AYINA TARUVATHA PROFILE UI
                    LoggedInProfileUI(
                        onLogout = {

                            val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)

                            prefs.edit()
                                .putBoolean("isLoggedIn", false)
                                .apply()

                        }
                    )

                } else {

                    // LOGIN KAAPOTHE SIGNIN UI
                    ProfileMenuView(
                        onNavigate = {
                            localView = it
                            onHeaderVisibilityChange(false)
                        },
                        onContactClick = {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:contact@iqrartimes.com")
                            }
                            context.startActivity(intent)
                        }
                    ) { openLogin() }

                }
            }

            "Terms" -> LocalWebViewScreen(
                title = if (lang == "Hindi") "टर्म्स एंड कंडीशंस" else "Terms & Conditions",
                url = "https://www.iqrartimes.com/terms-of-service",
                onBack = {
                    localView = "Main"
                    onHeaderVisibilityChange(true)
                }
            )

            "Privacy" -> LocalWebViewScreen(
                title = if (lang == "Hindi") "प्राइवेसी पॉलिसी" else "Privacy Policy",
                url = "https://www.iqrartimes.com/privacy-policy",
                onBack = {
                    localView = "Main"
                    onHeaderVisibilityChange(true)
                }
            )
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
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = BrandRed) }
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                color = TextBlack,
                fontSize = 18.sp,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
        AndroidView(factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.userAgentString =
                    "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
                webViewClient = WebViewClient()
                loadUrl(url)
            }
        }, modifier = Modifier.fillMaxSize())
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

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun EPaperScreen(url: String) {
    var webView: WebView? by remember { mutableStateOf(null) }
    var isLoading by remember { mutableStateOf(true) }
    BackHandler(enabled = webView?.canGoBack() == true) { webView?.goBack() }
    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.userAgentString =
                    "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
                webViewClient = object : WebViewClient() {
                    override fun onPageStarted(v: WebView?, u: String?, f: Bitmap?) {
                        isLoading = true
                    }

                    override fun onPageFinished(v: WebView?, u: String?) {
                        isLoading = false
                    }
                }
                loadUrl(url.trim()); webView = this
            }
        })
        if (isLoading) CircularProgressIndicator(color = BrandRed, modifier = Modifier.align(Alignment.Center))
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
    val latestNews = newsList.firstOrNull()

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