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
        // Home
        composable(Screen.Home.route) {
            HomeScreen(
                onBuyClick = { provider, country, service ->
                    val route = Screen.BuyNumber.createRoute(provider.name, service.id, country.dialCode)
                    navController.navigate(route)
                }
            )
        }

        // Buy Landing -> يعيد التوجيه إلى BuyNumber مع قيم افتراضية
        composable(Screen.BuyLanding.route) {
            val defaultRoute = Screen.BuyNumber.createRoute("Hero-SMS", "wa", "+20")
            navController.navigate(defaultRoute) {
                popUpTo(Screen.Home.route) { inclusive = false }
            }
        }

        // Buy Number مع وسائط
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

        // Orders
        composable(Screen.Orders.route) {
            OrdersScreen()
        }

        // Instructions
        composable(Screen.Instructions.route) {
            InstructionsScreen()
        }

        // Settings
        composable(Screen.Settings.route) {
            SettingsScreen()
        }
    }
}
