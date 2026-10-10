package com.example.focuslauncher.domain.model

/** How an app surfaces in the launcher. User picks one per app. */
enum class AppLevel {
    /** Normal: home, drawer, search. */
    VISIBLE,

    /** Hidden from home; lives in the "More" folder at the drawer bottom. */
    TUCKED,

    /** Not in the drawer; found only by typing the full name in search. */
    HIDDEN,

    /** Opens through the 10-second pause screen first. */
    PAUSE_GATED,
}
