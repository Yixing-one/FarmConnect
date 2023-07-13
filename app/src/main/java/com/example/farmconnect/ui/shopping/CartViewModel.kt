package com.example.farmconnect.ui.shopping

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class CartViewModel: ViewModel() {
    private val _items = mutableStateListOf<Item>()
    private val _map = mutableMapOf<Int, Int>()
    val items : List<Item> = _items

    private var total = MutableStateFlow(0)
    val count = total.asStateFlow()


    fun addToCart(item: Item){
        _items.add(item);
    }

    fun decrementFromCart(item: Item){
        _items.remove(item)
    }

    fun deleteFromCart(item: Item){
        _items.removeAll { it == item }
    }
}