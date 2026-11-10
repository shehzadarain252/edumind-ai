package com.edumind.ai.ui.navigation

/**
 * All navigation routes. Bottom navigation covers exactly five tabs
 * (Home, Ask AI, Subjects, Study Tools, Profile). There is deliberately
 * NO navigation drawer / sidebar in this app — navigation is bottom bar
 * only, plus pushed detail screens.
 */
object Routes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val ASK_AI = "ask_ai"
    const val SUBJECTS = "subjects"
    const val SUBJECT_DETAIL = "subject_detail/{subjectId}"
    const val TOOLS = "tools"
    const val TOOL_DETAIL = "tool/{toolId}"
    const val SCANNER = "scanner"
    const val GALLERY = "gallery"
    const val DOCUMENTS = "documents"
    const val CHAT_HISTORY = "chat_history"
    const val SAVED = "saved"
    const val FAVORITES = "favorites"
    const val PLANNER = "planner"
    const val PROFILE = "profile"
    const val HELP = "help"
    const val FEEDBACK = "feedback"
    const val PRIVACY = "privacy"
    const val TERMS = "terms"
    const val DISCLAIMER = "disclaimer"
    const val ABOUT = "about"
    const val CONTACT = "contact"
    const val FAQ = "faq"
    const val GUIDELINES = "guidelines"

    fun subjectDetail(subjectId: String) = "subject_detail/$subjectId"
    fun toolDetail(toolId: String) = "tool/$toolId"
}

/** The five bottom-navigation tabs. */
val BottomTabs = listOf(
    BottomTab(Routes.HOME, "Home"),
    BottomTab(Routes.ASK_AI, "Ask AI"),
    BottomTab(Routes.SUBJECTS, "Subjects"),
    BottomTab(Routes.TOOLS, "Study Tools"),
    BottomTab(Routes.PROFILE, "Profile"),
)

data class BottomTab(val route: String, val label: String)
