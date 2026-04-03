# Navigation

Midas uses **Compose Multiplatform Navigation 3** combined with **Koin** for dependency injection and composable resolution. Navigation is organized by feature, with each feature being fully self-contained in a single `*Routes.kt` file.

---

## Architecture overview

```
<Feature>Routes.kt
 ├── sealed interface <Feature> : NavKey   →  type-safe destination keys
 └── <feature>Module                       →  UiModule.kt  (combined with includes())
      ├── single(named<Feature>()) { SerializersModule }   →  aggregated in UiModule
      └── navigation<Feature.Screen> { }                   →  resolved by koinEntryProvider
```

Koin combines all entries into a single entry provider via `koinEntryProvider<NavKey>()`:

```kotlin
val backStack = rememberNavBackStack(koinInject<SavedStateConfiguration>(), Auth.SignIn)
NavDisplay(
    backStack = backStack,
    entryProvider = koinEntryProvider<NavKey>(),
)
```

---

## Feature file anatomy

Every feature follows the same two-part structure inside a single `<Feature>Routes.kt` file.

### 1. Route definitions — `sealed interface <Feature> : NavKey`

**Purpose:** Groups all destinations for a feature into a type-safe sealed hierarchy. Using `NavKey` as the direct supertype keeps each feature self-contained and avoids coupling between modules.

Destinations are nested inside the sealed interface as `data object` (no arguments) or `data class` (with arguments):

```kotlin
@Serializable
sealed interface Transaction : NavKey {

    @Serializable
    data class Add(val id: Long? = null) : Transaction   // null → create, non-null → edit

    @Serializable
    data class Detail(val id: Long) : Transaction
}
```

Using `@Serializable` on every type enables the back stack to survive process death and configuration changes on Android. On non-JVM platforms it also replaces reflection-based serialization that is unavailable there.

### 2. Koin module — `val <feature>Module: Module`

**Purpose:** Contains two things — the serializer registration and the composable factories for every destination.

**Serializer registration** uses `single(named<Feature>())` to expose a `SerializersModule` as a named Koin singleton. `subclassesOfSealed<Feature>()` auto-detects all subclasses, so no manual `subclass()` calls are needed when destinations are added:

```kotlin
val transactionModule = module {
    single(named<Transaction>()) {
        SerializersModule {
            polymorphic(NavKey::class) {
                subclassesOfSealed<Transaction>()
            }
        }
    }

    navigation<Transaction.Add> { /* View Here */ }
    navigation<Transaction.Detail> { /* View Here */ }
}
```

**Composable factories** are declared with `navigation<T>`. See [Implementing screens](#implementing-screens-with-navigationt) for details.

Each feature module is included in `UiModule.kt`, which is the single module registered with Koin at app startup:

```kotlin
// ui/UiModule.kt
val uiModule = module {
    includes(
        authModule,
        mainModule,
        transactionModule,
        categoryModule,
        csvImportModule,
    )

    single<SavedStateConfiguration> {
        val serializersModule = getAll<SerializersModule>()
            .reduce { acc, module -> acc + module }
        SavedStateConfiguration { this.serializersModule = serializersModule }
    }
}
```

`UiModule` aggregates all named `SerializersModule` singletons via `getAll<SerializersModule>()` and combines them into the single `SavedStateConfiguration` that is injected into the back stack.

---

## Back stack navigation

The back stack is a `SnapshotStateList<NavKey>`. Navigation is performed by mutating it directly:

```kotlin
// Navigate forward
backStack.add(Transaction.Detail(id = 42))

// Navigate back
backStack.removeLastOrNull()
```

Because `NavDisplay` observes the list, it reacts immediately to any change without needing a navigation controller object.

---

## See Also

- [Compose Multiplatform — Navigation 3 guide](https://kotlinlang.org/docs/multiplatform/compose-navigation-3.html)
- [Navigation 3 recipes — shared ViewModel example](https://github.com/android/nav3-recipes/tree/main/app/src/main/java/com/example/nav3recipes/sharedviewmodel)
- [Koin — Navigation 3 integration reference](https://insert-koin.io/docs/reference/koin-compose/navigation3)
