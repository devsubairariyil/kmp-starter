package com.kmpstarter.android.feature.login.data

import com.kmpstarter.android.core.common.IoDispatcher
import com.kmpstarter.android.core.database.UserDao
import com.kmpstarter.android.core.model.User
import com.kmpstarter.android.feature.login.domain.LoginRepository
import com.kmpstarter.android.feature.login.domain.RefreshResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class OfflineFirstLoginRepository
@Inject
constructor(
    private val userDao: UserDao,
    private val remoteDataSource: LoginRemoteDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : LoginRepository {
    override fun observeCurrentUser(): Flow<User?> = userDao.observeCurrentUser().map { it?.asExternalModel() }

    override suspend fun refreshCurrentUser(): RefreshResult =
        withContext(ioDispatcher) {
            runCatching {
                val remoteUser = remoteDataSource.fetchCurrentUser()
                userDao.upsert(remoteUser)
            }.fold(
                onSuccess = { RefreshResult.Success },
                onFailure = { RefreshResult.Failed(it) },
            )
        }

    override suspend fun logout() {
        userDao.clear()
    }
}
