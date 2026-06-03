# EnergyMap Tech Stack

Based on `../GOAL_app` and `../Sources_list`, EnergyMap should stay on the same native Android stack.

## Stack

- Kotlin
- Gradle Kotlin DSL
- Gradle Wrapper
- Android Gradle Plugin
- Jetpack Compose
- Material 3
- Navigation Compose
- Room
- KSP
- `ViewModel` + `StateFlow`
- simple repository layer

## Versions

Use the latest stable versions available as of `2026-06-02`:

- Gradle: `9.4.1`
- AGP: `9.2.0`
- Kotlin: `2.3.21`
- KSP: `2.3.9`
- Compose BOM: `2026.05.00`
- Material 3: `1.4.0`
- Navigation Compose: `2.9.8`
- Room: `2.8.4`
- Lifecycle: `2.10.0`
- Activity Compose: `1.13.0`
- Core KTX: `1.18.0`
- Java: `17`

## Constraints

- local-only app for v1
- AGP `9.x` built-in Kotlin enabled
- no backend
- no DI framework in v1
- keep architecture simple
- activity tracking stays on existing Compose + Room + `ViewModel` + `StateFlow` setup
- no new dependency required for the activity timeline feature
- debug APK output name: `EnergyMap-setup-debug.apk`
