package com.carloshinojosa.idealistachallenge.design.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

/** Idealista design-system typography — 9-level scale, Roboto (no extra font dependency). */

val PriceXL = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.ExtraBold,
    fontSize = 27.sp,
    lineHeight = 34.sp,
)

val PriceL = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.ExtraBold,
    fontSize = 23.sp,
    lineHeight = 28.sp,
)

val Headline = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.Bold,
    fontSize = 22.sp,
    lineHeight = 28.sp,
)

val Title = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.Bold,
    fontSize = 19.sp,
    lineHeight = 25.sp,
)

val Stat = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.Bold,
    fontSize = 17.sp,
    lineHeight = 22.sp,
)

val BodyLarge = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.Normal,
    fontSize = 15.sp,
    lineHeight = 24.sp,
)

val Body = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 20.sp,
)

val Label = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.SemiBold,
    fontSize = 12.sp,
    lineHeight = 16.sp,
)

val Overline = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.SemiBold,
    fontSize = 11.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.05.em,
)

/** M3 Typography wired to the DS scale. */
val IdealistaTypography = Typography(
    displaySmall = PriceXL,
    headlineLarge = PriceL,
    headlineMedium = Headline,
    titleLarge = Title,
    titleMedium = Stat,
    bodyLarge = BodyLarge,
    bodyMedium = Body,
    labelLarge = Label,
    labelSmall = Overline,
)
