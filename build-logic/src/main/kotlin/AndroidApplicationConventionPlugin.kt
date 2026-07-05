package kmpstarter.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import java.util.Properties

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) =
        with(target) {
            pluginManager.apply("com.android.application")
            val releaseKeystore =
                providers
                    .gradleProperty("KEYSTORE_FILE")
                    .orElse(providers.environmentVariable("KEYSTORE_FILE"))
                    .orNull
            val nonProdApiBaseUrl =
                quotedConfigValue(
                    localPropertyOrEnvironment(
                        "NON_PROD_API_BASE_URL",
                        "NONPROD_API_BASE_URL",
                        defaultValue = "https://api.nonprod.example.com/",
                    ),
                )
            val prodApiBaseUrl =
                quotedConfigValue(
                    localPropertyOrEnvironment(
                        "PROD_API_BASE_URL",
                        defaultValue = "https://api.example.com/",
                    ),
                )
            val firebaseWebApiKey =
                quotedConfigValue(
                    localPropertyOrEnvironment(
                        "FIREBASE_WEB_API_KEY",
                        defaultValue = "",
                        ensureTrailingSlash = false,
                    ),
                    ensureTrailingSlash = false,
                )
            val googleOAuthClientId =
                quotedConfigValue(
                    localPropertyOrEnvironment(
                        "GOOGLE_ANDROID_CLIENT_ID",
                        "GOOGLE_OAUTH_CLIENT_ID",
                        defaultValue = "",
                        ensureTrailingSlash = false,
                    ),
                    ensureTrailingSlash = false,
                )
            val googleOAuthRedirectScheme =
                localPropertyOrEnvironment(
                    "GOOGLE_OAUTH_REDIRECT_SCHEME",
                    defaultValue = "com.kmpstarter.android.oauth",
                    ensureTrailingSlash = false,
                )

            extensions.configure<ApplicationExtension> {
                namespace = "com.kmpstarter.android"
                compileSdk =
                    libs
                        .findVersion("compileSdk")
                        .get()
                        .requiredVersion
                        .toInt()
                buildFeatures.buildConfig = true
                buildFeatures.resValues = true

                defaultConfig {
                    applicationId = "com.kmpstarter.android"
                    minSdk =
                        libs
                            .findVersion("minSdk")
                            .get()
                            .requiredVersion
                            .toInt()
                    targetSdk =
                        libs
                            .findVersion("targetSdk")
                            .get()
                            .requiredVersion
                            .toInt()
                    versionCode = 1
                    versionName = "0.1.0"
                    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                    manifestPlaceholders["googleOauthRedirectScheme"] = googleOAuthRedirectScheme
                    buildConfigField("String", "FIREBASE_WEB_API_KEY", firebaseWebApiKey)
                    buildConfigField("String", "GOOGLE_OAUTH_CLIENT_ID", googleOAuthClientId)
                    buildConfigField("String", "GOOGLE_OAUTH_REDIRECT_SCHEME", quotedConfigValue(googleOAuthRedirectScheme, ensureTrailingSlash = false))
                }

                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_21
                    targetCompatibility = JavaVersion.VERSION_21
                }

                flavorDimensions += "environment"
                productFlavors {
                    create("nonProd") {
                        dimension = "environment"
                        applicationIdSuffix = ".nonprod"
                        versionNameSuffix = "-nonprod"
                        resValue("string", "app_name", "KMP Starter NonProd")
                        buildConfigField("String", "ENVIRONMENT", "\"nonProd\"")
                        buildConfigField("String", "API_BASE_URL", nonProdApiBaseUrl)
                    }
                    create("prod") {
                        dimension = "environment"
                        resValue("string", "app_name", "KMP Starter")
                        buildConfigField("String", "ENVIRONMENT", "\"prod\"")
                        buildConfigField("String", "API_BASE_URL", prodApiBaseUrl)
                    }
                }

                signingConfigs {
                    getByName("debug")
                    create("release") {
                        if (releaseKeystore != null) {
                            storeFile = rootProject.file(releaseKeystore)
                            storePassword = secret("KEYSTORE_PASSWORD")
                            keyAlias = secret("KEY_ALIAS")
                            keyPassword = secret("KEY_PASSWORD")
                        }
                    }
                }

                buildTypes {
                    debug {
                        applicationIdSuffix = ".debug"
                        isDebuggable = true
                    }
                    release {
                        isMinifyEnabled = true
                        isShrinkResources = true
                        if (releaseKeystore != null) {
                            signingConfig = signingConfigs.getByName("release")
                        }
                        proguardFiles(
                            getDefaultProguardFile("proguard-android-optimize.txt"),
                            "proguard-rules.pro",
                        )
                    }
                }

                packaging {
                    resources {
                        excludes += "/META-INF/{AL2.0,LGPL2.1}"
                        excludes += "/META-INF/versions/9/OSGI-INF/MANIFEST.MF"
                    }
                }
            }

            extensions.configure<KotlinAndroidProjectExtension> {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_21)
                }
            }

            extensions.configure<ApplicationAndroidComponentsExtension> {
                onVariants { variant ->
                    val versionName = extensions.getByType(ApplicationExtension::class.java).defaultConfig.versionName
                    variant.outputs.forEach { output ->
                        val variantName = variant.flavorName?.replaceFirstChar { it.uppercase() } ?: ""
                        val buildType = variant.buildType?.replaceFirstChar { it.uppercase() } ?: ""
                        output.outputFileName.set("kmp-starter-v$versionName-$variantName$buildType.apk")
                    }
                }
            }
        }

    private fun Project.localPropertyOrEnvironment(
        vararg keys: String,
        defaultValue: String,
        ensureTrailingSlash: Boolean = true,
    ): String {
        val localProperties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.isFile) {
            localPropertiesFile.inputStream().use(localProperties::load)
        }

        val value = keys.firstNotNullOfOrNull { key ->
            providers.gradleProperty(key).orNull
                ?: providers.environmentVariable(key).orNull
                ?: localProperties.getProperty(key)
        } ?: defaultValue

        return if (ensureTrailingSlash) value.ensureTrailingSlash() else value
    }

    private fun quotedConfigValue(
        value: String,
        ensureTrailingSlash: Boolean = true,
    ): String {
        val resolvedValue = if (ensureTrailingSlash) value.ensureTrailingSlash() else value
        return "\"${resolvedValue.trim().replace("\\", "\\\\").replace("\"", "\\\"")}\""
    }

    private fun String.ensureTrailingSlash(): String = if (endsWith("/")) this else "$this/"
}
