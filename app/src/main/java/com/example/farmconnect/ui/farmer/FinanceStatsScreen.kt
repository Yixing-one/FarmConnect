package com.example.farmconnect.ui.farmer

import android.content.ContentValues.TAG
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.farmconnect.ui.shopping.MarketplaceItem
import com.example.farmconnect.ui.theme.FarmConnectTheme
import com.example.farmconnect.ui.theme.lightGreen
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale

//credits: https://maneesha-erandi.medium.com/kotlin-with-jetpack-compose-data-tables-c28faf4334d9

class FinanceViewModel: ViewModel() {
    private val db = Firebase.firestore
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid.toString()

    private val _items = MutableStateFlow<List<TableItem>>(listOf())
    val items: StateFlow<List<TableItem>> = _items

    init {
        viewModelScope.launch {
            loadItems()
        }
    }

    private suspend fun loadItems() {
        val financeItems = db.collection("finance")
            .whereEqualTo("userId", currentUserId)
            .get()
            .await().documents[0].data?.getValue("items") as List<HashMap<*,*>>

        val itemsList = mutableListOf<TableItem>()
        for (item in financeItems) {
            val item = TableItem(
                name = item["name"].toString(),
                quantitySold = item["quantitySold"] as? Double ?: 0.0,
                dateSold = item["dateSold"] as? Timestamp ?: Timestamp.now(),
                revenue = item["revenue"] as? Double ?: 0.0
            )
            itemsList.add(item)
        }

        _items.emit(itemsList)
    }
}

data class TableItem(
    val name: String,
    val quantitySold: Double,
    val dateSold: Timestamp,
    val revenue: Double
)

@Composable
fun RowScope.Cell(
    text: Any,
    alignment: TextAlign = TextAlign.Center,
    weight: Float,
    title: Boolean = false
) {
    Text(
        text = text.toString(),
        Modifier
            .padding(5.dp)
            .weight(weight),
        fontWeight = if (title) FontWeight.Bold else FontWeight.Normal,
        textAlign = alignment,
    )
}


@Composable
fun DataTable(){

    val viewModel = viewModel<FinanceViewModel>()
    val financeItems by viewModel.items.collectAsState()

    LazyColumn(Modifier.padding(vertical = 10.dp)){
        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ){
                Cell(
                    text = "Item",
                    alignment = TextAlign.Left,
                    weight = .2f,
                    title = true
                )
                Cell(
                    text = "Quantity Sold (lbs)",
                    alignment = TextAlign.Center,
                    weight = .25f,
                    title = true
                )
                Cell(
                    text = "Date sold",
                    alignment = TextAlign.Center,
                    weight = .3f,
                    title = true
                )
                Cell(
                    text = "Revenue",
                    alignment = TextAlign.Right,
                    weight = .25f,
                    title = true
                )
            }
            Divider(
                color = Color.LightGray,
                modifier = Modifier
                    .height(1.dp)
                    .fillMaxHeight()
                    .fillMaxWidth()
            )
        }
        itemsIndexed(financeItems) { _, item ->
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Cell(
                    text = item.name,
                    alignment = TextAlign.Left,
                    weight = .22f
                )
                Cell(
                    text = String.format("%.2f", item.quantitySold),
                    alignment = TextAlign.Left,
                    weight = .15f
                )
                Cell(
                    text = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(item.dateSold.toDate()),
                    alignment = TextAlign.Center,
                    weight = .3f
                )
                Cell(
                    text = '$' + String.format("%.2f", item.revenue),
                    alignment = TextAlign.Right,
                    weight = .2f
                )
            }
            Divider(
                color = Color.LightGray,
                modifier = Modifier
                    .height(1.dp)
                    .fillMaxHeight()
                    .fillMaxWidth()
            )
        }
    }
}

@Composable
fun FinanceStatsScreen(){

    val viewModel = viewModel<FinanceViewModel>()
    val financeItems by viewModel.items.collectAsState()


    fun totalRevenue(): Double{
        var total: Double = 0.0
        financeItems.forEach { item  ->
            total += item.revenue
        }
        return total
    }
    fun totalQuantity(): Double {
        var total: Double = 0.0
        financeItems.forEach { item  ->
            total += item.quantitySold
        }
        return total
    }

    if (financeItems.isNotEmpty()) {
        try {
            val firstItem = financeItems[0]
            Log.d(TAG, "FinanceItems is not empty")
            Log.d(TAG, firstItem.toString())
        } catch (e: Exception) {
            Log.e(TAG, "Error accessing financeItems: ${e.message}", e)
        }

        Surface(modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 10.dp)
        ){
            Column(){
                Row(Modifier.fillMaxWidth()){
                    Card(
                        colors = CardDefaults.cardColors(containerColor = lightGreen),
                        modifier = Modifier
                            .weight(0.5f)
                            .padding(end = 10.dp),

                        ) {
                        Column(modifier = Modifier
                            .padding(10.dp)
                            .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ){
                            Text(text = "Total Revenue", color = Color.Black)
                            Text(text = '$' + String.format("%.2f", totalRevenue()), fontSize = 27.sp, fontWeight = FontWeight.Bold, color = Color.Black )
                        }
                    }
                    Card(
                        colors = CardDefaults.cardColors(containerColor = lightGreen),
                        modifier = Modifier
                            .weight(0.5f)
                            .padding(start = 10.dp)
                    ) {
                        Column(modifier = Modifier
                            .padding(10.dp)
                            .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ){
                            Text(text = "Total Quantity", color = Color.Black)
                            Text(text = String.format("%.0f", totalQuantity()) + " lbs", fontSize = 27.sp, fontWeight = FontWeight.Bold, color= Color.Black )
                        }
                    }
                }
                DataTable()
            }
        }
    }

}



@Preview(showBackground = true)
@Composable
fun FinanceScreenPreview() {
    FarmConnectTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            FinanceStatsScreen()
        }
    }
}