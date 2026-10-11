# FocusLauncher

An Android home-screen replacement that makes the productive path the easy
path — without making the user feel policed. Friction, not walls. Calm by
default. Local-first and private.

## Features

- **App drawer** — alphabetical list with fast scroller, fuzzy
  accent-insensitive search, tap to launch, long-press menu.
- **Home + dock** — 4 customizable dock slots persisted in DataStore,
  swipe-up drawer.
- **Hiding & gating (per app)** — `Visible`, `Tucked` (a "More" folder),
  `Hidden` (search the full name to find), `Pause-gated` (10-second
  breathing screen before opening). Phone, SMS, Settings, Clock and the
  launcher itself are always protected.
- **Daily limits** — via `UsageStatsManager`. Over-limit apps become
  pause-gated for the rest of the day. Works fully without the permission
  (limits stay off). Per-app limits: none / 15 / 30 / 60 / 120 min.
- **Focus mode** — profiles (Work, Study, Wind-down) with allowed apps,
  25/50/90/custom-minute sessions that survive app kill and reboot,
  session-end notification, emergency exit (long-press End + 10 s wait +
  optional reason), optional schedules and opt-in Do Not Disturb.
- **Productive home** — to-dos (quick add, swipe-to-complete), notes
  (quick capture, pinning), calendar card (next 3 events, opt-in),
  daily intention + tone-matched quote (3/day cap, quiet hours),
  reorderable/hideable cards with Minimal and Dashboard layouts.

## Stack

Kotlin, Jetpack Compose (Material 3), Hilt, Room, DataStore, WorkManager,
Coroutines/Flow. Single `app` module. `minSdk 26`, `targetSdk 35`.

## Build & test

```sh
./gradlew assembleDebug          # debug APK
./gradlew testDebugUnitTest      # unit tests
./gradlew ktlintCheck            # style
./gradlew lintDebug              # Android lint
./gradlew installDebug           # install on a connected device
```

CI (`.github/workflows/`) runs `assembleDebug` + `testDebugUnitTest`.

## Project layout (`com.example.focuslauncher`)

```
ui/        home, drawer, manage, pause, usage, focus, ...
data/      appconfig (Room), apps (LauncherApps), dock, usage, focus, motivation
domain/    model, rules, focus (pure-Kotlin, unit-tested)
di/        Hilt modules
util/
```

## Permissions

| Permission | Why |
|---|---|
| Home launcher intent | Core: be a home app |
| `QUERY_ALL_PACKAGES` | List installed apps (launcher use case) |
| `PACKAGE_USAGE_STATS` | Daily limits only; special-access screen, optional |
| `POST_NOTIFICATIONS` | Focus session-end reminder (Android 13+, optional) |
| `ACCESS_NOTIFICATION_POLICY` | Do Not Disturb during focus (opt-in, optional) |
| `RECEIVE_BOOT_COMPLETED` | Restore focus session / schedules after reboot |
| `READ_CALENDAR` | Calendar card (asked only on "Connect calendar") |

No analytics, no network calls, all data stays on-device.

## Roadmap

- [x] Phase 0 — skeleton, launcher intent, theme, CI
- [x] Phase 1 — drawer, search, dock
- [x] Phase 2 — hiding, pause screen, usage limits
- [x] Phase 3 — focus sessions, allowed apps, schedules, DND
- [x] Phase 4 — to-dos, notes, calendar, intention, quotes, home layouts
- [ ] Phase 5 — Spark hobby suggestions (offline-first)
- [ ] Phase 6 — polish, release
