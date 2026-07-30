package com.carloshinojosa.idealistachallenge.list

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.PathInterpolator
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.carloshinojosa.idealistachallenge.core.domain.util.UiText
import com.carloshinojosa.idealistachallenge.list.databinding.FragmentListingBinding
import com.carloshinojosa.idealistachallenge.list.presentation.model.FilterType
import com.carloshinojosa.idealistachallenge.list.presentation.model.ListingUiState
import com.carloshinojosa.idealistachallenge.list.recyclerView.PropertyAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ListingFragment : Fragment() {

    private var _binding: FragmentListingBinding? = null
    private val binding get() = requireNotNull(_binding) { "binding accessed outside of view lifetime" }
    private val viewModel: ListingViewModel by viewModels()
    private var navigator: ListingNavigator? = null

    private lateinit var adapter: PropertyAdapter
    private var tabWidth = 0f

    override fun onAttach(context: Context) {
        super.onAttach(context)
        navigator = context as? ListingNavigator
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentListingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObservers()
        initListeners()
        syncInitialVisibility()
        initUI()
    }

    private fun initObservers() {
        viewModel.state.observe(viewLifecycleOwner) { state -> renderState(state) }

        viewModel.filter.observe(viewLifecycleOwner) { filter ->
            updateChipLabel(filter)
            updateTabSelection(filter)
            binding.propertyList.scrollToPosition(0)
        }

        viewModel.favoritesCount.observe(viewLifecycleOwner) { count ->
            val label = buildString {
                append(getString(R.string.listing_filter_favorites))
                if (count > 0) append(" $count")
            }
            binding.tabFavorites.text = label
        }
    }

    private fun initListeners() {
        binding.retryButton.setOnClickListener { viewModel.onRetryClicked() }
        binding.filterChip.setOnClickListener { onFilterChipClicked() }
    }

    private fun initUI() {
        setupRecyclerView()
        setupSegmentedControl()
    }

    private fun setupRecyclerView() {
        adapter = PropertyAdapter(
            onItemClick = { id -> navigator?.navigateToDetail(id) },
            onFavoriteClick = { id -> viewModel.onFavoriteClicked(id) },
        )
        binding.propertyList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = this@ListingFragment.adapter
            addItemDecoration(
                VerticalSpaceDecoration(
                    resources.getDimensionPixelSize(R.dimen.space_18),
                ),
            )
        }
    }

    private fun setupSegmentedControl() {
        binding.segmentedContainer.doOnLayout { container ->
            val innerWidth = container.width - container.paddingStart - container.paddingEnd
            tabWidth = innerWidth / 3f
            binding.segmentIndicator.updateLayoutParams { width = tabWidth.toInt() }
            val currentIndex = (viewModel.filter.value ?: FilterType.SALE).ordinal
            binding.segmentIndicator.translationX = currentIndex * tabWidth
        }
        binding.tabSale.setOnClickListener { viewModel.onFilterChanged(FilterType.SALE) }
        binding.tabRent.setOnClickListener { viewModel.onFilterChanged(FilterType.RENT) }
        binding.tabFavorites.setOnClickListener { viewModel.onFilterChanged(FilterType.FAVORITES) }
    }


    private fun renderState(state: ListingUiState) {
        when (state) {
            ListingUiState.Loading -> {
                binding.stateFlipper.visibility = View.VISIBLE
                binding.stateFlipper.displayedChild = 0
                binding.shimmerLayout.startShimmer()
                binding.propertyList.visibility = View.GONE
                binding.metaRow.visibility = View.GONE
            }

            is ListingUiState.Content -> {
                binding.shimmerLayout.stopShimmer()
                binding.stateFlipper.visibility = View.GONE
                binding.propertyList.visibility = View.VISIBLE
                binding.metaRow.visibility = View.VISIBLE
                adapter.submitList(state.items)
                updateCountText(state.items.size)
            }

            ListingUiState.Empty -> {
                binding.shimmerLayout.stopShimmer()
                binding.stateFlipper.visibility = View.VISIBLE
                binding.stateFlipper.displayedChild = 2
                binding.propertyList.visibility = View.GONE
                binding.metaRow.visibility = View.GONE
            }

            is ListingUiState.Error -> {
                binding.shimmerLayout.stopShimmer()
                binding.stateFlipper.visibility = View.VISIBLE
                binding.stateFlipper.displayedChild = 1
                binding.propertyList.visibility = View.GONE
                binding.metaRow.visibility = View.GONE
                binding.errorMessage.text = state.message.resolve()
            }
        }
    }

    private fun updateTabSelection(filter: FilterType) {
        if (filter == FilterType.ALL) return
        val selectedIndex = filter.ordinal
        listOf(binding.tabSale, binding.tabRent, binding.tabFavorites)
            .forEachIndexed { index, button ->
                val isSelected = index == selectedIndex
                button.isSelected = isSelected
                button.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        if (isSelected) R.color.surface else R.color.on_surface_variant,
                    ),
                )
            }
        moveIndicatorTo(selectedIndex)
    }

    private fun moveIndicatorTo(index: Int) {
        val targetX = index * tabWidth
        if (tabWidth == 0f) {
            binding.segmentIndicator.translationX = targetX
            return
        }
        ObjectAnimator.ofFloat(binding.segmentIndicator, "translationX", targetX).apply {
            duration = 280L
            interpolator = PathInterpolator(0.34f, 1.2f, 0.4f, 1.0f)
            start()
        }
    }

    private fun onFilterChipClicked() {
        if (viewModel.filter.value == FilterType.ALL) {
            binding.segmentIndicator.translationX = 0f
            showSegmentedControl()
            viewModel.onFilterChanged(FilterType.SALE)
        } else {
            hideSegmentedControl()
            viewModel.onFilterChanged(FilterType.ALL)
        }
    }

    private fun showSegmentedControl() {
        binding.segmentedContainer.alpha = 0f
        binding.segmentedContainer.visibility = View.VISIBLE
        binding.segmentedContainer.animate()
            .alpha(1f)
            .setDuration(280L)
            .setInterpolator(PathInterpolator(0.34f, 1.2f, 0.4f, 1.0f))
            .start()
    }

    private fun hideSegmentedControl() {
        binding.segmentedContainer.animate()
            .alpha(0f)
            .setDuration(280L)
            .setInterpolator(PathInterpolator(0.34f, 1.2f, 0.4f, 1.0f))
            .withEndAction {
                binding.segmentedContainer.visibility = View.GONE
                binding.segmentedContainer.alpha = 1f
            }
            .start()
    }

    private fun updateChipLabel(filter: FilterType) {
        binding.filterChip.text = getString(
            if (filter == FilterType.ALL) R.string.listing_chip_filter
            else R.string.listing_chip_see_all,
        )
    }

    private fun syncInitialVisibility() {
        if (viewModel.filter.value == FilterType.ALL) {
            binding.segmentedContainer.visibility = View.GONE
        }
    }

    private fun updateCountText(count: Int) {
        val filter = viewModel.filter.value ?: FilterType.SALE
        binding.countText.text = when (filter) {
            FilterType.SALE -> getString(R.string.listing_count_sale, count)
            FilterType.RENT -> getString(R.string.listing_count_rent, count)
            FilterType.FAVORITES -> if (count == 1) {
                getString(R.string.listing_count_favorites_one, count)
            } else {
                getString(R.string.listing_count_favorites_other, count)
            }
            FilterType.ALL -> getString(R.string.listing_count_all, count)
        }
    }

    private fun UiText.resolve(): String = when (this) {
        is UiText.DynamicString -> value
        is UiText.StringResource -> requireContext().getString(resId, *args)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDetach() {
        super.onDetach()
        navigator = null
    }

    private class VerticalSpaceDecoration(private val space: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State,
        ) {
            if (parent.getChildAdapterPosition(view) != 0) {
                outRect.top = space
            }
        }
    }
}
