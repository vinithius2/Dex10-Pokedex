package com.vinithius.dex10.di

import androidx.room.Room
import com.vinithius.dex10.BuildConfig
import com.vinithius.dex10.datasource.data.AppPreferences
import com.vinithius.dex10.datasource.data.PremiumManager
import com.vinithius.dex10.datasource.database.AppDatabase
import com.vinithius.dex10.datasource.repository.IPokemonRepository
import com.vinithius.dex10.datasource.repository.PokemonRemoteDataSource
import com.vinithius.dex10.datasource.repository.PokemonRepository
import com.vinithius.dex10.ui.viewmodel.PokemonViewModel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


val repositoryModule = module {
    single { get<Retrofit>().create(PokemonRemoteDataSource::class.java) }
}

val repositoryDataModule = module {
    single<IPokemonRepository> { PokemonRepository(get(), get()) }
    single<com.vinithius.dex10.datasource.repository.ITeamRepository> { 
        com.vinithius.dex10.datasource.repository.TeamRepository(get()) 
    }
}

val viewModelModule = module {
    single { PokemonViewModel(get(), get()) }
    single { com.vinithius.dex10.ui.viewmodel.TeamViewModel(get(), get(), get()) }
}

val appPreferencesModule = module {
    single { AppPreferences(androidContext()) }
}

val premiumModule = module {
    single { PremiumManager(androidContext()) }
}

val networkModule = module {
    single { retrofit() }
}

val databaseModule = module {
    single { AppDatabase.getInstance(androidContext()) }
    single { get<AppDatabase>().pokemonDao() }
    single { get<AppDatabase>().teamDao() }
}

fun retrofit(): Retrofit {

    val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .apply {
            if (BuildConfig.DEBUG) {
                addInterceptor(
                    HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BODY
                    }
                )
            }
        }
        .build()

    return Retrofit.Builder()
        .baseUrl("https://pokeapi.co/api/v2/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

}

