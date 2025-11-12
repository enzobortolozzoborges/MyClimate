package br.androidapp.myclimate.fragments.home

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.clearFragmentResultListener
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import br.androidapp.myclimate.R
import br.androidapp.myclimate.data.CurrentLocation
import br.androidapp.myclimate.databinding.FragmentHomeBinding
import br.androidapp.myclimate.storage.SharedPreferencesManager
import com.google.android.gms.location.LocationServices
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeFragment : Fragment() {

    companion object {
        const val REQUEST_KEY_MANUAL_LOCATION_SEARCH = "manualLocationSearch"
        const val KEY_LOCATION_TEXT = "locationText"
        const val KEY_LATITUDE = "latitude"
        const val KEY_LONGITUDE = "longitude"
    }

    // --- ViewBinding ---
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = requireNotNull(_binding)

    // --- ViewModel (via Koin) ---
    private val homeViewModel: HomeViewModel by viewModel()

    // --- Services auxiliares ---
    private val fusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(requireContext())
    }
    private val geocoder by lazy { Geocoder(requireContext()) }

    // --- UI / Adapter ---
    private val weatherDataAdapter = WeatherDataAdapter(
        onLocationClicked = { showLocationOptions() }
    )

    // --- Storage (via Koin) ---
    private val sharedPreferencesManager: SharedPreferencesManager by inject()

    // --- Permissão de localização ---
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            getCurrentLocation()
        } else {
            Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private var isInitialLocationSet: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setWeatherDataAdapter()
        setObservers()
        setListeners()

        // Tenta carregar a última localização guardada
        if (!isInitialLocationSet) {
            setCurrentLocation(currentLocation = sharedPreferencesManager.getCurrentLocation())
            isInitialLocationSet = true
        }
    }

    private fun setListeners() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            setCurrentLocation(sharedPreferencesManager.getCurrentLocation())
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        clearFragmentResultListener(REQUEST_KEY_MANUAL_LOCATION_SEARCH)
        _binding = null
    }

    // ----------------------------------------------------------------------

    private fun setObservers() {
        with(homeViewModel) {

            // Observador para a Localização
            currentLocation.observe(viewLifecycleOwner) {
                val state = it.getContentIfNotHandled() ?: return@observe

                if (state.isLoading) showLoading()

                state.currentLocation?.let { current ->
                    hideLoading()
                    // Guarda a localização mais recente
                    sharedPreferencesManager.saveCurrentLocation(current)
                    // Atualiza a UI e busca os dados de clima
                    setCurrentLocation(current)
                }

                state.error?.let { error ->
                    hideLoading()
                    Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
                }
            }

            // Observador para os Dados de Clima (Atual + Previsão)
            weatherData.observe(viewLifecycleOwner) {
                val weatherDataState = it.getContentIfNotHandled() ?: return@observe

                binding.swipeRefreshLayout.isRefreshing = weatherDataState.isLoading

                // Atualiza o Clima Atual
                weatherDataState.currentWeather?.let { currentWeather ->
                    weatherDataAdapter.setCurrentWeather(currentWeather)
                }

                // CORRIGIDO: O nome da função é "setForecast"
                weatherDataState.forecast?.let { forecastList ->
                    weatherDataAdapter.setForecast(forecastList)
                }

                // Mostra erro se houver
                weatherDataState.error?.let { error ->
                    Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setWeatherDataAdapter() {
        binding.weatherDataRecyclerView.itemAnimator = null
        binding.weatherDataRecyclerView.adapter = weatherDataAdapter
    }

    private fun setCurrentLocation(currentLocation: CurrentLocation? = null) {
        // Define a localização (ou o estado de "Escolha a localização")
        weatherDataAdapter.setCurrentLocation(currentLocation ?: CurrentLocation())
        // Se temos uma localização real, busca os dados de clima para ela
        currentLocation?.let { getWeatherData(currentLocation = it) }
    }


    private fun getCurrentLocation() {
        homeViewModel.getCurrentLocation(fusedLocationProviderClient, geocoder)
    }

    private fun isLocationPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun proceedWithCurrentLocation() {
        if (isLocationPermissionGranted()) {
            getCurrentLocation()
        } else {
            requestLocationPermission()
        }
    }

    private fun showLocationOptions() {
        val options = arrayOf("Current Location", "Search Manually")
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Choose Location Method")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> proceedWithCurrentLocation() // Pede GPS
                    1 -> startManualLocationSearch() // Abre a outra tela
                }
            }
            .show()
    }

    private fun showLoading() {
        with(binding) {
            weatherDataRecyclerView.visibility = View.GONE
            swipeRefreshLayout.isEnabled = false
            swipeRefreshLayout.isRefreshing = true
        }
    }

    private fun hideLoading() {
        with(binding) {
            weatherDataRecyclerView.visibility = View.VISIBLE
            swipeRefreshLayout.isEnabled = true
            swipeRefreshLayout.isRefreshing = false
        }
    }

    // --- Navegação para a tela de Busca Manual ---

    private fun startManualLocationSearch() {
        startListeningManualLocationSelection()
        // Navega para o LocationFragment
        findNavController().navigate(R.id.action_home_fragment_to_location_fragment)
    }

    private fun startListeningManualLocationSelection() {
        // Ouve por um resultado vindo do LocationFragment
        setFragmentResultListener(REQUEST_KEY_MANUAL_LOCATION_SEARCH) { _, bundle ->
            stopListeningManualLocationSelection()

            // Cria um novo objeto CurrentLocation com os dados da busca
            val currentLocation = CurrentLocation(
                location = bundle.getString(KEY_LOCATION_TEXT) ?: "N/A",
                latitude = bundle.getDouble(KEY_LATITUDE),
                longitude = bundle.getDouble(KEY_LONGITUDE)
            )

            // Salva a localização e atualiza a UI
            sharedPreferencesManager.saveCurrentLocation(currentLocation)
            setCurrentLocation(currentLocation)
        }
    }

    private fun stopListeningManualLocationSelection() {
        clearFragmentResultListener(REQUEST_KEY_MANUAL_LOCATION_SEARCH)
    }

    // --- Busca de Dados ---

    private fun getWeatherData(currentLocation: CurrentLocation) {
        // Garante que temos latitude e longitude antes de chamar a API
        if (currentLocation.latitude != null && currentLocation.longitude != null) {
            homeViewModel.getWeatherData(
                latitude = currentLocation.latitude,
                longitude = currentLocation.longitude
            )
        }
    }

}