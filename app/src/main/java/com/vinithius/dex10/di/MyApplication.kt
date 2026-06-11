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
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


val repositoryModule = module {
    single { get<Retrofit>(qualifier = org.koin.core.qualifier.named("pokeapi")).create(PokemonRemoteDataSource::class.java) }
    single { get<Retrofit>(qualifier = org.koin.core.qualifier.named("tcg")).create(com.vinithius.dex10.datasource.repository.TcgRemoteDataSource::class.java) }
    single { get<Retrofit>(qualifier = org.koin.core.qualifier.named("jikan")).create(com.vinithius.dex10.datasource.repository.JikanRemoteDataSource::class.java) }
}

val repositoryDataModule = module {
    single<IPokemonRepository> { PokemonRepository(get(), get(), get(), get()) }
    single<com.vinithius.dex10.datasource.repository.ITeamRepository> { 
        com.vinithius.dex10.datasource.repository.TeamRepository(get()) 
    }
}

val viewModelModule = module {
    single { PokemonViewModel(get(), get(), get()) }
    viewModel { com.vinithius.dex10.ui.viewmodel.TeamViewModel(get(), get(), get()) }
    viewModel { com.vinithius.dex10.ui.viewmodel.ScannerViewModel(get(), androidContext()) }
}

val appPreferencesModule = module {
    single { AppPreferences(androidContext()) }
}

val scannerModule = module {
    single { com.vinithius.dex10.scanner.ScannerModelManager(androidContext()) }
}

// BillingHandler is provided by the flavor-specific billingModule (google/amazon/huawei)
val premiumModule = module {
    single { PremiumManager(androidContext(), get(), appPreferences = get()) }
}

val networkModule = module {
    single(qualifier = org.koin.core.qualifier.named("pokeapi")) { retrofit("https://pokeapi.co/api/v2/") }
    single(qualifier = org.koin.core.qualifier.named("tcg")) { retrofit("https://api.tcgdex.net/v2/en/") }
    single(qualifier = org.koin.core.qualifier.named("jikan")) { retrofit("https://api.jikan.moe/v4/") }
}

val databaseModule = module {
    single { AppDatabase.getInstance(androidContext()) }
    single { get<AppDatabase>().pokemonDao() }
    single { get<AppDatabase>().teamDao() }
}

fun retrofit(baseUrl: String): Retrofit {

    val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", "PokeDexApp/1.0")
                .build()
            chain.proceed(request)
        }
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
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

}
