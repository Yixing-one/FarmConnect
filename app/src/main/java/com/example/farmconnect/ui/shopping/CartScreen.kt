package com.example.farmconnect.ui.shopping

import android.util.Log
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
import androidx.compose.material3.Button
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
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.farmconnect.R
import com.example.farmconnect.ui.theme.FarmConnectTheme
import kotlinx.coroutines.launch

@Composable
fun CartItem(item: MarketplaceItem, quantity:Int, cartViewModel: CartViewModel){
    fun isEnabled(): Boolean {
        val grouped = cartViewModel.items.groupBy { it.id }
        if(!grouped.containsKey(item.id)){
            return true;
        }
        val addedQuantity = grouped[item.id]?.size
        if (addedQuantity != null) {
            return addedQuantity <= item.quantityRemaining - 1
        }
        return true;
    }
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
                Text(text = "${item.quantityRemaining} lbs available", style = MaterialTheme.typography.titleMedium)
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
                        Text(text = "$quantity lb", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 5.dp, end = 5.dp))
                        IconButton(onClick = { cartViewModel.addToCart(item) },
                            Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .padding(0.dp)
                                .size(24.dp)
                                .background(color = Color.White),
                            enabled = isEnabled()
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

fun callCheckout(cartViewModel: CartViewModel){
    cartViewModel.viewModelScope.launch {
        cartViewModel.checkOut()
    }
}
fun LazyListScope.CartTotal(cartViewModel: CartViewModel){

    val groupedItems = cartViewModel.items.groupBy { it.id }
    fun getTotal(): String {
        var total : Double = 0.0;
        groupedItems.forEach { entry ->
            val quantity = entry.value.size;
            total += entry.value.first().price * quantity;
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

            Button(onClick = { callCheckout(cartViewModel) } , modifier = Modifier.fillMaxWidth()) {
                Text(text = "Checkout", style = MaterialTheme.typography.titleLarge)

        }

    }
}

@Composable
fun CartScreen(cartViewModel: CartViewModel){
    val grouped = cartViewModel.items.groupBy { it.id }
    val cartItems = mutableListOf<MarketplaceItem>();
    grouped.forEach { entry ->
        cartItems.add(entry.value.first());
        print("${entry.key} : ${entry.value}")
    }

    Surface(modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 12.dp)
    ) {
        Column {
            Text("Your cart", style = MaterialTheme.typography.titleLarge,)
            Spacer(modifier = Modifier.height(10.dp))
            if (grouped.isEmpty()){
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
                    items(cartItems.size) { item ->
                        grouped[cartItems[item].id]?.let { CartItem(item = cartItems[item], quantity = it.size, cartViewModel) }

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