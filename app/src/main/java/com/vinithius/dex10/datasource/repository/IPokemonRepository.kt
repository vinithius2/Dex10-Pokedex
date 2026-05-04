package com.vinithius.dex10.datasource.repository

import android.content.Context
import com.vinithius.dex10.datasource.database.PokemonEntity
import com.vinithius.dex10.datasource.database.PokemonWithDetails
import com.vinithius.dex10.datasource.response.Characteristic
import com.vinithius.dex10.datasource.response.Damage
import com.vinithius.dex10.datasource.response.EvolutionChain
import com.vinithius.dex10.datasource.response.Location
import com.vinithius.dex10.datasource.response.Pokemon
import com.vinithius.dex10.datasource.response.Specie

/**
 * Abstraction layer for Pokemon data operations.
 * Follows the Dependency Inversion Principle (DIP) — high-level modules
 * depend on this interface, not on the concrete implementation.
 */
interface IPokemonRepository {

    // List operations
    suspend fun getPokemonEntityList(
        context: Context,
        callBackLoadingFirebaseCounter: ((progress: Float) -> Unit),
        callBackLoadingFirebase: (() -> Unit),
        callBackLoading: (() -> Unit),
        callBackError: ((e: Exception) -> Unit),
    ): List<PokemonWithDetails>?

    suspend fun getAllPokemonEntities(): List<PokemonEntity>

    // Detail operations
    suspend fun getPokemonWithDetailsByName(name: String): PokemonWithDetails?
    suspend fun getPokemonWithDetailsByListName(pokemonNames: List<String>): List<PokemonWithDetails>
    suspend fun getPokemonWithDetailsById(id: Int): PokemonWithDetails?
    suspend fun getPokemonColorById(id: Int): String?

    // Remote API operations
    suspend fun getPokemonDetail(id: Int): Pokemon?
    suspend fun getPokemonEncounters(id: Int): List<Location>?
    suspend fun getPokemonEvolution(id: Int): EvolutionChain?
    suspend fun getPokemonCharacteristic(id: Int): Characteristic?
    suspend fun getPokemonSpecies(id: Int): Specie?
    suspend fun getPokemonDamageRelations(type: String): Damage?

    // Favorites
    suspend fun setFavorite(pokemon: PokemonEntity): Boolean

    // Moves
    suspend fun getMovesForPokemon(pokemonId: Int): List<com.vinithius.dex10.datasource.response.MoveResponse>?
    suspend fun getMoveDetails(moveName: String): com.vinithius.dex10.datasource.response.MoveDetailsResponse?

    // TCG
    suspend fun getTcgCards(name: String): List<com.vinithius.dex10.datasource.response.TcgCard>?

    // Anime (Jikan)
    suspend fun getAnimeInfo(name: String): com.vinithius.dex10.datasource.response.JikanAnimeInfo?

    // Enrichment (background, non-blocking)
    suspend fun getLocalPokemonList(): List<PokemonWithDetails>?
    suspend fun runEnrichmentIfNeeded(context: Context)
}
