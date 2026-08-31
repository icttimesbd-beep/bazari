package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.ui.events.EventDetailScreen
import com.example.ui.events.EventsScreen
import com.example.ui.history.HistoryScreen
import com.example.ui.history.HistoryViewModel
import com.example.ui.home.HomeScreen
import com.example.ui.home.HomeViewModel
import com.example.ui.lists.ListDetailScreen
import com.example.ui.lists.ListViewModel
import com.example.ui.product_picker.ProductPickerScreen
import com.example.ui.product_picker.ProductPickerViewModel
import com.example.ui.settings.SettingsScreen
import com.example.ui.templates.TemplateDetailScreen
import com.example.ui.templates.TemplatesScreen
import com.example.ui.templates.TemplatesViewModel

@Composable
fun BazariNavGraph(
    navController: NavHostController,
    homeViewModel: HomeViewModel = viewModel()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = homeViewModel,
                onNavigateToList = { listId ->
                    navController.navigate(Screen.ListDetail.createRoute(listId))
                },
                onNavigateToCatalog = { listId ->
                    navController.navigate(Screen.ProductPicker.createRoute(listId))
                },
                onNavigateToTemplates = {
                    navController.navigate(Screen.Templates.route)
                },
                onNavigateToTemplateDetail = { templateId ->
                    navController.navigate(Screen.TemplateDetail.createRoute(templateId))
                },
                onNavigate = { route ->
                    if (route != Screen.Home.route) {
                        navController.navigate(route) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }

        composable(
            route = Screen.ListDetail.route,
            arguments = listOf(navArgument("listId") { type = NavType.StringType })
        ) { backStackEntry ->
            val listId = backStackEntry.arguments?.getString("listId") ?: ""
            val listViewModel: ListViewModel = viewModel()
            ListDetailScreen(
                listId = listId,
                viewModel = listViewModel,
                onBackClick = { navController.popBackStack() },
                onNavigateToCatalog = { id ->
                    navController.navigate(Screen.ProductPicker.createRoute(id))
                },
                onNavigateToHistory = {
                    navController.navigate(Screen.History.route) {
                        popUpTo(Screen.Home.route)
                    }
                }
            )
        }

        composable(
            route = Screen.ProductPicker.route,
            arguments = listOf(navArgument("listId") { type = NavType.StringType })
        ) { backStackEntry ->
            val listId = backStackEntry.arguments?.getString("listId") ?: ""
            val pickerViewModel: ProductPickerViewModel = viewModel()
            ProductPickerScreen(
                listId = listId,
                viewModel = pickerViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Templates.route) {
            val templatesViewModel: TemplatesViewModel = viewModel()
            TemplatesScreen(
                viewModel = templatesViewModel,
                onNavigateToTemplateDetail = { templateId ->
                    navController.navigate(Screen.TemplateDetail.createRoute(templateId))
                },
                onNavigateToList = { listId ->
                    navController.navigate(Screen.ListDetail.createRoute(listId))
                },
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(Screen.Home.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }

        composable(
            route = Screen.TemplateDetail.route,
            arguments = listOf(navArgument("templateId") { type = NavType.StringType })
        ) { backStackEntry ->
            val templateId = backStackEntry.arguments?.getString("templateId") ?: ""
            val templatesViewModel: TemplatesViewModel = viewModel()
            TemplateDetailScreen(
                templateId = templateId,
                viewModel = templatesViewModel,
                onBackClick = { navController.popBackStack() },
                onNavigateToList = { listId ->
                    navController.navigate(Screen.ListDetail.createRoute(listId)) {
                        popUpTo(Screen.Templates.route)
                    }
                }
            )
        }

        composable(Screen.History.route) {
            val historyViewModel: HistoryViewModel = viewModel()
            HistoryScreen(
                viewModel = historyViewModel,
                onNavigateToList = { listId ->
                    navController.navigate(Screen.ListDetail.createRoute(listId))
                },
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(Screen.Home.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }

        composable(Screen.Events.route) {
            EventsScreen(
                onNavigateToEventDetail = { eventId ->
                    navController.navigate(Screen.EventDetail.createRoute(eventId))
                },
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(Screen.Home.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }

        composable(
            route = Screen.EventDetail.route,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
            EventDetailScreen(
                eventId = eventId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToList = { listId ->
                    navController.navigate(Screen.ListDetail.createRoute(listId))
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                homeViewModel = homeViewModel,
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(Screen.Home.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}
