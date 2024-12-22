package com.example.mybyk


import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Package(
    val packageId: String = "",
    val packageName: String = "",
    val packageDesc: String = "",
    val packagePrice: Double = 0.0,
    val packagePrice1: Double = 0.0,
    val packageValidity: String = ""
) : Parcelable

