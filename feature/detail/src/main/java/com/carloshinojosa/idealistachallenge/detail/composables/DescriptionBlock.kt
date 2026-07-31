package com.carloshinojosa.idealistachallenge.detail.composables

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import com.carloshinojosa.idealistachallenge.design.ui.theme.IdealistaTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carloshinojosa.idealistachallenge.design.ui.theme.BodyLarge
import com.carloshinojosa.idealistachallenge.design.ui.theme.OnSurface
import com.carloshinojosa.idealistachallenge.design.ui.theme.PrimaryStrong
import com.carloshinojosa.idealistachallenge.detail.R

private const val COLLAPSED_LINES = 5

private const val PREVIEW_TEXT = "Magnífico piso en el corazón del barrio de Salamanca. " +
    "La vivienda cuenta con amplios espacios, techos altos y una distribución perfecta. " +
    "Dispone de salón comedor con terraza, cocina equipada, tres dormitorios con armarios " +
    "empotrados y dos baños completos. El edificio cuenta con portero, ascensor y trastero. " +
    "Orientación sur, muy luminoso. Reformado en 2022 con materiales de primera calidad."

@Composable
internal fun DescriptionBlock(
    text: String,
    modifier: Modifier = Modifier,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
    ) {
        Text(
            text = text,
            style = BodyLarge,
            color = OnSurface,
            maxLines = if (expanded) Int.MAX_VALUE else COLLAPSED_LINES,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(if (expanded) R.string.detail_read_less else R.string.detail_read_more),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryStrong,
            modifier = Modifier
                .minimumInteractiveComponentSize()
                .clickable { expanded = !expanded }
                .testTag("read_more_button"),
        )
    }
}

@Preview(name = "DescriptionBlock — colapsado", showBackground = true, widthDp = 360)
@Composable
private fun PreviewDescriptionCollapsed() {
    IdealistaTheme { DescriptionBlock(text = PREVIEW_TEXT) }
}
