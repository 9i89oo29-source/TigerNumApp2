package com.tigernum.app.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.tigernum.app.ui.home.HomeScreen
import com.tigernum.app.ui.buy.BuyNumberScreen
import com.tigernum.app.ui.orders.OrdersScreen
import com.tigernum.app.ui.instructions.InstructionsScreen
import com.tigernum.app.ui.settings.SettingsScreen

/**
 * Main navigation graph with smooth transitions and type-safe argument passing.
 */
@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier,
        enterTransition = {
            fadeIn(animationSpec = tween(300)) +
                slideInHorizontally(initialOffsetX = { it / 4 }, animationSpec = tween(300))
        },
        exitTransition = {
            fadeOut(animationSpec = tween(300))
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(300))
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(300)) +
                slideOutHorizontally(targetOffsetX = { it / 4 }, animationSpec = tween(300))
        }
    ) {
        // Home Screen
        composable(Screen.Home.route) {
            HomeScreen(
                onBuyClick = { provider, country, service ->
                    val route = Screen.BuyNumber.createRoute(provider.name, service.id, country.dialCode)
                    navController.navigate(route)
                }
            )
        }

        // Buy Landing (bottom nav target – redirects to BuyNumber with empty args if needed)
        composable(Screen.BuyLanding.route) {
            // Auto-navigate to actual BuyNumber with default values
            // In real app, might show a quick setup or just redirect.
            val defaultRoute = Screen.BuyNumber.createRoute("Hero-SMS", "wa", "+20")
            navController.navigate(defaultRoute) {
                popUpTo(Screen.Home.route) { inclusive = false }
            }
        }

        // BuyNumber with arguments
        composable(
            route = Screen.BuyNumber.route,
            arguments = listOf(
                navArgument("provider") { type = NavType.StringType },
                navArgument("serviceId") { type = NavType.StringType },
                navArgument("countryCode") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val provider = backStackEntry.arguments?.getString("provider") ?: "Hero-SMS"
            val serviceId = backStackEntry.arguments?.getString("serviceId") ?: ""
            val countryCode = backStackEntry.arguments?.getString("countryCode") ?: ""
            BuyNumberScreen(
                provider = provider,
                serviceId = serviceId,
                countryCode = countryCode
            )
        }

        // Orders Screen
        composable(Screen.Orders.route) {
            OrdersScreen()
        }

        // Instructions Screen
        composable(Screen.Instructions.route) {
            InstructionsScreen()
        }

        // Settings Screen
        composable(Screen.Settings.route) {
            SettingsScreen()
        }
    }
}
