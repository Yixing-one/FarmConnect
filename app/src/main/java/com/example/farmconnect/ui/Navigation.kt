package com.example.farmconnect.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults

import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

import androidx.compose.ui.unit.dp
import com.example.farmconnect.data.DataSource
import com.example.farmconnect.ui.theme.PurpleGrey40

import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(
    onMenuClick: () -> Unit){
    TopAppBar(
        title = { } ,
        navigationIcon = {
            Icon(imageVector = Icons.Filled.Menu, contentDescription = "Menu", modifier = Modifier
                .clickable { onMenuClick()  }
                .size(55.dp)
                .padding(10.dp))

        },


    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawerContent(drawerState: DrawerState){
    val items = DataSource.menuOptions
    val scope = rememberCoroutineScope()
    ModalDrawerSheet {
        DrawerHeader()
        Spacer(Modifier.height(12.dp))
        items.forEach { item ->
            NavigationDrawerItem(
                icon = { Icon(item.second, contentDescription = null) },
                label = { Text(item.first) },
                selected = false,
                onClick = {
                    scope.launch { drawerState.close() }
                },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
        }
    }
}

@Composable
fun DrawerHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(PurpleGrey40)
            .height(130.dp)
    ){
        Text(text = "User info and other stuff")
    }
}
