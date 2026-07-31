package com.carloshinojosa.idealistachallenge.detail.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.carloshinojosa.idealistachallenge.design.ui.theme.IdealistaTheme
import com.carloshinojosa.idealistachallenge.design.ui.theme.OnPrimaryContainer
import com.carloshinojosa.idealistachallenge.design.ui.theme.OnSurface
import com.carloshinojosa.idealistachallenge.detail.R

@Composable
internal fun CommunityCostsCard(
    costsLabel: String,
    modifier: Modifier = Modifier,
) {
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
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_house),
                contentDescription = null,
                tint = OnPrimaryContainer,
                modifier = Modifier.size(20.dp),
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = stringResource(R.string.detail_community_costs_title),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = OnSurface,
            modifier = Modifier.weight(1f),
        )

        Text(
            text = costsLabel,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = OnSurface,
        )
    }
}

@Preview(name = "CommunityCostsCard", showBackground = true, widthDp = 360)
@Composable
private fun PreviewCommunityCostsCard() {
    IdealistaTheme { CommunityCostsCard(costsLabel = "330 €/mes") }
}
