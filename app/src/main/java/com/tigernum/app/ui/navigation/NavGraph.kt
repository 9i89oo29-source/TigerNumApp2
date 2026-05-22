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
        composable(Screen.Home.route) {
            HomeScreen(
                onBuyClick = { provider, country, service ->
                    val route = Screen.BuyNumber.createRoute(provider.id, service.id, country.dialCode)
                    navController.navigate(route)
                }
            )
        }

        composable(Screen.BuyLanding.route) {
            val defaultRoute = Screen.BuyNumber.createRoute("hero-sms", "wa", "+20")
            navController.navigate(defaultRoute) {
                popUpTo(Screen.Home.route) { inclusive = false }
            }
        }

        composable(
            route = Screen.BuyNumber.route,
            arguments = listOf(
                navArgument("provider") { type = NavType.StringType },
                navArgument("serviceId") { type = NavType.StringType },
                navArgument("countryCode") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val providerSlug = backStackEntry.arguments?.getString("provider") ?: "hero-sms"
            val serviceId = backStackEntry.arguments?.getString("serviceId") ?: ""
            val countryCode = backStackEntry.arguments?.getString("countryCode") ?: ""
            BuyNumberScreen(
                providerSlug = providerSlug,
                serviceId = serviceId,
                countryCode = countryCode
            )
        }

        composable(Screen.Orders.route) {
            OrdersScreen()
        }

        composable(Screen.Instructions.route) {
            InstructionsScreen()
        }

        composable(Screen.Settings.route) {
            SettingsScreen()
        }
    }
}
