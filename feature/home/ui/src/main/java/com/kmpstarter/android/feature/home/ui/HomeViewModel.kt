package com.kmpstarter.android.feature.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kmpstarter.android.feature.home.domain.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HomeViewModel
@Inject
constructor(
    repository: HomeRepository,
) : ViewModel() {
    val uiState: StateFlow<HomeUiState> =
        repository
            .observeCurrentUser()
            .map { user -> HomeUiState(title = "Welcome, ${user?.firstName ?: "guest"}") }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())
}

data class HomeUiState(
    val title: String = "Welcome",
)
