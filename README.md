# GolfPerfTracker

A Golf Performance Tracker Android application built as part of a take-home assignment for Rapsodo.
The app retrieves and displays golf player and shot data, demonstrating clean architecture, offline-first design, reactive UI, and scalable engineering practices.

---

## Screens

| Screen | Description |
|---|---|
| **Player List** | Paginated list of golfers with search by name and filter by club type |
| **Player Detail** | Full player profile with performance stats, career stats, progress bar visualizations and shot history |
| **Shot Detail** | Complete breakdown of an individual shot with animated metric bars |

---

## API Source

Data is served from a static mock REST API hosted on **GitHub Pages**:

| Endpoint | Description |
|---|---|
| `GET https://kianningt.github.io/golf-app-api/players.json` | Returns a list of 25 golf players with summary stats |
| `GET https://kianningt.github.io/golf-app-api/players/{id}.json` | Returns full detail for a single player including their shot history (IDs 1–25) |

The base URL is injected via `BuildConfig.BASE_URL` defined in `app/build.gradle.kts`, making it easy to swap for a real API endpoint without touching source code.

---

##  Architecture

The project follows **Clean Architecture** with strict separation across three layers:

```
Presentation  →  Domain  →  Data
```

- **Presentation** — Jetpack Compose screens + `ViewModel` + `UiState` data classes
- **Domain** — Pure Kotlin use cases + repository interfaces + domain models
- **Data** — Retrofit DTOs + Room entities + mapper functions + repository implementation

### Design Patterns
- **MVVM** with `ViewModel` + `StateFlow` for reactive UI state
- **Repository Pattern** with Room as the single source of truth
- **Offline-First** — cache is always served first, network refreshes happen in the background
- **Single Activity** with Compose Navigation and a nested nav graph structure

---

## 🛠️ Tech Stack

| Category | Library |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + Clean Architecture |
| Dependency Injection | Dagger Hilt |
| Networking | Retrofit 2 + OkHttp 3 + Moshi |
| Local Database | Room |
| Pagination | Paging 3 |
| Image Loading | Coil |
| Async | Coroutines + Flow |
| Theme Persistence | DataStore Preferences |
| Logging | Timber |
| Splash Screen | AndroidX Core SplashScreen |

---

## 📦 Module & Package Structure

```
com.play.golf.perf.tracker
├── core/
│   ├── common/         # Resource, ResourceState, ResourceFlow, AppDispatchers
│   ├── network/        # NetworkObserver, NetworkUtils
│   └── datastore/      # ThemeDataStore
├── data/
│   ├── local/          # Room database, DAOs, entities
│   ├── remote/         # Retrofit API service, DTOs
│   ├── mapper/         # DTO → Entity → Domain mappers
│   └── repository/     # GolfRepositoryImpl
├── domain/
│   ├── model/          # Player, PlayerDetail, Shot
│   ├── repository/     # GolfRepository interface
│   └── usecase/        # GetPlayersUseCase, GetPlayerDetailUseCase, GetShotDetailUseCase
├── presentation/
│   ├── main/           # MainViewModel (theme + network)
│   ├── playerlist/     # Screen 1
│   ├── playerdetail/   # Screen 2
│   ├── shotdetail/     # Screen 3
│   └── components/     # SearchBar, SearchBarField, PullToRefreshLayout
├── navigation/         # RootNavGraph, GolfNavGraph, GolfNavArgs
├── di/                 # Hilt modules (Network, Database, Repository, DataStore)
└── ui/theme/           # Color, Type, Theme
```

---

##  Room Database (Offline-First)

Room is used as the **single source of truth** for all data in the app.

**Tables:**

| Table | Description |
|---|---|
| `players` | Caches the full player list fetched from `/players.json` |
| `player_details` | Caches individual player detail fetched from `/players/{id}.json` |
| `shots` | Caches shot data per player with a foreign key to `player_details` |

**Key behaviours:**
- On app launch, the player list is served immediately from Room while a background network refresh runs silently
- Player detail pages serve from cache first — no blank loading screen on revisit
- Shots are linked to their parent player via a `ForeignKey` with `CASCADE` delete — clearing a player detail automatically clears their shots
- Shot inserts are wrapped in a `@Transaction` to ensure atomic writes — no partial cache states
- The `shots` table is cleared before re-insert on every detail refresh to prevent duplicates

**Location:** `data/local/`

---

## Pagination (Paging 3)

Paging 3 is implemented using **Room as the paging source** — the recommended offline-first approach.

- `PlayerDao` exposes two `PagingSource<Int, PlayerEntity>` queries — one for all players, one for filtered/searched results
- `GolfRepositoryImpl` creates a `Pager` with `pageSize = 5` and `prefetchDistance = 2`
- The ViewModel uses `combine + debounce(300ms) + flatMapLatest` to restart the pager whenever the search query or club filter changes — previous pager is automatically cancelled
- `cachedIn(viewModelScope)` ensures the paged data survives configuration changes without re-fetching
- The UI uses `collectAsLazyPagingItems()` and `itemKey` for stable list identity
- An append loading spinner is shown at the bottom of the list while the next page loads

> **Note:** Since the API is a static JSON file returning all players at once, pagination operates entirely within Room. The architecture is designed to support true server-side pagination — only `GolfApiService` and `GolfRepositoryImpl` would need updating if the API added `page` and `size` query parameters.

**Location:** `data/local/dao/PlayerDao.kt`, `data/repository/GolfRepositoryImpl.kt`, `presentation/playerlist/PlayerListViewModel.kt`

---

## Search & Filter

Search and filter on the Player List screen work **entirely locally against the Room cache** — no API call is made on every keystroke.

- **Search by name** — `SearchBar` at the top of Screen 1, filters players using a `LIKE '%query%'` Room query
- **Filter by club type** — animated chip row below the search bar, populated dynamically from distinct club values in the cache. Tapping a chip filters the list; tapping the active chip clears the filter (toggle behaviour)
- Both search and filter can be active simultaneously — the Room query handles all combinations with a single parameterised SQL statement
- Search input is **debounced by 300ms** in the ViewModel to avoid redundant Room queries on rapid typing
- Clearing the search bar immediately resets the list via `onTextChange`

**Location:** `presentation/playerlist/PlayerListScreen.kt`, `presentation/playerlist/PlayerListViewModel.kt`, `data/local/dao/PlayerDao.kt`

---

## Network Observability

Real-time connectivity is observed using `ConnectivityManager.NetworkCallback` wrapped in a Kotlin `callbackFlow`.

- `NetworkObserver` is a `@Singleton` — one shared callback registration for the entire app lifetime
- Uses `NET_CAPABILITY_VALIDATED` in addition to `NET_CAPABILITY_INTERNET` — only reports `Available` when the network has genuine internet access, not just a captive portal
- Emits the current connectivity state immediately on collection so screens never miss the initial state
- `distinctUntilChanged()` prevents duplicate emissions when the underlying network switches (e.g. WiFi → mobile) without an actual interruption
- Both the Player List and Player Detail screens show an **animated offline banner** when connectivity is lost
- The Player List screen **auto-refreshes** when the network reconnects — no manual pull-to-refresh needed after coming back online
- The Player Detail screen silently re-fetches fresh data in the background on reconnection if detail was already loaded

**Location:** `core/network/NetworkObserver.kt`, `presentation/playerlist/PlayerListViewModel.kt`, `presentation/playerdetail/PlayerDetailViewModel.kt`

---

##️ Error Handling

Errors are handled at every layer with distinct UI responses depending on whether cached data is available.

**Repository layer:**
- `ResourceFlow.dispatchWithRetry()` wraps all Retrofit calls in a `Flow<Resource<T>>` emitting `Loading`, `Success`, or `Error`
- Connectivity errors (`UnknownHostException`, `ConnectException`) are flagged with `isConnectivityError = true` so the UI can show a contextual message
- HTTP errors include the status code and message from the response

**ViewModel layer:**
- Distinguishes between `isLoading` (first load, no cache) and `isRefreshing` (background update, cache available) — screens never go blank on a refresh error
- Non-connectivity errors on Screen 1 are surfaced as a `Snackbar` — non-blocking
- Full-screen error state is shown only when there is no cached data to display

**UI layer:**
- **Screen 1** — `Snackbar` for refresh errors; empty state composable when search/filter returns no results
- **Screen 2** — Full-screen error with retry button when no cache; `Snackbar` for background refresh errors when cache is present
- **Screen 3** — Full-screen error with "Go Back" button if the shot is not found in cache (should not occur under normal navigation flow)

**Location:** `core/common/ResourceFlow.kt`, `core/common/Resource.kt`, `presentation/playerlist/PlayerListScreen.kt`, `presentation/playerdetail/PlayerDetailScreen.kt`

---

## Retry Mechanism

All API calls use an **exponential backoff retry** strategy implemented in `ResourceFlow.dispatchWithRetry()`.

- Retries are triggered on `HttpException`, `UnknownHostException`, and `IOException`
- Delay schedule: `500ms × 2^attempt` — 500ms → 1000ms → 2000ms → …
- Player list and player detail calls are configured with `retryCount = 2` (up to 2 retries after the initial attempt)
- All retry attempts and outcomes are logged via Timber with the attempt number, delay, and cause
- A global OkHttp timeout of **35 seconds** applies equally to connect, read, and write operations

**Location:** `core/common/ResourceFlow.kt`, `data/repository/GolfRepositoryImpl.kt`, `di/NetworkModule.kt`

---

## Animations

The app includes multiple animations across all three screens:

| Animation | Location | Implementation |
|---|---|---|
| **Offline banner slide** | Screen 1 & 2 | `AnimatedVisibility` with `expandVertically + fadeIn / shrinkVertically + fadeOut` |
| **Club filter chips** | Screen 1 | `AnimatedVisibility` — chips appear once the cache is populated |
| **Stats card expand/collapse** | Screen 2 | `animateContentSize` with `spring(DampingRatioMediumBouncy)` for a physical bounce feel |
| **Screen fade-in** | Screen 3 | `Animatable(0f → 1f)` applied to the entire content column on first composition |
| **Staggered metric bars** | Screen 3 | Four `AnimatedStatBar` composables each using `Animatable` with staggered `delay` (0ms, 100ms, 200ms, 300ms) animating from 0 to their target value over 800ms with `FastOutSlowInEasing` |
| **Splash screen** | App launch | AndroidX `SplashScreen` with golf green `#1A3C2E` background |

---

## Light & Dark Theme

The app supports full **light and dark themes** with a manual toggle.

- Theme preference is persisted across sessions using **DataStore Preferences** — the correct theme is applied from the very first frame with no flash
- A sun/moon `IconButton` in the top app bar of Screen 1 toggles the theme live
- All colours are defined as `MaterialTheme.colorScheme.*` tokens — every screen adapts automatically
- Custom golf-themed colour palette: deep greens (`#1A3C2E`, `#2D6A4F`) as primary, fairway gold (`#C9A84C`) as accent

---

## Key Files Reference

| What you're looking for | File |
|---|---|
| API interface | `data/remote/api/GolfApiService.kt` |
| Retry + exponential backoff | `core/common/ResourceFlow.kt` |
| Offline-first repository | `data/repository/GolfRepositoryImpl.kt` |
| Room database + entities | `data/local/GolfDatabase.kt`, `data/local/entity/` |
| Paging source queries | `data/local/dao/PlayerDao.kt` |
| Network connectivity observer | `core/network/NetworkObserver.kt` |
| Theme persistence | `core/datastore/ThemeDataStore.kt` |
| Search + filter ViewModel | `presentation/playerlist/PlayerListViewModel.kt` |
| Animated metric bars | `presentation/shotdetail/ShotDetailScreen.kt` |
| Navigation graph | `navigation/GolfNavGraph.kt` |
| Hilt DI modules | `di/` |

---

## Getting Started

1. Clone the repository
2. Open in **Android Studio Hedgehog** or later
3. Sync Gradle
4. Run on an emulator or physical device (API 26+)
5. No API keys required — the mock API is publicly accessible

---

## Screenshots and Video files


https://github.com/user-attachments/assets/c0194fbe-5220-43b8-a4ad-253528d75af1

<img width="1080" height="2376" alt="s9" src="https://github.com/user-attachments/assets/327e9250-7be1-440f-9fc3-eb8d05649bd3" />
<img width="1080" height="2376" alt="s8" src="https://github.com/user-attachments/assets/b3d95ee5-bd15-4cf8-8d6e-ae16b1ca3533" />
<img width="1080" height="2376" alt="s7" src="https://github.com/user-attachments/assets/eeb5adb7-6047-4afd-9bf4-38428323f6ce" />
<img width="1080" height="2376" alt="s6" src="https://github.com/user-attachments/assets/0dbd23be-b57d-41e7-b91e-13152359c834" />
<img width="1080" height="2376" alt="s5" src="https://github.com/user-attachments/assets/be277076-83c6-4c00-b0f3-f3e623315501" />
<img width="1080" height="2376" alt="s4" src="https://github.com/user-attachments/assets/2eedb760-4a8a-4e7a-bb00-bc2c451bde94" />
<img width="1080" height="2376" alt="s3" src="https://github.com/user-attachments/assets/c464a373-2b7f-46bf-b0c0-84cc8b4e4241" />
<img width="1080" height="2376" alt="s2" src="https://github.com/user-attachments/assets/03d9db77-ccbe-495f-ac0b-f8ebb6d0961c" />
<img width="1080" height="2376" alt="s1" src="https://github.com/user-attachments/assets/5b56e13a-05b7-4f90-b711-e5049bb52f15" />


*Built using Kotlin, Jetpack Compose, and Clean Architecture.*
