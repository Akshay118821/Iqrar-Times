package com.example.iqrarnewscompose

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin // 🔥 Swift's anchorPoint equivalent
import androidx.compose.ui.graphics.graphicsLayer // 🔥 Swift's CATransform3D equivalent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.absoluteValue

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
    val filteredNews = remember(allNews, savedCategories) {
        if (savedCategories.isEmpty()) {
            allNews
        } else {
            allNews.filter { news ->
                // వార్తకు ఉన్న categories లిస్ట్ లో మన saved ID ఉందో లేదో చూస్తుంది
                news.categories?.any { it in savedCategories } == true
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (filteredNews.isEmpty()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = BrandRed)
        } else {
            val pagerState = rememberPagerState(
                initialPage = initialPage,
                pageCount = { filteredNews.size }
            )

            LaunchedEffect(pagerState.currentPage) {
                onPageChange(pagerState.currentPage)
            }

            VerticalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                beyondViewportPageCount = 1
            ) { page ->
                val news = filteredNews[page]

                // 🔥 SWIFT EXTRACTED LOGIC: Calculate page offset
                val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction)

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            // 1. Swift logic: angle = progress * .pi (180 degrees)
                            val rotation = pageOffset * -180f
                            rotationX = rotation.coerceIn(-180f, 180f)

                            // 2. Swift logic: anchorPoint (TransformOrigin)
                            // Page moving up -> Pivot at Top (0f). Page coming from bottom -> Pivot at Bottom (1f)
                            transformOrigin = TransformOrigin(
                                pivotFractionX = 0.5f,
                                pivotFractionY = if (pageOffset > 0) 0f else 1f
                            )

                            // 3. Swift logic: m34 perspective depth
                            cameraDistance = 16 * density

                            // 4. Swift logic: front/back visibility (Halfway flip)
                            // If rotation > 90 degrees, hide the card to reveal the one behind
                            alpha = if (rotation.absoluteValue > 90f) 0f else 1f

                            // 5. Translation to keep the card centered during the flip
                            translationY = pageOffset * size.height
                        }
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onScreenTap()
                        }
                ) {
                    // --- NEWS CARD UI ---
                    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
                        Image(
                            painter = rememberAsyncImagePainter(news.icon),
                            contentDescription = null,
                            modifier = Modifier.fillMaxWidth().weight(0.4f),
                            contentScale = ContentScale.Crop
                        )

                        // Red Strip
                        Box(modifier = Modifier.fillMaxWidth().background(BrandRed).padding(horizontal = 16.dp, vertical = 6.dp)) {
                            Text(text = "IQRAR TIMES", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic)
                        }

                        // Content
                        Column(modifier = Modifier.fillMaxWidth().weight(0.6f).padding(16.dp)) {
                            Text(text = (news.name ?: "") + "..", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.Black, lineHeight = 28.sp, maxLines = 3)
                            Spacer(modifier = Modifier.height(8.dp))

                            // Relative Time



                            Spacer(modifier = Modifier.height(16.dp))

                            // More... Logic
                            val cleanDesc = android.text.Html.fromHtml(news.content ?: "", 0).toString()
                            val annotatedString = buildAnnotatedString {
                                val limit = 800
                                append(if (cleanDesc.length > limit) cleanDesc.take(limit) else cleanDesc)
                                pushStringAnnotation(tag = "NAV", annotation = "more")
                                withStyle(style = SpanStyle(color = BrandRed, fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic)) {
                                    append(" more...")
                                }
                                pop()
                            }

                            ClickableText(
                                text = annotatedString,
                                style = TextStyle(fontSize = 16.sp, color = Color(0xFF333333), lineHeight = 24.sp),
                                maxLines = 15,
                                overflow = TextOverflow.Ellipsis,
                                onClick = { offset ->
                                    annotatedString.getStringAnnotations(tag = "NAV", start = offset, end = offset)
                                        .firstOrNull()?.let { onNewsClick(news) } ?: onScreenTap()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}