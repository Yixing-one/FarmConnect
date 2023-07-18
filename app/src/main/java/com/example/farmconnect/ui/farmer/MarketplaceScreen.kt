package com.example.farmconnect.ui.farmer

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.farmconnect.R
import com.example.farmconnect.ui.theme.FarmConnectTheme

@Composable
fun MarketItemCard(item: Item, modifier: Modifier = Modifier){
    Card(
        modifier = modifier.width(350.dp).height(270.dp)
    ) {
        Column{
//            Image(
//                painter = painterResource(id = item.imageId),
//                contentDescription = "image",
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(120.dp),
//                contentScale = ContentScale.Crop
//            )
            Text(
                text = "$${item.price}",
                modifier = Modifier.padding(start = 13.dp, end = 10.dp, top = 10.dp, bottom = 7.dp),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "${item.name}",
                modifier = Modifier.padding(start = 13.dp, end = 10.dp, top = 3.dp, bottom = 3.dp),
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = "${item.quantity} lb",
                modifier = Modifier.padding(start = 13.dp, end = 10.dp, top = 0.dp, bottom = 10.dp),
                style = MaterialTheme.typography.bodySmall,
            )
            if(item.quantity != 0){
                Image(
                    painter = painterResource(R.drawable.plus_sign),
                    contentDescription = "image",
                    modifier = Modifier
                        .width(40.dp)
                        .height(40.dp)
                        .padding(5.dp, 0.dp),
                    contentScale = ContentScale.Fit
                )
                Text(
                    text = "Sold 1lb",
                    modifier = Modifier.padding(start = 0.dp, end = 0.dp, top = 0.dp, bottom = 0.dp),
                    style = TextStyle(
                        fontSize = 15.sp,
                        color = Color.Blue,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketScreen(){
    val viewModel = viewModel<MainViewModel>()
    val searchText by viewModel.searchText.collectAsState()
    val theFoodItems by viewModel.items.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ){
        Row{
            TextField(
                value = searchText,
                onValueChange = viewModel::onSearchTextChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {Text(text = "Search")},
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 128.dp)
        ) {
            items(theFoodItems.size) { item ->
                MarketItemCard(
                    item = theFoodItems.get(item),
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(150.dp))

        Row(verticalAlignment = Alignment.Bottom) {
            Spacer(modifier = Modifier.width(150.dp))
            Text(
                text = "Total earning on June 26:",
                modifier = Modifier.padding(8.dp, 3.dp, 5.dp, 0.dp),
                style = TextStyle(
                    fontSize = 30.sp,
                    color = Color.Blue,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = "   610 CAD",
                modifier = Modifier.padding(8.dp, 3.dp, 5.dp, 0.dp),
                style = TextStyle(
                    fontSize = 40.sp,
                    color = Color.Cyan,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }


}

@Composable
fun MarketplaceScreen(){
    FarmConnectTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            MarketScreen()
        }
    }
}