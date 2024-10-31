package com.vinithius.poke10.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vinithius.poke10.datasource.repository.PokemonRepository
import com.vinithius.poke10.datasource.response.Damage
import com.vinithius.poke10.datasource.response.Pokemon
import com.vinithius.poke10.extension.getIdIntoUrl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PokemonViewModel(private val repository: PokemonRepository) : ViewModel() {

    private val _pokemonList = MutableLiveData<List<Pokemon>>()
    val pokemonList: LiveData<List<Pokemon>>
        get() = _pokemonList

    private val _pokemonDetail = MutableLiveData<Pokemon?>()
    val pokemonDetail: LiveData<Pokemon?>
        get() = _pokemonDetail

    private val _pokemonDetailLoading = MutableLiveData<Boolean>()
    val pokemonDetailLoading: LiveData<Boolean>
        get() = _pokemonDetailLoading

    private val _pokemonDetailError = MutableLiveData<Boolean>()
    val pokemonDetailError: LiveData<Boolean>
        get() = _pokemonDetailError

    private var _idPokemon: Int = 0

    fun setIdPokemon(value: Int) {
        _idPokemon = value
    }

    fun cleanPokemon() {
        _pokemonDetail.value = null
    }

    /**
     * Get pokemons list using Paging3.
     */
    fun getPokemonList() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                _pokemonList.value = repository.getPokemonList()
            } catch (e: Exception) {
                Log.e("Error list pokemons", e.toString())
            }
        }
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
            apiSpecie.evolution_chain.url.getIdIntoUrl()?.let {
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
            repository.getPokemonDamageRelations(typeList.type.name)?.let {
                it.type = typeList.type
                damageList.add(it)
            }
        }
        pokemon.damage = damageList
    }


    /**
     * Set favorite pokemon to webhook.
     */
    fun setFavorite(pokemon: Pokemon, context: Context?) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                //repository.setFavorite(pokemon, context)
            } catch (e: Exception) {
                Log.e("setFavorite", e.toString())
            }
        }
    }
}
