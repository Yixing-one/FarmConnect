package com.example.farmconnect.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.farmconnect.R
import com.example.farmconnect.Screens
import com.example.farmconnect.ui.theme.FarmConnectTheme
import com.example.farmconnect.ui.theme.darkGreen

@Composable
fun farmModeHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(darkGreen)
            .height(100.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Farm Mode",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
    }
}

data class PanelItem(
    val title: String,
    val description: String,
    val icon: Painter
)

@Composable
fun PanelGrid(
    panelItems: List<PanelItem>,
    onItemClick: (PanelItem) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        itemsIndexed(panelItems) { _, item ->
            PanelItemCard(panelItem = item, onItemClick = onItemClick)
        }
    }
}

@Composable
fun PanelItemCard(
    panelItem: PanelItem,
    onItemClick: (PanelItem) -> Unit
) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .clickable { onItemClick(panelItem) }
            .fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Image(
                painter = panelItem.icon, // Use the icon property as the painter
                contentDescription = "Photo",
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 8.dp)
            )

            Divider(color = Color.Black, thickness = 1.dp, modifier = Modifier.padding(bottom = 8.dp))

            Text(
                text = panelItem.title,
                fontWeight = FontWeight.Bold
            )
            Text(text = panelItem.description)
        }
    }
}

@Composable
fun getFarmerModeIcons(title: String): Painter {
    return when (title) {
        "Finance" -> painterResource(id = R.drawable.hand)
        "Marketplace" -> painterResource(id = R.drawable.marketplace)
        else -> painterResource(id = R.drawable.checklists) // Replace with a default icon if needed
    }
}

@Composable
fun getFarmerModeDescription(title: String): String {
    return when (title) {
        "Finance" -> "Check out your finances"
        "Marketplace" -> "Buy or sell items"
        else -> "Check your inventory"
    }
}

@Composable
fun farmModeScreenNav(navController: NavController) {
    val currentRoute = navController.currentBackStackEntry?.destination?.route

    val screenMap: HashMap<String, List<String>> = hashMapOf(
        Screens.Farm.name to listOf(Screens.Finance.name, Screens.Marketplace.name, Screens.Inventory.name),
        Screens.Charity.name to listOf(Screens.Charity.name),
        Screens.Settings.name to listOf(Screens.Settings.name)
    )

    val farmChildScreens = screenMap[Screens.Farm.name] ?: emptyList()

    PanelGrid(
        panelItems = farmChildScreens.map { screenName ->
            val icon = getFarmerModeIcons(screenName)
            val description = getFarmerModeDescription(screenName)
            PanelItem(screenName, description, icon)
        },
        onItemClick = { panelItem ->
            navController.navigate(panelItem.title)
        }
    )
}

@Composable
fun FarmModeScreen(navController: NavController) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        farmModeHeader()
        Spacer(modifier = Modifier.height(16.dp))
        farmModeScreenNav(navController)
    }
}

@Preview
@Composable
fun FarmModeScreenPreview() {
    FarmConnectTheme {
        val navController = rememberNavController()
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            FarmModeScreen(navController)
        }
    }
}
