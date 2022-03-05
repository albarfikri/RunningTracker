package com.albar.runningtracker.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.albar.runningtracker.repositories.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    val mainRepository: MainRepository
) : ViewModel() {
}