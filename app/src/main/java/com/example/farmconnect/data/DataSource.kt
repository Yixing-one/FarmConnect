package com.example.farmconnect.data

import com.example.farmconnect.view.Screens

data class NavItem(
    val title : String,
    val route: String,
)
object DataSource {
    val menuOptions = listOf(
        NavItem(
            title = "Farmer",
            route = Screens.Farm.name,
        ),
        NavItem(
            title = "Charity",
            route = Screens.Charity.name,
        ),
        NavItem(
            title = "Settings",
            route = Screens.Settings.name,
        )
    )
}