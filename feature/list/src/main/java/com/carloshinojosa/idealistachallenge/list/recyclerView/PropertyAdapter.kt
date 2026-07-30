package com.carloshinojosa.idealistachallenge.list.recyclerView

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.carloshinojosa.idealistachallenge.list.databinding.ItemPropertyBinding
import com.carloshinojosa.idealistachallenge.list.presentation.model.PropertyCardUiModel

internal class PropertyAdapter(
    private val onItemClick: (propertyId: String) -> Unit,
    private val onFavoriteClick: (propertyId: String) -> Unit,
) : ListAdapter<PropertyCardUiModel, PropertyViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PropertyViewHolder {
        val binding = ItemPropertyBinding.inflate(
            LayoutInflater.from(parent.context), parent, false,
        )
        return PropertyViewHolder(binding, onItemClick, onFavoriteClick)
    }

    override fun onBindViewHolder(holder: PropertyViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private companion object {
        val DiffCallback = object : DiffUtil.ItemCallback<PropertyCardUiModel>() {
            override fun areItemsTheSame(a: PropertyCardUiModel, b: PropertyCardUiModel) =
                a.id == b.id

            override fun areContentsTheSame(a: PropertyCardUiModel, b: PropertyCardUiModel) =
                a == b
        }
    }
}
