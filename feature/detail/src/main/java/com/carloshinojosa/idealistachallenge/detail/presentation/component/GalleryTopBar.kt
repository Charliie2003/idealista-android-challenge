package com.carloshinojosa.idealistachallenge.detail.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.carloshinojosa.idealistachallenge.design.ui.theme.Favorite
import com.carloshinojosa.idealistachallenge.design.ui.theme.IdealistaTheme
import com.carloshinojosa.idealistachallenge.design.ui.theme.OnSurface
import com.carloshinojosa.idealistachallenge.detail.R

@Composable
internal fun GalleryTopBar(
    isFavorite: Boolean,
    onBackClick: () -> Unit,
    onShareClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, top = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        GalleryButton(
            onClick = onBackClick,
            contentDescription = stringResource(R.string.cd_back),
            testTag = "back_button",
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_arrow_back),
                contentDescription = null,
                tint = OnSurface,
                modifier = Modifier.size(20.dp),
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        GalleryButton(
            onClick = onShareClick,
            contentDescription = stringResource(R.string.cd_share),
            testTag = "share_button",
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_share),
                contentDescription = null,
                tint = OnSurface,
                modifier = Modifier.size(20.dp),
            )
        }
        Box(modifier = Modifier.size(8.dp))
        GalleryButton(
            onClick = onFavoriteToggle,
            contentDescription = stringResource(
                if (isFavorite) R.string.cd_property_saved else R.string.cd_save_property
            ),
            testTag = "favorite_toggle",
        ) {
            Icon(
                painter = painterResource(
                    if (isFavorite) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline
                ),
                contentDescription = null,
                tint = if (isFavorite) Favorite else OnSurface,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Preview(name = "GalleryTopBar — sin favorito", showBackground = true, backgroundColor = 0xFF00A650)
@Composable
private fun PreviewGalleryTopBarDefault() {
    IdealistaTheme {
        GalleryTopBar(
            isFavorite = false,
            onBackClick = {},
            onShareClick = {},
            onFavoriteToggle = {},
        )
    }
}

@Preview(name = "GalleryTopBar — favorito activo", showBackground = true, backgroundColor = 0xFF00A650)
@Composable
private fun PreviewGalleryTopBarFavorited() {
    IdealistaTheme {
        GalleryTopBar(
            isFavorite = true,
            onBackClick = {},
            onShareClick = {},
            onFavoriteToggle = {},
        )
    }
}

@Composable
private fun GalleryButton(
    onClick: () -> Unit,
    contentDescription: String,
    testTag: String,
    content: @Composable () -> Unit,
) {
    // Outer Box: full 48dp touch target with semantics for TalkBack
    Box(
        modifier = Modifier
            .size(48.dp)
            .semantics { this.contentDescription = contentDescription }
            .clickable(onClick = onClick)
            .testTag(testTag),
        contentAlignment = Alignment.Center,
    ) {
        // Inner Box: purely visual — shadow + clip + background
        Box(
            modifier = Modifier
                .size(44.dp)
                .shadow(elevation = 2.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(Color(0xF0FFFFFF)),
            contentAlignment = Alignment.Center,
        ) {
            content()
        }
    }
}
