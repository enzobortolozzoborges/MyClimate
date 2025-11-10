package br.androidapp.myclimate.dependency_injection

import br.androidapp.myclimate.fragments.home.HomeViewModel
import br.androidapp.myclimate.fragments.location.LocationViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { HomeViewModel(weatherDataRepository = get()) }
    viewModel { LocationViewModel(weatherDataRepository = get()) }
}