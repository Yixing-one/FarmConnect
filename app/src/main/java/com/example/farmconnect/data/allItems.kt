package com.example.farmconnect.data

import android.graphics.Bitmap
import android.graphics.Picture
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.farmconnect.R
import com.example.farmconnect.model.Item
//import com.example.farmconnect.ui.farmer.Item

@RequiresApi(Build.VERSION_CODES.P)
val allItems = listOf(
    Item(
        name = "Carrots",
        price = 1.55,
//        imageId = R.drawable.bell_pepper,
        quantity = 71,
        imageBitmap = Bitmap.createBitmap(Picture())

    )
)

