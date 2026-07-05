package com.kmpstarter.android.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kmpstarter.android.core.model.User

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phoneNumber: String?,
    val photoUrl: String?,
    val isHost: Boolean,
    val updatedAtEpochMillis: Long,
) {
    fun asExternalModel(): User =
        User(
            id = id,
            firstName = firstName,
            lastName = lastName,
            email = email,
            phoneNumber = phoneNumber,
            photoUrl = photoUrl,
            isHost = isHost,
        )
}
