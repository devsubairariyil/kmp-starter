package com.kmpstarter.android.feature.home.data

import com.kmpstarter.android.feature.home.domain.HomeRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class HomeDataModule {
    @Binds
    @Singleton
    abstract fun bindHomeRepository(repository: HomeRepositoryImpl): HomeRepository
}
