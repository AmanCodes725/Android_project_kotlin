package com.example.myapplication.utils

import com.example.myapplication.R


object iconPicker {
    var icons = arrayOf(
        R.drawable.icon1,
        R.drawable.icon2,
        R.drawable.icon3,
        R.drawable.icon4,
        R.drawable.icon5,
        R.drawable.icon6
    )
    var index = 0

    fun geticon():Int{
        index = (index + 1) % icons.size
        return icons[index]
    }
}