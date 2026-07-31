package com.carloshinojosa.idealistachallenge.detail.composables

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.carloshinojosa.idealistachallenge.design.ui.theme.IdealistaTheme
import com.carloshinojosa.idealistachallenge.design.ui.theme.OnSurface
import com.carloshinojosa.idealistachallenge.design.ui.theme.OnSurfaceVariant
import com.carloshinojosa.idealistachallenge.design.ui.theme.Overline
import com.carloshinojosa.idealistachallenge.design.ui.theme.Stat
import com.carloshinojosa.idealistachallenge.detail.R
import com.carloshinojosa.idealistachallenge.detail.model.PropertyDetailUiModel

@Composable
internal fun HighlightsRow(
    highlights: PropertyDetailUiModel.Highlights,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(16.dp),
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HighlightCell(
            value = highlights.sizeLabel,
            key = stringResource(R.string.detail_highlight_size),
            modifier = Modifier.weight(1f),
        )
        VerticalDivider(
            modifier = Modifier.fillMaxHeight().padding(vertical = 10.dp),
            color = MaterialTheme.colorScheme.outline,
        )
        HighlightCell(
            value = "${highlights.rooms}",
            key = stringResource(R.string.detail_highlight_rooms),
            modifier = Modifier.weight(1f),
        )
        VerticalDivider(
            modifier = Modifier.fillMaxHeight().padding(vertical = 10.dp),
            color = MaterialTheme.colorScheme.outline,
        )
        HighlightCell(
            value = "${highlights.bathrooms}",
            key = stringResource(R.string.detail_highlight_baths),
            modifier = Modifier.weight(1f),
        )
        VerticalDivider(
            modifier = Modifier.fillMaxHeight().padding(vertical = 10.dp),
            color = MaterialTheme.colorScheme.outline,
        )
        HighlightCell(
            value = highlights.floorLabel.ifBlank { "—" },
            key = stringResource(R.string.detail_highlight_floor),
            modifier = Modifier.weight(1f),
        )
    }
}

@Preview(name = "HighlightsRow", showBackground = true, widthDp = 360)
@Composable
private fun PreviewHighlightsRow() {
    IdealistaTheme {
        HighlightsRow(
            highlights = PropertyDetailUiModel.Highlights(
                sizeLabel = "133 m²",
                rooms = 3,
                bathrooms = 2,
                floorLabel = "2ª",
            ),
        )
    }
}

@Composable
private fun HighlightCell(
    value: String,
    key: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(vertical = 14.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            style = Stat.copy(fontFeatureSettings = "tnum"),
            color = OnSurface,
        )
        Text(
            text = key,
            style = Overline,
            color = OnSurfaceVariant,
        )
    }
}
