# ViewModel locale bug on API 26-32

## Problem

ViewModels use `getApplication<Application>().resources` for string formatting (error messages, duration labels, cooldown messages). When a user selects a per-app language via `AppCompatDelegate.setApplicationLocales()`, AppCompat only patches the **Activity** context — the `Application` context stays on the system locale.

This means ViewModel-produced strings use the system locale while Compose UI strings (via `stringResource()`) use the per-app locale. If a user's phone is set to English but they pick German in the app, error messages and duration labels stay in English.

**Scope:** API 26-32 only. On API 33+, the framework handles per-app locale at the process level (patching `Application` too), and AppCompat delegates to that.

## Affected call sites

### SweetSpotViewModel

- `formatDuration()` calls (lines 271, 285, 393, 551) — duration labels
- `getString()` calls (lines 266, 360, 379, 530, 542, 596, 612, 633) — error messages, cooldown messages

### WearViewModel

- `formatDuration()` calls (lines 193, 237) — duration labels
- `getString()` calls (lines 192, 198, 224, 260) — error messages

## Options

### A. Pass Activity resources into ViewModel

Pass `Resources` from the composable (via `LocalContext.current.resources`) to ViewModel functions. The Activity context has the correct per-app locale.

- Requires changing ViewModel method signatures to accept `Resources`
- Mixes concerns (UI passes resources to business logic)
- Simple, targeted fix

### B. Return resource IDs from ViewModel, resolve in UI

Instead of resolving strings in the ViewModel, return sealed types or `@StringRes Int` values in `UiState`. Let Compose UI resolve them with `stringResource()`.

- Cleanest separation of concerns
- Larger refactor — every error message and formatted string needs a new UiState field or wrapper type
- `formatRelative()` already works this way (called from composables)

### C. Wrap Application context with correct locale

Create a locale-aware `Context` in the ViewModel by applying the AppCompat locale to the Application context:

```kotlin
val config = Configuration(app.resources.configuration).apply {
    setLocales(AppCompatDelegate.getApplicationLocales().unwrap() as LocaleList)
}
val localizedResources = app.createConfigurationContext(config).resources
```

- Keeps ViewModel signatures unchanged
- Extra complexity, need to refresh when locale changes

### D. Accept the limitation

API 26-32 is a shrinking device population. Users who change the per-app language on those devices will see mixed-language strings in edge cases (error messages, cooldown labels). Core UI text is unaffected.

- Zero effort
- Not great UX for affected users
