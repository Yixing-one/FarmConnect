package com.example.farmconnect.data

import com.example.farmconnect.R

data class Item(
    val id: Int,
    val name: String,
    val price: Double,
    val quantity: Int,
    val imageId: Int

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

val postedItems = listOf(
    Item(
        id = 1,
        name = "Carrots",
        price = 1.55,
        quantity = 71,
        imageId = R.drawable.carrot
    ),
    Item(
        id = 2,
        name = "Tomatoes",
        price = 3.89,
        quantity = 93,
        imageId = R.drawable.tomatoes
    ),
    Item(
        id = 3,
        name = "Corn",
        price = 2.29,
        quantity = 87,
        imageId = R.drawable.corn
    ),
    Item(
        id = 4,
        name = "Bell Peppers",
        price = 0.99,
        quantity = 22,
        imageId = R.drawable.bell_pepper
    ),
    Item(
        id = 5,
        name = "Potatoes",
        price = 1.25,
        quantity = 103,
        imageId = R.drawable.potatoes
    ),
    Item(
        id = 6,
        name = "Onions",
        price = 4.23,
        quantity = 98,
        imageId = R.drawable.onions
    ),
    Item(
        id = 7,
        name = "Ice Lettuce",
        price = 1.46,
        quantity = 19,
        imageId = R.drawable.iceberg_lettuce
    ),
    Item(
        id = 8,
        name = "Cucumbers",
        price = 5.89,
        quantity = 34,
        imageId = R.drawable.cucumber
    ),
    Item(
        id = 9,
        name = "Rom Lettuce",
        price = 3.29,
        quantity = 45,
        imageId = R.drawable.romaine_lettuce
    ),
    Item(
        id = 10,
        name = "Bell Peppers",
        price = 0.99,
        quantity = 22,
        imageId = R.drawable.bell_pepper
    ),
    Item(
        id = 11,
        name = "Potatoes",
        price = 1.25,
        quantity = 103,
        imageId = R.drawable.potatoes
    ),
    Item(
        id = 12,
        name = "Onions",
        price = 4.23,
        quantity = 98,
        imageId = R.drawable.onions
    ),
    Item(
        id = 13,
        name = "Ice Lettuce",
        price = 1.46,
        quantity = 19,
        imageId = R.drawable.iceberg_lettuce
    ),
    Item(
        id = 14,
        name = "Cucumbers",
        price = 5.89,
        quantity = 34,
        imageId = R.drawable.cucumber
    ),
    Item(
        id = 15,
        name = "Rom Lettuce",
        price = 3.29,
        quantity = 45,
        imageId = R.drawable.romaine_lettuce
    ),
)