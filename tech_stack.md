# EnergyMap Tech Stack

## Observed Stack In Neighboring Projects

The sibling apps in `../GOAL_app` and `../Sources_list` use a consistent native Android stack:

- Kotlin
- Gradle Kotlin DSL (`build.gradle.kts`)
- Android Gradle Plugin 9.x
- Jetpack Compose for UI
- Material 3
- Navigation Compose
- Room for local persistence
- KSP for Room code generation
- ViewModel-based state management
- Repository layer between UI and database
- Local-only storage with no backend

## Recommended Stack For EnergyMap

EnergyMap should use the same foundation so it stays aligned with the other apps and is easy to maintain in this workspace.

- Language: Kotlin
- UI: Jetpack Compose
- Design system: Material 3
- Navigation: Navigation Compose
- Persistence: Room
- Annotation/code generation: KSP
- State management: `ViewModel` + `StateFlow`
- Data access pattern: Repository layer over DAOs
- Build system: Gradle Kotlin DSL
- App type: Local-only Android app, no backend

## Suggested Architecture

Keep the architecture simple and consistent with `Sources_list`:

- `MainActivity` sets up Compose and creates the database
- `AppDatabase` holds Room entities and DAOs
- Repositories wrap database access
- One `ViewModel` per screen or feature area
- Compose screens observe `StateFlow` and render UI state

Suggested feature structure:

- `data/entity`
- `data/dao`
- `data/repository`
- `ui/home`
- `ui/energy`
- `ui/activity`
- `ui/history`
- `ui/theme`

## Data Layer Recommendation

EnergyMap has two primary record types, so Room is a strong fit.

- `EnergyEntry`
  - `id`
  - `timestamp`
  - `energyLevel`
  - `note`

- `ActivityEntry`
  - `id`
  - `title`
  - `startTime`
  - `endTime`
  - `note`
  - `isOngoing`

Use Room from the start because:

- the app is local-first
- timeline queries will matter early
- edit/delete/history features become straightforward
- this matches the existing app patterns in the parent directory

## Version Direction

If you want consistency with the newest sibling project, use the `Sources_list` direction:

- AGP: 9.2.x
- Kotlin: 2.3.x
- Compose BOM: 2026.05.x
- Navigation Compose: 2.9.8
- Room: 2.8.4

## Recommendations And Constraints

- Prefer Java 17 compile options, since `Sources_list` already uses it.
- Keep `minSdk` aligned with your existing apps if this is for personal use and consistency.
- If you want broader device support later, revisit `minSdk` separately instead of changing the rest of the stack.
- Do not add dependency injection, syncing, analytics, or a backend in v1.
- Do not add heavy architecture layers unless the app grows beyond simple local tracking.

## Final Recommendation

Use the same native Android stack already present in your sibling apps:

Kotlin + Jetpack Compose + Material 3 + Navigation Compose + Room + KSP + ViewModel/StateFlow + Repository pattern.

That is the right stack for EnergyMap's first version because it matches your current codebase habits, supports local-first timeline tracking well, and avoids unnecessary complexity.
