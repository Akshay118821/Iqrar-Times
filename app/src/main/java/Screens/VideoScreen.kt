

import android.webkit.WebView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.rememberAsyncImagePainter
import com.example.iqrarnewscompose.TextBlack
import com.example.iqrarnewscompose.TextGray
import com.example.iqrarnewscompose.NewsViewModel


data class VideoArticle(
    val title: String,
    val description: String,
    val thumbUrl: String,
    val videoUrl: String,
    val date: String,
    val views: String
)

@Composable
fun VideosScreen(
    currentLanguage: String,
    viewModel: NewsViewModel
) {
    var selectedVideoUrl by remember { mutableStateOf<String?>(null) }
    var showLoader by remember { mutableStateOf(true) }

    val videoNews = remember { mutableStateListOf<com.example.iqrarnewscompose.api.ApiNewsArticle>() }

    LaunchedEffect(Unit) {
        showLoader = true
        viewModel.loadNewsSeparate("Home") { data: List<com.example.iqrarnewscompose.api.ApiNewsArticle> ->
            videoNews.clear()
            videoNews.addAll(data)
            showLoader = false
        }
    }



    Box(modifier = Modifier.fillMaxSize()) {

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {

            item {
                Text(
                    text = if (currentLanguage == "Hindi") "वीडियो" else "Videos",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextBlack,
                    modifier = Modifier.padding(16.dp)
                )
            }

            selectedVideoUrl?.let { url ->
                item {
                    AndroidView(
                        factory = { context ->
                            WebView(context).apply {
                                settings.javaScriptEnabled = true
                                loadUrl(url)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .padding(horizontal = 16.dp)
                    )
                }
            }
//This section of videos has been done
            items(videoNews) { item ->

                val context = LocalContext.current

                val videoArticle = VideoArticle(
                    title = item.name ?: "",
                    description = item.content ?: "",
                    thumbUrl = item.icon ?: "",
                    videoUrl = if (!item.youtube_url.isNullOrEmpty() && item.youtube_url.first().isNotEmpty()) {
                        "videoUrl = https://www.youtube.com/watch?v=dQw4w9WgXcQ,"
                    } else {
                        item.video?.firstOrNull() ?: ""
                    },
                    date = item.date ?: "",
                    views = "1K"
                )


                VideoItemCard(videoArticle) {
                    if (videoArticle.videoUrl.isNotEmpty()) {
                        selectedVideoUrl = videoArticle.videoUrl
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }

        if (showLoader) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color.Red
            )
        }
    }
}

@Composable
fun VideoItemCard(video: VideoArticle, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp)
            .clickable { onClick() }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Card(
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .padding(horizontal = 16.dp)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(video.thumbUrl),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Icon(
                imageVector = Icons.Default.PlayCircleFilled,
                contentDescription = "Play",
                tint = Color.Red,
                modifier = Modifier
                    .size(60.dp)
                    .background(Color.White, shape = RoundedCornerShape(50))
            )
        }

        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {

            Text(
                text = video.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextBlack,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = android.text.Html.fromHtml(
                    video.description ?: "",
                    android.text.Html.FROM_HTML_MODE_LEGACY
                ).toString(),
                fontSize = 14.sp,
                color = TextBlack,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DateRange, null, tint = TextGray, modifier = Modifier.size(14.dp))
                    Text(text = " ${video.date}", fontSize = 11.sp, color = TextGray)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.RemoveRedEye, null, tint = TextGray, modifier = Modifier.size(14.dp))
                    Text(text = " ${video.views} views", fontSize = 11.sp, color = TextGray)
                }
            }
        }
    }
}
