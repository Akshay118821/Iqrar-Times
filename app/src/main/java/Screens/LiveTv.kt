package com.example.iqrarnewscompose.Screens

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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.iqrarnewscompose.BrandRed
import com.example.iqrarnewscompose.TextBlack
import com.example.iqrarnewscompose.TextGray

data class LiveItem(val thumbUrl: String, val title: String, val meta: String)

@Composable
fun LiveTVScreen(currentLanguage: String) {
    val liveList = listOf(
        LiveItem("https://i.ibb.co/b155FKJ/black-thumb.png", "Breaking: New Climate Report", "1h ago • 10 min read"),
        LiveItem("https://i.ibb.co/NFxqXqK/muslim-news-thumb.png", "Tech Giants Face Antitrust", "2h ago • 15 min read"),
        LiveItem("https://i.ibb.co/tPKyd1P/breaking-news-thumb.png", "Local Elections: Key Races", "3h ago • 8 min read")
    )

    LazyColumn(modifier = Modifier.fillMaxSize().background(Color.White)) {
        item {
            Text("Live News", fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp))

            // ✅ MAIN BLACK LIVE CARD
            Box(Modifier.padding(horizontal = 16.dp).clickable { }, contentAlignment = Alignment.Center) {
                Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().height(220.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF111118))) {
                    Image(rememberAsyncImagePainter("https://i.ibb.co/4W6xm7V/live-streaming-banner.png"), null, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize().padding(24.dp))
                }
            }

            Text("Live Coverage: Masjid Al-Aqsa Developments", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(16.dp))
            Row(Modifier.padding(start = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Watch Live", color = BrandRed, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(6.dp))
                Box(Modifier.size(8.dp).background(BrandRed, RoundedCornerShape(50)))
            }
            Spacer(Modifier.height(16.dp))
        }

        items(liveList) { item ->
            Row(Modifier.fillMaxWidth().padding(16.dp, 8.dp).clickable { }, verticalAlignment = Alignment.CenterVertically) {
                Card(shape = RoundedCornerShape(8.dp), modifier = Modifier.size(100.dp, 70.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Image(rememberAsyncImagePainter(item.thumbUrl), null, contentScale = ContentScale.Crop)
                        Icon(Icons.Default.PlayCircleFilled, null, tint = Color.Red)
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(item.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(item.meta, color = TextGray, fontSize = 12.sp)
                }
            }
        }
    }
}