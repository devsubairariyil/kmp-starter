package com.kmpstarter.android.feature.home.data

import com.kmpstarter.android.core.database.UserDao
import com.kmpstarter.android.core.model.User
import com.kmpstarter.android.feature.home.domain.HomeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class HomeRepositoryImpl
@Inject
constructor(
    private val userDao: UserDao,
) : HomeRepository {
    override fun observeCurrentUser(): Flow<User?> = userDao.observeCurrentUser().map { it?.asExternalModel() }
}
