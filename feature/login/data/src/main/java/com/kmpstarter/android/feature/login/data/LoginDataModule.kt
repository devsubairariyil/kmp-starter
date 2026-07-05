package com.kmpstarter.android.feature.login.data

import com.kmpstarter.android.feature.login.domain.LoginRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LoginDataModule {
    @Binds
    @Singleton
    abstract fun bindLoginRepository(repository: OfflineFirstLoginRepository): LoginRepository
}
