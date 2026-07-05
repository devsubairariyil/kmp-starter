# KMP Starter Agent Guide

This file is the operating contract for AI agents and automation in this repository. Follow it before making code changes.

## Product Direction

KMP Starter is a Kotlin Multiplatform starter project with Android and iOS applications. Product UI and product runtime behavior must be shared by default.

- Android app: `app`
- iOS app wrapper: `iosApp`
- Desktop validation app: `desktopApp`
- Shared product surface: `shared/app`
- Android package: `com.kmpstarter.android`
- Shared runtime config: `KmpStarterRuntimeConfig`

## Hard Rules

- Build product UI in shared Compose Multiplatform first.
- New reusable composables must live in `shared/app/src/commonMain` or another shared KMP UI module and compile for Android, iOS, and desktop.
- Do not put Android-only APIs in shared `commonMain`: no Hilt, Room, WorkManager, AndroidX lifecycle/navigation, Android resources, `Context`, Retrofit JVM APIs, or generated OpenAPI classes.
- Use platform source sets or thin platform adapters for APIs that cannot be shared.
- Environment IDs and default API URLs belong in shared KMP runtime config, not independently in Android or iOS.
- Android flavors and iOS build settings may only adapt platform values into shared config.
- Keep platform launchers thin. They should launch shared UI and pass platform config/services inward.
- Room remains the offline source of truth while persistence is Android-only. UI observes local database-backed flows, not raw remote responses.
- Do not hardcode secrets, API tokens, keystore paths, signing passwords, or credentials.
- Do not edit generated OpenAPI clients. Change the spec or generator configuration instead.
- Use Timber for Android logs. Never log secrets, tokens, personal data, request bodies, or payment data.
- Prefer small, focused changes that preserve module boundaries.

## Dependency Rules

Keep dependencies flowing inward:

- `ui` depends on `domain`
- `data` depends on `domain`
- `domain` does not depend on Android UI, Room, Retrofit, Hilt, generated API classes, or platform launchers
- `core` modules do not depend on feature modules
- `shared commonMain` does not depend on platform-only libraries
- `app`, `iosApp`, and `desktopApp` depend on shared code; shared code does not depend on launchers

## Where Code Belongs

- Shared product UI, shared screen state, UI events: `shared/app/src/commonMain`
- Shared runtime environment config: `shared/app/src/commonMain`
- iOS `ComposeUIViewController` entry point: `shared/app/src/iosMain`
- Android launcher and app-level Hilt bindings: `app`
- iOS native wrapper, plist/build settings, Swift launcher: `iosApp`
- Desktop launcher: `desktopApp`
- Android-only route glue and Hilt ViewModels during migration: `feature/<name>/ui`
- Use cases and repository interfaces: `feature/<name>/domain`
- Repository implementations, data sources, mappers, workers: `feature/<name>/data`
- Shared models used across features: `core/model`
- Android design system code waiting for KMP migration: `core/design-system`
- Retrofit, OkHttp, network contracts, generated OpenAPI clients: `core/network`
- Room database, DAOs, entities: `core/database`
- Preferences and lightweight persistent app settings: `core/datastore`
- Test fakes, coroutine helpers, builders: `core/testing`

## AI Code Generation Standard

When generating code, choose the most scalable KMP placement by default:

- If it renders product UI, generate it as a shared composable.
- If it represents user-visible state, generate it as platform-neutral Kotlin.
- If it selects environment or backend behavior, add it to shared runtime config first.
- If platform-specific behavior is required, define a small interface or `expect`/`actual` boundary and keep platform code narrow.
- Do not duplicate screens separately in Android and iOS.
- Do not create one-off platform configuration values that drift from shared KMP config.
- Do not add abstractions unless they reduce real duplication or preserve a cross-platform boundary.

## Verification

Run the narrowest relevant checks first, then broader checks when possible:

```bash
./gradlew spotlessCheck
./gradlew detekt ktlintCheck
./gradlew test
./gradlew lint
./gradlew :app:assembleNonProdDebug
./gradlew :shared:app:linkDebugFrameworkIosSimulatorArm64
```

Run OpenAPI generation when the spec or generator configuration changes:

```bash
./gradlew :core:network:openApiGenerate
```

For iOS wrapper changes, also verify:

```bash
env DEVELOPER_DIR=/Applications/Xcode.app/Contents/Developer xcodebuild -project iosApp/KmpStarter.xcodeproj -scheme KmpStarter -sdk iphonesimulator -configuration Debug build
```
