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
import androidx.compose.material.icons.filled.Close
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

@Composable
fun LiveTVScreen(
    lang: String,
    viewModel: NewsViewModel
) {

    var showLoader by remember { mutableStateOf(true) }
    var selectedItem by remember { mutableStateOf<ApiNewsArticle?>(null) }

    val liveNews = remember { mutableStateListOf<ApiNewsArticle>() }

    LaunchedEffect(lang) {
        showLoader = true
        val langParam = if (lang == "Hindi") "HINDI" else "ENGLISH"

        viewModel.loadNews("84a69d51-4e22-4d76-b700-0d51aee23e37", langParam) {
            liveNews.clear()
            liveNews.addAll(viewModel.newsList)

            selectedItem = liveNews.firstOrNull()   // ✅ default trending
            showLoader = false
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

        // ✅ STATIC BIG CARD CONTAINER
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

                if (videoUrl.isNotEmpty()) {

                    // ✅ PLAYER INSIDE FIXED CARD
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
                    // fallback LIVE banner if no video
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            "LIVE STREAMING",
                            color = Color.Red,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ✅ Headline Section (Static Style)
        selectedItem?.let {
            Column(Modifier.padding(horizontal = 16.dp)) {
                Text(
                    it.name ?: "",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
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

        // ✅ SMALL CARDS LIST (Scrollable)
        LazyColumn {

            items(liveNews) { item ->

                if (item.id != selectedItem?.id) {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedItem = item }   // ✅ only update big card
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
                                fontSize = 16.sp
                            )
                            Text(
                                formatDisplayDate(item.date ?: ""),
                                fontSize = 12.sp,
                                color = TextGray
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }

        if (showLoader) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = BrandRed)
            }
        }
    }
}

@Composable
fun FullScreenVideoPlayer(
    videoUrl: String,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {

        VideoItemCard(
            video = VideoArticle(
                title = "",
                description = "",
                thumbUrl = "",
                videoUrl = videoUrl,
                date = "",
                views = ""
            ),
            isPlaying = true,
            onPlayClick = {}
        )

        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Close, contentDescription = null, tint = Color.White)
        }
    }
}