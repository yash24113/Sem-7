package com.example.mybyk

data class Area(
    val documentId: String,
    val areaId: Int,
    val areaName: String,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)
