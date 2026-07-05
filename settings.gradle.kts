pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

includeBuild("build-logic")

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "kmp-starter"

include(":app")
include(":desktopApp")
include(":shared:app")

include(":core:common")
include(":core:model")
include(":core:design-system")
include(":core:network")
include(":core:database")
include(":core:datastore")
include(":core:navigation")
include(":core:testing")

include(":feature:login:domain")
include(":feature:login:data")
include(":feature:login:ui")
include(":feature:home:domain")
include(":feature:home:data")
include(":feature:home:ui")
