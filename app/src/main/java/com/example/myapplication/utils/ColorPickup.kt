package com.example.myapplication.utils

object ColorPickup{
    val colors = arrayOf(
        "#FF5733",  // Red-Orange
        "#33FF57",  // Green
        "#3357FF",  // Blue
        "#FF33A1",  // Pink
        "#8A33FF",  // Purple
        "#33FFF5"   // Aqua
    )
    var currentColor = 0

    fun getColor():String{
        currentColor = (currentColor + 1) % colors.size
        return colors[currentColor]
    }

}
