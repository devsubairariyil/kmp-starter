# KMP Starter Product Engineering Guide

KMP Starter is a starter project with Android and iOS applications backed by a shared Kotlin Multiplatform app layer. This guide defines where product behavior belongs, how code should scale, and how future AI-generated changes must preserve KMP boundaries.

## Product Principles

- Build product behavior once in shared KMP whenever possible.
- Treat Android and iOS as delivery surfaces, not separate product implementations.
- Keep platform launchers thin and replaceable.
- Keep domain logic independent from UI frameworks, persistence engines, generated clients, and platform lifecycles.
- Keep offline behavior predictable. Room is the Android source of truth until persistence is migrated further into KMP.
- Make environment selection explicit, injectable, and shared.
- Prefer readable, boring code over premature abstractions.

## Product Surfaces

KMP Starter includes through:

- Android app in `app`
- iOS app wrapper in `iosApp`
- Desktop app in `desktopApp` for shared UI validation

The product-facing app UI starts in `shared:app`. Desktop exists to exercise shared Compose code quickly; it is not a separate product direction.

## Module Ownership

### `shared:app`

Owns shared product surface:

- Compose Multiplatform app root
- shared screens and components
- shared UI state and UI events
- runtime environment model
- default environment URLs
- iOS `ComposeUIViewController` entry point

`commonMain` must remain platform-neutral. It cannot import Android framework APIs, Hilt, Room, WorkManager, AndroidX lifecycle/navigation, Android resources, Retrofit JVM APIs, or generated OpenAPI classes.

Use platform source sets only for small adapters, such as `iosMain/MainViewController.kt`.

### `app`

Owns Android launch and Android-specific integration:

- `Application`
- root `Activity`
- Android Hilt graph entry points
- Android flavor/build config adaptation
- Android-only services that have not moved behind shared boundaries

The app module passes `BuildConfig.ENVIRONMENT` and `BuildConfig.API_BASE_URL` into `KmpStarterRuntimeConfig`.

### `iosApp`

Owns the native iOS wrapper:

- Xcode project
- `Info.plist`
- SwiftUI app launcher
- iOS build settings
- framework search/link settings

The wrapper reads `KMP_STARTER_ENVIRONMENT` and `KMP_STARTER_API_BASE_URL` from `Info.plist` and passes them into shared Kotlin.

### `desktopApp`

Owns only the desktop launcher. It renders the same shared app root as Android and iOS.

### `core:*`

Core modules provide reusable foundations. They must not depend on features or platform launchers.

- `core:common`: result types, dispatcher qualifiers, small Kotlin utilities
- `core:model`: stable cross-feature models
- `core:design-system`: Android design-system code; migrate reusable UI tokens/components into shared Compose before cross-platform reuse
- `core:network`: Retrofit, OkHttp, network contracts, generated OpenAPI clients
- `core:database`: Room database, DAOs, entities
- `core:datastore`: preferences and lightweight app settings
- `core:navigation`: route contracts
- `core:testing`: fakes, builders, coroutine test utilities

### `feature:<name>:domain`

Owns business language:

- use cases
- repository interfaces
- domain-specific models
- validation and business rules

Domain should be plain Kotlin. It must not expose Retrofit DTOs, Room entities, Compose types, Android resources, Hilt modules, or generated OpenAPI classes.

### `feature:<name>:data`

Owns implementations:

- repositories
- local and remote data sources
- mappers
- sync workers
- wrappers around generated OpenAPI clients

Data modules may know about Room, network clients, and generated API wrappers. They hide those details behind domain interfaces.

### `feature:<name>:ui`

Currently owns Android presentation glue during migration:

- Android route glue
- Hilt ViewModels
- Android navigation entry points

New reusable screen UI should not start here. Build shared screen composables and state in `shared:app`, then keep Android route glue thin.

## Dependency Rules

Allowed direction:

```text
app -> shared:app
app -> feature:*:ui
app -> feature:*:data
app -> core:*

iosApp -> shared framework
desktopApp -> shared:app

feature:*:ui -> feature:*:domain
feature:*:data -> feature:*:domain
feature:*:domain -> core:model/core:common

feature:*:data -> core:database/core:network/core:datastore/core:model/core:common
```

Disallowed:

- `shared commonMain -> Android-only libraries`
- `shared -> app`
- `shared -> iosApp`
- `domain -> data`
- `domain -> ui`
- `domain -> app`
- `core -> feature`
- `feature A -> feature B` unless a shared contract is moved into `core`

## KMP UI Standard

Compose UI is shared by default.

Shared composables must:

- compile from `commonMain`
- receive state and lambdas rather than platform services
- avoid platform lifecycles and `Context`
- keep layout, typography, and interactions consistent across Android, iOS, and desktop
- be stateless where practical

Use this structure for product screens:

```text
Shared state model -> Shared screen composable -> Thin platform route/entry point
```

Only keep UI platform-specific when:

- it uses a platform-only control that has no shared equivalent
- it handles platform permissions or system UI
- it integrates with native navigation while shared navigation is not available

When platform-specific UI is unavoidable, isolate it behind a small adapter and keep the product state model shared.

## AI-Generated Code Standard

AI-generated code must preserve the KMP architecture.

Before generating or editing code, classify the change:

- Product UI: shared Compose in `shared/app/src/commonMain`
- Product state/event: shared Kotlin in `shared/app/src/commonMain` or a future shared domain module
- Environment behavior: shared runtime config first
- Android lifecycle/DI/persistence: Android platform modules only
- iOS launcher/build settings: `iosApp` only
- Generated OpenAPI client: generator/spec only, never manual edits

AI-generated code must not:

- duplicate Android and iOS screens
- create platform-specific environment names outside shared config
- put Android APIs into shared `commonMain`
- make shared code depend on app launchers
- bypass offline-first data flow
- hardcode URLs, credentials, or signing paths
- add broad abstractions without a clear cross-platform need

## Runtime Environment

Shared KMP owns:

- environment IDs
- display names
- default API URLs
- `KmpStarterRuntimeConfig`

Current environments:

```text
nonProd -> https://api.nonprod.example.com/
prod    -> https://api.example.com/
```

Android adapts Gradle flavors:

- `nonProd` flavor passes `BuildConfig.ENVIRONMENT=nonProd`
- `prod` flavor passes `BuildConfig.ENVIRONMENT=prod`

iOS adapts Xcode build settings:

- `Debug` passes `KMP_STARTER_ENVIRONMENT=nonProd`
- `Release` passes `KMP_STARTER_ENVIRONMENT=prod`

Add new environments in shared KMP first, then bind Android and iOS build settings to the shared IDs.

## Offline-First Standard

KMP Starter includes an offline-first sample.

- Room is the Android source of truth for persistent screen data.
- UI observes repository `Flow` streams backed by local storage.
- Remote refresh writes into local storage before UI updates.
- WorkManager handles retryable background sync.
- DataStore stores sync cursors, lightweight preferences, and auth-adjacent state.
- Conflict resolution belongs in data modules, not UI.

Preferred read flow:

```text
Composable -> ViewModel -> UseCase -> Repository -> Room Flow -> UI state
```

Preferred refresh flow:

```text
ViewModel/Worker -> UseCase -> Repository -> Remote data source -> Mapper -> Room -> observed Flow
```

## Network And OpenAPI

- `core:network` owns generated OpenAPI clients.
- Generated sources live under `core/network/build/generated/openapi`.
- Run generation with `./gradlew :core:network:openApiGenerate`.
- Generated code is disposable and must not be manually edited.
- Wrap awkward generated APIs with handwritten data sources outside generated directories.
- Domain modules must never expose generated request or response types.

## Dependency Injection

Hilt is the Android DI standard.

- Use constructor injection wherever possible.
- Use `@Binds` for interface-to-implementation mappings.
- Use `@Provides` only for third-party builders or construction logic.
- Put app/flavor bindings in `app`.
- Put repository bindings in feature data modules.
- Put reusable infrastructure bindings in core modules.

Shared KMP code should avoid Hilt dependencies. Pass dependencies through constructors or small interfaces when shared code needs behavior.

## Logging

- Use Timber for Android logging.
- Plant Timber trees only in the Android `app` module.
- Release logs must not expose secrets, personal data, request bodies, tokens, or payment data.
- Shared code should prefer injectable logging abstractions only when logging is genuinely needed.

## Coroutines And Flow

- Repositories expose cold `Flow` streams unless there is a clear reason not to.
- ViewModels expose immutable `StateFlow`.
- Inject dispatchers using project qualifiers.
- Do IO work on IO dispatchers.
- Do not use `GlobalScope`.
- Do not expose mutable flows publicly.

## Testing

Minimum expectations:

- use case tests for business rules
- ViewModel tests for state transitions
- repository tests for offline-first behavior
- DAO tests for queries and migrations
- shared Compose tests for critical shared UI behavior when test infrastructure exists

Use `core:testing` for fakes, builders, coroutine rules, and reusable test utilities.

## Verification

Before committing:

```bash
./gradlew spotlessCheck
./gradlew detekt ktlintCheck
./gradlew test
./gradlew lint
./gradlew :app:assembleNonProdDebug
```

For shared UI or iOS entry point changes:

```bash
./gradlew :shared:app:compileKotlinDesktop
./gradlew :desktopApp:compileKotlin
./gradlew :shared:app:linkDebugFrameworkIosSimulatorArm64
```

For iOS wrapper changes:

```bash
env DEVELOPER_DIR=/Applications/Xcode.app/Contents/Developer xcodebuild -project iosApp/KmpStarter.xcodeproj -scheme KmpStarter -sdk iphonesimulator -configuration Debug build
```

Android builds require the Gradle daemon to run on a full JDK 21. If Android compilation fails with `androidJdkImage` and `jlink executable ... does not exist`, the IDE is likely launching Gradle with a bundled JRE. Point `JAVA_HOME`, Android Studio, or VS Code's Gradle JVM setting at a full JDK 21 whose `bin` directory contains `jlink`.

Install hooks once:

```bash
git config core.hooksPath .githooks
```

The pre-commit hook runs `spotlessCheck detekt ktlintCheck`. The pre-push hook runs `:core:network:openApiGenerate test lint :app:assembleNonProdDebug`.

## Code Review Checklist

- Product UI is shared Compose unless there is a documented platform reason.
- Shared `commonMain` has no Android-only imports.
- Platform launchers remain thin.
- Environment IDs/default URLs are in shared KMP.
- Domain modules do not expose persistence, network, generated, DI, or UI types.
- Persistent data follows the offline-first flow.
- Generated code is not manually edited.
- Secrets and URLs are not hardcoded outside approved config points.
- Tests or verification commands cover the changed behavior.

## Changing Architecture

Use an ADR for changes to:

- module dependency direction
- KMP source-set ownership
- shared navigation strategy
- DI strategy
- database ownership
- generated client strategy
- environment strategy

Use `docs/architecture/adr-template.md`.
