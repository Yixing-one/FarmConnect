package com.example.farmconnect.ui

import android.graphics.drawable.Icon
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.NavigationDrawerItemDefaults.colors
import androidx.compose.material3.Surface

import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview

import androidx.compose.ui.unit.dp
import com.example.farmconnect.App
import com.example.farmconnect.R
import com.example.farmconnect.data.DataSource
import com.example.farmconnect.ui.theme.FarmConnectTheme
import com.example.farmconnect.ui.theme.PurpleGrey40
import com.example.farmconnect.ui.theme.darkGreen
import com.example.farmconnect.ui.theme.lightGreen

import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(
    onMenuClick: () -> Unit){
    TopAppBar(
        title = { } ,
        navigationIcon = {
            Icon(imageVector = Icons.Filled.Menu, contentDescription = "Menu", modifier = Modifier
                .clickable { onMenuClick() }
                .size(55.dp)
                .padding(10.dp))

        },

    )
}

@Composable
fun getIcon(title : String) {
    when(title){
        "Farmer" -> Icon(painter = painterResource(id = R.drawable.baseline_agriculture_24), contentDescription = null)
        "Charity" -> Icon(painter = painterResource(id = R.drawable.baseline_back_hand_24), contentDescription = null )
        "Settings" -> Icon(imageVector = Icons.Filled.Settings, contentDescription = null )
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawerContent(drawerState: DrawerState){
    val items = DataSource.menuOptions
    val scope = rememberCoroutineScope()

    ModalDrawerSheet  {
        DrawerHeader()
        Spacer(Modifier.height(12.dp))
        Column(Modifier.fillMaxHeight(), verticalArrangement = Arrangement.SpaceBetween) {
            Column {
                items.forEach { item ->
                    NavigationDrawerItem(
                        icon = { getIcon(title = item.title) },
                        label = { Text(item.title) },
                        selected = item.title == "Farmer",
                        colors = colors(selectedContainerColor = lightGreen) ,
                        onClick = {
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
            Row (modifier = Modifier.weight(1f, false)) {
                NavigationDrawerItem(
                    icon = { Icon(painter = painterResource(id = R.drawable.baseline_login_24), contentDescription = null ) },
                    label = { Text(text = "Log Out") },
                    selected = false,
                    onClick = {
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
                    painter = painterResource(id = R.drawable.profile_pic),
                    contentDescription = "profile image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .wrapContentSize()
                )
            }
            Text(text = "Name", color = Color.White)
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun NavPreview() {
    FarmConnectTheme {
        val drawerState = rememberDrawerState(DrawerValue.Open)
        val scope = rememberCoroutineScope()
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                DrawerContent(drawerState)
            }
        ) {}
    }
}