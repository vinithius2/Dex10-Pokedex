package com.vinithius.poke10.datasource.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update

@Dao
interface PokemonDao {

    // INSERTS
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPokemon(pokemon: PokemonEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertType(type: Type): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAbility(ability: Ability): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertStat(stat: Stat): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPokemonType(pokemonType: PokemonType): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPokemonStat(pokemonStat: PokemonStat): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPokemonAbility(pokemonAbility: PokemonAbility): Long

    // UPDATE
    @Update(onConflict = OnConflictStrategy.IGNORE)
    suspend fun updatePokemonIsFavorite(pokemonAbility: PokemonAbility): Int

    // SELECT
    @Query("SELECT * FROM pokemon")
    suspend fun getPokemonListWithDetails(): List<PokemonWithDetails>?

    @Query("SELECT * FROM pokemon WHERE id = :pokemonId")
    suspend fun getPokemonWithDetailsById(pokemonId: Int): PokemonWithDetails?

    @Query("SELECT * FROM pokemon WHERE name = :pokemonName")
    suspend fun getPokemonWithDetailsByName(pokemonName: String): PokemonWithDetails?

    @Query("SELECT * FROM stat WHERE id = :id")
    suspend fun getStatById(id: Int): Stat?

    @Query("SELECT * FROM ability WHERE id = :id")
    suspend fun getAbilityById(id: Int): Ability?

    @Query("SELECT * FROM type WHERE id = :id")
    suspend fun getTypeById(id: Int): Type?

    @Query("SELECT * FROM pokemon WHERE id = :id")
    suspend fun getPokemonById(id: Int): PokemonEntity?

    @Query("SELECT * FROM pokemon_type WHERE pokemon_id = :pokemonId")
    suspend fun getTypesByPokemonId(pokemonId: Int): List<PokemonType>

    @Query("SELECT * FROM pokemon_stat WHERE pokemon_id = :pokemonId")
    suspend fun getStatsByPokemonId(pokemonId: Int): List<PokemonStat>

    @Query("SELECT * FROM pokemon_ability WHERE pokemon_id = :pokemonId")
    suspend fun getAbilitiesByPokemonId(pokemonId: Int): List<PokemonAbility>

    @Query("SELECT COUNT(*) FROM pokemon")
    suspend fun getCountPokemons(): Int

    // Lista de todos os Pokémon
    @Transaction
    @Query("SELECT * FROM pokemon")
    suspend fun getAllPokemonsEntities(): List<PokemonEntity>

    // Deleções
    @Delete
    suspend fun deletePokemon(pokemon: PokemonEntity)

    @Delete
    suspend fun deleteType(type: Type)

    @Delete
    suspend fun deleteAbility(ability: Ability)

    @Delete
    suspend fun deleteStat(stat: Stat)

    @Delete
    suspend fun deletePokemonType(pokemonType: PokemonType)

    @Delete
    suspend fun deletePokemonStat(pokemonStat: PokemonStat)

    @Delete
    suspend fun deletePokemonAbility(pokemonAbility: PokemonAbility)
}