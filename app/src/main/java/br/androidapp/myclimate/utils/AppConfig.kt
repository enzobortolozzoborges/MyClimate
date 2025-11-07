package br.androidapp.myclimate.utils

import android.app.Application
import br.androidapp.myclimate.dependency_injection.repositoryModule
import br.androidapp.myclimate.dependency_injection.viewModelModule
import org.koin.core.context.startKoin

class AppConfig : Application() {
    override fun onCreate(){
        super.onCreate()
        startKoin {
            modules(listOf(repositoryModule, viewModelModule))
        }
    }
}