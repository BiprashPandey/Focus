# FocusLauncher: Project Guidelines

> Working name: **FocusLauncher** (alternatives: Anchor, Stride, Clearday, Tempo). Rename freely.
> An Android home-screen replacement that helps people spend their phone time on what matters, and suggests meaningful things to do when they have nothing to do.

---

## 1. Vision and Principles

**Vision:** A launcher that makes the *productive path the easy path* without making the user feel policed.

**Core principles**

1. **Friction, not walls.** Slow down unproductive behavior (delays, prompts, grayscale) before blocking it. Always keep an escape hatch.
2. **User is in control.** The user defines what is "unproductive". The app never decides for them.
3. **Calm by default.** No red badges, no streak-shaming, no guilt copy.
4. **Local-first and private.** Data stays on the device unless the user opts in to cloud features.
5. **Fast.** A launcher is opened 100+ times a day. Cold start under 300 ms to a usable home screen. Jank is a dealbreaker.
6. **Ethical engagement.** The goal is *less* time on the phone, not more time in our app.

**Non-goals (v1):** Parental control, enterprise MDM, social network features, iOS.

---

## 2. Target Users and Personas

| Persona | Pain | What they need |
|---|---|---|
| **Student (Aarav, 20)** | Reels/YouTube before exams | Study focus mode, scheduled hiding, to-dos |
| **Remote worker (Sita, 29)** | Context switching, notifications | Work profile, calendar-first home, deep work timer |
| **Creative/Freelancer (Mina, 25)** | Idle time spent scrolling | AI hobby/skill suggestions, notes, inspiration |
| **Self-improver (Rohan, 34)** | Wants habits to stick | Streaks (gentle), daily intentions, weekly review |

---

## 3. Feature Specifications

### 3.1 Focus Mode

**Goal:** Temporarily reduce the phone to only what the user needs.

**Behavior**
- Start manually (one tap from home or a quick-settings tile), via **schedule** (e.g. Mon-Fri 9-12), or via **calendar event** (auto-start during events titled "Focus"/"Deep work", optional).
- Durations: presets 25 / 50 / 90 min, custom, or "until I turn it off".
- While active:
  - Only **allowed apps** appear in the drawer and on home.
  - Blocked apps show a **pause screen** (see 3.2) instead of launching.
  - Optional: enable Do Not Disturb (needs Notification Policy access) with allowed contacts.
  - Optional: grayscale-style minimal home (monochrome icons, no widgets except timer + current task).
- **Emergency exit:** Long-press "End focus", type a short reason or wait 10 s countdown. Log it (private) so the user can see patterns in the weekly review.
- Session end: gentle summary ("You focused 47 min. 1 task completed.") and a break suggestion.

**Focus profiles:** Work, Study, Sleep/Wind-down, Custom. Each has its own allowed apps, DND setting, and wallpaper/theme.

**Edge cases:** Phone calls and alarms are always allowed. Reboot mid-session restores state. Timezone change recomputes schedules. Battery saver must not kill the schedule (use `AlarmManager.setExactAndAllowWhileIdle` or WorkManager, see 6.5).

### 3.2 Hiding Unproductive Apps

**Levels (user picks per app)**

| Level | Behavior |
|---|---|
| **Visible** | Normal |
| **Tucked** | Hidden from home, available in a "More" folder at the bottom of drawer |
| **Hidden** | Not in drawer; reachable only via search (typed full name) |
| **Pause-gated** | Visible, but opening shows a 10-second "Do you really want to?" screen with breathing animation, a daily-limit counter, and options: *Open anyway / Do something else / Close* |
| **Scheduled hide** | Hidden only during chosen hours/focus profiles |
| **Daily limit** | After N minutes (from `UsageStatsManager`), becomes Pause-gated or Hidden for the rest of the day |

**Rules**
- Never hide: Phone, Messages (default), Settings, Clock/Alarm, Emergency, and the launcher itself. Warn if the user tries to hide Settings.
- Provide **"Reveal hidden apps"** behind a PIN/biometric/delay so the user cannot trivially undo their own commitment (optional "commitment lock" feature).
- Suggest apps to hide using **usage stats + app category** (social, games, video), shown as suggestions, never auto-applied.
- Hiding is **launcher-level only** (the app is not uninstalled or disabled). Be honest about this in the UI: users can still launch from other launchers or Settings.

**Pause screen content:** current usage today, a rotating motivational line, the user's top to-do, and a "Do this instead" AI suggestion (see 3.6).

### 3.3 Motivation Messages

**Types**
- **Daily intention** (morning): user writes or picks one sentence ("Today I finish the report").
- **Contextual nudges:** shown on pause screen, session end, and unlock (if opted in).
- **Quote of the day:** curated offline list (500+ quotes bundled), optionally themed (stoic, athlete, creator).
- **Personalized message:** AI-generated from the user's goals and progress (opt-in, see 3.6).
- **Evening reflection:** "What went well today?" (one line, stored locally).

**Tone guidelines**
- Encouraging, concrete, short (max ~140 chars).
- No shame ("You wasted 3 hours" is forbidden). Use neutral facts ("3h 10m on video today. Want a different plan for tomorrow?").
- Let users choose a tone: Gentle / Coach / Stoic / Funny.
- Frequency caps: max 3 proactive messages per day by default, user-configurable. Respect quiet hours.

### 3.4 Productive Home Screen

**Layout (default, single page + drawer)**
```
+--------------------------------------+
|  Date, weather (optional)            |
|  Today's Intention                   |
+--------------------------------------+
|  Calendar widget (next 3 events)     |
+--------------------------------------+
|  To-dos (top 3 + quick add)          |
+--------------------------------------+
|  Quick note (inline, 1 tap)          |
+--------------------------------------+
|  Quote / Spark card (AI suggestion)  |
+--------------------------------------+
|  Dock: 4 essential apps              |
+--------------------------------------+
   Swipe up: app drawer + search
   Swipe left: Focus & Stats page
   Swipe right: Notes / Journal page
```

**Built-in widgets (native, no AppWidgetHost needed)**
- **Calendar:** reads `CalendarContract` (READ_CALENDAR), agenda view, tap to open event.
- **To-dos:** local Room DB; priority, due date, repeat; "Today" and "Next" smart lists; swipe to complete.
- **Notes:** plain text/markdown, pin to home, quick capture from a gesture (double-tap).
- **Quote card:** daily quote, share as image.
- **Focus timer:** compact Pomodoro.
- **Habit tracker (small):** 3-5 daily habits with check-ins.

**Third-party widgets:** Support via `AppWidgetHost` in v1.1 (allow Google Calendar, Keep, etc.). Needs `BIND_APPWIDGET` handling and careful lifecycle management.

**Customization:** Reorderable cards, hide/show, light/dark/AMOLED, icon packs (v1.1), font size, minimal mode (text-only launcher list like Olauncher).

**Search:** Unified bar: apps (fuzzy), to-dos, notes, calendar events, quick actions ("start focus 25").

### 3.5 Other Motivating Features (Brainstorm)

Pick a subset; ordered by value-to-effort.

| Idea | Description | Effort |
|---|---|---|
| **Screen-time reframing** | Show time as "that's 6 books/year" or "a language's A1 level". Optional, kind tone | Low |
| **Gentle streaks** | Streak freezes and "best week" instead of a hard reset. Never punish a miss | Low |
| **Weekly review** | Sunday card: focus hours, tasks done, top distraction, one suggestion | Medium |
| **Goal tracker** | 1-3 long-term goals with milestones; home shows progress ring | Medium |
| **Unlock intention** | On unlock (optional): "What are you here to do?" with 3 quick choices | Medium |
| **Wallpapers that evolve** | Wallpaper grows (tree/garden/skyline) with focus time | Medium |
| **Wind-down mode** | Evening: dims screen, grayscale, hides distractions, shows tomorrow's plan | Medium |
| **Accountability buddy** | Share weekly summary with one friend (opt-in, v2) | High |
| **Achievements/badges** | Light and skill-based ("First 5-day focus week"), never pay-to-win | Low |
| **Breathing/stretch breaks** | 60-second guided micro-breaks after sessions | Low |
| **Phone-free challenges** | "1 hour no phone" with Wear/lock-based verification (v2) | High |
| **Mood check-in** | 1-tap emoji; correlates mood with usage in weekly review | Medium |
| **Learning streak** | If user picks a hobby (see 3.6), track practice time | Medium |
| **Voice capture** | Hold a gesture to dictate a note/to-do | Medium |

### 3.6 AI "Spark" Suggestions: Hobbies, Skills, and Things To Do

**Purpose:** When the user has free time (or is about to open a gated app), offer a concrete, low-barrier alternative: learn to sing, dance, origami, sketch, a coding mini-project, a career-relevant skill, a walk, etc.

**When it triggers**
- User taps "I'm bored" / "Spark me" button on home.
- Pause screen on gated apps ("Do this instead").
- Detected idle window: calendar is free for the next N minutes + user opted in to idle suggestions.
- After a focus session ends (break idea).
- Never interrupts during Focus Mode.

**Inputs (all optional, user-controlled in onboarding and settings)**
- Interests (multi-select chips + free text)
- Career/field and career goals (e.g. "backend developer", "nurse", "student of design")
- Available time (5 / 15 / 30 / 60+ min)
- Energy level (low / medium / high)
- Place (home / outside / commute / work)
- Resources (instrument? art supplies? none?)
- Budget (free only toggle)
- Past feedback (liked / not for me / done)

**Output: a Spark card**
```json
{
  "title": "Fold a paper crane",
  "category": "creative",
  "duration_min": 15,
  "difficulty": "beginner",
  "why_for_you": "Quick, calming, and builds patience between work blocks.",
  "first_step": "Grab any square sheet. Watch a 3-minute beginner tutorial.",
  "resources": ["https://..."],
  "career_link": null,
  "tags": ["hands-on", "offline", "free"]
}
```
Actions on the card: **Do it now** (starts a timer), **Save for later**, **Not for me** (feeds preference model), **Another idea**.

**Suggestion categories**
- Creative (origami, sketching, singing, instrument, writing, photography)
- Movement (dance, stretching, walk, bodyweight workout, yoga)
- Mind (puzzle, language, reading, chess, meditation)
- Career growth (course module, portfolio piece, read an industry article, practice interview, small project)
- Social (call a friend, write a thank-you note)
- Practical (tidy desk, meal prep, plan tomorrow)

**Design rules**
1. **Mix** career-related (about 40%) and pure-fun/wellbeing (about 60%) by default; user can slide the ratio.
2. **Be specific and tiny.** "Learn guitar" is bad. "Learn the Em chord and strum 4 times" is good.
3. **Respect context:** nothing noisy at night, nothing outdoors if the user is "at work".
4. **Avoid repetition:** keep last 30 suggestions; apply cooldowns.
5. **No medical/therapeutic claims.** If the user signals distress, show gentle supportive resources instead of a hobby (see Safety below).
6. **Explain why** each suggestion fits ("because you said you like music").

**Architecture options**

| Option | Pros | Cons |
|---|---|---|
| **A. Rule-based + curated library** (200+ activities in JSON, tagged) | Offline, free, instant, private | Less personal |
| **B. Cloud LLM via your backend proxy** | Rich personalization, fresh ideas | Cost, latency, privacy, needs network |
| **C. On-device small model** (e.g. Gemini Nano via AICore where available) | Private, offline | Limited device support, weaker quality |
| **Recommended: A as baseline + B as opt-in enhancement (+ C later)** | Works for everyone; AI improves it | More engineering |

**Cloud AI implementation rules**
- **Never ship an LLM API key in the APK.** Call your own backend (Cloud Run/Workers/Lambda) which holds the key, rate-limits per install, and validates input.
- Send **minimum data**: coarse interests, field of work, time available. Do **not** send app usage logs, note contents, calendar event titles, contacts, or exact location.
- Ask for **structured JSON output** and validate against a schema; fall back to the curated library on any failure or timeout (3 s budget).
- Cache a batch of 10-20 suggestions per day (prefetch on Wi-Fi/charging) so the UI is instant.
- Add a **content filter** and an allowlist of categories; reject anything unsafe.
- Clear in-app disclosure: "Spark uses AI. Only the info you see in Settings > Spark Data is sent."

**Sample system prompt (backend)**
```
You are Spark, a friendly suggestion engine inside a productivity launcher.
Given the user profile and context, return exactly {n} activity ideas as JSON
matching the provided schema. Rules:
- Each idea must be doable in <= {time_available} minutes with the listed resources.
- Be concrete: include a tiny first step.
- Mix career-growth ideas and fun/wellbeing ideas according to career_ratio.
- Never suggest anything dangerous, illegal, expensive, or medical.
- Do not repeat any title in recent_titles.
- Tone: warm, encouraging, never guilt-inducing. Max 140 chars for why_for_you.
Return only JSON.
```

**Personalization loop:** store likes/dislikes locally as tag weights (simple weighted scoring) to re-rank both library and AI results. No ML training needed for v1.

**Safety:** If the user's free-text inputs include self-harm or crisis language, do not generate hobby suggestions. Show a supportive message and local helpline info. Keep this on-device with a keyword + simple classifier check before any cloud call.

---

## 4. UX and Design Guidelines

**Visual**
- Material 3 with dynamic color (Android 12+) plus a calm custom palette fallback.
- Large whitespace, one primary action per card, 16 dp grid, min touch target 48 dp.
- Typography: one family (e.g. Inter or system), max 3 sizes per screen.
- Motion: subtle, under 250 ms; respect "Remove animations".

**Interaction**
- Gestures: swipe up (drawer), swipe left/right (pages), double-tap (lock screen, via accessibility optional or device admin: prefer a simple alternative), long-press (edit/menu).
- One-handed friendly: key actions in the bottom half.
- Onboarding: max 5 steps, each skippable: (1) set as default launcher, (2) pick goals, (3) pick apps to gate, (4) optional Spark interests, (5) permissions explained one at a time.

**Accessibility**
- TalkBack labels on every control, contrast 4.5:1, support font scaling to 200%, don't rely on color alone, support RTL.

**Copywriting**
- Second person, friendly, short. No guilt, no exclamation overload. Examples:
  - Good: "Want a 10-minute reset instead?"
  - Bad: "You're addicted to Instagram!"

**Anti-dark-pattern checklist (must pass before release)**
- [ ] Every blocking screen has a visible way out
- [ ] No fake urgency or countdown manipulation
- [ ] Cancel/disable is as easy as enable
- [ ] No notifications whose purpose is just to re-open the app
- [ ] Paywall never gates the core safety/escape features

---

## 5. Permissions and Play Store Policy

| Permission / API | Used for | Notes |
|---|---|---|
| `HOME` + `DEFAULT` intent filter | Be a launcher | Core; user sets as default via `RoleManager` (Android 10+) |
| `QUERY_ALL_PACKAGES` | List installed apps | Launchers are an **allowed** use case on Play, but declare it in the Play Console form |
| `PACKAGE_USAGE_STATS` | Daily limits, suggestions | Special access screen; explain clearly; feature must work without it |
| `READ_CALENDAR` | Calendar widget | Runtime permission, ask in context |
| `ACCESS_NOTIFICATION_POLICY` | DND during focus | Optional |
| `POST_NOTIFICATIONS` | Reminders, session end | Android 13+, optional |
| `SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM` | Focus schedules | Prefer WorkManager unless exactness matters; `USE_EXACT_ALARM` is restricted to alarm/calendar apps |
| `BIND_APPWIDGET` | Third-party widgets | v1.1 |
| `INTERNET` | Cloud Spark, quotes sync | Optional features only |
| `FOREGROUND_SERVICE` (specialUse) | Only if a running timer needs it | Avoid if possible |

**Avoid in v1:** `AccessibilityService` (heavy Play review, privacy concerns). If later needed for real app blocking or double-tap-to-lock, make it strictly optional with clear in-app disclosure and a Play-compliant declaration. Also avoid `SYSTEM_ALERT_WINDOW` (overlay) unless absolutely necessary.

**How blocking actually works without Accessibility:** because the user launches apps *through* your launcher, you intercept at launch time (pause screen). Users can bypass via notifications, recents, or other launchers. State this limitation honestly; offer the optional stronger mode later.

**Play Store requirements checklist**
- Privacy policy URL (required; explain usage stats, calendar, AI data)
- Data Safety form accurate (what is collected, shared, retained)
- Prominent disclosure for sensitive permissions
- Target the latest API level required by Play
- No misleading claims ("cures addiction", etc.)

---

## 6. Technical Guidelines

### 6.1 Stack
- **Language:** Kotlin (100%)
- **UI:** Jetpack Compose (Material 3), Compose Navigation
- **Architecture:** MVVM with unidirectional data flow (UDF), Clean Architecture layers
- **DI:** Hilt
- **Async:** Kotlin Coroutines + Flow
- **Local DB:** Room (to-dos, notes, sessions, suggestions, preferences weights)
- **Settings:** Jetpack DataStore (Proto or Preferences)
- **Background:** WorkManager (daily prefetch, schedules, weekly review)
- **Networking:** Retrofit/Ktor + kotlinx.serialization (only for opt-in cloud)
- **Images/Icons:** Coil; app icons via `PackageManager` / `LauncherApps`
- **Min SDK:** 26 (Android 8.0) covers about 95%+ of devices; **Target:** latest stable
- **Build:** Gradle Kotlin DSL, version catalog (`libs.versions.toml`)

### 6.2 Architecture and Modules
```
app/                      -> Application, navigation, DI wiring
core/
  common/                 -> utils, result types, dispatchers
  designsystem/           -> theme, components
  database/               -> Room entities, DAOs
  datastore/              -> preferences
  model/                  -> pure Kotlin domain models
feature/
  home/                   -> home screen, cards
  drawer/                 -> app list, search
  focus/                  -> focus modes, timer, schedules
  appcontrol/             -> hide/gate/limits, pause screen
  todos/
  notes/
  calendar/
  motivation/             -> quotes, intentions, nudges
  spark/                  -> suggestion engine (library + AI)
  insights/               -> usage stats, weekly review
  settings/
  onboarding/
data/
  apps/                   -> installed apps repo (LauncherApps)
  usage/                  -> UsageStatsManager wrapper
  calendar/               -> CalendarContract wrapper
  spark/                  -> local library + remote client
```
Rules: features depend on `core`/`data` abstractions, never on each other directly. Domain layer has no Android imports where practical.

### 6.3 Launcher Fundamentals
- Manifest activity: `launchMode="singleTask"`, `stateNotNeeded`, `excludeFromRecents="false"`, intent filters `MAIN` + `HOME` + `DEFAULT`.
- Use `LauncherApps` (not just `queryIntentActivities`) to support work profiles, shortcuts, and app install/uninstall callbacks (`LauncherApps.Callback`).
- Listen to package changes to keep the app list live.
- Handle back button (do nothing/close drawer), home press (return to top), and `onNewIntent`.
- Cache app list + icons in memory and a lightweight DB; **never block the main thread** when loading icons.
- Support themed icons (Android 13+) and adaptive icons.

### 6.4 Data Model (Room, simplified)
```
AppConfig(packageName PK, level, dailyLimitMin, scheduleJson, lastOpenedAt)
FocusProfile(id PK, name, allowedAppsJson, dndEnabled, scheduleJson, themeId)
FocusSession(id PK, profileId, startAt, endAt, plannedMin, endedEarly, exitReason?)
Todo(id PK, title, notes?, dueAt?, priority, repeatRule?, doneAt?, createdAt)
Note(id PK, title?, body, pinned, updatedAt)
Intention(date PK, text, completed)
Habit(id PK, name) / HabitLog(habitId, date)
SparkItem(id PK, title, category, durationMin, difficulty, tagsJson, source[lib|ai], shownAt?, feedback?, savedAt?)
TagWeight(tag PK, weight)
UsageSnapshot(date, packageName, minutes)   // derived, can be recomputed
```
Export/import all user data as JSON (user trust + backups). Provide "Delete all my data".

### 6.5 Background Work and Battery
- Focus schedules: use `AlarmManager` inexact + WorkManager; exact alarms only for session end if the user is waiting on it.
- Prefetch Spark suggestions with a periodic WorkManager job (constraints: unmetered, battery not low).
- Usage stats queried on demand and cached; avoid polling loops.
- No persistent foreground service unless the timer must survive aggressive OEM killers (then use a minimal, user-visible notification).
- **OEM caveats:** Xiaomi, Oppo, Vivo, Huawei, Samsung can kill background work. Include an in-app "Keep running reliably" guide per OEM (link to dontkillmyapp.com style instructions).

### 6.6 Performance Budgets
- Cold start to interactive home: **< 300 ms** (warm < 100 ms)
- Drawer scroll: 60 fps (90/120 where supported) with 300+ apps
- APK size: **< 15 MB**; RAM at idle: **< 80 MB**
- Use Baseline Profiles + Macrobenchmark; check with Android Studio Profiler and `StrictMode` in debug.

### 6.7 Security and Privacy
- All user content in app-private storage; enable `allowBackup` only with explicit encryption rules (or disable and offer manual export).
- Optional app lock for hidden-apps list (biometric via `BiometricPrompt`).
- Network: HTTPS only, certificate pinning optional for your backend, no third-party analytics SDKs by default.
- Telemetry: **opt-in**, anonymous, event-level only (feature used, not content). Prefer privacy-friendly tools (self-hosted PostHog/Plausible-like) or none.
- Backend: rate limiting, abuse protection, no storage of prompts beyond transient processing, no user identifiers beyond a random install ID.
- Follow OWASP MASVS basics; run dependency vulnerability scans in CI.

---

## 7. Code Style and Engineering Practices

- Follow the official Kotlin style guide; enforce with **ktlint + detekt** in CI.
- Compose: stateless composables, hoist state, preview every component, avoid heavy work in composition, use `remember`/`derivedStateOf` wisely, stable models (`@Immutable`/`@Stable` where needed).
- ViewModels expose `StateFlow<UiState>` and take `UiEvent`s; one-off effects via a channel/`SharedFlow`.
- No hard-coded strings or dimensions; all text in `strings.xml` (i18n-ready from day 1, start with English; plan for Hindi/Nepali/Spanish).
- Naming: `FeatureNameScreen`, `FeatureNameViewModel`, `FeatureNameRepository`, `FeatureNameUiState`.
- Error handling: sealed `Result`/`Outcome` types; no silent catch; user-friendly errors.
- Logging: Timber, stripped in release.
- Docs: KDoc for public APIs; an `ARCHITECTURE.md` and ADRs (`/docs/adr/`) for major decisions.

**Git workflow**
- Trunk-based with short-lived branches: `feat/…`, `fix/…`, `chore/…`
- Conventional Commits (`feat(focus): add schedule picker`)
- PRs: small (< 400 lines), checklist (tests, screenshots, a11y, strings), at least one review (or self-review checklist if solo)
- Semantic versioning; changelog generated from commits

**CI/CD (GitHub Actions)**
1. Lint (ktlint, detekt, Android Lint) 2. Unit tests 3. Build debug APK 4. (nightly) instrumented tests on emulator 5. Release: signed AAB to Play internal track via Fastlane/Gradle Play Publisher.
Keep signing keys in secrets, never in the repo.

---

## 8. Testing Strategy

| Layer | Tools | Targets |
|---|---|---|
| Unit | JUnit5, MockK, Turbine | Use cases, schedule logic, limit rules, Spark ranking, JSON validation (80%+ on domain) |
| UI | Compose UI tests, Paparazzi/Roborazzi screenshots | Home cards, pause screen, onboarding |
| Integration | Room in-memory, WorkManager test utils | DAOs, migrations, schedulers |
| E2E | Espresso/UI Automator | Set default launcher, start/stop focus, hide app, launch gated app |
| Performance | Macrobenchmark | Startup, drawer scroll |
| Manual | Real-device matrix | Pixel, Samsung One UI, Xiaomi MIUI/HyperOS, a low-RAM device, Android 8-15 |

Critical scenarios to test: reboot during focus, timezone/DST change, package uninstall while configured, work profile, permission revoked mid-use, offline Spark fallback, app updated and Room migration, tablets/foldables, gesture vs 3-button navigation, large font and dark mode.

---

## 9. Analytics and Success Metrics

**North-star:** Weekly minutes the user spends in *self-chosen* focus or on intentional activities, **and** reduction in time on self-gated apps. (Not DAU or time-in-app.)

| Area | Metric |
|---|---|
| Activation | Set as default launcher within 24h; completed onboarding; first focus session |
| Engagement | Focus sessions/week, to-dos completed, Spark cards acted on ("Do it now" rate) |
| Impact | % change in gated-app minutes after 2 weeks |
| Quality | Crash-free sessions > 99.5%, ANR < 0.2%, startup p95 |
| Trust | Uninstall reasons, "launcher reset to previous" rate, review sentiment |
| Retention | D1 / D7 / D30 (benchmark: launchers often see D30 of 10-20%) |

---

## 10. Monetization (Ethical)

- **Free forever:** Core launcher, hiding, basic focus mode, to-dos, notes, calendar, curated Spark library.
- **Pro (one-time or low subscription):** AI Spark personalization, multiple focus profiles/schedules, themes and icon packs, widgets customization, insights/weekly review history, encrypted backup.
- **Never:** Ads, selling data, paywalling escape hatches or safety features.
- Offer a lifetime option; people distrust yet another subscription for a launcher.

---

## 11. Roadmap

**Phase 0: Foundations (1-2 weeks)**
Project setup, CI, design system, launcher manifest, installed apps list, default-launcher role flow.

**Phase 1: MVP (4-6 weeks)**
- App drawer + search + dock
- Hide/tuck apps, pause-gated apps
- Focus mode (manual + timer + allowed apps)
- Home: intention, to-dos, notes, quote card
- Offline curated Spark (rule-based) + "Spark me" button
- Onboarding, settings, data export
- **Exit criteria:** daily-drivable by you for 2 weeks without frustration.

**Phase 2: Beta (4-6 weeks)**
- Calendar widget, schedules, DND integration
- Daily limits via usage stats, insights page
- Motivation tone settings, evening reflection, weekly review
- Cloud AI Spark (opt-in) via backend proxy, personalization loop
- Closed beta (50-100 users) on Play internal/closed testing

**Phase 3: v1.0 Public Launch**
- Polish, performance (baseline profiles), accessibility audit
- Third-party widget host, themed icons, icon packs
- Play listing, privacy policy, Data Safety, landing page
- Launch channels: r/androidapps, r/minimalism, r/getdisciplined, Product Hunt, XDA

**Phase 4: v1.x / v2 ideas**
- On-device AI (Gemini Nano/AICore), voice capture
- Optional accessibility-based strong blocking
- Wear OS/Tablet layouts, buddy accountability, learning-path mode (30-day mini-courses for a chosen hobby)
- Localization

---

## 12. Risks and Mitigations

| Risk | Mitigation |
|---|---|
| Users abandon because launcher switching is annoying | Great onboarding, import current layout, easy revert, "light mode" that keeps familiar grid |
| OEMs kill background tasks | Minimize background needs, OEM guide, schedule recalculation on launch |
| Play policy rejection (QUERY_ALL_PACKAGES / usage stats / accessibility) | Declare launcher use case, prominent disclosures, avoid accessibility in v1 |
| Easy bypass makes blocking feel pointless | Honest messaging, commitment lock option, focus on friction + awareness |
| AI costs scale | Curated-first, cache, small models, per-user quotas, Pro tier |
| AI gives poor/unsafe suggestions | Schema validation, allowlist, content filter, feedback buttons, fallback to library |
| Privacy backlash | Local-first, no analytics by default, clear data page, open-source consideration |
| Scope creep | Hold the roadmap; every new feature must support "less time wasted, more time well spent" |
| Solo-dev burnout | Ship the MVP small; dogfood; one feature at a time |

---

## 13. Open Questions (Decide Early)

1. Open-source (builds trust, community) or closed? Consider open core.
2. Minimal text-launcher aesthetic vs. rich card home? (Could ship both as "Minimal" and "Dashboard" modes.)
3. Own backend vs. serverless proxy for AI? (Serverless is recommended.)
4. Which regions/languages first?
5. Name and branding (check trademarks and Play Store availability).

---

## 14. Definition of Done (Per Feature)

- [ ] Meets acceptance criteria and works offline (unless explicitly cloud)
- [ ] Unit + UI tests added; no regression in startup/scroll benchmarks
- [ ] Accessible (TalkBack, font scale, contrast)
- [ ] Strings externalized; dark mode checked
- [ ] Permissions requested in context with rationale; works gracefully if denied
- [ ] Handles edge cases (reboot, timezone, uninstall, work profile)
- [ ] Anti-dark-pattern checklist passed
- [ ] Documented (README/ADR/changelog) and reviewed

---

## 15. Appendix

### A. Launcher manifest snippet
```xml
<activity
    android:name=".MainActivity"
    android:exported="true"
    android:launchMode="singleTask"
    android:stateNotNeeded="true"
    android:resumeWhilePaused="true"
    android:theme="@style/Theme.FocusLauncher">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.HOME" />
        <category android:name="android.intent.category.DEFAULT" />
    </intent-filter>
</activity>
```

### B. Requesting the home role (Android 10+)
```kotlin
val roleManager = getSystemService(RoleManager::class.java)
if (roleManager.isRoleAvailable(RoleManager.ROLE_HOME) &&
    !roleManager.isRoleHeld(RoleManager.ROLE_HOME)) {
    startActivityForResult(
        roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME), REQUEST_HOME
    )
}
```

### C. Querying today's app usage
```kotlin
val usm = context.getSystemService(UsageStatsManager::class.java)
val start = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
val stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, start, System.currentTimeMillis())
val minutesByPackage = stats.associate { it.packageName to it.totalTimeInForeground / 60_000 }
```

### D. Curated Spark library entry (offline)
```json
{
  "id": "origami-crane-01",
  "title": "Fold a paper crane",
  "category": "creative",
  "durationMin": 15,
  "difficulty": "beginner",
  "energy": "low",
  "place": ["home", "work"],
  "cost": "free",
  "tags": ["hands-on", "calming", "offline"],
  "careerTags": [],
  "firstStep": "Cut a square from any paper, then follow a beginner crane tutorial.",
  "whyTemplate": "A calm, screen-free reset that builds focus."
}
```
Starter library targets: 200 entries across categories, with 10+ career clusters (software, design, healthcare, education, business/finance, engineering, law, trades, creative/media, student).

### E. Simple Spark ranking (v1, no ML)
```
score = base
      + 2.0 * matchInterests
      + 1.5 * matchCareerIf(careerRatio)
      + 1.0 * fitsTimeAvailable
      + 0.5 * matchesEnergy/place
      + tagWeights.sum()            // learned from likes/dislikes
      - 3.0 * shownRecently
      - 5.0 * markedNotForMe
pick top-N with a bit of randomness (epsilon-greedy) so it stays fresh
```

### F. Sample copy bank
- Pause screen: "You've spent 28 min here today. Want a 10-minute reset instead?"
- Focus start: "25 minutes. One thing. You've got this."
- Focus end: "Nice work: 25 min done. Stretch for a minute?"
- Evening: "What's one thing that went well today?"
- Spark empty state: "Free time? Here's a tiny idea."

### G. Useful references
- Android: *Create a home screen launcher*, `LauncherApps`, `UsageStatsManager`, `RoleManager`, `AppWidgetHost` docs
- Google Play: *User Data policy*, *Permissions & APIs that access sensitive info*, *QUERY_ALL_PACKAGES policy*
- Inspiration (study, don't copy): Olauncher, Niagara Launcher, Before Launcher, Lawnchair, Digital Wellbeing, one sec, Forest, Opal
- Behavior design: *Atomic Habits* (James Clear), *Hooked* (read as what to avoid), *Indistractable* (Nir Eyal)

---

*Next suggested steps:* (1) lock the name and MVP scope, (2) set up the repo with the module structure above, (3) build drawer + default-launcher flow first, (4) dogfood daily.