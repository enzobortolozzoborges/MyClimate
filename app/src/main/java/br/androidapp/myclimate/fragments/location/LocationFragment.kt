package br.androidapp.myclimate.fragments.location

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import br.androidapp.myclimate.data.RemoteLocation
import br.androidapp.myclimate.databinding.FragmentLocationBinding
import br.androidapp.myclimate.fragments.home.HomeFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class LocationFragment : Fragment() {
    private var _binding: FragmentLocationBinding? = null
    private val binding get() = requireNotNull(_binding)

    // 1. Injetar o ViewModel e inicializar o Adapter
    private val locationViewModel: LocationViewModel by viewModel()
    private val locationsAdapter = LocationsAdapter(::onLocationClicked)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLocationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 2. Chamar as funções de configuração
        setListeners()
        setLocationsAdapter()
        setObservers()
    }

    private fun setListeners() {
        // Listener para fechar a tela
        binding.imageClose.setOnClickListener { findNavController().popBackStack() }

        // 3. Listener para o "Enter" (Search) do teclado no campo de busca
        binding.inputSearch.editText?.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = binding.inputSearch.editText?.text.toString()
                if (query.length > 2) {
                    locationViewModel.searchLocation(query)
                }
                true
            } else {
                false
            }
        }
    }

    // 4. Configurar o RecyclerView com o Adapter
    private fun setLocationsAdapter() {
        binding.locationsRecyclerVew.adapter = locationsAdapter
    }

    // 5. Observar as mudanças do ViewModel
    private fun setObservers() {
        locationViewModel.searchResult.observe(viewLifecycleOwner) { state ->

            // Gerencia o ProgressBar
            binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE

            // Popula o adapter com os locais encontrados
            state.locations?.let {
                binding.locationsRecyclerVew.visibility = View.VISIBLE
                locationsAdapter.setData(it) //
            }

            // Exibe um erro, se houver
            state.error?.let {
                binding.locationsRecyclerVew.visibility = View.GONE
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 6. O que acontece ao clicar em um local da lista
    private fun onLocationClicked(location: RemoteLocation) {

        // Prepara os dados para enviar de volta ao HomeFragment
        val result = bundleOf(
            HomeFragment.KEY_LOCATION_TEXT to "${location.name}, ${location.region}",
            HomeFragment.KEY_LATITUDE to location.lat,
            HomeFragment.KEY_LONGITUDE to location.lon
        )

        // Envia o resultado para o Fragment anterior (HomeFragment)
        setFragmentResult(HomeFragment.REQUEST_KEY_MANUAL_LOCATION_SEARCH, result)

        // Fecha a tela de busca
        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}