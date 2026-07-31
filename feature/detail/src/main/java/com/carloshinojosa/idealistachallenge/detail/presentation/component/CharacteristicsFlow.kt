package com.carloshinojosa.idealistachallenge.detail.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.carloshinojosa.idealistachallenge.design.ui.theme.IdealistaTheme
import com.carloshinojosa.idealistachallenge.detail.R
import com.carloshinojosa.idealistachallenge.detail.presentation.model.CharacteristicUiModel

/**
 * Displays property characteristics as a [FlowRow] of non-interactive [SuggestionChip] items.
 * [SuggestionChip] is the M3 chip for read-only informational content; [AssistChip] implies
 * an action. [FlowRow] is used instead of a 2-column grid to avoid nested scrolling constraints
 * and to naturally reflow on narrow screens — see ADR-0009.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun CharacteristicsFlow(
    characteristics: List<CharacteristicUiModel>,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        characteristics.forEach { item ->
            SuggestionChip(
                onClick = {},
                label = { Text(text = "${item.label}: ${item.value}") },
                icon = {
                    Icon(
                        painter = painterResource(item.icon),
                        contentDescription = null,
                        modifier = Modifier.size(SuggestionChipDefaults.IconSize),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
            )
        }
    }
}

@Preview(name = "CharacteristicsFlow", showBackground = true, widthDp = 360)
@Composable
private fun PreviewCharacteristicsFlow() {
    IdealistaTheme {
        CharacteristicsFlow(
            characteristics = listOf(
                CharacteristicUiModel(R.drawable.ic_ruler, "Superficie", "133 m²"),
                CharacteristicUiModel(R.drawable.ic_bed, "Habitaciones", "3"),
                CharacteristicUiModel(R.drawable.ic_bath, "Baños", "2"),
                CharacteristicUiModel(R.drawable.ic_floor, "Planta", "2ª · Exterior"),
                CharacteristicUiModel(R.drawable.ic_elevator, "Ascensor", "Sí"),
                CharacteristicUiModel(R.drawable.ic_house, "Trastero", "Sí"),
            ),
        )
    }
}
