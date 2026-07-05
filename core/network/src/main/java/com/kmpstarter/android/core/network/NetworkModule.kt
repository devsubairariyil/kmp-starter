package com.kmpstarter.android.core.network

import com.kmpstarter.android.core.common.DefaultDispatcher
import com.kmpstarter.android.core.common.IoDispatcher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import timber.log.Timber
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun okHttpClient(): OkHttpClient =
        OkHttpClient
            .Builder()
            .addInterceptor(
                HttpLoggingInterceptor { message ->
                    Timber.tag("OkHttp").d(message)
                }.apply {
                    level = HttpLoggingInterceptor.Level.BASIC
                },
            ).build()

    @Provides
    @Singleton
    fun retrofit(
        client: OkHttpClient,
        environment: NetworkEnvironment,
    ): Retrofit =
        Retrofit
            .Builder()
            .baseUrl(environment.baseUrl.ensureTrailingSlash())
            .client(client)
            .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
            .build()

    @Provides
    @IoDispatcher
    fun ioDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @DefaultDispatcher
    fun defaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    private fun String.ensureTrailingSlash(): String = if (endsWith("/")) this else "$this/"
}
