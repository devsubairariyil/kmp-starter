package com.kmpstarter.android.feature.login.data

import com.kmpstarter.android.core.database.UserEntity
import javax.inject.Inject

class LoginRemoteDataSource
@Inject
constructor() {
    suspend fun fetchCurrentUser(): UserEntity =
        UserEntity(
            id = "offline-seed",
            firstName = "Starter",
            lastName = "Guest",
            email = "guest@example.com",
            phoneNumber = null,
            photoUrl = null,
            isHost = false,
            updatedAtEpochMillis = System.currentTimeMillis(),
        )
}
