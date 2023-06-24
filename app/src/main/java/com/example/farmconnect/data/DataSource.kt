package com.example.farmconnect.data

import android.graphics.drawable.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import com.example.farmconnect.MainActivity
import com.example.farmconnect.R
import com.example.farmconnect.Screens

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