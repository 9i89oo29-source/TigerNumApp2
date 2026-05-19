package com.tigernum.app.ui.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object BuyLanding : Screen("buy_landing")
    data object Orders : Screen("orders")
    data object Instructions : Screen("instructions")
    data object Settings : Screen("settings")

    data object BuyNumber : Screen("buy_number/{provider}/{serviceId}/{countryCode}") {
        fun createRoute(provider: String, serviceId: String, countryCode: String): String {
            return "buy_number/${provider}/${serviceId}/${countryCode}"
        }
    }
}
