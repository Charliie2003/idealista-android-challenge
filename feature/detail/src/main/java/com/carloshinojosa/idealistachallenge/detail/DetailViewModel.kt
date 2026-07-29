package com.carloshinojosa.idealistachallenge.detail

import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor() : ViewModel() {

    init {
        Log.d("IdealistaVM", "DetailViewModel: Hilt injection OK")
    }
}
