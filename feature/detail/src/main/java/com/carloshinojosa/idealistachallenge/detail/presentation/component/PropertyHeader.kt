package com.carloshinojosa.idealistachallenge.detail.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.carloshinojosa.idealistachallenge.design.ui.theme.BodyLarge
import com.carloshinojosa.idealistachallenge.design.ui.theme.IdealistaTheme
import com.carloshinojosa.idealistachallenge.design.ui.theme.OnSurface
import com.carloshinojosa.idealistachallenge.design.ui.theme.OnSurfaceVariant
import com.carloshinojosa.idealistachallenge.design.ui.theme.PriceXL
import com.carloshinojosa.idealistachallenge.design.ui.theme.Title
import com.carloshinojosa.idealistachallenge.detail.R
import com.carloshinojosa.idealistachallenge.detail.presentation.model.EnergyUiModel
import com.carloshinojosa.idealistachallenge.detail.presentation.model.ImageUiModel
import com.carloshinojosa.idealistachallenge.detail.presentation.model.PropertyDetailUiModel

/** Fully-circular corner radius percentage for pill-shaped chips. */
private const val CHIP_CORNER_PERCENT = 50

@Composable
internal fun PropertyHeader(
    property: PropertyDetailUiModel,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        TagChipsRow(
            operationLabel = property.operationLabel,
            statusLabel = property.statusLabel,
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row {
            Text(
                text = property.priceLabel,
                style = PriceXL,
                color = OnSurface,
                modifier = Modifier.alignByBaseline(),
            )
            Text(
                text = "  ${property.priceSuffix}",
                style = BodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = OnSurfaceVariant,
                modifier = Modifier.alignByBaseline(),
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = property.title,
            style = Title,
            color = OnSurface,
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(R.drawable.ic_pin),
                contentDescription = null,
                tint = OnSurfaceVariant,
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = "  ${property.neighborhood}",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = OnSurface,
            )
            Text(
                text = " · ${property.district}, ${property.municipality}",
                fontSize = 14.sp,
                color = OnSurfaceVariant,
            )
        }
    }
}

@Suppress("MagicNumber")
@Preview(name = "PropertyHeader — venta con estado", showBackground = true, widthDp = 360)
@Composable
private fun PreviewPropertyHeader() {
    IdealistaTheme {
        PropertyHeader(
            property = PropertyDetailUiModel(
                id = "1",
                operationLabel = "Venta",
                statusLabel = "Buen estado",
                priceLabel = "1.195.000",
                priceSuffix = "€",
                title = "Calle de Serrano, 42",
                neighborhood = "Salamanca",
                district = "Salamanca",
                municipality = "Madrid",
                images = listOf(ImageUiModel(url = "", localizedName = "")),
                highlights = PropertyDetailUiModel.Highlights("133 m²", 3, 2, "2ª"),
                description = "",
                characteristics = emptyList(),
                energyCertification = EnergyUiModel("E", 4),
                communityCostsLabel = null,
                latitude = 40.4168,
                longitude = -3.7038,
            ),
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Composable
private fun TagChipsRow(
    operationLabel: String,
    statusLabel: String?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SurfaceChip(
            text = operationLabel,
            backgroundColor = MaterialTheme.colorScheme.primaryContainer,
            textColor = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        if (statusLabel != null) {
            SurfaceChip(
                text = statusLabel,
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                textColor = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SurfaceChip(
    text: String,
    backgroundColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = textColor,
        modifier = modifier
            .clip(RoundedCornerShape(CHIP_CORNER_PERCENT))
            .background(backgroundColor)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}
