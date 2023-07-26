package com.example.farmconnect.ui.shopping

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.farmconnect.ui.farmer.TableItem
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

class CartViewModel: ViewModel() {
    private val _items = mutableStateListOf<MarketplaceItem>()
    private val _map = mutableMapOf<String, MutableList<MarketplaceItem>>()
    val items : List<MarketplaceItem> = _items



    fun addToCart(item: MarketplaceItem){
        _items.add(item)
    }

    fun decrementFromCart(item: MarketplaceItem){
        _items.remove(item)
    }

    fun deleteFromCart(item: MarketplaceItem){
        _items.removeAll { it.id == item.id }
    }

    suspend fun checkOut(){
        val grouped = _items.groupBy { it.id }

        val db = Firebase.firestore
        val financeColRef = db.collection("finance");
        val marketplaceColRef = db.collection("marketplace")
        grouped.forEach { entry ->
            val item = entry.value.first()
            val quantity = entry.value.size
            val revenue = item.price * quantity;
            val data = hashMapOf(
                "name" to item.name,
                "quantitySold" to quantity,
                "dateSold" to Timestamp.now(),
                "revenue" to revenue
            )

            // check if farmer id exists in finance
            if(financeColRef.whereEqualTo("userId", item.userId).limit(1).get().await().documents.size == 0){
                // add new field for this farmer
                financeColRef.add(
                    hashMapOf("items" to listOf(data), "userId" to item.userId)
                );

            } else {
                //edit existing field
                val id = financeColRef.whereEqualTo("userId", item.userId).get().await().documents.first().id;
                financeColRef.document(id).update("items", FieldValue.arrayUnion(data));

            }

            //update marketplace fields
            val id = marketplaceColRef.whereEqualTo("userId", item.userId).whereEqualTo("name", item.name).get().await().documents.first().id;
            if(id != null){
                val docData = marketplaceColRef.document(id).get().await().data;
                var quantRemaining = docData?.getValue("quantityRemaining").toString().toInt() ;
                var quantSold = docData?.getValue("quantitySold").toString().toInt() ;
                quantRemaining -= quantity;
                quantSold += quantity;
                marketplaceColRef.document(id).update("quantityRemaining", quantRemaining).await();
                marketplaceColRef.document(id).update("quantitySold", quantSold).await();
            }


        }

        _items.clear();



    }
}