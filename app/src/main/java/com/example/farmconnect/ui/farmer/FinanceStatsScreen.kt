package com.example.farmconnect.ui.farmer

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.farmconnect.ui.theme.FarmConnectTheme
import com.example.farmconnect.ui.theme.lightGreen

//credits: https://maneesha-erandi.medium.com/kotlin-with-jetpack-compose-data-tables-c28faf4334d9

data class TableItem(
    val name: String,
    val quantity: Double,
    val date: String,
    val revenue: Double
)

val data = listOf(
    TableItem("Apples", 38.5, "21/04/23", 23.5),
    TableItem("Oranges", 24.5, "16/06/23", 12.4),
    TableItem("Bananas", 10.0, "14/06/23", 10.32),
    TableItem("Potatoes", 30.5, "23/04/23", 45.3),
    TableItem("Mango", 21.5, "23/05/23", 34.3),
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
                    text = "Quantity (lbs)",
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
        itemsIndexed(data) { _, item ->
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
                    text = String.format("%.2f", item.quantity),
                    alignment = TextAlign.Left,
                    weight = .15f
                )
                Cell(
                    text = item.date,
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
    fun totalRevenue(): Double{
        var total: Double = 0.0
        data.forEach { item  ->
            total += item.revenue
        }
        return total
    }
    fun totalQuantity(): Double {
        var total: Double = 0.0
        data.forEach { item  ->
            total += item.quantity
        }
        return total
    }
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



@Preview(showBackground = true)
@Composable
fun FinanceScreenPreview() {
    FarmConnectTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            FinanceStatsScreen()
        }
    }
}