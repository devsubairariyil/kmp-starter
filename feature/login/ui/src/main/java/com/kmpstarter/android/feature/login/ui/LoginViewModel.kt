package com.kmpstarter.android.feature.login.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kmpstarter.android.feature.login.domain.ObserveCurrentUserUseCase
import com.kmpstarter.android.feature.login.domain.RefreshCurrentUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel
@Inject
constructor(
    observeCurrentUser: ObserveCurrentUserUseCase,
    private val refreshCurrentUser: RefreshCurrentUserUseCase,
) : ViewModel() {
    val uiState: StateFlow<LoginUiState> =
        observeCurrentUser()
            .map { user -> LoginUiState(isAuthenticated = user != null, displayName = user?.firstName) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LoginUiState())

    fun continueAsGuest() {
        viewModelScope.launch {
            refreshCurrentUser()
        }
    }
}

data class LoginUiState(
    val isAuthenticated: Boolean = false,
    val displayName: String? = null,
)
