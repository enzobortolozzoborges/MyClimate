package br.androidapp.myclimate.dependency_injection

import br.androidapp.myclimate.network.repository.WeatherDataRepository
import org.koin.dsl.module

val repositoryModule = module {
    single { WeatherDataRepository() }
}