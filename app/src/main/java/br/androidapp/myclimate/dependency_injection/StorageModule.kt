package br.androidapp.myclimate.dependency_injection

import br.androidapp.myclimate.storage.SharedPreferencesManager
import org.koin.dsl.module

val storageModule = module{
    single { SharedPreferencesManager(context = get(), gson = get()) }
}