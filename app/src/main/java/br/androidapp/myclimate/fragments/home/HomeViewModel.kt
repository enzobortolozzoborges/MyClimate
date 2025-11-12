package br.androidapp.myclimate.fragments.home

import android.location.Geocoder
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.androidapp.myclimate.data.CurrentLocation
import br.androidapp.myclimate.data.CurrentWeather
// Import 'Forecast' já estava correto no seu arquivo
import br.androidapp.myclimate.data.Forecast
import br.androidapp.myclimate.data.LiveDataEvent
import br.androidapp.myclimate.network.repository.WeatherDataRepository
import com.google.android.gms.location.FusedLocationProviderClient
import kotlinx.coroutines.launch

class HomeViewModel(private val weatherDataRepository: WeatherDataRepository) : ViewModel() {

    //region Current Location
    private val _currentLocation = MutableLiveData<LiveDataEvent<CurrentLocationDataState>>()
    val currentLocation: LiveData<LiveDataEvent<CurrentLocationDataState>> get() = _currentLocation


    // FUNÇÃO ATUALIZADA
    fun getCurrentLocation(
        fusedLocationProviderClient: FusedLocationProviderClient,
        geocoder: Geocoder
    ) {
        viewModelScope.launch { // Coroutine principal
            emitCurrentLocationUiState(isLoading = true)

            weatherDataRepository.getCurrentLocation(
                fusedLocationProviderClient = fusedLocationProviderClient,
                onSuccess = { currentLocation ->
                    // MUDANÇA: Novo 'launch' para chamar a 'suspend fun' do repositório
                    viewModelScope.launch {
                        val updatedLocation = weatherDataRepository.updateAddressText(currentLocation, geocoder)
                        emitCurrentLocationUiState(currentLocation = updatedLocation)
                    }
                },
                onFailure = {
                    emitCurrentLocationUiState(error = "Unable to fetch current location")
                }
            )
        }
    }

    // A função 'private fun updateAddressText' foi REMOVIDA daqui.
    // Ela agora existe (como 'suspend fun') dentro do WeatherDataRepository.

    private fun emitCurrentLocationUiState(
        isLoading: Boolean = false,
        currentLocation: CurrentLocation? = null,
        error: String? = null
    ) {
        val currentLocationDataState = CurrentLocationDataState(isLoading, currentLocation, error)
        _currentLocation.value = LiveDataEvent(currentLocationDataState)

    }

    data class CurrentLocationDataState(
        val isLoading: Boolean,
        val currentLocation: CurrentLocation?,
        val error: String?
    )
    //endregion

    // ==========================================================
    // NOTA: Toda a sua lógica de Weather Data (abaixo)
    // foi mantida EXATAMENTE como você enviou.
    // ==========================================================

    //region Weather Data
    private val _weatherData = MutableLiveData<LiveDataEvent<WeatherDataState>>()
    val weatherData: LiveData<LiveDataEvent<WeatherDataState>> get() = _weatherData

    fun getWeatherData(latitude: Double, longitude: Double) {
        viewModelScope.launch {

            emitWeatherDataUiState(isLoading = true)

            weatherDataRepository.getWeatherData(latitude, longitude)?.let { weatherData ->

                // 1. Mapear o Clima Atual (como antes)
                val currentWeather = CurrentWeather(
                    icon = weatherData.current.condition.icon,
                    temperature = weatherData.current.temperature,
                    wind = weatherData.current.wind,
                    humidity = weatherData.current.humidity,
                    chanceOfRain = weatherData.forecast.forecastDay.first().day.chanceOfRain
                )

                // 2. ADICIONADO: Mapear a Lista de Previsão por Hora
                // A API nos dá a lista 'hour'
                val forecastList = weatherData.forecast.forecastDay.first().hour.map { forecastHour ->
                    Forecast(
                        time = forecastHour.time,
                        temperature = forecastHour.temperature,
                        feelsLikeTemperature = forecastHour.feelsLikeTemperature,
                        icon = forecastHour.condition.icon
                    )
                }

                // 3. ADICIONADO: Enviar *ambos* para a UI
                emitWeatherDataUiState(
                    currentWeather = currentWeather,
                    forecast = forecastList // Passa a nova lista
                )

            } ?: emitWeatherDataUiState(error = "Unable to fetch weather data")
        }
    }

    private fun emitWeatherDataUiState(
        isLoading: Boolean = false,
        currentWeather: CurrentWeather? = null,
        // ADICIONADO: Novo parâmetro para a lista de previsão
        forecast: List<Forecast>? = null,
        error: String? = null
    ) {
        // ADICIONADO: Passa a lista de previsão para o DataState
        val weatherDataState = WeatherDataState(isLoading, currentWeather, forecast, error)
        _weatherData.value = LiveDataEvent(weatherDataState)
    }

    data class WeatherDataState(
        val isLoading: Boolean,
        val currentWeather: CurrentWeather?,
        // ADICIONADO: Novo campo para a lista de previsão
        val forecast: List<Forecast>?,
        val error: String?
    )
    //endregion
}