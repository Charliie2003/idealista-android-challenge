package com.carloshinojosa.idealistachallenge.detail.presentation.model

import androidx.annotation.DrawableRes

data class CharacteristicUiModel(
    @param:DrawableRes val icon: Int,
    val label: String,
    val value: String,
)
