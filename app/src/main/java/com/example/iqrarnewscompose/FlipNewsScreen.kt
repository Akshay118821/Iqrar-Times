package com.example.iqrarnewscompose

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import kotlin.math.absoluteValue
import java.text.SimpleDateFormat
import java.util.*

// 🔥 TIME AGO HELPER
fun getTimeAgo(dateString: String?): String {
    if (dateString.isNullOrEmpty()) return "Just now"
    return try {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        format.timeZone = TimeZone.getTimeZone("UTC")
        val past = format.parse(dateString) ?: return "Just now"
        val now = Date()
        val diffInSeconds = (now.time - past.time) / 1000
        val diffInMinutes = diffInSeconds / 60
        val diffInHours = diffInMinutes / 60
        val diffInDays = diffInHours / 24

        when {
            diffInSeconds < 60 -> "Just now"
            diffInMinutes < 60 -> "${diffInMinutes}m ago"
            diffInHours < 24 -> "${diffInHours}h ago"
            else -> "${diffInDays}d ago"
        }
    } catch (e: Exception) { "Just now" }
}

@Composable
fun FlipNewsScreen(
    viewModel: NewsViewModel,
    onBack: () -> Unit,
    onPageChange: (Int) -> Unit,
    onNewsClick: (com.example.iqrarnewscompose.api.ApiNewsArticle) -> Unit,
    onScreenTap: () -> Unit,
    initialPage: Int
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
    val savedCategories = prefs.getStringSet("selected_categories", emptySet()) ?: emptySet()

    val allNews = viewModel.newsList
    val filteredNews = if (savedCategories.isEmpty()) allNews else allNews.filter { news ->
        news.categories?.any { it in savedCategories } == true
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (filteredNews.isEmpty()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = BrandRed)
        } else {
            val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { filteredNews.size })

            LaunchedEffect(pagerState.currentPage) { onPageChange(pagerState.currentPage) }

            VerticalPager(state = pagerState, modifier = Modifier.fillMaxSize(), beyondViewportPageCount = 1) { page ->
                val news = filteredNews[page]
                val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction)
                val progress = pageOffset.absoluteValue.coerceIn(0f, 1f)
                val isScrollingToNext = pageOffset > 0
                val isScrollingToPrev = pageOffset < 0

                Box(modifier = Modifier.fillMaxSize().clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onScreenTap() }) {
                    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {

                        // TOP HALF
                        Box(modifier = Modifier.fillMaxWidth().weight(0.5f).graphicsLayer {
                            if (isScrollingToPrev && page == pagerState.currentPage + 1) {
                                rotationX = -progress * 180f
                                transformOrigin = TransformOrigin(0.5f, 1f)
                                cameraDistance = 16 * density
                                alpha = if (progress > 0.5f) 0f else 1f
                            }
                        }.shadow(elevation = if (progress > 0 && progress < 1) (progress * 16).dp else 0.dp, clip = false)
                            .drawWithContent {
                                drawContent()
                                if (isScrollingToNext && page == pagerState.currentPage) {
                                    drawRect(Color.Black.copy(alpha = progress * 0.15f))
                                }
                            }) {
                            Column(modifier = Modifier.fillMaxSize()) {
                                Box(modifier = Modifier.fillMaxWidth().weight(0.8f)) {
                                    Image(
                                        painter = rememberAsyncImagePainter(news.icon),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )

                                    // 🔥 View Count Badge (Top Right)
                                    Surface(
                                        color = Color.Black.copy(alpha = 0.6f),
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier.align(Alignment.TopEnd).padding(12.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Icon(
                                                imageVector = androidx.compose.material.icons.Icons.Default.LiveTv,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = news.formattedViewCount,
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                                Box(modifier = Modifier.fillMaxWidth().background(BrandRed).padding(horizontal = 16.dp, vertical = 6.dp)) {
                                    Text(text = "IQRAR TIMES", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic)
                                }
                            }
                        }

                        // BOTTOM HALF
                        Box(modifier = Modifier.fillMaxWidth().weight(0.5f).graphicsLayer {
                            if (isScrollingToNext && page == pagerState.currentPage) {
                                rotationX = progress * 180f
                                transformOrigin = TransformOrigin(0.5f, 0f)
                                cameraDistance = 16 * density
                                alpha = if (progress > 0.5f) 0f else 1f
                            }
                        }.shadow(elevation = if (progress > 0 && progress < 1) (progress * 16).dp else 0.dp, clip = false)
                            .drawWithContent {
                                drawContent()
                                if (isScrollingToPrev && page == pagerState.currentPage + 1) {
                                    drawRect(Color.Black.copy(alpha = progress * 0.15f))
                                }
                            }) {
                            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                                Text(text = (news.name ?: "") + "..", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.Black, lineHeight = 28.sp, maxLines = 3)

                                Spacer(modifier = Modifier.height(12.dp))

                                val cleanDesc = android.text.Html.fromHtml(news.content ?: "", 0).toString()
                                val annotatedString = buildAnnotatedString {
                                    val limit = 550
                                    append(if (cleanDesc.length > limit) cleanDesc.take(limit) + "..." else cleanDesc)
                                }

                                ClickableText(
                                    text = annotatedString,
                                    style = TextStyle(fontSize = 16.sp, color = Color(0xFF333333), lineHeight = 24.sp),
                                    maxLines = 10,
                                    overflow = TextOverflow.Ellipsis,
                                    onClick = { onScreenTap() }
                                )

                                Spacer(modifier = Modifier.weight(1f))

                                // 🔥 FOOTER: Time Ago and Read More
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Ikkada 'news.date' ani petta mama, nee backend date field idi.
                                    Text(
                                        text = getTimeAgo(news.date),
                                        style = TextStyle(color = Color.Gray, fontSize = 12.sp)
                                    )

                                    Text(
                                        text = "Read More",
                                        modifier = Modifier.clickable { onNewsClick(news) },
                                        style = TextStyle(color = BrandRed, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}