package br.androidapp.myclimate.network.repository

import android.annotation.SuppressLint
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import br.androidapp.myclimate.data.CurrentLocation
import android.location.Geocoder
import br.androidapp.myclimate.data.RemoteLocation
import br.androidapp.myclimate.data.RemoteWeatherData
import br.androidapp.myclimate.network.repository.api.WeatherAPI
import retrofit2.http.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class WeatherDataRepository (private val weatherAPI: WeatherAPI){


    @SuppressLint("MissingPermission")
    fun getCurrentLocation(
        fusedLocationProviderClient: FusedLocationProviderClient,
        onSuccess: (currentLocation: CurrentLocation) -> Unit,
        onFailure: () -> Unit
    ) {
        fusedLocationProviderClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            CancellationTokenSource().token
        ).addOnSuccessListener { location ->
            if (location == null) {
                onFailure()
            } else {
                onSuccess(
                    CurrentLocation(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        date = "Today"
                    )
                )
            }
        }.addOnFailureListener {
            onFailure()
        }
    }



    @Suppress("DEPRECATION")
    // FUNÇÃO ATUALIZADA - agora é 'suspend fun'
    suspend fun updateAddressText(
        currentLocation: CurrentLocation,
        geocoder: Geocoder
    ): CurrentLocation {
        val latitude = currentLocation.latitude ?: return currentLocation
        val longitude = currentLocation.longitude ?: return currentLocation

        // MUDANÇA: Movido para a thread de IO (background)
        return withContext(Dispatchers.IO) {
            try {
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)

                if (addresses.isNullOrEmpty()) {
                    return@withContext currentLocation.copy(location = "Localização desconhecida")
                }

                val address = addresses[0]

                // MUDANÇA: Lógica de lista para evitar "null"
                val locationParts = mutableListOf<String>()

                val city = address.locality ?: address.subAdminArea
                if (!city.isNullOrBlank()) {
                    locationParts.add(city)
                }

                if (!address.adminArea.isNullOrBlank()) {
                    locationParts.add(address.adminArea)
                }

                if (!address.countryName.isNullOrBlank()) {
                    locationParts.add(address.countryName)
                }

                val finalAddressText = locationParts.joinToString(", ")

                currentLocation.copy(
                    location = if (finalAddressText.isBlank()) "Localização desconhecida" else finalAddressText
                )
            } catch (e: Exception) {
                // MUDANÇA: Tratamento de erro (ex: offline)
                currentLocation.copy(location = "Erro ao buscar endereço")
            }
        }
    }

    suspend fun searchLocation(query: String): List<RemoteLocation>? {
        val response = weatherAPI.searchLocation(query = query)
        return if (response.isSuccessful) response.body() else null
    }

    suspend fun getWeatherData(latitude: Double, longitude: Double): RemoteWeatherData? {
        val response = weatherAPI.getWeatherData(query = "$latitude, $longitude")
        return if (response.isSuccessful) response.body() else null
    }


}