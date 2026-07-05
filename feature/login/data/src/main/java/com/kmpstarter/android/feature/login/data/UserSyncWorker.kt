package com.kmpstarter.android.feature.login.data

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kmpstarter.android.feature.login.domain.LoginRepository
import com.kmpstarter.android.feature.login.domain.RefreshResult
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class UserSyncWorker
@AssistedInject
constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val repository: LoginRepository,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result =
        when (repository.refreshCurrentUser()) {
            RefreshResult.Success -> Result.success()
            is RefreshResult.Failed -> Result.retry()
        }
}
