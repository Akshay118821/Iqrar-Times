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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import coil.compose.rememberAsyncImagePainter
import com.example.iqrarnewscompose.ui.theme.IqrarNewsComposeTheme
import kotlinx.coroutines.delay

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
            // This prevents blank screen
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

    var selectedScreen by remember { mutableStateOf("Home") }
    var currentLanguage by remember { mutableStateOf("Hindi") }

    LaunchedEffect(currentLanguage) {
        val langParam = if (currentLanguage == "Hindi") "HINDI" else "ENGLISH"
        viewModel.loadCategories(langParam)
    }

    var showLanguageMenu by remember { mutableStateOf(false) }
    var isSearchActive by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }
    var isMainHeaderVisible by remember { mutableStateOf(true) }

    var selectedArticle by remember { mutableStateOf<NewsArticle?>(null) }

    BackHandler(enabled = selectedArticle != null) {
        selectedArticle = null
        isMainHeaderVisible = true
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (selectedArticle != null) {
                val displayCategory = if (currentLanguage == "Hindi") {
                    when (selectedScreen) {
                        else -> selectedScreen
                    }
                } else selectedScreen

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
                        if (isSearchActive) {
                            TextField(
                                value = searchText,
                                onValueChange = { searchText = it },
                                placeholder = {
                                    Text(
                                        if (currentLanguage == "Hindi") "खोजें..." else "Search...",
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                },
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    cursorColor = Color.White,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Image(
                                    painter = painterResource(id = R.drawable.logo),
                                    contentDescription = "Logo",
                                    modifier = Modifier
                                        .height(38.dp)
                                        .width(180.dp),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        if (isSearchActive) {
                            IconButton(onClick = { isSearchActive = false; searchText = "" }) {
                                Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                            }
                        } else {
                            IconButton(onClick = {}) { Icon(Icons.Default.Menu, null, tint = Color.White) }
                        }
                    },
                    actions = {
                        if (!isSearchActive) {
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
                                                viewModel.loadNews(selectedScreen, "ENGLISH") { }
                                            } else {
                                                viewModel.loadNews("", "ENGLISH") { }
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
                                                viewModel.loadNews(selectedScreen, "HINDI") { }
                                            } else {
                                                viewModel.loadNews("", "HINDI") { }
                                            }
                                        }
                                    )
                                }
                            }
                            IconButton(onClick = { isSearchActive = true }) {
                                Icon(Icons.Default.Search, null, tint = Color.White)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandRed)
                )
            }
        },
        bottomBar = {
            if (isMainHeaderVisible && selectedArticle == null) {
                IqrarBottomBar(selectedScreen, currentLanguage, onNavigate = {
                    selectedScreen = it
                    isMainHeaderVisible = true
                })
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            if (selectedArticle != null) {
                NewsDetailScreen(article = selectedArticle!!)
            } else {
                when (selectedScreen) {

                    "Home" -> HomeScreen(
                        lang = currentLanguage,
                        onNavigate = { selectedScreen = it },
                        onNewsClick = { selectedArticle = it },
                        categories = viewModel.categories,
                        viewModel = viewModel
                    )

                    "Profile" -> ProfileScreen(currentLanguage, onHeaderVisibilityChange = { isMainHeaderVisible = it })
                    "Live TV" -> LiveTVScreen(currentLanguage, viewModel)
                    "E-Paper" -> EPaperScreen("https://www.iqrartimes.com/epaper/delhi?page=1")
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

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {

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
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Share", color = BrandRed)
            }
        }
    }
}

@Composable
fun ProfileScreen(lang: String, onHeaderVisibilityChange: (Boolean) -> Unit) {
    val context = LocalContext.current
    var localView by remember { mutableStateOf("Main") }

    BackHandler(enabled = localView != "Main") {
        localView = "Main"
        onHeaderVisibilityChange(true)
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        when (localView) {
            "Main" -> {
                LaunchedEffect(Unit) { onHeaderVisibilityChange(true) }
                ProfileMenuView(
                    lang = lang,
                    onNavigate = {
                        localView = it
                        onHeaderVisibilityChange(false)
                    },
                    onContactClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:contact@iqrartimes.com")
                        }
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                        }
                    }
                )
            }

            "Terms" -> LocalWebViewScreen(
                title = if (lang == "Hindi") "टर्म्स एंड कंडीशंस" else "Terms & Conditions",
                url = "https://www.iqrartimes.com/terms-of-service",
                onBack = { localView = "Main"; onHeaderVisibilityChange(true) }
            )

            "Privacy" -> LocalWebViewScreen(
                title = if (lang == "Hindi") "प्राइवेसी पॉलिसी" else "Privacy Policy",
                url = "https://www.iqrartimes.com/privacy-policy",
                onBack = { localView = "Main"; onHeaderVisibilityChange(true) }
            )
        }
    }
}

@Composable
fun ProfileMenuView(lang: String, onNavigate: (String) -> Unit, onContactClick: () -> Unit) {
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
            Text(
                if (lang == "Hindi") "साइन इन / साइन अप" else "SIGN IN / SIGN UP",
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (lang == "Hindi") "साइन इन टू एक्सेस योर सेव็ड आर्टिकल्स" else "Sign in to access your saved articles.",
            textAlign = TextAlign.Center,
            color = TextBlack,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(35.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ProfileGridItem(Icons.Default.Language, if (lang == "Hindi") "लैंग्वेज" else "Language", Modifier.weight(1f))
            ProfileGridItem(Icons.Default.Notifications, if (lang == "Hindi") "नोटिफिकेशन" else "Notifications", Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ProfileGridItem(Icons.Default.Settings, if (lang == "Hindi") "प्रेफरेंस" else "Preferences", Modifier.weight(1f))
            ProfileGridItem(Icons.Default.Info, if (lang == "Hindi") "बारे में" else "About", Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(30.dp))
        ProfileListTile(Icons.Default.Security, if (lang == "Hindi") "प्राइवेसी पॉलिसी" else "Privacy Policy") { onNavigate("Privacy") }
        ProfileListTile(Icons.Default.Description, if (lang == "Hindi") "टर्म्स एंड कंडीशंस" else "Terms & Conditions") { onNavigate("Terms") }
        ProfileListTile(Icons.Default.Email, if (lang == "Hindi") "कॉन्टैक्टें" else "Contact") { onContactClick() }
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
        .sortedBy { it.priority ?: 0 }


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
