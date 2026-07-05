# KMP Starter iOS Wrapper

`iosApp` is the native iOS delivery wrapper for the KMP Starter app. It should stay thin: launch SwiftUI, read Xcode build settings, and render shared Compose UI from `shared:app`.

Do not implement product screens in Swift unless a platform-native surface is explicitly required. Product UI belongs in shared Compose.

## Run NonProd

From the repository root:

```bash
./gradlew :shared:app:linkDebugFrameworkIosSimulatorArm64
open iosApp/KmpStarter.xcodeproj
```

In Xcode:

- scheme: `KmpStarter`
- run build configuration: `Debug`
- simulator: arm64 iOS simulator

`Debug` maps to `KMP_STARTER_ENVIRONMENT=nonProd`.

## Run Prod

Use Xcode `Release` build configuration.

`Release` maps to `KMP_STARTER_ENVIRONMENT=prod`.

The current project links the debug simulator framework path by default. Add release/device framework paths before using Release as a production archive path.

## Environment Contract

Shared KMP owns environment IDs and default API URLs in `KmpStarterRuntimeConfig`.

The iOS wrapper passes Xcode build settings through `Info.plist`:

- `KMP_STARTER_ENVIRONMENT`
- `KMP_STARTER_API_BASE_URL`

Override `KMP_STARTER_API_BASE_URL` in Xcode build settings only when a local or CI build needs a non-default backend URL.

## Framework Wiring

`KmpStarterShared.framework` is a static Kotlin framework. Link it from:

```text
shared/app/build/bin/iosSimulatorArm64/debugFramework
```

Do not add it to an Embed Frameworks phase.

`Info.plist` is intentionally stored at `iosApp/Info.plist` instead of inside the synchronized app source folder. Keep `CADisableMinimumFrameDurationOnPhone` enabled; Compose Multiplatform requires it on iOS.
