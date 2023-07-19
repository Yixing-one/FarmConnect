package com.example.farmconnect.ui.shopping

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.farmconnect.R
import com.example.farmconnect.ui.theme.FarmConnectTheme

@Composable
fun CartItem(item: MarketplaceItem, quantity:Int, cartViewModel: CartViewModel){
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(3.dp)
            .clip(RoundedCornerShape(16.dp))
    ){
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ){
            val image: Painter = rememberAsyncImagePainter(model = item.imageBitmap)
            Image(
                modifier = Modifier
                    .size(80.dp, 80.dp)
                    .clip(RoundedCornerShape(16.dp)),
                painter = image,
                alignment = Alignment.CenterStart,
                contentDescription = "",
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(10.dp))
            
            Column {
                Row( Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween){
                    Text(text = item.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    IconButton(
                        onClick = { cartViewModel.deleteFromCart(item) },
                        modifier = Modifier
                            .padding(0.dp)
                            .size(23.dp)
                    )
                    {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Delete from Cart",
                            Modifier
                                .size(25.dp)
                                .padding(0.dp)

                        )
                    }
                }
                Text(text = "${item.quantityRemaining} lbs", style = MaterialTheme.typography.titleMedium)
                Row( Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Row {
                        Text(text = "Quantity: ", style = MaterialTheme.typography.titleMedium)
                        IconButton(onClick = { cartViewModel.decrementFromCart(item) },
                            Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .padding(0.dp)
                                .size(23.dp)
                                .background(color = Color.White)
                        )
                        {
                            Icon(
                                painter = painterResource(id = R.drawable.imgbin_dash_computer_icons_png),
                                contentDescription = "Add to Cart",
                                Modifier
                                    .size(18.dp)
                                    .width(13.dp)
                                    .clip(
                                        RoundedCornerShape(16.dp)
                                    )
                            )
                        }
                        Text(text = "$quantity", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 5.dp, end = 5.dp))
                        IconButton(onClick = { cartViewModel.addToCart(item) },
                            Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .padding(0.dp)
                                .size(24.dp)
                                .background(color = Color.White)
                        )
                        {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Add to Cart",
                                Modifier
                                    .size(21.dp)
                                    .clip(
                                        RoundedCornerShape(16.dp)
                                    )
                            )
                        }

                    }
                    Text(text = "$ ${item.price}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                }
                
            }
        }
    }
}

fun LazyListScope.CartTotal(cartViewModel: CartViewModel){
    val allItems = cartViewModel.items
    val items = cartViewModel.items.toSet().toList()
    fun getTotal(): String {
        var total : Double = 0.0;
        items.forEach { item ->
            val quantity = allItems.count {it == item}
            total += item.price * quantity;
        }
        return String.format("%.2f", total)
    }
    items(1){
        Spacer(modifier = Modifier.height(10.dp))
        Divider()
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "Total", style = MaterialTheme.typography.titleLarge)
            Text(text = "$ ${getTotal()}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun CartScreen(cartViewModel: CartViewModel){
    val allItems = cartViewModel.items
    val items = cartViewModel.items.toSet().toList()

    Surface(modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 12.dp)
    ) {
        Column {
            Text("Your cart", style = MaterialTheme.typography.titleLarge,)
            Spacer(modifier = Modifier.height(10.dp))
            if (items.isEmpty()){
                Card(
                    Modifier
                        .fillMaxWidth()
                        .height(100.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Your cart is empty", style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 25.dp))
                    }

                }
                
            } else {
                LazyColumn {
                    items(items.size) { item ->
                        CartItem(item = items[item], quantity = allItems.count { it == items[item]}, cartViewModel)

                    }
                    CartTotal(cartViewModel = cartViewModel)
                }

            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CartScreenPreview(){
    val cartViewModel = viewModel<CartViewModel>();
    FarmConnectTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            CartScreen(cartViewModel)
        }
    }
}