package com.example.farmconnect.navigation

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.NavigationDrawerItemDefaults.colors

import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview

import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.farmconnect.R
import com.example.farmconnect.view.Screens
import com.example.farmconnect.view.SignIn
import com.example.farmconnect.data.DataSource
import com.example.farmconnect.data.user_role
import com.example.farmconnect.ui.shopping.CartViewModel
import com.example.farmconnect.ui.theme.FarmConnectTheme
import com.example.farmconnect.ui.theme.darkGreen
import com.example.farmconnect.ui.theme.lightGreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.delay

import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(
    onMenuClick: () -> Unit,
    showShoppingCart: Boolean, // Add a boolean parameter
    showCloseIcon: Boolean,
    navController: NavController,
    cartViewModel: CartViewModel
) {
    val item_count = cartViewModel.items.size;
    TopAppBar(
        title = { },
        navigationIcon = {
            Icon(
                imageVector = Icons.Filled.Menu,
                contentDescription = "Menu",
                modifier = Modifier
                    .clickable { onMenuClick() }
                    .size(55.dp)
                    .padding(10.dp)
            )
        },
        actions = {
            if (showShoppingCart) { // Check the boolean value
                Button(onClick = { navController.navigate(Screens.Cart.name) }, Modifier.padding(end = 23.dp, top = 2.dp) ) {
                    Text(text = "$item_count", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.size(10.dp))
                    Icon(painter = painterResource(id = R.drawable.baseline_shopping_cart_24), contentDescription = "Shopping Cart" )


                }

            }
            if (showCloseIcon) {
                IconButton(
                    onClick = { navController.navigateUp() },
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close Cart"
                    )
                }
            }
        }
    )
}

@Composable
fun getIcon(title : String) {
    when(title){
        "Farmer" -> Icon(painter = painterResource(id = R.drawable.baseline_agriculture_24), contentDescription = null)
        "Charity" -> Icon(painter = painterResource(id = R.drawable.baseline_back_hand_24), contentDescription = null )
        "Shopping Center" -> Icon(painter = painterResource(id = R.drawable.shoppingcenter), modifier = Modifier.size(25.dp),contentDescription = null )
        "Settings" -> Icon(imageVector = Icons.Filled.Settings, contentDescription = null )
    }

}

val screenMap: HashMap<String, List<String>> = hashMapOf(
    Screens.Farm.name to listOf(Screens.Farm.name, Screens.Finance.name, Screens.Marketplace.name, Screens.Inventory.name),
    Screens.Charity.name to listOf(Screens.Charity.name),
    Screens.Shopping.name to listOf(Screens.Shopping.name),
    Screens.Settings.name to listOf(Screens.Settings.name)
)

fun isSelected(currentScreen: String?, currentNavItem: String): Boolean {
    return screenMap[currentNavItem]?.contains(currentScreen) == true

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawerContent(drawerState: DrawerState, navController: NavController){
    val items = DataSource.menuOptions
    val scope = rememberCoroutineScope()
    val currentRoute = navController.currentBackStackEntry?.destination?.route
    ModalDrawerSheet (
        Modifier
            .fillMaxHeight()
            .width(310.dp))  {
        DrawerHeader()
        Spacer(Modifier.height(12.dp))
        Column(Modifier.fillMaxHeight(), verticalArrangement = Arrangement.SpaceBetween) {
            Column {
                items.forEach { item ->
                    Log.d("TAGrole3,", user_role.value);
                    Log.d("TAGrole1,", item.title);
                    if(((item.title == "Farmer") && (user_role.value == "FARMER"))
                        || ((item.title == "Shopping Center") && (user_role.value == "BUYER"))
                        || ((item.title == "Charity") && (user_role.value == "CHARITY"))
                        || (user_role.value== "ADMIN")){
                        //navController.navigate(item.route)
                        NavigationDrawerItem(
                            icon = { getIcon(title = item.title) },
                            label = { Text(item.title) },
                            selected = isSelected(currentRoute, item.route),
                            colors = colors(selectedContainerColor = lightGreen) ,
                            onClick = {
                                scope.launch {
                                    drawerState.close()
                                }
                                navController.navigate(item.route)

                            },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                    }
                }
            }

            val context = LocalContext.current

            Row (modifier = Modifier.weight(1f, false)) {
                NavigationDrawerItem(
                    icon = { Icon(painter = painterResource(id = R.drawable.baseline_login_24), contentDescription = null ) },
                    label = { Text(text = "Log Out") },
                    selected = false,
                    onClick = {
                        Firebase.auth.signOut()
                        context.startActivity(Intent(context, SignIn::class.java))
                        (context as Activity).finish()

                        Log.d(TAG, FirebaseAuth.getInstance().currentUser.toString())
                    },
                    modifier = Modifier
                        .padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }

    }
}

@Composable
fun DrawerHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(darkGreen)
            .height(200.dp)
    ){
        var currentUser = FirebaseAuth.getInstance().currentUser
        Column (
            modifier = Modifier
                .padding(15.dp)
                .fillMaxWidth()
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center

        ) {
            Card(
                shape = CircleShape,
                modifier = Modifier
                    .padding(15.dp)
                    .size(75.dp)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(currentUser?.photoUrl),
                    contentDescription = "profile image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .wrapContentSize()
                        .size(80.dp)
                )
            }
            Text(text = currentUser?.displayName.toString(), color = Color.White)
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun NavPreview() {
    FarmConnectTheme {
        val drawerState = rememberDrawerState(DrawerValue.Open)
        val navController = rememberNavController()
        val scope = rememberCoroutineScope()
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                DrawerContent(drawerState, navController = navController)
            }
        ) {}
    }
}