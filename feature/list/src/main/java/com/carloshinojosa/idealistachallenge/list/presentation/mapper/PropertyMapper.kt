package com.carloshinojosa.idealistachallenge.list.presentation.mapper

import android.content.Context
import com.carloshinojosa.idealistachallenge.core.domain.model.Property
import com.carloshinojosa.idealistachallenge.list.R
import com.carloshinojosa.idealistachallenge.list.presentation.model.PropertyCardUiModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.NumberFormat
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Maps a domain [com.carloshinojosa.idealistachallenge.core.domain.model.Property] to [com.carloshinojosa.idealistachallenge.list.presentation.model.PropertyCardUiModel] for display in the listing screen.
 * String resources are resolved via [android.content.Context] to avoid hardcoding user-visible text.
 */
@Singleton
class PropertyMapper @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    private val priceFormatter = NumberFormat.getInstance(Locale.forLanguageTag("es-ES"))
    private val dateFormatter = DateTimeFormatter
        .ofLocalizedDate(FormatStyle.LONG)
        .withLocale(Locale.forLanguageTag("es-ES"))

    fun map(property: Property): PropertyCardUiModel {
        val operationLabel = when (property.operation) {
            Property.Companion.OPERATION_SALE -> context.getString(R.string.listing_filter_sale)
            Property.Companion.OPERATION_RENT -> context.getString(R.string.listing_filter_rent)
            else           -> property.operation
        }

        val amount = property.priceInfo.amount
        val priceAmountText = priceFormatter.format(amount)
        val priceSuffixText = property.priceInfo.currencySuffix.trim()

        val favDateLabel = property.favoritedAt?.let { instant ->
            val zdt = instant.atZone(ZoneId.systemDefault())
            val formatted = dateFormatter.format(zdt)
            context.getString(R.string.listing_saved_on, formatted)
        }

        val thumbnail = property.thumbnail
            ?: property.images.firstOrNull()?.url
            ?: ""

        return PropertyCardUiModel(
            id = property.id,
            thumbnailUrl = thumbnail,
            operationType = property.operation,
            operationLabel = operationLabel,
            priceAmountText = priceAmountText,
            priceSuffixText = priceSuffixText,
            neighborhood = property.neighborhood,
            district = property.district,
            rooms = property.rooms,
            bathrooms = property.bathrooms,
            sizeLabel = context.getString(R.string.listing_size_label, property.size.toInt()),
            isFavorite = property.isFavorited,
            favoritedDateLabel = favDateLabel,
            isNew = property.isNew,
        )
    }
}