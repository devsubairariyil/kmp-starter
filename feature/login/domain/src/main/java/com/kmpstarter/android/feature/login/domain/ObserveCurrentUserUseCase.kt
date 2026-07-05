package com.kmpstarter.android.feature.login.domain

import javax.inject.Inject

class ObserveCurrentUserUseCase
@Inject
constructor(
    private val repository: LoginRepository,
) {
    operator fun invoke() = repository.observeCurrentUser()
}
