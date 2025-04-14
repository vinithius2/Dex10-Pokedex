package com.vinithius.poke10x.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import coil.compose.AsyncImagePainter
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.vinithius.poke10x.datasource.database.PokemonWithDetails
import com.vinithius.poke10x.datasource.repository.PokemonRepository
import com.vinithius.poke10x.datasource.response.Damage
import com.vinithius.poke10x.datasource.response.Pokemon
import com.vinithius.poke10x.extension.getIdIntoUrl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.vinithius.poke10x.R
import com.vinithius.poke10x.datasource.data.AlertMessage

class PokemonViewModel(private val repository: PokemonRepository) : ViewModel() {

    // Filter

    // Mapa reativo para filtros
    private val _filterMap: MutableLiveData<Map<String, SnapshotStateMap<String, Boolean>>> =
        MutableLiveData()
    val filterMap: LiveData<Map<String, SnapshotStateMap<String, Boolean>>>
        get() = _filterMap

    private val _isFavoriteFilter = MutableLiveData<Boolean>()
    val isFavoriteFilter: LiveData<Boolean>
        get() = _isFavoriteFilter

    private val _pokemonIsFavorite = MutableLiveData<Pokemon>()
    val pokemonIsFavorite: LiveData<Pokemon>
        get() = _pokemonIsFavorite

    private val _searchNameFilter = MutableLiveData<String>()
    val searchNameFilter: LiveData<String>
        get() = _searchNameFilter

    // Screens

    // ENUMS
    private val _stateDetail =
        MutableLiveData<RequestStateDetail<List<String>>>(RequestStateDetail.Loading)
    val stateDetail: LiveData<RequestStateDetail<List<String>>> get() = _stateDetail

    private val _stateList =
        MutableLiveData<RequestStateList<List<String>>>(RequestStateList.Loading)
    val stateList: LiveData<RequestStateList<List<String>>> get() = _stateList

    // FIM ENUMS

    private val _loadingPercent = MutableLiveData<Float>()
    val loadingPercent: LiveData<Float>
        get() = _loadingPercent

    private val _pokemonListEvolutionNames = MutableLiveData<List<PokemonWithDetails>>()
    val pokemonListEvolutionNames: LiveData<List<PokemonWithDetails>>
        get() = _pokemonListEvolutionNames

    private val _pokemonListBackup = MutableLiveData<List<PokemonWithDetails>>()
    val pokemonListBackup: LiveData<List<PokemonWithDetails>>
        get() = _pokemonListBackup

    private val _pokemonList = MutableLiveData<List<PokemonWithDetails>>()
    val pokemonList: LiveData<List<PokemonWithDetails>>
        get() = _pokemonList

    private val _pokemonFilterList =
        MutableLiveData<Map<String, SnapshotStateMap<String, Boolean>>>()
    val pokemonFilterList: LiveData<Map<String, SnapshotStateMap<String, Boolean>>>
        get() = _pokemonFilterList

    private val _pokemonDetail = MutableLiveData<Pokemon?>()
    val pokemonDetail: LiveData<Pokemon?>
        get() = _pokemonDetail

    private val _pokemonDetailError = MutableLiveData<Boolean>()
    val pokemonDetailError: LiveData<Boolean>
        get() = _pokemonDetailError

    private val _idPokemon = MutableLiveData(0)
    val idPokemon: LiveData<Int>
        get() = _idPokemon

    // Pokemon images
    private val _sharedPokemonImages = MutableLiveData<Map<String, AsyncImagePainter>>()
    val sharedPokemonImages: LiveData<Map<String, AsyncImagePainter>> = _sharedPokemonImages

    // Remote config

    private val _adUnitIdList = MutableLiveData(String())
    val adUnitIdList: LiveData<String>
        get() = _adUnitIdList

    fun setAdUnitIdList(value: String) {
        _adUnitIdList.postValue(value)
    }

    private val _adUnitIdDetails = MutableLiveData(String())
    val adUnitIdDetails: LiveData<String>
        get() = _adUnitIdDetails

    fun setAdUnitIdDetails(value: String) {
        _adUnitIdDetails.postValue(value)
    }

    // Is Rewarded

    private val _isRewarded = MutableLiveData(true)
    val isRewarded: LiveData<Boolean>
        get() = _isRewarded

    fun setIsRewarded(value: Boolean) {
        _isRewarded.postValue(value)
    }

    // Hide Pokemon Of TheDay

    private val _hidePokemonOfTheDay = MutableLiveData(true)
    val hidePokemonOfTheDay: LiveData<Boolean>
        get() = _hidePokemonOfTheDay

    fun setHidePokemonOfTheDay(value: Boolean) {
        _hidePokemonOfTheDay.postValue(value)
    }

    // Choice of the day

    private val _choiceOfTheDay = MutableLiveData(false)
    val choiceOfTheDay: LiveData<Boolean>
        get() = _choiceOfTheDay

    fun setChoiceOfTheDay(value: Boolean) {
        _choiceOfTheDay.postValue(value)
    }

    // Interstitial

    private val _adUnitIdChoiceOfTheDayInterstitial = MutableLiveData(String())
    val adUnitIdChoiceOfTheDayInterstitial: LiveData<String>
        get() = _adUnitIdChoiceOfTheDayInterstitial

    fun setAdUnitIdChoiceOfTheDayInterstitial(value: String) {
        _adUnitIdChoiceOfTheDayInterstitial.postValue(value)
    }

    private val _choiceOfTheDayInterstitialShow = MutableLiveData(false)
    val choiceOfTheDayInterstitialShow: LiveData<Boolean>
        get() = _choiceOfTheDayInterstitialShow

    fun adUnitIdChoiceOfTheDayInterstitialShow(value: Boolean) {
        _choiceOfTheDayInterstitialShow.postValue(value)
    }

    private val _isAdLoadedInterstitial = MutableLiveData(false)
    val isAdLoadedInterstitial: LiveData<Boolean>
        get() = _isAdLoadedInterstitial

    fun setIsAdLoadedInterstitial(value: Boolean) {
        _isAdLoadedInterstitial.postValue(value)
    }

    // Rewarded

    data class AdData(
        val id: Int,
        val name: String,
        val color: String,
        val choiceOfTheDayStatus: Boolean
    )

    private val _adDataToDetails = MutableLiveData<AdData>(null)
    val adDataToDetails: LiveData<AdData>
        get() = _adDataToDetails

    fun setAdDataToDetails(value: AdData) {
        _adDataToDetails.postValue(value)
    }

    private val _adUnitIdChoiceOfTheDayRewarded = MutableLiveData(String())
    val adUnitIdChoiceOfTheDayRewarded: LiveData<String>
        get() = _adUnitIdChoiceOfTheDayRewarded

    fun setAdUnitIdChoiceOfTheDayRewarded(value: String) {
        _adUnitIdChoiceOfTheDayRewarded.postValue(value)
    }

    private val _choiceOfTheDayRewardedShow = MutableLiveData(false)
    val choiceOfTheDayRewardedShow: LiveData<Boolean>
        get() = _choiceOfTheDayRewardedShow

    fun adUnitIdChoiceOfTheDayRewardedShow(value: Boolean) {
        _choiceOfTheDayRewardedShow.postValue(value)
    }

    private val _isAdLoadedRewarded = MutableLiveData(false)
    val isAdLoadedRewarded: LiveData<Boolean>
        get() = _isAdLoadedRewarded

    fun setIsAdLoadedRewarded(value: Boolean) {
        _isAdLoadedRewarded.postValue(value)
    }

    // Social Media

    private val _facebookUrl = MutableLiveData(String())
    val facebookUrl: LiveData<String>
        get() = _facebookUrl

    fun setFacebookUrl(value: String) {
        _facebookUrl.postValue(value)
    }

    private val _instagranUrl = MutableLiveData(String())
    val instagranUrl: LiveData<String>
        get() = _instagranUrl

    fun setInstagranUrl(value: String) {
        _instagranUrl.postValue(value)
    }

    private val _redditUrl = MutableLiveData(String())
    val redditUrl: LiveData<String>
        get() = _redditUrl

    fun setRedditUrl(value: String) {
        _redditUrl.postValue(value)
    }

    private val _googleForm = MutableLiveData(String())
    val googleForm: LiveData<String>
        get() = _googleForm

    fun setGoogleForm(value: String) {
        _googleForm.postValue(value)
    }

    private val _paypalId = MutableLiveData(String())
    val paypalId: LiveData<String>
        get() = _paypalId

    fun setPaypalId(value: String) {
        _paypalId.postValue(value)
    }

    private val _topAlertMessage = MutableLiveData<AlertMessage?>(null)
    val topAlertMessage: LiveData<AlertMessage?>
        get() = _topAlertMessage

    fun setTopAlertMessage(data: AlertMessage) {
        _topAlertMessage.postValue(data)
    }

    // FIM - Remote config

    private val _pokemonColor = MutableLiveData(String())
    val pokemonColor: LiveData<String?>
        get() = _pokemonColor

    fun setPokemonColor(color: String?) {
        _pokemonColor.postValue(color)
    }

    fun getPokemonColor(): String? {
        return _pokemonColor.value
    }

    private val _isDetailScreen = MutableLiveData(false)
    val isDetailScreen: LiveData<Boolean>
        get() = _isDetailScreen

    fun setDetailsScreen(isDetails: Boolean) {
        _isDetailScreen.postValue(isDetails)
    }

    private val _isDetailFavorite = MutableLiveData(false)
    val isDetailFavorite: LiveData<Boolean>
        get() = _isDetailFavorite

    fun updateSharedImage(pokemonId: String, imagePainter: AsyncImagePainter) {
        val currentImages = _sharedPokemonImages.value.orEmpty()
        _sharedPokemonImages.value = currentImages + (pokemonId to imagePainter)
    }

    fun getSharedImage(pokemonId: String): AsyncImagePainter? {
        return _sharedPokemonImages.value?.get(pokemonId)
    }

    // ### ----- SCREEN LIST POKEMON'S ----- ### //

    fun setIdPokemon(value: Int) {
        _idPokemon.postValue(value)
    }

    /**
     * Get pokemons list with Pokémon of the Day logic.
     */
    fun getPokemonList(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            _isDetailFavorite.postValue(false)
            try {
                val result = repository.getPokemonEntityList(
                    context,
                    callBackLoadingFirebase = {
                        _stateList.postValue(RequestStateList.LoadingFirebase)
                    },
                    callBackLoadingFirebaseCounter = { progress ->
                        _loadingPercent.postValue(progress)
                    },
                    callBackLoading = {
                        _stateList.postValue(RequestStateList.Loading)
                    },
                    callBackError = { e ->
                        _stateList.postValue(RequestStateList.Error(e))
                    }
                ) ?: emptyList()


                // Update the list with the Pokémon of the day
                val updatedList = updatePokemonOfTheDay(context, result)
                _pokemonListBackup.postValue(updatedList)
                _pokemonList.postValue(updatedList)
                makeFilter(updatedList, context)

                _stateList.postValue(RequestStateList.Success)
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
                _stateList.postValue(RequestStateList.Error(e))
                Log.e("Error list pokemons", e.toString())
            }
        }
    }

    /**
     * Atualiza a lista com o Pokémon do dia, garantindo que não haja repetição até o ciclo reiniciar.
     */
    private fun updatePokemonOfTheDay(
        context: Context,
        pokemonList: List<PokemonWithDetails>
    ): List<PokemonWithDetails> {
        val sharedPreferences = context.getSharedPreferences("pokemon_prefs", Context.MODE_PRIVATE)
        val lastSelectionDate = sharedPreferences.getString("last_selection_date", null)
        val currentDate = getCurrentDate()

        // Recuperar a lista de Pokémon já selecionados no ciclo atual
        val selectedPokemons =
            sharedPreferences.getStringSet("selected_pokemons", mutableSetOf())?.toMutableSet()
                ?: mutableSetOf()

        // Se for um novo dia
        if (lastSelectionDate != currentDate) {
            // Verificar se todos os Pokémon já foram selecionados
            if (selectedPokemons.size >= pokemonList.size) {
                selectedPokemons.clear() // Reiniciar o ciclo
            }

            // Filtrar Pokémon que ainda não foram selecionados
            val availablePokemons =
                pokemonList.filter { !selectedPokemons.contains(it.pokemon.name) }

            if (availablePokemons.isNotEmpty()) {
                // Selecionar um Pokémon aleatório dos disponíveis
                val randomIndex = (0 until availablePokemons.size).random()
                val pokemonOfTheDay = availablePokemons[randomIndex]

                // Adicionar à lista de selecionados
                selectedPokemons.add(pokemonOfTheDay.pokemon.name)

                // Salvar a data, o Pokémon do dia e a lista de selecionados
                with(sharedPreferences.edit()) {
                    putString("last_selection_date", currentDate)
                    putBoolean("hide_pokemon_of_the_day", true)
                    putString("pokemon_of_the_day", pokemonOfTheDay.pokemon.name)
                    putStringSet("selected_pokemons", selectedPokemons)
                    apply()
                }

                // Criar nova lista com o Pokémon do dia no topo
                val updatedList = mutableListOf<PokemonWithDetails>()
                updatedList.add(pokemonOfTheDay)
                updatedList.addAll(pokemonList.filter { it != pokemonOfTheDay })
                return updatedList
            }
        } else {
            // Recuperar o Pokémon do dia atual
            val pokemonOfTheDayName = sharedPreferences.getString("pokemon_of_the_day", null)
            val pokemonOfTheDay = pokemonList.find { it.pokemon.name == pokemonOfTheDayName }

            if (pokemonOfTheDay != null) {
                // Mover o Pokémon do dia para o topo
                val updatedList = mutableListOf<PokemonWithDetails>()
                updatedList.add(pokemonOfTheDay)
                updatedList.addAll(pokemonList.filter { it != pokemonOfTheDay })
                return updatedList
            }
        }

        // Se não houver Pokémon disponível ou não for um novo dia, retornar a lista original
        return pokemonList
    }

    /**
     * Retorna a data atual no formato "yyyy-MM-dd".
     */
    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }

    private fun makeFilter(
        pokemonList: List<PokemonWithDetails>,
        context: Context
        ) {
        val checkboxStateMapTypes = checkboxStateMap(
            pokemonList.flatMap { pokemon -> pokemon.types.map { it.typeName } }
        )
        val checkboxStateMapAbilities = checkboxStateMap(
            pokemonList.flatMap { pokemon -> pokemon.abilities.map { it.name } }
        )
        val checkboxStateMapColors = checkboxStateMap(
            pokemonList.map { it.pokemon.color }
        )
        val checkboxStateMapHabitats = checkboxStateMap(
            pokemonList.map { it.pokemon.habitat }
        )
        context.getString(R.string.type)
        val filterMap = mapOf(
            context.getString(R.string.type) to checkboxStateMapTypes,
            context.getString(R.string.ability) to checkboxStateMapAbilities,
            context.getString(R.string.color) to checkboxStateMapColors,
            context.getString(R.string.habitat) to checkboxStateMapHabitats,
        )
        _pokemonFilterList.postValue(filterMap)
    }

    private fun checkboxStateMap(filters: List<String>): SnapshotStateMap<String, Boolean> {
        return mutableStateMapOf<String, Boolean>().apply {
            filters.distinct().sorted().forEach { filter ->
                put(filter, false)
            }
        }
    }

    /**
     * Set favorite pokemon to database.
     */
    fun setFavorite(pokemonId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                _pokemonList.value?.map { pokemonMap ->
                    if (pokemonMap.pokemon.id == pokemonId) {
                        pokemonMap.pokemon.favorite = pokemonMap.pokemon.favorite.not()
                        _isDetailFavorite.postValue(pokemonMap.pokemon.favorite)
                        repository.setFavorite(pokemonMap.pokemon)
                    }
                }
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
                Log.e("setFavorite", e.toString())
            }
        }
    }

    private fun getDetailFavorite() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val isFavorite = _pokemonList.value?.firstOrNull {
                    it.pokemon.id == _idPokemon.value
                }?.pokemon?.favorite
                _isDetailFavorite.postValue(isFavorite)
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
                Log.e("getIsFavorite", e.toString())
            }
        }
    }

    /**
     * Remove item if not a favorite from the filter favorites
     */
    fun removeItemIfNotIsFavorite(
        context: Context
    ) {
        _isFavoriteFilter.takeIf { it.value ?: false }?.run {
            getPokemonFavoriteList(true, context)
        }
    }

    // FILTERS

    fun getPokemonSearch(
        search: String,
        context: Context,
        ) {
        _searchNameFilter.value = search
        getFilterPokemon(context)
    }

    fun getPokemonFavoriteList(
        isFavorite: Boolean,
        context: Context,
        ) {
        _isFavoriteFilter.value = isFavorite
        getFilterPokemon(context)
    }

    fun updateFilterState(
        filter: Map<String, SnapshotStateMap<String, Boolean>>,
        context: Context,
        ) {
        _filterMap.value = filter
        getFilterPokemon(context)
    }

    private fun getFilterPokemon(
        context: Context,
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            _stateList.postValue(RequestStateList.Loading)
            try {
                val searchQuery = _searchNameFilter.value.orEmpty()
                val isFavorite = _isFavoriteFilter.value ?: false
                val filterMapValues = _filterMap.value ?: emptyMap()

                val filteredList = _pokemonListBackup.value?.filter { pokemonWithDetails ->
                    val matchesSearch = pokemonWithDetails.pokemon.name.contains(
                        searchQuery,
                        ignoreCase = true
                    )
                    val matchesFavorites = isFavorite.not() || pokemonWithDetails.pokemon.favorite
                    val matchesFilters = filterMapValues.all { (key, stateMap) ->
                        val selectedValues = stateMap.filterValues { it }.keys
                        if (selectedValues.isEmpty()) {
                            true
                        } else {
                            context.getString(R.string.ability)
                            when (key) {
                                context.getString(R.string.type) -> pokemonWithDetails.types.any { it.typeName in selectedValues }
                                context.getString(R.string.ability) -> pokemonWithDetails.abilities.any { it.name in selectedValues }
                                context.getString(R.string.color) -> pokemonWithDetails.pokemon.color in selectedValues
                                context.getString(R.string.habitat) -> pokemonWithDetails.pokemon.habitat in selectedValues
                                else -> true
                            }
                        }
                    }
                    matchesSearch && matchesFavorites && matchesFilters
                }
                withContext(Dispatchers.Main) {
                    _pokemonList.value = filteredList ?: _pokemonListBackup.value
                }
                _stateList.postValue(RequestStateList.Success)
                Log.d("getFilterPokemon", "Filtered list size: ${filteredList?.size}")
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
                _stateList.postValue(RequestStateList.Error(e))
                Log.e("getFilterPokemon", "Erro ao aplicar filtros: ${e.message}", e)
            }
        }
    }

    // ### ----- SCREEN DETAILS POKEMON ----- ### //

    /**
     * Get all details pokemon.
     */
    fun getPokemonDetail() {
        val id = _idPokemon.value
        if (id == null) {
            _stateDetail.postValue(RequestStateDetail.Error(NullPointerException("Pokemon ID is null")))
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            _stateDetail.postValue(RequestStateDetail.Loading)

            try {
                val pokemon = repository.getPokemonDetail(id)

                if (pokemon == null) {
                    _stateDetail.postValue(RequestStateDetail.Error(NullPointerException("Pokemon not found")))
                    return@launch
                }

                // Lançar as tarefas em paralelo
                val encountersDeferred = async { getPokemonEncounters(pokemon) }
                val characteristicDeferred = async { getPokemonCharacteristic(pokemon) }
                val speciesDeferred = async { getPokemonSpecies(pokemon) }
                val damageRelationsDeferred = async { getPokemonDamageRelations(pokemon) }

                // Esperar todas as tarefas completarem
                encountersDeferred.await()
                characteristicDeferred.await()
                speciesDeferred.await()
                damageRelationsDeferred.await()

                _stateDetail.postValue(RequestStateDetail.Success)
                _pokemonDetail.postValue(pokemon)
                getDetailFavorite()

            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
                FirebaseCrashlytics.getInstance().setCustomKey("pokemon_id", id)
                _stateDetail.postValue(RequestStateDetail.Error(e))
                Log.e("getPokemon", e.toString())
            }
        }
    }

    /**
     * Get Pokemon's Encounters.
     */
    private suspend fun getPokemonEncounters(pokemon: Pokemon) {
        repository.getPokemonEncounters(_idPokemon.value ?: 0)?.let { apiLocationList ->
            pokemon.apply { encounters = apiLocationList }
        }
    }

    /**
     * Get Pokemon's Characteristic.
     */
    private suspend fun getPokemonCharacteristic(pokemon: Pokemon) {
        repository.getPokemonCharacteristic(_idPokemon.value ?: 0)?.let { apiCharacteristic ->
            pokemon.apply { characteristic = apiCharacteristic }
        }
    }

    /**
     * Get Pokemon's Species.
     */
    private suspend fun getPokemonSpecies(pokemon: Pokemon) {
        repository.getPokemonSpecies(_idPokemon.value ?: 0)?.let { apiSpecie ->
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

    fun getIdByNames(pair: List<Pair<String, String>>): List<Pair<Int, String>>? {
        val names = pair.map { it.first }
        return _pokemonListBackup.value?.filter { it.pokemon.name in names }
            ?.map { it.pokemon.id to it.pokemon.name }
    }

}
