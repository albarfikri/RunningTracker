package com.albar.runningtracker.ui.fragment

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.albar.runningtracker.R
import com.albar.runningtracker.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RunFragment: Fragment(R.layout.fragment_run) {
    private val viewModel: MainViewModel by viewModels()
}