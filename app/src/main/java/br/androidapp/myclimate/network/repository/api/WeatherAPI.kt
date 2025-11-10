package br.androidapp.myclimate.network.repository.api

import android.R
import br.androidapp.myclimate.data.RemoteLocation
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherAPI {

    companion object {
        const val BASE_URL = "https://api.weatherapi.com/v1"
        const val API_KEY = "7e333745e5cc4bc796e11033251011"
    }

    @GET("search.json")
    suspend fun searchLocation(
        @Query ("key") key:String = API_KEY,
        @Query ("q") query:String
    ): retrofit2.Response<List<RemoteLocation>>
}