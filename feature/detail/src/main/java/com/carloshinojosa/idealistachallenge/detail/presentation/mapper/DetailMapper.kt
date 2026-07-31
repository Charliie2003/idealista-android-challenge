package com.carloshinojosa.idealistachallenge.detail.presentation.mapper

import android.content.Context
import com.carloshinojosa.idealistachallenge.core.domain.model.EnergyCertification
import com.carloshinojosa.idealistachallenge.core.domain.model.MoreCharacteristics
import com.carloshinojosa.idealistachallenge.core.domain.model.Property
import com.carloshinojosa.idealistachallenge.core.domain.model.PropertyDetail
import com.carloshinojosa.idealistachallenge.detail.R
import com.carloshinojosa.idealistachallenge.detail.presentation.model.CharacteristicUiModel
import com.carloshinojosa.idealistachallenge.detail.presentation.model.EnergyUiModel
import com.carloshinojosa.idealistachallenge.detail.presentation.model.ImageUiModel
import com.carloshinojosa.idealistachallenge.detail.presentation.model.PropertyDetailUiModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Maps [com.carloshinojosa.idealistachallenge.core.domain.model.PropertyDetail] (domain) to [com.carloshinojosa.idealistachallenge.detail.presentation.model.PropertyDetailUiModel] (UI layer).
 * String resources and number formatting are resolved here so Composables stay stateless.
 * java.time is available on API 24+ via coreLibraryDesugaring — no @RequiresApi needed.
 */
@Singleton
class DetailMapper @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    private val priceFormatter = NumberFormat.getInstance(Locale.forLanguageTag("es-ES"))
    private val dateFormatter: DateTimeFormatter = DateTimeFormatter
        .ofLocalizedDate(FormatStyle.LONG)
        .withLocale(Locale.forLanguageTag("es-ES"))

    fun map(detail: PropertyDetail): PropertyDetailUiModel {
        val property = detail.property
        val mc = detail.moreCharacteristics

        val operationLabel = when (property.operation) {
            Property.OPERATION_SALE -> context.getString(R.string.detail_op_sale)
            Property.OPERATION_RENT -> context.getString(R.string.detail_op_rent)
            else -> property.operation
        }

        val images = detail.images.map { img ->
            ImageUiModel(url = img.url, localizedName = img.localizedName ?: img.tag)
        }.ifEmpty {
            listOf(ImageUiModel(url = property.thumbnail ?: "", localizedName = ""))
        }

        return PropertyDetailUiModel(
            id = property.id,
            operationLabel = operationLabel,
            statusLabel = mc?.let { resolveStatus(it.status) },
            priceLabel = priceFormatter.format(property.priceInfo.amount.toLong()),
            priceSuffix = property.priceInfo.currencySuffix.trim(),
            title = property.address,
            neighborhood = property.neighborhood,
            district = property.district,
            municipality = property.municipality,
            images = images,
            highlights = PropertyDetailUiModel.Highlights(
                sizeLabel = "${mc?.constructedArea ?: property.size.toInt()} m²",
                rooms = mc?.roomNumber ?: property.rooms,
                bathrooms = mc?.bathNumber ?: property.bathrooms,
                floorLabel = floorString(mc?.floor ?: property.floor),
            ),
            description = detail.description.ifBlank { property.description },
            characteristics = if (mc != null) buildCharacteristics(mc) else emptyList(),
            energyCertification = detail.energyCertification?.let { mapEnergy(it) },
            communityCostsLabel = mc?.communityCosts?.takeIf { it > 0 }?.let {
                context.getString(
                    R.string.detail_community_costs_value,
                    priceFormatter.format(it.toLong())
                )
            },
            latitude = property.latitude,
            longitude = property.longitude,
        )
    }

    fun formatFavoriteDate(instant: Instant): String {
        val formatted = dateFormatter.format(instant.atZone(ZoneId.systemDefault()))
        return context.getString(R.string.detail_saved_on, formatted)
    }

    private fun resolveStatus(status: String): String? = when (status.lowercase()) {
        "renew" -> context.getString(R.string.detail_status_renew)
        "good" -> context.getString(R.string.detail_status_good)
        "newdevelopment" -> context.getString(R.string.detail_status_new)
        else -> null
    }

    private fun floorString(floor: String?): String = floor ?: ""

    private fun buildCharacteristics(mc: MoreCharacteristics): List<CharacteristicUiModel> = buildList {
        add(
            CharacteristicUiModel(
                icon = R.drawable.ic_ruler,
                label = context.getString(R.string.detail_char_surface),
                value = "${mc.constructedArea} m²",
            )
        )
        add(
            CharacteristicUiModel(
                icon = R.drawable.ic_bed,
                label = context.getString(R.string.detail_char_rooms),
                value = "${mc.roomNumber}",
            )
        )
        add(
            CharacteristicUiModel(
                icon = R.drawable.ic_bath,
                label = context.getString(R.string.detail_char_baths),
                value = "${mc.bathNumber}",
            )
        )
        add(
            CharacteristicUiModel(
                icon = R.drawable.ic_floor,
                label = context.getString(R.string.detail_char_floor),
                value = mc.floor
            )
        )
        add(
            CharacteristicUiModel(
                icon = R.drawable.ic_elevator,
                label = context.getString(R.string.detail_char_elevator),
                value = if (mc.lift) context.getString(R.string.detail_yes) else context.getString(R.string.detail_no),
            )
        )
        add(
            CharacteristicUiModel(
                icon = R.drawable.ic_house,
                label = context.getString(R.string.detail_char_boxroom),
                value = if (mc.boxroom) context.getString(R.string.detail_yes) else context.getString(
                    R.string.detail_no
                ),
            )
        )
    }

    private fun mapEnergy(ec: EnergyCertification): EnergyUiModel? {
        val letter = ec.energyConsumptionType.uppercase().firstOrNull()?.toString() ?: return null
        val index = when (letter) {
            "A" -> 0; "B" -> 1; "C" -> 2; "D" -> 3; "E" -> 4; "F" -> 5; "G" -> 6
            else -> return null
        }
        return EnergyUiModel(letter = letter, activeIndex = index)
    }
}