package com.carloshinojosa.idealistachallenge.detail.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carloshinojosa.idealistachallenge.design.ui.theme.EnergyA
import com.carloshinojosa.idealistachallenge.design.ui.theme.EnergyB
import com.carloshinojosa.idealistachallenge.design.ui.theme.EnergyC
import com.carloshinojosa.idealistachallenge.design.ui.theme.EnergyD
import com.carloshinojosa.idealistachallenge.design.ui.theme.EnergyE
import com.carloshinojosa.idealistachallenge.design.ui.theme.EnergyF
import com.carloshinojosa.idealistachallenge.design.ui.theme.EnergyG
import androidx.compose.ui.tooling.preview.Preview
import com.carloshinojosa.idealistachallenge.design.ui.theme.IdealistaTheme
import com.carloshinojosa.idealistachallenge.design.ui.theme.OnSurface
import com.carloshinojosa.idealistachallenge.design.ui.theme.OnSurfaceVariant
import com.carloshinojosa.idealistachallenge.detail.R
import com.carloshinojosa.idealistachallenge.detail.model.EnergyUiModel

private val energyColors = listOf(EnergyA, EnergyB, EnergyC, EnergyD, EnergyE, EnergyF, EnergyG)

@Composable
internal fun EnergyCertificationCard(
    energy: EnergyUiModel,
    modifier: Modifier = Modifier,
) {
    val badgeColor = energyColors.getOrElse(energy.activeIndex) { EnergyG }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(badgeColor),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = energy.letter,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.detail_energy_title),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = OnSurface,
            )
            Text(
                text = stringResource(R.string.detail_energy_class, energy.letter),
                fontSize = 12.sp,
                color = OnSurfaceVariant,
            )
        }

        EnergyScale(activeIndex = energy.activeIndex)
    }
}

@Preview(name = "EnergyCertificationCard — E", showBackground = true, widthDp = 360)
@Composable
private fun PreviewEnergyCertificationE() {
    IdealistaTheme { EnergyCertificationCard(energy = EnergyUiModel(letter = "E", activeIndex = 4)) }
}

@Preview(name = "EnergyCertificationCard — A", showBackground = true, widthDp = 360)
@Composable
private fun PreviewEnergyCertificationA() {
    IdealistaTheme { EnergyCertificationCard(energy = EnergyUiModel(letter = "A", activeIndex = 0)) }
}

@Composable
private fun EnergyScale(activeIndex: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        energyColors.forEachIndexed { index, color ->
            val isActive = index == activeIndex
            Box(
                modifier = Modifier
                    .width(7.dp)
                    .height(if (isActive) 22.dp else 16.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color.copy(alpha = if (isActive) 1f else 0.35f)),
            )
        }
    }
}
