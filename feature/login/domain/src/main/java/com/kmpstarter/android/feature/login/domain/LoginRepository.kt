package com.kmpstarter.android.feature.login.domain

import com.kmpstarter.android.core.model.User
import kotlinx.coroutines.flow.Flow

interface LoginRepository {
    fun observeCurrentUser(): Flow<User?>

    suspend fun refreshCurrentUser(): RefreshResult

    suspend fun logout()
}

sealed interface RefreshResult {
    data object Success : RefreshResult

    data class Failed(
        val throwable: Throwable,
    ) : RefreshResult
}
