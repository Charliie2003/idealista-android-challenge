package com.carloshinojosa.idealistachallenge.detail.presentation.component

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.carloshinojosa.idealistachallenge.design.ui.theme.SurfaceVariant
import com.carloshinojosa.idealistachallenge.detail.R
import com.carloshinojosa.idealistachallenge.detail.presentation.model.ImageUiModel

/** Auto-scroll interval for the image gallery in milliseconds. */
private const val GALLERY_AUTO_SCROLL_DELAY_MS = 5_000L

/** Width-to-height ratio for the gallery box (4:3). */
private const val GALLERY_ASPECT_RATIO_W = 4f
private const val GALLERY_ASPECT_RATIO_H = 3f

/** Scrim gradient stop positions — where the dark-to-transparent fade transitions. */
private const val SCRIM_TOP_FADE_END = 0.34f
private const val SCRIM_BOTTOM_FADE_START = 0.72f

/** Hex color for the RoomTag semi-transparent background. */
private const val ROOM_TAG_BG_COLOR = 0x9E14201A

/** Maximum number of visible pager dots before truncation. */
private const val PAGER_DOT_MAX_COUNT = 10

/** Alpha for pager dots that are not the current page. */
private const val PAGER_DOT_INACTIVE_ALPHA = 0.75f

/** Fully-circular corner radius percentage for pill-shaped elements (dots, chips). */
private const val PILL_CORNER_PERCENT = 50

@Composable
internal fun PropertyGallery(
    images: List<ImageUiModel>,
    isFavorite: Boolean,
    onBackClick: () -> Unit,
    onShareClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pageCount = images.size.coerceAtLeast(1)
    val pagerState = rememberPagerState(pageCount = { pageCount })
    val currentPage = pagerState.currentPage

    if (pageCount > 1) {
        LaunchedEffect(pagerState.settledPage) {
            delay(GALLERY_AUTO_SCROLL_DELAY_MS)
            val next = (pagerState.settledPage + 1) % pageCount
            pagerState.animateScrollToPage(next)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(GALLERY_ASPECT_RATIO_W / GALLERY_ASPECT_RATIO_H),
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .testTag("gallery_pager"),
        ) { page ->
            AsyncImage(
                model = images.getOrNull(page)?.url,
                contentDescription = stringResource(
                    R.string.cd_gallery_image,
                    page + 1,
                    images.size,
                ),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .background(SurfaceVariant),
            )
        }

        GalleryScrim(modifier = Modifier.fillMaxSize())

        GalleryTopBar(
            isFavorite = isFavorite,
            onBackClick = onBackClick,
            onShareClick = onShareClick,
            onFavoriteToggle = onFavoriteToggle,
            modifier = Modifier.align(Alignment.TopCenter),
        )

        val roomName = images.getOrNull(currentPage)?.localizedName.orEmpty()
        if (roomName.isNotBlank()) {
            RoomTag(
                text = roomName,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp, bottom = 30.dp),
            )
        }

        if (images.size > 1) {
            PagerDots(
                currentPage = currentPage,
                pageCount = images.size,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 36.dp),
            )
        }
    }
}

@Composable
private fun GalleryScrim(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(
            Brush.verticalGradient(
                colorStops = arrayOf(
                    0.00f to Color.Black.copy(alpha = 0.40f),
                    SCRIM_TOP_FADE_END to Color.Transparent,
                    SCRIM_BOTTOM_FADE_START to Color.Transparent,
                    1.00f to Color.Black.copy(alpha = 0.50f),
                ),
            ),
        ),
    )
}

@Composable
private fun RoomTag(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(ROOM_TAG_BG_COLOR))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun PagerDots(
    currentPage: Int,
    pageCount: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pageCount.coerceAtMost(PAGER_DOT_MAX_COUNT)) { index ->
            val isActive = index == currentPage
            val dotWidth by animateDpAsState(
                targetValue = if (isActive) 16.dp else 5.dp,
                label = "dot_width_$index",
            )
            Box(
                modifier = Modifier
                    .width(dotWidth)
                    .height(5.dp)
                    .clip(RoundedCornerShape(PILL_CORNER_PERCENT))
                    .background(Color.White.copy(alpha = if (isActive) 1f else PAGER_DOT_INACTIVE_ALPHA)),
            )
        }
    }
}
