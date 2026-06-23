# EventPass — Smart Event Check-in System

EventPass is an Android app for checking attendees into an event by scanning their
QR codes. It's built as a college project to demonstrate a clean **MVVM**
architecture with Jetpack Compose, Room, CameraX, and ML Kit.

---

## ✨ Features

| Screen | What it does |
|--------|--------------|
| **Login** | Local username/password login with validation (no backend). Demo creds: `admin` / `admin123`. |
| **Dashboard** | Live counts: total registered, checked in, and remaining. Buttons to scan, view/add attendees, and see stats. |
| **QR Scanner** | CameraX preview + ML Kit barcode scanning. On a successful scan, looks the attendee up in Room and marks them checked in with a timestamp. Handles camera permission and shows a result dialog (success / already checked in / not found). |
| **Attendee list** | All attendees with check-in status + time, and a search bar to filter by name. |
| **Add attendee** | Form (name, email, ticket type) that saves the attendee and displays a generated QR code (ZXing). |
| **Stats** | Check-in progress summary with a progress bar and a numeric breakdown. |

The app ships with **10 sample attendees** (ids `101`–`110`) seeded on first launch,
so everything works immediately.

---

## 🧱 Architecture (MVVM)

```
UI (Composable screens)
        │  observes StateFlow
        ▼
   ViewModel  ──────────────►  Repository  ──────────►  Room (DAO + Database)
   (state)                     (single source            (local SQLite)
                                of truth)
```

- **Data layer** — `Attendee` entity, `AttendeeDao`, `EventPassDatabase` (with seed data).
- **Repository** — `AttendeeRepository` wraps the DAO and owns the check-in logic
  (`CheckInResult`: Success / AlreadyCheckedIn / NotFound).
- **ViewModels** — one per screen, exposing immutable UI state via `StateFlow`.
- **UI** — Jetpack Compose (Material 3), navigated with Navigation Compose.
- **DI** — lightweight manual injection via `AppViewModelFactory` + the
  `EventPassApplication` (no Hilt/Dagger to keep the project approachable).

### Package layout

```
com.example.eventpass
├── EventPassApplication.kt      # owns DB + repository singletons
├── MainActivity.kt              # hosts Compose + NavHost
├── data
│   ├── local                    # Attendee, AttendeeDao, EventPassDatabase
│   └── repository               # AttendeeRepository, CheckInResult
├── ui
│   ├── navigation               # routes + NavHost
│   ├── login | dashboard | scanner | attendees | addattendee | stats
│   ├── components               # shared loading / empty / error states
│   └── theme                    # Material 3 theme
└── util                         # QR parse/generate, date format, VM factory
```

---

## 🛠 Tech stack

- **Kotlin** + **Coroutines / Flow**
- **Jetpack Compose** (Material 3)
- **Navigation Compose**
- **Room** (local persistence, via KSP)
- **CameraX** (camera preview + image analysis)
- **ML Kit Barcode Scanning** (reads QR codes)
- **ZXing** (`core`) for QR code generation
- **MVVM** with `ViewModel` + `StateFlow`

---

## ▶️ Running the app

1. Open the project in **Android Studio** (latest stable).
2. Let Gradle sync (it downloads Room, CameraX, ML Kit, ZXing, etc.).
3. Run on a **physical device** or an emulator. A real device is recommended for
   the QR scanner since it needs a working camera.
4. Log in with **`admin` / `admin123`**.

### Trying the scanner

- The seeded attendees have ids `101`–`110`. Generate a test QR online encoding,
  for example, `101` or `{"id":"101","name":"Aarav Sharma"}`.
- Or use the **Add attendee** screen to create someone new and scan the QR it
  generates right on screen.

---

## 🔍 How a scan becomes a check-in

1. `QrCodeAnalyzer` (ML Kit) decodes each camera frame and emits the raw QR text.
2. `ScannerViewModel.onQrCodeScanned` parses the attendee id via `QrPayload`
   (handles both plain ids and `{"id":...}` JSON).
3. `AttendeeRepository.checkIn(id)` looks the attendee up:
   - not found → `NotFound`
   - already checked in → `AlreadyCheckedIn`
   - otherwise → stamps `System.currentTimeMillis()` and returns `Success`.
4. The screen shows a result dialog and resumes scanning.

---

## 📝 Notes & limitations

- Authentication is **local/demo only** — there is no server or password hashing.
  Don't reuse the login as-is for anything real.
- The database is local to the device. Uninstalling the app clears all data and
  re-seeds the sample attendees on next launch.
- Generated attendee ids use a timestamp; for a real system you'd use server-issued ids.
