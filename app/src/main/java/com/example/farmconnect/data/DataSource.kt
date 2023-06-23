package com.example.farmconnect.data

import android.graphics.drawable.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import com.example.farmconnect.R

data class NavItem(
    val title : String,
    val route: String,
)
object DataSource {
    val menuOptions = listOf(
        NavItem(
            title = "Farmer",
            route = "farmer",
        ),
        NavItem(
            title = "Charity",
            route = "charity",
        ),
        NavItem(
            title = "Settings",
            route = "settings",
        )
    )
}