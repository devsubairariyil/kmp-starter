package com.kmpstarter.android.feature.home.domain

import com.kmpstarter.android.core.model.User
import kotlinx.coroutines.flow.Flow

interface HomeRepository {
    fun observeCurrentUser(): Flow<User?>
}
