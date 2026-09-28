# Android architecture

## Module responsibilities

- `:domain` owns business models, policies, use cases, and repository/platform ports. It must not depend on Android, Room, `:data`, or `:presentation`.
- `:data` implements domain ports and owns persistence, platform adapters, and data-layer scheduling. It may depend on `:domain` only.
- `:presentation` owns Compose screens, UI state, and ViewModels. It may depend on `:domain` only.
- `:app` is the Android composition root and owns application entry points, navigation assembly, and app-specific integrations. It may depend on `:domain`, `:data`, and `:presentation`.

```text
:app          -> :domain, :data, :presentation
:data         -> :domain
:presentation -> :domain
:domain       -> Kotlin/JVM libraries only
```

`javax.inject` is an explicit exception in `:domain`: it supplies the standard constructor-injection annotation without tying domain code to Hilt or Android. Keep Android and Hilt APIs out of that module.

## Dependency checks

Run `./gradlew verifyArchitectureBoundaries` to check source imports, the exact allowed Gradle project-dependency graph, Kotlin package-to-directory alignment, and test package ownership. CI runs this check together with unit tests. The check covers Kotlin sources in all modules and source sets.

## Placement rules

- Domain unit tests belong in `domain/src/test`.
- Data adapter, persistence, and repository tests belong in `data/src/test`.
- UI/ViewModel tests belong in `presentation/src/test`.
- `app/src/test` is reserved for composition-root and application integration tests.
- Kotlin package declarations must match their path below each `src/<sourceSet>/java` or `kotlin` root. Domain, data, and presentation tests must use packages owned by their module; app integration tests must not use another module's package.
- Hilt modules that bind `:data` implementations belong under `com.ssajudn.bareuang.data.di`.

Add a new port only when an outer layer needs to call an adapter without depending on its implementation. Application clients use the narrow capability they need. Transaction queries and commands have separate ports because dashboard, analytics, entry, import, and detail clients consume independent subsets; `TransactionRepository` is the combined adapter contract implemented by `:data`.

## Repository behavior

Repository methods returning `Result` return expected failures as `Result.failure`; they rethrow coroutine cancellation. Observation flows emit domain models and leave upstream failures visible to collectors. Expected business and adapter failures use typed domain reasons; `:presentation` maps them to localized text. `ValidateTransactionUseCase` is the shared source for transaction request rules; the entry check coordinates those rules with the daily-budget confirmation policy, while `CreateTransactionUseCase` validates again before writing. Persistence repeats defensive guards and checks the latest wallet balance inside the database transaction. Data adapters do not format user-facing messages.

`ResetLocalDataUseCase` coordinates the local wipe and onboarding reset. It propagates cancellation and reports a wipe failure without resetting onboarding, so the app does not hide data that failed to clear.

The Android bill-reminder adapter is an explicit platform exception: it renders the system notification while a background worker is running, so it owns the localized title, due-date labels, and currency display. Notification copy stays in Android resources and amounts use the shared currency formatter. This is not repository or domain error text; other data adapters must continue returning typed failures without user-facing copy.

## Checker scope

`verify-architecture.py` enforces module dependencies, forbidden layer references, Kotlin package paths, and test package ownership. It cannot determine whether a class has one coherent reason to change or whether an interface satisfies SOLID in practice. Review feature responsibilities and collaboration boundaries alongside the checker; a passing result is structural evidence, not a complete architecture or SOLID assessment.
