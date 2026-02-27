package com.example.iqrarnewscompose.Screens

import VideoArticle
import VideoItemCard
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.iqrarnewscompose.*
import com.example.iqrarnewscompose.api.ApiNewsArticle
import formatDisplayDate
import kotlinx.coroutines.delay

@Composable
fun LiveTVScreen(
    lang: String,
    viewModel: NewsViewModel
) {

    var showLoader by remember { mutableStateOf(true) }
    var selectedItem by remember { mutableStateOf<ApiNewsArticle?>(null) }
    var isUserSelected by remember { mutableStateOf(false) } // Track user selection

    val liveNews = remember { mutableStateListOf<ApiNewsArticle>() }

    // ✅ Initial Load
    LaunchedEffect(lang) {
        showLoader = true
        val langParam = if (lang == "Hindi") "HINDI" else "ENGLISH"

        viewModel.loadNews("84a69d51-4e22-4d76-b700-0d51aee23e37", langParam) {
            liveNews.clear()
            liveNews.addAll(viewModel.newsList)

            if (!isUserSelected) {
                selectedItem = liveNews.firstOrNull()
            }
            showLoader = false
        }
    }

    // ✅ Auto-refresh Logic (Updates Trending if user hasn't clicked anything)
    LaunchedEffect(lang) {
        while (true) {
            delay(30_000L) // 30 Seconds refresh
            val langParam = if (lang == "Hindi") "HINDI" else "ENGLISH"

            viewModel.loadNews("84a69d51-4e22-4d76-b700-0d51aee23e37", langParam) {
                liveNews.clear()
                liveNews.addAll(viewModel.newsList)

                // Only update big card automatically if user hasn't selected manually
                if (!isUserSelected) {
                    selectedItem = liveNews.firstOrNull()
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F2F2))
    ) {

        // ✅ Title
        Text(
            text = if (lang == "Hindi") "लाइव न्यूज़" else "Live News",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )

        // ✅ BIG CARD CONTAINER
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Black)
        ) {

            selectedItem?.let { item ->

                val videoUrl = when {
                    !item.youtube_url.isNullOrEmpty() && item.youtube_url.first().isNotEmpty() ->
                        item.youtube_url.first()
                    else ->
                        item.video?.firstOrNull() ?: ""
                }

                // Force recomposition when item changes
                key(item.id) {
                    if (videoUrl.isNotEmpty()) {
                        // ✅ PLAY VIDEO
                        VideoItemCard(
                            video = VideoArticle(
                                title = item.name ?: "",
                                description = item.content ?: "",
                                thumbUrl = item.icon ?: "",
                                videoUrl = videoUrl,
                                date = formatDisplayDate(item.date ?: ""),
                                views = "1K"
                            ),
                            isPlaying = true,
                            onPlayClick = {}
                        )
                    } else {
                        // ✅ CHANGE HERE: Show Image instead of "Live Streaming" text
                        Box(contentAlignment = Alignment.Center) {
                            Image(
                                painter = rememberAsyncImagePainter(item.icon),
                                contentDescription = null,
                                contentScale = ContentScale.Crop, // Fill the card
                                modifier = Modifier.fillMaxSize()
                            )

                            // Optional: Show a Play Icon overlay to look nice
                            Icon(
                                imageVector = Icons.Default.PlayCircleFilled,
                                contentDescription = "Play",
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(50.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ✅ Headline Section
        selectedItem?.let {
            Column(Modifier.padding(horizontal = 16.dp)) {
                Text(
                    it.name ?: "",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Watch Live",
                        color = Color.Red,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color.Red, RoundedCornerShape(50))
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ✅ SMALL CARDS LIST
        LazyColumn {

            items(liveNews, key = { it.id ?: it.hashCode() }) { item ->

                if (item.id != selectedItem?.id) {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedItem = item
                                isUserSelected = true // User clicked, so stop auto-refresh override
                            }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Card(
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.size(100.dp, 70.dp)
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
                                    tint = Color.Red
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                item.name ?: "",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                maxLines = 2
                            )
                            Text(
                                formatDisplayDate(item.date ?: ""),
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                    Divider(color = Color.LightGray, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }

        if (showLoader) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.Red)
            }
        }
    }
}