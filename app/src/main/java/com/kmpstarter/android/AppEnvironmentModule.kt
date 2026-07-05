package com.kmpstarter.android

import com.kmpstarter.android.core.network.NetworkEnvironment
import com.kmpstarter.android.core.network.ProductEnvironment
import com.kmpstarter.shared.app.KmpStarterEnvironment
import com.kmpstarter.shared.app.KmpStarterRuntimeConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppEnvironmentModule {
    @Provides
    @Singleton
    fun networkEnvironment(): NetworkEnvironment =
        BuildConfigNetworkEnvironment(
            environment = BuildConfig.ENVIRONMENT,
            apiBaseUrl = BuildConfig.API_BASE_URL,
        )
}

private class BuildConfigNetworkEnvironment(
    environment: String,
    apiBaseUrl: String,
) : NetworkEnvironment {
    private val runtimeConfig =
        KmpStarterRuntimeConfig.from(
            environmentName = environment,
            apiBaseUrl = apiBaseUrl,
        )

    override val productEnvironment: ProductEnvironment =
        when (runtimeConfig.environment) {
            KmpStarterEnvironment.Prod -> ProductEnvironment.Prod
            else -> ProductEnvironment.NonProd
        }

    override val baseUrl: String = runtimeConfig.apiBaseUrl
}
