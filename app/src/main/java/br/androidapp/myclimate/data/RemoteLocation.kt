package br.androidapp.myclimate.data

import android.R

data class RemoteLocation(
    val name: String,
    val region: String,
    val country: String,
    val lat: Double,
    val lon: Double,
)
