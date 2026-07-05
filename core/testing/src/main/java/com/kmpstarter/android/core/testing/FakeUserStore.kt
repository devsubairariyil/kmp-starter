package com.kmpstarter.android.core.testing

import com.kmpstarter.android.core.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeUserStore(
    initialUser: User? = null,
) {
    private val _user = MutableStateFlow(initialUser)
    val user: StateFlow<User?> = _user

    fun setUser(user: User?) {
        _user.value = user
    }
}
