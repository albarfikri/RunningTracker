package com.albar.runningtracker.ui.fragment

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.albar.runningtracker.R
import com.albar.runningtracker.ui.viewmodels.StatisticsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StatisticsFragment: Fragment(R.layout.fragment_statistics) {
    private val viewModel: StatisticsViewModel by viewModels()
}