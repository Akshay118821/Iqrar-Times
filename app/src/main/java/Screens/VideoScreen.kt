import android.net.Uri
import android.content.Intent
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.rememberAsyncImagePainter
import com.example.iqrarnewscompose.TextBlack
import com.example.iqrarnewscompose.TextGray
import com.example.iqrarnewscompose.NewsViewModel
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.compose.ui.platform.LocalContext

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
    var showLoader by remember { mutableStateOf(true) }
    var playingVideoId by remember { mutableStateOf<String?>(null) }

    val videoNews = remember { mutableStateListOf<com.example.iqrarnewscompose.api.ApiNewsArticle>() }

    LaunchedEffect(Unit) {
        showLoader = true
        viewModel.loadNewsSeparate("84a69d51-4e22-4d76-b700-0d51aee23e37") { data ->
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

            items(videoNews) { item ->
                val videoArticle = VideoArticle(
                    title = item.name ?: "",
                    description = item.content ?: "",
                    thumbUrl = item.icon ?: "",
                    videoUrl = when {
                        !item.youtube_url.isNullOrEmpty() && item.youtube_url.first().isNotEmpty() ->
                            item.youtube_url.first()
                        else ->
                            item.video?.firstOrNull() ?: ""
                    },
                    date = formatDisplayDate(item.date ?: ""),
                    views = "1K"
                )

                if (videoArticle.videoUrl.isNotEmpty()) {
                    VideoItemCard(
                        video = videoArticle,
                        isPlaying = playingVideoId == item.id,
                        onPlayClick = {
                            playingVideoId = if (playingVideoId == item.id) null else item.id
                        }
                    )
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
fun VideoItemCard(
    video: VideoArticle,
    isPlaying: Boolean,
    onPlayClick: () -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp)
    ) {
        if (isPlaying) {
            if (isDirectVideo(video.videoUrl)) {
                val exoPlayer = remember(video.videoUrl) {
                    ExoPlayer.Builder(context).build().apply {
                        setMediaItem(MediaItem.fromUri(video.videoUrl))
                        prepare()
                        playWhenReady = true
                    }
                }
                DisposableEffect(Unit) {
                    onDispose { exoPlayer.release() }
                }
                AndroidView(
                    factory = {
                        PlayerView(context).apply {
                            player = exoPlayer
                            useController = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .padding(horizontal = 16.dp)
                )
            } else {
                val videoId = extractYouTubeVideoId(video.videoUrl)
                if (videoId.isNotEmpty()) {
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .padding(horizontal = 16.dp)) {

                        val playerView = remember(videoId) {
                            YouTubePlayerView(context).apply {
                                enableAutomaticInitialization = false
                                val options = IFramePlayerOptions.Builder()
                                    .controls(1)
                                    .fullscreen(1)
                                    .build()

                                initialize(object : AbstractYouTubePlayerListener() {
                                    override fun onReady(youTubePlayer: YouTubePlayer) {
                                        youTubePlayer.loadVideo(videoId, 0f)
                                    }
                                }, options)
                            }
                        }

                        DisposableEffect(playerView) {
                            lifecycleOwner.lifecycle.addObserver(playerView)
                            onDispose {
                                lifecycleOwner.lifecycle.removeObserver(playerView)
                                playerView.release()
                            }
                        }

                        AndroidView(
                            factory = { playerView },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        } else {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.clickable { onPlayClick() }
            ) {
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
                    video.description,
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
                    Text(" ${video.date}", fontSize = 11.sp, color = TextGray)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.RemoveRedEye, null, tint = TextGray, modifier = Modifier.size(14.dp))
                    Text(" ${video.views} views", fontSize = 11.sp, color = TextGray)
                }
            }
        }
    }
}

fun extractYouTubeVideoId(url: String): String {
    return when {
        url.contains("youtu.be/") ->
            url.substringAfter("youtu.be/").substringBefore("?").substringBefore("&")
        url.contains("youtube.com/watch") ->
            Uri.parse(url).getQueryParameter("v") ?: ""
        url.contains("youtube.com/shorts/") ->
            url.substringAfter("shorts/").substringBefore("?").substringBefore("&")
        url.contains("v=") ->
            url.substringAfter("v=").substringBefore("&")
        else -> ""
    }
}

fun isDirectVideo(url: String): Boolean {
    val lowerUrl = url.lowercase()
    return lowerUrl.contains(".mp4") ||
            lowerUrl.contains(".m3u8") ||
            lowerUrl.contains(".mpd")
}

fun formatDisplayDate(dateString: String): String {
    return try {
        dateString.split("T").first()
    } catch (e: Exception) {
        dateString
    }
}
