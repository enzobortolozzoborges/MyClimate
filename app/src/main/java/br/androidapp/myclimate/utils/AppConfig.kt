package br.androidapp.myclimate.utils

import android.app.Application
import br.androidapp.myclimate.dependency_injection.networkModule
import br.androidapp.myclimate.dependency_injection.repositoryModule
import br.androidapp.myclimate.dependency_injection.serializerModule
import br.androidapp.myclimate.dependency_injection.storageModule
import br.androidapp.myclimate.dependency_injection.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class AppConfig : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@AppConfig)
            modules(
                listOf(
                    repositoryModule,
                    viewModelModule,
                    serializerModule,
                    storageModule,
                    networkModule
                )
            )
        }
    }
}