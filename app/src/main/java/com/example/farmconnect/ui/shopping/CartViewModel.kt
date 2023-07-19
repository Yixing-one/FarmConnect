package com.example.farmconnect.ui.shopping

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

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
}