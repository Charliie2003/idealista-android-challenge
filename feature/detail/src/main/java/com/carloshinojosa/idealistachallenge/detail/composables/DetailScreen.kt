package com.carloshinojosa.idealistachallenge.detail.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.carloshinojosa.idealistachallenge.core.domain.util.UiText
import com.carloshinojosa.idealistachallenge.design.ui.theme.Canvas
import com.carloshinojosa.idealistachallenge.design.ui.theme.FavBg
import com.carloshinojosa.idealistachallenge.design.ui.theme.Favorite
import com.carloshinojosa.idealistachallenge.design.ui.theme.IdealistaTheme
import com.carloshinojosa.idealistachallenge.design.ui.theme.OnSurface
import com.carloshinojosa.idealistachallenge.design.ui.theme.Surface
import com.carloshinojosa.idealistachallenge.detail.R
import com.carloshinojosa.idealistachallenge.detail.model.CharacteristicUiModel
import com.carloshinojosa.idealistachallenge.detail.model.DetailUiState
import com.carloshinojosa.idealistachallenge.detail.model.EnergyUiModel
import com.carloshinojosa.idealistachallenge.detail.model.ImageUiModel
import com.carloshinojosa.idealistachallenge.detail.model.PropertyDetailUiModel

@Composable
internal fun DetailScreen(
    state: DetailUiState,
    onFavoriteToggle: () -> Unit,
    onRetry: () -> Unit,
    onBackClick: () -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (state) {
        is DetailUiState.Loading -> LoadingState(modifier = modifier)
        is DetailUiState.Error -> {
            val context = LocalContext.current
            val message = when (val msg = state.message) {
                is UiText.DynamicString -> msg.value
                is UiText.StringResource -> context.getString(msg.resId, *msg.args)
            }
            ErrorState(message = message, onRetry = onRetry, modifier = modifier)
        }
        is DetailUiState.Content -> DetailContent(
            state = state,
            onFavoriteToggle = onFavoriteToggle,
            onBackClick = onBackClick,
            onShareClick = onShareClick,
            modifier = modifier,
        )
    }
}

@Composable
private fun DetailContent(
    state: DetailUiState.Content,
    onFavoriteToggle: () -> Unit,
    onBackClick: () -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val property = state.property
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Canvas)
            .verticalScroll(rememberScrollState()),
    ) {
        PropertyGallery(
            images = property.images,
            isFavorite = state.isFavorite,
            onBackClick = onBackClick,
            onShareClick = onShareClick,
            onFavoriteToggle = onFavoriteToggle,
        )

        // Sheet: rounded top, -20dp offset to overlap gallery bottom
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-20).dp)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(Surface)
                .padding(start = 18.dp, end = 18.dp, top = 22.dp, bottom = 40.dp),
        ) {
            PropertyHeader(property = property)

            Spacer(modifier = Modifier.height(20.dp))

            HighlightsRow(highlights = property.highlights)

            SectionDivider()

            SectionTitle(text = stringResource(R.string.detail_section_description))
            Spacer(modifier = Modifier.height(10.dp))
            DescriptionBlock(text = property.description)

            if (property.characteristics.isNotEmpty()) {
                SectionDivider()
                SectionTitle(text = stringResource(R.string.detail_section_characteristics))
                Spacer(modifier = Modifier.height(10.dp))
                CharacteristicsFlow(characteristics = property.characteristics)
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (property.energyCertification != null) {
                Spacer(modifier = Modifier.height(12.dp))
                EnergyCertificationCard(energy = property.energyCertification)
            }

            if (property.communityCostsLabel != null) {
                Spacer(modifier = Modifier.height(12.dp))
                CommunityCostsCard(costsLabel = property.communityCostsLabel)
            }

            if (state.favoritedDateLabel != null) {
                Spacer(modifier = Modifier.height(20.dp))
                FavoritePill(label = state.favoritedDateLabel)
            }
        }
    }
}

@Composable
private fun SectionDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 24.dp),
        color = MaterialTheme.colorScheme.outline,
    )
}

@Composable
private fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = OnSurface,
        modifier = modifier,
    )
}

@Composable
private fun FavoritePill(label: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(FavBg)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_heart_filled),
            contentDescription = null,
            tint = Favorite,
            modifier = Modifier.size(14.dp),
        )
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Favorite,
        )
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

private fun previewProperty() = PropertyDetailUiModel(
    id = "1",
    operationLabel = "Venta",
    statusLabel = "Buen estado",
    priceLabel = "1.195.000",
    priceSuffix = "€",
    title = "Calle de Serrano, 42",
    neighborhood = "Salamanca",
    district = "Salamanca",
    municipality = "Madrid",
    images = listOf(ImageUiModel(url = "", localizedName = "Salón")),
    highlights = PropertyDetailUiModel.Highlights(
        sizeLabel = "133 m²",
        rooms = 3,
        bathrooms = 2,
        floorLabel = "2ª",
    ),
    description = "Magnífico piso en el corazón del barrio de Salamanca. La vivienda cuenta " +
        "con amplios espacios, techos altos y una distribución perfecta. Dispone de salón " +
        "comedor con terraza, cocina equipada y tres dormitorios con armarios empotrados.",
    characteristics = listOf(
        CharacteristicUiModel(R.drawable.ic_ruler, "Superficie", "133 m²"),
        CharacteristicUiModel(R.drawable.ic_bed, "Habitaciones", "3"),
        CharacteristicUiModel(R.drawable.ic_bath, "Baños", "2"),
        CharacteristicUiModel(R.drawable.ic_floor, "Planta", "2ª · Exterior"),
        CharacteristicUiModel(R.drawable.ic_elevator, "Ascensor", "Sí"),
        CharacteristicUiModel(R.drawable.ic_house, "Trastero", "Sí"),
    ),
    energyCertification = EnergyUiModel(letter = "E", activeIndex = 4),
    communityCostsLabel = "330 €/mes",
    latitude = 40.4168,
    longitude = -3.7038,
)

@Preview(name = "DetailScreen — Loading", showBackground = true)
@Composable
private fun PreviewDetailLoading() {
    IdealistaTheme {
        DetailScreen(
            state = DetailUiState.Loading,
            onFavoriteToggle = {},
            onRetry = {},
            onBackClick = {},
            onShareClick = {},
        )
    }
}

@Preview(name = "DetailScreen — Error", showBackground = true)
@Composable
private fun PreviewDetailError() {
    IdealistaTheme {
        DetailScreen(
            state = DetailUiState.Error(UiText.DynamicString("No se ha podido cargar el detalle")),
            onFavoriteToggle = {},
            onRetry = {},
            onBackClick = {},
            onShareClick = {},
        )
    }
}

@Preview(name = "DetailScreen — Contenido", showBackground = true, device = "spec:width=360dp,height=800dp,dpi=420")
@Composable
fun PreviewDetailContent() {
    IdealistaTheme {
        DetailScreen(
            state = DetailUiState.Content(
                property = previewProperty(),
                isFavorite = true,
                favoritedDateLabel = "Guardado el 28 de julio de 2026",
            ),
            onFavoriteToggle = {},
            onRetry = {},
            onBackClick = {},
            onShareClick = {},
        )
    }
}
