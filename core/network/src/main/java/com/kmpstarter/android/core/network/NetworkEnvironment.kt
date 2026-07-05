package com.kmpstarter.android.core.network

interface NetworkEnvironment {
    val productEnvironment: ProductEnvironment
    val baseUrl: String
}

enum class ProductEnvironment {
    NonProd,
    Prod,
}
