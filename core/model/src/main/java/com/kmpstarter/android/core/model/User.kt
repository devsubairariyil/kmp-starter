package com.kmpstarter.android.core.model

data class User(
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phoneNumber: String?,
    val photoUrl: String?,
    val isHost: Boolean,
)
