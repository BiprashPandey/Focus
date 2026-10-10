# FocusLauncher: Build Guide for Muse Spark 1.3 (Contributor Free) in OpenCode

> This file is written **for an AI coding agent** (Muse Spark 1.3 Contributor Free, running in OpenCode via Zen).
> Put it in the project root, then copy it to `AGENTS.md` so OpenCode loads it automatically every session.
> The full product vision lives in `guidelines.md`. This file is the **execution plan**: strict rules, small steps, checkable results.

---

## 0. Setup (human, do once)

1. Install and start OpenCode: `npm install -g opencode-ai`, then run `opencode` in the project folder.
2. `/connect` -> choose **OpenCode Zen** -> paste your Zen API key.
3. `/models` -> choose **Muse Spark 1.3 Contributor Free** (id: `muse-spark-1.3-contributor-free`).
4. Copy this file: `cp guidelinesForMuse.md AGENTS.md`
5. Initialize git first (`git init`) so every step can be committed and rolled back.

### Free-tier realities (read before you start)

| Fact | What to do about it |
|---|---|
| **Contributor Free means your prompts and outputs may be used to train future models.** | Never paste API keys, keystore files, passwords, personal data, or private code. Use placeholders (`YOUR_API_KEY`). Keep secrets in `local.properties` (git-ignored) and never open them in the session. |
| **Free access is limited-time and rate limited** (429 errors have occurred). | Work in small tasks, commit often, and retry after a pause. Don't launch huge "build everything" prompts. |
| **Large context (about 1M tokens) but quality drops when the session gets cluttered.** | Start a **new session per phase**. Keep this file short in context; tell the agent which sections to read. |
| **Model output can be wrong or use outdated Android APIs.** | Always build and run tests after each step (see section 4). Treat the compiler as the judge. |

---

## 1. Rules for the Agent (must follow)

1. **One task at a time.** Do only the step the user names. Do not start the next step.
2. **Plan, then code.** Before editing, print a 3-6 line plan listing files to create or change.
3. **Small diffs.** Max about 300 lines changed per step. Split bigger work.
4. **Build after every step.** Run `./gradlew assembleDebug` and `./gradlew testDebugUnitTest`. Fix errors before reporting done. If you cannot run them, say so explicitly.
5. **Never invent APIs.** If unsure whether an Android/Compose/Room API exists in the pinned version, check the docs in the repo (`docs/`) or ask. Do not guess class names.
6. **Use only the pinned stack** in section 2. Do not add libraries without asking.
7. **No secrets in code or chat.** No API keys, no signing keys. Use `BuildConfig` from `local.properties` placeholders.
8. **No placeholders left behind.** No `TODO()` stubs that crash, no fake data in production paths (sample data goes in `debug` source set or previews only).
9. **Keep user data on-device.** No analytics SDKs. Network calls only for the opt-in Spark cloud feature (Phase 5).
10. **Always give the user an escape hatch** in any blocking UI (see Anti-dark-pattern rules in `guidelines.md` section 4).
11. **After each step, output:** (a) files changed, (b) how to test manually, (c) what is NOT done yet.
12. **Commit** after each green step: `git add -A && git commit -m "feat(scope): message"` (ask first if the user prefers manual commits).

---

## 2. Pinned Stack and Conventions

- Kotlin only, Jetpack Compose + Material 3, single `app` module to start (split into modules later, **not** now).
- minSdk 26, targetSdk = latest stable, Gradle Kotlin DSL + version catalog (`gradle/libs.versions.toml`).
- Architecture: MVVM, `StateFlow<UiState>`, repository pattern.
- DI: Hilt. DB: Room. Settings: DataStore. Background: WorkManager. Async: Coroutines/Flow.
- Package: `com.example.focuslauncher` (change `example` to the owner's id).
- Folder layout inside `app/src/main/java/.../`:
```
ui/        (screens + composables, per feature: home, drawer, focus, pause, todos, notes, settings, onboarding, spark)
data/      (room, datastore, repositories, apps, usage, calendar, spark)
domain/    (models, use cases, rules)
di/        (Hilt modules)
util/
```
- Naming: `XxxScreen`, `XxxViewModel`, `XxxUiState`, `XxxRepository`, `XxxDao`.
- All strings in `res/values/strings.xml`. No hard-coded colors outside the theme.
- Never block the main thread (icons, usage stats, DB: use `Dispatchers.IO`).

---

## 3. Build Plan (phases and steps)

Each step has **Prompt**, **Done when**. Paste the prompt into OpenCode, one at a time. **Start a fresh session at each phase boundary.**

### Phase 0: Project skeleton

**Step 0.1: Create project**
> Read AGENTS.md sections 1 and 2. Create a new Android project in this folder: Kotlin, Compose, Material 3, Hilt, Room, DataStore, WorkManager, with a version catalog. Package `com.example.focuslauncher`. Show a plan first. Do not build features yet.

Done when: `./gradlew assembleDebug` succeeds, app opens to a blank Compose screen.

**Step 0.2: Make it a launcher**
> Make MainActivity a home launcher: add the MAIN + HOME + DEFAULT intent filter, `launchMode="singleTask"`, handle `onNewIntent` (return to home top), ignore the back button on home. Add a small flow that asks the user to set it as default via `RoleManager.ROLE_HOME`.

Done when: After install, Android offers this app as a Home app and pressing Home opens it.

**Step 0.3: Theme and CI**
> Add Material 3 theme (light/dark/dynamic color) and a GitHub Actions workflow that runs `assembleDebug` and `testDebugUnitTest`. Add ktlint.

Done when: CI file exists and the build still passes.

### Phase 1: App drawer (core)

**Step 1.1: Installed apps repository**
> Create `AppsRepository` using `LauncherApps` (not only `queryIntentActivities`). Expose `Flow<List<AppInfo>>` (label, packageName, icon loader, user handle). Register `LauncherApps.Callback` so installs/uninstalls update live. Load icons lazily off the main thread with an in-memory cache. Add unit tests for sorting and filtering.

Done when: A debug screen lists all installed apps and updates when an app is installed or removed.

**Step 1.2: Drawer UI and search**
> Build the swipe-up app drawer: alphabetical list with fast scroller, search field at top (fuzzy match, accent-insensitive), tap to launch, long-press menu (App info, Uninstall, Hide). Keep scrolling smooth with 300+ apps (stable keys, no heavy work in composition).

Done when: Smooth scroll, search works, apps launch.

**Step 1.3: Dock and basic home**
> Create the home screen with a bottom dock of 4 customizable apps and swipe-up to open the drawer. Persist dock choices in DataStore.

Done when: Dock survives app restart.

### Phase 2: Hiding and gating apps

**Step 2.1: Data model**
> Create Room entity `AppConfig(packageName PK, level, dailyLimitMin, scheduleJson)` with levels: VISIBLE, TUCKED, HIDDEN, PAUSE_GATED. Add DAO, repository, and a use case `getVisibleApps()` that applies the levels. Add unit tests. Include a protected list that can never be hidden: Phone, default Messages, Settings, Clock, and this launcher.

Done when: Tests pass for each level and the protected list.

**Step 2.2: Settings UI for hiding**
> Add a "Manage apps" screen: searchable list, each app has a level selector. Add "Tucked" apps folder at the bottom of the drawer and "search-only" behavior for HIDDEN apps (found only by typing the full name).

Done when: Changing a level immediately changes the drawer.

**Step 2.3: Pause screen**
> Build the pause screen shown when launching a PAUSE_GATED app: 10-second breathing animation, today's usage for that app (placeholder 0 until usage stats exist), one motivational line from a local list, and three buttons: Open anyway (enabled after countdown), Do something else, Close. The screen must always be dismissible.

Done when: Launching a gated app shows the pause screen first; all three buttons work.

**Step 2.4: Usage stats and daily limits**
> Add a `UsageRepository` using `UsageStatsManager` and a screen explaining why access is needed with a button to the special-access settings. If permission is missing, everything must still work (only limits are disabled). When a daily limit is reached, promote the app to PAUSE_GATED for the rest of the day.

Done when: Limits work with permission; app works fine without it.

### Phase 3: Focus mode

**Step 3.1: Focus session engine**
> Create `FocusProfile` and `FocusSession` entities, a `FocusManager` with start/stop/restore, durations 25/50/90/custom. State must survive process death and reboot (persist end time, restore on boot via WorkManager/BOOT_COMPLETED). Unit-test the state machine.

Done when: Starting a session, killing the app, and reopening shows the correct time left.

**Step 3.2: Focus UI and allowed apps**
> During focus, the drawer and dock show only the profile's allowed apps; blocked apps open the pause screen. Add a focus timer card on home and a notification at session end. Add an emergency exit: long-press End, then a 10-second wait with optional reason. Phone calls and alarms always allowed.

Done when: Blocked apps cannot be launched from the launcher during focus, exit works.

**Step 3.3: Schedules and DND (optional features)**
> Add schedule support (days and times per profile) using WorkManager/AlarmManager and optional Do Not Disturb via `NotificationManager` policy access (ask permission only when the user turns the option on).

Done when: A scheduled profile starts at the right time, including after reboot and timezone change.

### Phase 4: Productive home screen

**Step 4.1: To-dos and notes**
> Add Room entities and screens for to-dos (title, due date, priority, done) and notes (text, pinned). Home shows top 3 to-dos with quick add and swipe-to-complete, plus a quick-note card.

**Step 4.2: Calendar card**
> Add a calendar card showing the next 3 events from `CalendarContract`. Request READ_CALENDAR only when the user taps "Connect calendar". Handle denied permission with a friendly empty state.

**Step 4.3: Intention, quotes, and motivation**
> Add "Today's intention" (one sentence, set each morning), a bundled `quotes.json` (start with 100 original or public-domain quotes; do NOT copy copyrighted text), a tone setting (Gentle / Coach / Stoic / Funny), and a cap of 3 proactive messages per day with quiet hours.

**Step 4.4: Customization**
> Let the user reorder or hide home cards and choose a "Minimal" (text list) or "Dashboard" (cards) layout.

Done when (all of Phase 4): Each card works offline, survives restarts, supports dark mode and font scale 200%.

### Phase 5: Spark (hobby and activity suggestions)

**Step 5.1: Local Spark library**
> Create `spark_library.json` with 60 starter activities (expand to 200 later). Fields: id, title, category (creative/movement/mind/career/social/practical), durationMin, difficulty, energy, place[], cost, tags[], careerTags[], firstStep, whyTemplate. Include things like origami, singing basics, dance steps, sketching, stretching, short language practice, and career-skill micro-tasks. Write original text only. Load it into Room on first launch.

**Step 5.2: Ranking and UI**
> Implement the Spark ranker (pure Kotlin, unit-tested): score = interests match + career match (weighted by user career ratio) + fits available time + energy/place match + learned tag weights - recently shown - not-for-me, with a little randomness. Build the Spark card with actions: Do it now (starts a timer), Save for later, Not for me, Another idea. Add a "Spark me" button on home and a "Do this instead" suggestion on the pause screen.

**Step 5.3: Onboarding for Spark**
> Add optional onboarding steps: pick interests (chips), career field, usual free time, free-only toggle. Everything skippable and editable in settings under "Spark data".

**Step 5.4: Cloud AI (opt-in, last)**
> Add an optional cloud mode. The app must NOT contain any LLM API key. It calls a backend URL from `BuildConfig` (blank by default). Send only: interests, career field, time available, energy, recent suggestion titles. Request JSON matching the Spark schema, validate it, 3-second timeout, and fall back to the local library on any failure. Cache a daily batch. Add a clear disclosure screen. Add a crisis-language check on free text before any network call: if triggered, show supportive resources instead of suggestions.
> Also create `backend/` with a minimal serverless proxy example (reads the key from an environment variable, rate-limits per install ID, logs no prompts).

Done when (Phase 5): Spark works fully offline; with the backend URL blank, no network calls occur.

### Phase 6: Polish and release

- Insights page and weekly review (local data only).
- Baseline Profile + startup check (target: home interactive under 300 ms warm/cold on a mid-range device).
- Accessibility pass (TalkBack labels, contrast, 48 dp targets).
- Data export/import as JSON, and "Delete all my data".
- Privacy policy, Play Data Safety answers, `QUERY_ALL_PACKAGES` justification (launcher core function).
- Test on: Pixel, Samsung, Xiaomi, an older Android 8-9 device.

---

## 4. Verification Checklist (agent runs after each step)

```
./gradlew ktlintCheck
./gradlew assembleDebug
./gradlew testDebugUnitTest
./gradlew lintDebug
```
If any fails: fix it, rerun, then report. Never say "done" while the build is red.

Manual smoke test (tell the user to do this when relevant):
- Set as default launcher, press Home, open drawer, search, launch an app
- Hide an app, confirm it disappears; reveal it again
- Start a 25-minute focus session, force-stop the app, reopen: timer still correct
- Turn on airplane mode: everything still works

---

## 5. When Things Go Wrong

| Problem | Fix |
|---|---|
| **429 / rate limit error** | Wait a minute and resend the same prompt. Make prompts smaller. Don't run parallel sessions. |
| **Agent loops or edits unrelated files** | Stop it. `git diff` to inspect, `git checkout .` to revert, then re-prompt with a narrower task and name the exact files. |
| **Hallucinated API / compile errors about missing symbols** | Paste the exact compiler error back and say: "Check the real API for the pinned version; do not guess." |
| **Context getting long and sloppy** | Commit, start a new session, and point it to AGENTS.md plus only the files needed. |
| **Model proposes adding many libraries** | Reply: "Use only the pinned stack in AGENTS.md section 2." |
| **Free model offer ends** | Switch with `/models` to another Zen model; this guide is model-agnostic. |

---

## 6. Prompt Template for Any New Task

```
Read AGENTS.md sections 1 and 2.
Task: <one clear sentence>.
Files you may touch: <list or "decide and tell me first">.
Constraints: <e.g., no new libraries, must work offline>.
Acceptance: <observable result>.
First print a plan (max 6 lines), then implement, then run the verification
commands from section 4 and report: files changed, how to test, what is not done.
```

---

## 7. Definition of Done (per step)

- [ ] Builds, lints, and unit tests are green
- [ ] Acceptance criterion demonstrated or manual test steps given
- [ ] No secrets, no hard-coded strings, no leftover stubs
- [ ] Permission requested only in context, app works if denied
- [ ] Blocking screens are dismissible; tone is kind, never shaming
- [ ] Committed with a clear message