package com.kmpstarter.android.core.testing

import com.kmpstarter.android.core.model.User

object TestUsers {
    fun user(
        id: String = "user-test",
        firstName: String = "Test",
        lastName: String = "User",
        email: String = "test@example.com",
    ) = User(
        id = id,
        firstName = firstName,
        lastName = lastName,
        email = email,
        phoneNumber = null,
        photoUrl = null,
        isHost = false,
    )
}
