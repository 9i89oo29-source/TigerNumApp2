package com.tigernum.app.ui.navigation

/**
 * Type-safe navigation routes.
 * BuyNumber route accepts parameters to pass between screens.
 */
sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Orders : Screen("orders")
    data object Instructions : Screen("instructions")
    data object Settings : Screen("settings")

    // BuyNumber with arguments: provider, serviceId, countryCode
    data object BuyNumber : Screen("buy_number/{provider}/{serviceId}/{countryCode}") {
        fun createRoute(provider: String, serviceId: String, countryCode: String): String {
            return "buy_number/${provider}/${serviceId}/${countryCode}"
        }
    }
}
