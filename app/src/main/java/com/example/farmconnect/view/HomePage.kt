package com.example.farmconnect.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.farmconnect.nevigation.AppBar
import com.example.farmconnect.nevigation.DrawerContent
import com.example.farmconnect.ui.theme.FarmConnectTheme
import androidx.navigation.compose.rememberNavController
import com.example.farmconnect.R
import com.example.farmconnect.ui.charity.CharityModeScreen
import com.example.farmconnect.ui.farmer.FarmModeScreen
import com.example.farmconnect.ui.farmer.FinanceStatsScreen
import com.example.farmconnect.ui.farmer.InventoryScreen
import com.example.farmconnect.ui.farmer.MarketplaceScreen
import com.example.farmconnect.ui.SettingsScreen
import kotlinx.coroutines.launch

class HomePage : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FarmConnectTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    App()
                }
            }
        }
    }
}

enum class Screens(@StringRes val title: Int) {
    //    Farm mode screens:
    Farm(title = R.string.farm_mode),
    Inventory(title = R.string.inventory),
    Finance(title = R.string.finance),
    Marketplace(title = R.string.marketplace),
    Donate(title = R.string.Donate),
    //    Charity mode screens:
    Charity(title = R.string.charity),
    Settings(title= R.string.settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    // Get the name of the current screen
    val currentScreen = Screens.valueOf(
        backStackEntry?.destination?.route ?: Screens.Farm.name
    )
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(drawerState, navController)
        }
    ) {
        Scaffold(
            topBar = { AppBar(onMenuClick = { scope.launch { drawerState.open() }}) }
        ) {
                paddingValues ->

            Surface(modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 15.dp)
            ){

                NavHost(
                    navController = navController,
                    startDestination = Screens.Farm.name

                ){
//                  Farm mode:
                    composable(route = Screens.Farm.name){
                        FarmModeScreen(navController)
                    }
                    composable(route = Screens.Inventory.name){
                        InventoryScreen()
                    }
                    composable(route = Screens.Finance.name){
                        FinanceStatsScreen()
                    }
                    composable(route = Screens.Marketplace.name){
                        MarketplaceScreen()
                    }
//                  Charity mode:
                    composable(route = Screens.Charity.name){
                        CharityModeScreen()
                    }

//                    Settings
                    composable(route = Screens.Settings.name){
                        SettingsScreen()
                    }


                }
            }
        }
    }

}

@Preview(showBackground = true)
@Composable
fun AppPreview() {
    FarmConnectTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            App()
        }
    }
}