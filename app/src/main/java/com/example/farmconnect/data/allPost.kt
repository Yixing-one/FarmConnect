package com.example.farmconnect.data

import com.example.farmconnect.R
import com.example.farmconnect.ui.charity.Post


 val allPosts = mutableListOf<Post>(
    Post(
        postId = 1,
        charity_name = "Food for All",
        charity_location = "123 Maple Street, Toronto, Ontario",
        charity_distance = 107.0,
        item_name = "Carrots",
        item_amount = 2.3,
        imageId = R.drawable.carrot
    ),
    Post(
        postId = 2,
        charity_name = "NutriHope",
        charity_location = "654 Spruce Avenue, Edmonton, Alberta",
        charity_distance = 150.0,
        item_name = "Tomatoes",
        item_amount = 3.1,
        imageId = R.drawable.tomatoes
    ),
    Post(
        postId = 3,
        charity_name = "Feed the Need",
        charity_location = "456 Elm Lane, Montreal, Quebec",
        charity_distance = 200.0,
        item_name = "Corn",
        item_amount = 0.4,
        imageId = R.drawable.corn
    ),
    Post(
        postId = 4,
        charity_name = "Bell Peppers",
        charity_location = "789 Oak Avenue, Vancouver, British Columbia",
        charity_distance = 382.0,
        item_name = "Carrots",
        item_amount = 39.0,
        imageId = R.drawable.bell_pepper
    ),
    Post(
        postId = 5,
        charity_name = "Full Bellies Foundation",
        charity_location = "789 Oak Avenue, Cityville, Canada",
        charity_distance = 399.0,
        item_name = "Potatoes",
        item_amount = 13.0,
        imageId = R.drawable.potatoes
    ),
    Post(
        postId = 6,
        charity_name = "FoodCare Network",
        charity_location = "123 Maple Street, Anytown, USA",
        charity_distance = 510.0,
        item_name = "Onions",
        item_amount = 7.0,
        imageId = R.drawable.onions
    ),
)