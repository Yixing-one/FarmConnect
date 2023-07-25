package com.example.farmconnect.data


import android.graphics.Bitmap

data class InventoryItemCharity(
    val documentID: String,
    val name: String,
    val price: Double,
    val quantity: Int,
    val imageBitmap: Bitmap
){}

object InventoryItemsCharity {
    var item_list = mutableListOf<InventoryItemCharity>()
    var update_item_list = mutableListOf<InventoryItemCharity>()

    fun addItem(docId: String, name: String, price: Double, quantity:Int, imageBitmap:Bitmap) {
        item_list.add(
            InventoryItemCharity(
                documentID = docId,
                name = name,
                price = price,
                quantity = quantity,
                imageBitmap = imageBitmap
            )
        )
    }
}