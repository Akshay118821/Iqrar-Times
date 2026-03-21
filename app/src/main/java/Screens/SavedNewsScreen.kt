package com.example.iqrarnewscompose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedNewsScreen(
    savedArticles: List<NewsArticle>,
    onBack: () -> Unit,
    onNewsClick: (NewsArticle) -> Unit,
    onRemoveBookmark: (NewsArticle) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // --- 1. BODY HEADER (White section with back button and title) ---
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextBlack
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Saved News",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextBlack
                )
            }
            Text(
                text = "Your bookmarked news",
                fontSize = 14.sp,
                color = TextGray,
                modifier = Modifier.padding(start = 56.dp) // Align with title text
            )
        }

        // --- 3. LIST OF SAVED ARTICLES ---
        if (savedArticles.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "No bookmarked news found", color = TextGray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(savedArticles) { article ->
                    SavedNewsItem(
                        article = article,
                        onClick = { onNewsClick(article) },
                        onRemove = { onRemoveBookmark(article) }
                    )
                }
            }
        }
    }
}

@Composable
fun SavedNewsItem(
    article: NewsArticle,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // News Image
            Card(
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.size(90.dp, 80.dp)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(article.image),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Text content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = article.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = TextBlack,
                    lineHeight = 18.sp
                )
                
                Spacer(modifier = Modifier.weight(1f))

                // Metadata row (Optional as per some mockup states)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccessTime, null, tint = TextGray, modifier = Modifier.size(10.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "1 day ago", fontSize = 10.sp, color = TextGray) // Placeholder or actual date
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = article.author.uppercase(), fontSize = 10.sp, color = TextGray)
                }
            }

            // Bookmark Icon (Red and filled in Saved screen)
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Default.Bookmark,
                    contentDescription = "Remove Bookmark",
                    tint = BrandRed,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
