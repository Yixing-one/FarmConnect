package com.example.farmconnect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.farmconnect.ui.AppBar
import com.example.farmconnect.ui.DrawerContent
import com.example.farmconnect.ui.DrawerHeader
import com.example.farmconnect.ui.theme.FarmConnectTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FarmConnectTheme {
                // A surface container using the 'background' color from the theme
//                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
//                   App("Android")
//                }
                val viewModel = viewModel<MainViewModel>()
                val searchText by viewModel.searchText.collectAsState()
                val items by viewModel.items.collectAsState()
                val isSearching by viewModel.isSearching.collectAsState()
//                var cart by remember { mutableStateOf(0) }
                var cart = 0;

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ){
                    TextField(
                        value = searchText,
                        onValueChange = viewModel::onSearchTextChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {Text(text = "Search")}
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        items(items) { item ->
                            ItemCard(
                                item = item,
                                modifier = Modifier.padding(8.dp)

                            )
                        }

                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(name: String, modifier: Modifier = Modifier) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(drawerState)
        }
    ) {
        Scaffold(
            topBar = { AppBar(onMenuClick = { scope.launch { drawerState.open() }}) }
        ) {
                paddingValues ->

            Surface(modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)){
                Text(
                    text = "Hello $name!",
                    modifier = modifier
                )
            }
        }
    }

}

@Composable
fun ItemCard(item: Item, modifier: Modifier = Modifier){
    var cartCounter = remember { mutableStateOf(0) }

    Card(modifier = modifier) {
        Column{
            Image(
                painter = painterResource(id = R.drawable.myimage),
                contentDescription = "image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(194.dp),
                contentScale = ContentScale.Crop
            )
            Text(
                text = "${item.name} for $${item.price}/lb   ${cartCounter.value}",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.headlineSmall
            )
            IconButton(onClick = { cartCounter.value++ }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "button")
            }
        }
    }
}

@Preview
@Composable
private fun ItemCardPreview() {
    ItemCard(Item(name = "Green Apple", price = 4.55, R.drawable.myimage))
}


@Preview(showBackground = true)
@Composable
fun AppPreview() {
    FarmConnectTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            App("Android")
        }
    }
}