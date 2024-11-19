package com.vinithius.poke10.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vinithius.poke10.datasource.database.PokemonWithDetails
import com.vinithius.poke10.datasource.repository.PokemonRepository
import com.vinithius.poke10.datasource.response.Damage
import com.vinithius.poke10.datasource.response.Pokemon
import com.vinithius.poke10.extension.getIdIntoUrl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PokemonViewModel(private val repository: PokemonRepository) : ViewModel() {

    private val _pokemonListBackup = MutableLiveData<List<PokemonWithDetails>>()
    private val _pokemonList = MutableLiveData<List<PokemonWithDetails>>()
    val pokemonList: LiveData<List<PokemonWithDetails>>
        get() = _pokemonList

    private val _isFavoriteFilter = MutableLiveData<Boolean>()
    val isFavoriteFilter: LiveData<Boolean>
        get() = _isFavoriteFilter

    private val _pokemonDetail = MutableLiveData<Pokemon?>()
    val pokemonDetail: LiveData<Pokemon?>
        get() = _pokemonDetail

    private val _pokemonDetailLoading = MutableLiveData<Boolean>()
    val pokemonDetailLoading: LiveData<Boolean>
        get() = _pokemonDetailLoading

    private val _pokemonDetailError = MutableLiveData<Boolean>()
    val pokemonDetailError: LiveData<Boolean>
        get() = _pokemonDetailError

    private val _pokemonIsFavorite = MutableLiveData<Pokemon>()
    val pokemonIsFavorite: LiveData<Pokemon>
        get() = _pokemonIsFavorite

    private var _idPokemon: Int = 0

    fun setIdPokemon(value: Int) {
        _idPokemon = value
    }

    fun cleanPokemon() {
        _pokemonDetail.value = null
    }

    /**
     * Get pokemons list.
     */
    fun getPokemonList(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = repository.getPokemonEntityList(context) ?: emptyList()
                _pokemonListBackup.postValue(result)
                _pokemonList.postValue(result)
            } catch (e: Exception) {
                Log.e("Error list pokemons", e.toString())
            }
        }
    }

    fun getPokemonFavoriteList(isFavorite: Boolean) {
        _isFavoriteFilter.value = isFavorite
    }

    /**
     * Get all details pokemon.
     */
    fun getPokemonDetail() {
        CoroutineScope(Dispatchers.IO).launch {
            _pokemonDetailError.postValue(false)
            try {
                _pokemonDetailLoading.postValue(true)
                val pokemon = repository.getPokemonDetail(_idPokemon)
                pokemon?.let {
                    getPokemonEncounters(it)
                    getPokemonCharacteristic(it)
                    getPokemonSpecies(it)
                    getPokemonDamageRelations(it)
                }
                _pokemonDetail.postValue(pokemon)
            } catch (e: Exception) {
                _pokemonDetailError.postValue(true)
                Log.e("getPokemon", e.toString())
            }
            _pokemonDetailLoading.postValue(false)
        }
    }

    /**
     * Get Pokemon's Encounters.
     */
    private suspend fun getPokemonEncounters(pokemon: Pokemon) {
        repository.getPokemonEncounters(_idPokemon)?.let { apiLocationList ->
            pokemon.apply { encounters = apiLocationList }
        }
    }

    /**
     * Get Pokemon's Characteristic.
     */
    private suspend fun getPokemonCharacteristic(pokemon: Pokemon) {
        repository.getPokemonCharacteristic(_idPokemon)?.let { apiCharacteristic ->
            pokemon.apply { characteristic = apiCharacteristic }
        }
    }

    /**
     * Get Pokemon's Species.
     */
    private suspend fun getPokemonSpecies(pokemon: Pokemon) {
        repository.getPokemonSpecies(_idPokemon)?.let { apiSpecie ->
            pokemon.apply { specie = apiSpecie }
            apiSpecie.evolution_chain?.url?.getIdIntoUrl()?.let {
                getPokemonEvolution(pokemon, it.toInt())
            }
        }
    }

    /**
     * Get Pokemon's Evolution.
     */
    private suspend fun getPokemonEvolution(pokemon: Pokemon, specieId: Int) {
        repository.getPokemonEvolution(specieId)?.let { apiEvolution ->
            pokemon.apply { evolution = apiEvolution }
        }
    }

    /**
     * Get Pokemon's Damage Relations.
     */
    private suspend fun getPokemonDamageRelations(pokemon: Pokemon) {
        val damageList: MutableList<Damage> = mutableListOf()
        pokemon.types?.forEach { typeList ->
            repository.getPokemonDamageRelations(typeList.type.name!!)?.let {
                it.type = typeList.type
                damageList.add(it)
            }
        }
        pokemon.damage = damageList
    }


    /**
     * Set favorite pokemon to sharedPreferences.
     */
    fun setFavorite(pokemon: PokemonWithDetails) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                _pokemonList.value?.map { pokemonMap ->
                    if (pokemonMap.pokemon.id == pokemon.pokemon.id) {
                        pokemon.pokemon.favorite = pokemon.pokemon.favorite.not()
                        repository.setFavorite(pokemon.pokemon)
                    }
                }
            } catch (e: Exception) {
                Log.e("setFavorite", e.toString())
            }
        }
    }

    fun removeItemIfNotIsFavorite() {
        _isFavoriteFilter.takeIf { it.value ?: false }?.run {
            getPokemonFavoriteList(true)
        }
    }

    fun getFilterPokemon(
        search: String = String()
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            delay(100)
            val filteredList = if (search.isNotEmpty()) {
                _pokemonListBackup.value?.run {
                    var filtered = this.filter { pokemon ->
                        pokemon.pokemon.name.contains(search, ignoreCase = true)
                    }
                    if (_isFavoriteFilter.value == true) {
                        filtered = filtered.filter { it.pokemon.favorite }
                    }
                    filtered
                }
            } else {
                _pokemonListBackup.value
            }
            withContext(Dispatchers.Main) {
                _pokemonList.value = filteredList ?: _pokemonListBackup.value
            }
        }
    }
}
