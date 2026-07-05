plugins {
    id("kmpstarter.android.library")
    id("kmpstarter.android.hilt")
    id("kmpstarter.android.test")
    id("org.openapi.generator")
}

android {
    namespace = "com.kmpstarter.android.core.network"
}

androidComponents {
    onVariants {
        it.sources.kotlin?.addStaticSourceDirectory(
            layout.buildDirectory
                .dir("generated/openapi/src/main/kotlin")
                .get()
                .asFile
                .absolutePath,
        )
    }
}

openApiGenerate {
    generatorName.set("kotlin")
    inputSpec.set(
        rootProject.layout.projectDirectory
            .file("kmp-starter.openapi.yaml")
            .asFile
            .absolutePath,
    )
    outputDir.set(
        layout.buildDirectory
            .dir("generated/openapi")
            .get()
            .asFile
            .absolutePath,
    )
    apiPackage.set("com.kmpstarter.android.core.network.generated.api")
    modelPackage.set("com.kmpstarter.android.core.network.generated.model")
    invokerPackage.set("com.kmpstarter.android.core.network.generated")
    configOptions.set(
        mapOf(
            "dateLibrary" to "java8",
            "library" to "jvm-retrofit2",
            "serializationLibrary" to "kotlinx_serialization",
            "useCoroutines" to "true",
        ),
    )
}

val openApiGenerateTask = tasks.named("openApiGenerate")

tasks.named("preBuild").configure {
    dependsOn(openApiGenerateTask)
}

tasks
    .matching {
        it.name.startsWith("ksp") || (it.name.startsWith("compile") && it.name.endsWith("Kotlin"))
    }.configureEach {
        dependsOn(openApiGenerateTask)
    }

dependencies {
    implementation(project(":core:common"))
    implementation(libs.coroutines.core)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.kotlinx)
    implementation(libs.retrofit.converter.scalars)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.timber)
}
