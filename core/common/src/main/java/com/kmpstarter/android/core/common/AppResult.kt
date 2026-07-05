package com.kmpstarter.android.core.common

sealed interface AppResult<out T> {
    data class Success<T>(
        val value: T,
    ) : AppResult<T>

    data class Error(
        val throwable: Throwable,
    ) : AppResult<Nothing>
}
