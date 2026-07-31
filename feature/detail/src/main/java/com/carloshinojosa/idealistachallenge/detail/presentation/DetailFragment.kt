package com.carloshinojosa.idealistachallenge.detail.presentation

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import com.carloshinojosa.idealistachallenge.design.ui.theme.IdealistaTheme
import com.carloshinojosa.idealistachallenge.detail.R
import com.carloshinojosa.idealistachallenge.detail.databinding.FragmentDetailBinding
import com.carloshinojosa.idealistachallenge.detail.presentation.DetailUiState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DetailFragment : Fragment() {

    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DetailViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        binding.composeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                IdealistaTheme {
                    val state by viewModel.state.collectAsStateWithLifecycle()
                    DetailScreen(
                        state = state,
                        onFavoriteToggle = viewModel::onFavoriteToggle,
                        onRetry = viewModel::onRetry,
                        onBackClick = { findNavController().navigateUp() },
                        onShareClick = { shareProperty(state) },
                    )
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun shareProperty(state: DetailUiState) {
        val content = state as? DetailUiState.Content ?: return
        val p = content.property
        val text = "${p.title}\n${p.operationLabel} · ${p.priceLabel} ${p.priceSuffix}\n${p.neighborhood}, ${p.district}, ${p.municipality}"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            putExtra(Intent.EXTRA_SUBJECT, p.title)
        }
        startActivity(Intent.createChooser(intent, getString(R.string.cd_share)))
    }
}
