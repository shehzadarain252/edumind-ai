package com.edumind.ai.ui.navigation

import android.app.Activity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.edumind.ai.AppContainer
import com.edumind.ai.ui.components.BannerAd
import com.edumind.ai.ui.screens.AboutScreen
import com.edumind.ai.ui.screens.AskAiScreen
import com.edumind.ai.ui.screens.ChatHistoryScreen
import com.edumind.ai.ui.screens.ContactScreen
import com.edumind.ai.ui.screens.DisclaimerScreen
import com.edumind.ai.ui.screens.DocumentUploadScreen
import com.edumind.ai.ui.screens.FaqScreen
import com.edumind.ai.ui.screens.FavoritesScreen
import com.edumind.ai.ui.screens.FeedbackScreen
import com.edumind.ai.ui.screens.GalleryScreen
import com.edumind.ai.ui.screens.GuidelinesScreen
import com.edumind.ai.ui.screens.HelpScreen
import com.edumind.ai.ui.screens.HomeScreen
import com.edumind.ai.ui.screens.OnboardingScreen
import com.edumind.ai.ui.screens.PlannerScreen
import com.edumind.ai.ui.screens.PrivacyScreen
import com.edumind.ai.ui.screens.ProfileScreen
import com.edumind.ai.ui.screens.SavedAnswersScreen
import com.edumind.ai.ui.screens.ScannerScreen
import com.edumind.ai.ui.screens.SplashScreen
import com.edumind.ai.ui.screens.SubjectDetailScreen
import com.edumind.ai.ui.screens.SubjectsScreen
import com.edumind.ai.ui.screens.TermsScreen
import com.edumind.ai.ui.screens.ToolDetailScreen
import com.edumind.ai.ui.screens.ToolsScreen
import com.edumind.ai.ui.viewmodel.AskAiViewModel
import com.edumind.ai.ui.viewmodel.DocumentViewModel
import com.edumind.ai.ui.viewmodel.PlannerViewModel
import com.edumind.ai.ui.viewmodel.ScannerViewModel
import com.edumind.ai.ui.viewmodel.SettingsViewModel
import com.edumind.ai.ui.viewmodel.ToolsViewModel

fun <T : ViewModel> vmFactory(creator: () -> T): ViewModelProvider.Factory =
    object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <V : ViewModel> create(modelClass: Class<V>): V = creator() as V
    }

private val tabIcons: Map<String, ImageVector> = mapOf(
    Routes.HOME to Icons.Filled.Home,
    Routes.ASK_AI to Icons.Filled.Chat,
    Routes.SUBJECTS to Icons.Filled.MenuBook,
    Routes.TOOLS to Icons.Filled.Build,
    Routes.PROFILE to Icons.Filled.Person,
)

@Composable
fun EduMindNavHost(
    container: AppContainer,
    navController: NavHostController = rememberNavController(),
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = BottomTabs.any { it.route == currentRoute }
    val context = LocalContext.current

    // Any option tap navigates somewhere. 5 seconds after each navigation
    // (except Splash/Onboarding, which are not taps), schedule an
    // interstitial — it shows only if an ad is loaded and the 60-second
    // cooldown has passed. A newer tap restarts the 5-second wait.
    LaunchedEffect(currentRoute) {
        val route = currentRoute ?: return@LaunchedEffect
        if (route == Routes.SPLASH || route == Routes.ONBOARDING) return@LaunchedEffect
        val activity = context as? Activity ?: return@LaunchedEffect
        container.adMobManager.scheduleInterstitialAfterDelay(activity)
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                Column {
                    // LIVE banner ad, above the bottom navigation bar.
                    BannerAd(container = container)
                    NavigationBar {
                        BottomTabs.forEach { tab ->
                            NavigationBarItem(
                                selected = currentRoute == tab.route,
                                onClick = {
                                    navController.navigate(tab.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = tabIcons[tab.route] ?: Icons.Filled.Home,
                                        contentDescription = tab.label,
                                    )
                                },
                                label = { Text(tab.label) },
                            )
                        }
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.SPLASH,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Routes.SPLASH) {
                SplashScreen(
                    container = container,
                    onFinished = { seenOnboarding ->
                        navController.navigate(
                            if (seenOnboarding) Routes.HOME else Routes.ONBOARDING,
                        ) { popUpTo(Routes.SPLASH) { inclusive = true } }
                    },
                )
            }
            composable(Routes.ONBOARDING) {
                OnboardingScreen(
                    container = container,
                    onDone = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.ONBOARDING) { inclusive = true }
                        }
                    },
                )
            }
            composable(Routes.HOME) {
                HomeScreen(container = container, navController = navController)
            }
            composable(Routes.ASK_AI) {
                val vm: AskAiViewModel = viewModel(
                    factory = vmFactory { AskAiViewModel(container) },
                )
                AskAiScreen(container = container, viewModel = vm, navController = navController)
            }
            composable(Routes.SUBJECTS) {
                SubjectsScreen(navController = navController)
            }
            composable(
                Routes.SUBJECT_DETAIL,
                arguments = listOf(navArgument("subjectId") { type = NavType.StringType }),
            ) { entry ->
                SubjectDetailScreen(
                    subjectId = entry.arguments?.getString("subjectId").orEmpty(),
                    container = container,
                    navController = navController,
                )
            }
            composable(Routes.TOOLS) {
                ToolsScreen(navController = navController)
            }
            composable(
                Routes.TOOL_DETAIL,
                arguments = listOf(navArgument("toolId") { type = NavType.StringType }),
            ) { entry ->
                val vm: ToolsViewModel = viewModel(
                    factory = vmFactory { ToolsViewModel(container) },
                )
                ToolDetailScreen(
                    toolId = entry.arguments?.getString("toolId").orEmpty(),
                    viewModel = vm,
                    navController = navController,
                )
            }
            composable(Routes.SCANNER) {
                val vm: ScannerViewModel = viewModel(
                    factory = vmFactory { ScannerViewModel(container) },
                )
                ScannerScreen(viewModel = vm, navController = navController)
            }
            composable(Routes.GALLERY) {
                GalleryScreen(container = container, navController = navController)
            }
            composable(Routes.DOCUMENTS) {
                val vm: DocumentViewModel = viewModel(
                    factory = vmFactory { DocumentViewModel(container) },
                )
                DocumentUploadScreen(viewModel = vm, navController = navController)
            }
            composable(Routes.CHAT_HISTORY) {
                ChatHistoryScreen(container = container, navController = navController)
            }
            composable(Routes.SAVED) {
                SavedAnswersScreen(container = container, navController = navController)
            }
            composable(Routes.FAVORITES) {
                FavoritesScreen(container = container, navController = navController)
            }
            composable(Routes.PLANNER) {
                val vm: PlannerViewModel = viewModel(
                    factory = vmFactory { PlannerViewModel(container) },
                )
                PlannerScreen(viewModel = vm, navController = navController)
            }
            composable(Routes.PROFILE) {
                val vm: SettingsViewModel = viewModel(
                    factory = vmFactory { SettingsViewModel(container) },
                )
                ProfileScreen(container = container, viewModel = vm, navController = navController)
            }
            composable(Routes.HELP) { HelpScreen(navController = navController) }
            composable(Routes.FEEDBACK) { FeedbackScreen(container = container, navController = navController) }
            composable(Routes.PRIVACY) { PrivacyScreen(navController = navController) }
            composable(Routes.TERMS) { TermsScreen(navController = navController) }
            composable(Routes.DISCLAIMER) { DisclaimerScreen(navController = navController) }
            composable(Routes.ABOUT) { AboutScreen(navController = navController) }
            composable(Routes.CONTACT) { ContactScreen(container = container, navController = navController) }
            composable(Routes.FAQ) { FaqScreen(navController = navController) }
            composable(Routes.GUIDELINES) { GuidelinesScreen(navController = navController) }
        }
    }
}
