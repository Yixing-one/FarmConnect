package com.example.farmconnect.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Menu

object DataSource {
    val menuOptions = listOf(
        Pair("Switch", Icons.Default.Menu ),
        Pair("Inventory", Icons.Filled.Build),
        Pair("Test", Icons.Default.Add)
    )
}