package com.carloshinojosa.idealistachallenge.list

import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ListingViewModel @Inject constructor() : ViewModel() {

    init {
        Log.d("IdealistaVM", "ListingViewModel: Hilt injection OK")
    }
}
