package com.carloshinojosa.idealistachallenge.list

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.carloshinojosa.idealistachallenge.list.databinding.FragmentListingBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ListingFragment : Fragment() {

    private var _binding: FragmentListingBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ListingViewModel by viewModels()
    private var navigator: ListingNavigator? = null

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDetach() {
        super.onDetach()
        navigator = null
    }
}
