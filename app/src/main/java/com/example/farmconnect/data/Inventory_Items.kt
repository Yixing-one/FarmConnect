package com.example.farmconnect.data

import android.graphics.Bitmap

data class Inventory_Item(
    val documentID: String,
    val name: String,
    val price: Double,
    val quantity: Int,
    val imageBitmap: Bitmap
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


object Inventory_Items {
    var item_list = mutableListOf<Inventory_Item>()
    var update_item_list = mutableListOf<Inventory_Item>()

    fun addItem(documentID: String, name: String, price: Double, quantity:Int, imageBitmap:Bitmap) {
        item_list.add(
            Inventory_Item(
                documentID = documentID,
                name = name,
                price = price,
                quantity = quantity,
                imageBitmap = imageBitmap
            )
        )
    }

}