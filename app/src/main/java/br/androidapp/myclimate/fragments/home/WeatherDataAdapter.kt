package br.androidapp.myclimate.fragments.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import br.androidapp.myclimate.data.CurrentLocation
import br.androidapp.myclimate.data.CurrentWeather
import br.androidapp.myclimate.data.Forecast
import br.androidapp.myclimate.data.WeatherData
import br.androidapp.myclimate.databinding.ItemContainerCurrentLocationBinding
import br.androidapp.myclimate.databinding.ItemContainerCurrentWeatherBinding
import br.androidapp.myclimate.databinding.ItemContainerForecastBinding
import coil.load
import java.text.SimpleDateFormat
import java.util.Locale

// CORRIGIDO: A classe deve herdar de "RecyclerView.Adapter", não "ViewHolder"
class WeatherDataAdapter(
    private val onLocationClicked: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private companion object {
        // CORRIGIDO: Padronizando todos os nomes para "INDEX_"
        const val INDEX_CURRENT_LOCATION = 0
        const val INDEX_CURRENT_WEATHER = 1
        const val INDEX_FORECAST = 2

        // Formatadores de data
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    }

    private val weatherData = mutableListOf<WeatherData>()


    fun setCurrentLocation(currentLocation: CurrentLocation) {
        if (weatherData.isEmpty()) {
            // CORRIGIDO: Usando a constante padronizada
            weatherData.add(INDEX_CURRENT_LOCATION, currentLocation)
            notifyItemInserted(INDEX_CURRENT_LOCATION)
        } else {
            // CORRIGIDO: Usando a constante padronizada
            weatherData[INDEX_CURRENT_LOCATION] = currentLocation
            notifyItemChanged(INDEX_CURRENT_LOCATION)
        }
    }

    fun setCurrentWeather(currentWeather: CurrentWeather) {
        if (weatherData.getOrNull(INDEX_CURRENT_WEATHER) != null) {
            // CORRIGIDO: Usando a constante padronizada
            weatherData[INDEX_CURRENT_WEATHER] = currentWeather
            notifyItemChanged(INDEX_CURRENT_WEATHER)
        } else {
            // CORRIGIDO: Usando a constante padronizada
            weatherData.add(INDEX_CURRENT_WEATHER, currentWeather)
            notifyItemInserted(INDEX_CURRENT_WEATHER)
        }
    }

    fun setForecast(forecasts: List<Forecast>) {
        val oldForecastSize = weatherData.count { it is Forecast }
        if (oldForecastSize > 0) {
            // CORRIGIDO: Usando "INDEX_FORECAST" (que é 2)
            weatherData.subList(INDEX_FORECAST, weatherData.size).clear()
            notifyItemRangeRemoved(INDEX_FORECAST, oldForecastSize)
        }
        weatherData.addAll(forecasts)

        if (forecasts.isNotEmpty()) {
            // CORRIGIDO: Usando "INDEX_FORECAST" (que é 2)
            notifyItemRangeInserted(INDEX_FORECAST, forecasts.size)
        }
    }

    fun setForecastData(forecast: List<Forecast>) {
        weatherData.removeAll { it is Forecast }
        notifyItemRangeRemoved(INDEX_FORECAST, weatherData.size)
        weatherData.addAll(INDEX_FORECAST, forecast)
        notifyItemRangeChanged(INDEX_FORECAST, weatherData.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            INDEX_CURRENT_LOCATION -> CurrentLocationViewHolder(
                ItemContainerCurrentLocationBinding.inflate(inflater, parent, false)
            )
            INDEX_CURRENT_WEATHER -> CurrentWeatherViewHolder(
                ItemContainerCurrentWeatherBinding.inflate(inflater, parent, false)
            )
            INDEX_FORECAST -> ForecastViewHolder(
                ItemContainerForecastBinding.inflate(inflater, parent, false)
            )
            else -> throw IllegalArgumentException("ViewType inválido: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is CurrentLocationViewHolder -> holder.bind(weatherData[position] as CurrentLocation)
            is CurrentWeatherViewHolder -> holder.bind(weatherData[position] as CurrentWeather)
            is ForecastViewHolder -> holder.bind(weatherData[position] as Forecast)
        }
    }

    override fun getItemCount(): Int {
        return weatherData.size
    }

    override fun getItemViewType(position: Int): Int {
        // Esta função já estava correta
        return when (weatherData[position]) {
            is CurrentLocation -> INDEX_CURRENT_LOCATION
            is CurrentWeather -> INDEX_CURRENT_WEATHER
            is Forecast -> INDEX_FORECAST
        }
    }

    // --- ViewHolders Internos (sem alterações) ---

    inner class CurrentLocationViewHolder(
        private val binding: ItemContainerCurrentLocationBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(currentLocation: CurrentLocation) {
            with(binding) {
                textCurrentDate.text = currentLocation.date
                textCurrentLocation.text = currentLocation.location
                imageCurrentLocation.setOnClickListener { onLocationClicked() }
                textCurrentLocation.setOnClickListener { onLocationClicked() }
            }
        }
    }

    inner class CurrentWeatherViewHolder(
        private val binding: ItemContainerCurrentWeatherBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(currentWeather: CurrentWeather) {
            with(binding) {
                imageIcon.load("https:${currentWeather.icon}") { crossfade(true) }
                textTemperature.text = String.format("%.0f\u00B0C", currentWeather.temperature)
                textWind.text = String.format("%.0f km/h", currentWeather.wind)
                textHumidity.text = String.format("%s%%", currentWeather.humidity)
                textChanceOfRain.text = String.format("%s%%", currentWeather.chanceOfRain)
            }
        }
    }

    inner class ForecastViewHolder(
        private val binding: ItemContainerForecastBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(forecast: Forecast) {
            with(binding) {

                try {
                    val date = inputFormat.parse(forecast.time)
                    textTime.text = outputFormat.format(date)
                } catch (e: Exception) {
                    textTime.text = forecast.time
                }

                textTemperature.text = String.format("%.0f\u00B0C", forecast.temperature)
                textFeelsLikeTemperature.text =
                    String.format("Sensação: %.0f\u00B0C", forecast.feelsLikeTemperature)

                imageIcon.load("https:${forecast.icon}") {
                    crossfade(true)
                }
            }
        }
    }
}