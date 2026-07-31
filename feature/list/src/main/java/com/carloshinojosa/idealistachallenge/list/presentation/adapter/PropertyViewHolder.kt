package com.carloshinojosa.idealistachallenge.list.presentation.adapter

import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StyleSpan
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.carloshinojosa.idealistachallenge.list.R
import com.carloshinojosa.idealistachallenge.list.databinding.ItemPropertyBinding
import com.carloshinojosa.idealistachallenge.list.presentation.model.PropertyCardUiModel

internal class PropertyViewHolder(
    private val binding: ItemPropertyBinding,
    private val onItemClick: (propertyId: String) -> Unit,
    private val onFavoriteClick: (propertyId: String) -> Unit,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(model: PropertyCardUiModel) {
        binding.root.setOnClickListener { onItemClick(model.id) }

        binding.propertyImage.load(model.thumbnailUrl) {
            placeholder(R.drawable.placeholder_property)
            error(R.drawable.placeholder_property)
        }

        binding.newBadge.visibility = if (model.isNew) View.VISIBLE else View.GONE
        binding.operationBadge.text = model.operationLabel

        binding.favIcon.isActivated = model.isFavorite
        binding.favTarget.contentDescription = binding.root.context.getString(
            if (model.isFavorite) R.string.cd_fav_saved else R.string.cd_fav_save,
        )
        // Stop propagation: favorite click does NOT trigger the card's item click.
        binding.favTarget.setOnClickListener { onFavoriteClick(model.id) }

        binding.priceAmount.text = model.priceAmountText
        binding.priceSuffix.text = model.priceSuffixText

        binding.whereText.text = buildLocationText(model.neighborhood, model.district)

        binding.bedroomsText.text = model.rooms.toString()
        binding.bathroomsText.text = model.bathrooms.toString()
        binding.areaText.text = model.sizeLabel

        val showSavedPill = model.isFavorite && model.favoritedDateLabel != null
        binding.savedPill.visibility = if (showSavedPill) View.VISIBLE else View.GONE
        if (showSavedPill) {
            binding.savedDateText.text = model.favoritedDateLabel
        }
    }

    private fun buildLocationText(neighborhood: String, district: String): CharSequence {
        if (neighborhood.isEmpty()) return district
        val sb = SpannableStringBuilder(neighborhood)
        sb.setSpan(StyleSpan(Typeface.BOLD), 0, neighborhood.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        if (district.isNotEmpty()) sb.append(", $district")
        return sb
    }
}
