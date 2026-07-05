package com.kmpstarter.android.feature.login.domain

import javax.inject.Inject

class RefreshCurrentUserUseCase
@Inject
constructor(
    private val repository: LoginRepository,
) {
    suspend operator fun invoke(): RefreshResult = repository.refreshCurrentUser()
}
