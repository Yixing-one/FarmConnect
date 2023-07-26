package com.example.farmconnect.model

import android.graphics.Bitmap
import android.graphics.Picture
import androidx.annotation.DrawableRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.farmconnect.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class MainViewModel: ViewModel() {
    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    private val _items = MutableStateFlow(allItems)
    val items = searchText
        .combine(_items){ text, items ->
            if(text.isBlank()){
                items
            }
            else{
                items.filter{
                    it.doesMatchSearchQuery(text)
                }
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            _items.value
        )

    fun onSearchTextChange(text: String){
        _searchText.value = text
    }

}

data class Item(
    val name: String,
    val price: Double,
    val quantity: Int,
    val imageBitmap: Bitmap

//    @DrawableRes val imageId: Int
) {

    fun doesMatchSearchQuery(query: String): Boolean {
        val matchingCombinations = listOf(
            "$name",
        )
        return matchingCombinations.any {
            it.contains(query, ignoreCase = true)
        }
    }
}

private val allItems = listOf(
    Item(
        name = "Green Apple",
        price = 4.55,
                quantity = 71,
        imageBitmap = Bitmap.createBitmap(Picture())
    ),
    Item(
        name = "Red Apple",
        price = 3.99,
                quantity = 71,
        imageBitmap = Bitmap.createBitmap(Picture())
    ),
    Item(
        name = "Mango",
        price = 15.34,
                quantity = 71,
        imageBitmap = Bitmap.createBitmap(Picture())
    ),
    Item(
        name = "Carrot",
        price = 8.25,
        quantity = 71,
        imageBitmap = Bitmap.createBitmap(Picture())
    ),
)