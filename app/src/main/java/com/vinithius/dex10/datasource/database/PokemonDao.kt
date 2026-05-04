package com.vinithius.dex10.datasource.database

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
    suspend fun updatePokemonIsFavorite(pokemon: PokemonEntity): Int

    // SELECT
    @Transaction
    @Query("SELECT * FROM pokemon")
    suspend fun getPokemonListWithDetails(): List<PokemonWithDetails>?

    @Transaction
    @Query("SELECT * FROM pokemon WHERE id = :pokemonId")
    suspend fun getPokemonWithDetailsById(pokemonId: Int): PokemonWithDetails?

    @Transaction
    @Query("SELECT * FROM pokemon WHERE name = :pokemonName")
    suspend fun getPokemonWithDetailsByName(pokemonName: String): PokemonWithDetails?

    @Transaction
    @Query("SELECT * FROM pokemon WHERE name IN (:pokemonNames)")
    suspend fun getPokemonWithDetailsByListName(pokemonNames: List<String>): List<PokemonWithDetails>

    @Query("SELECT * FROM stat WHERE id = :id")
    suspend fun getStatById(id: Int): Stat?

    @Query("SELECT * FROM ability WHERE id = :id")
    suspend fun getAbilityById(id: Int): Ability?

    @Query("SELECT * FROM type WHERE id = :id")
    suspend fun getTypeById(id: Int): Type?

    @Query("SELECT * FROM pokemon WHERE id = :id")
    suspend fun getPokemonById(id: Int): PokemonEntity?

    @Query("SELECT color FROM pokemon WHERE id = :id")
    suspend fun getPokemonColorById(id: Int): String?

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

    // Validation queries — used by PokemonRepository.validateAndCorrectPokemonData()

    /** Returns IDs of Pokémon whose only type is "normal" (possible Firebase default). */
    @Query("""
        SELECT p.id FROM pokemon p
        INNER JOIN pokemon_type pt ON p.id = pt.pokemon_id
        INNER JOIN type t ON pt.type_id = t.id
        WHERE t.type_name = 'normal'
        GROUP BY p.id
        HAVING COUNT(DISTINCT pt.type_id) = 1
    """)
    suspend fun getPokemonIdsWithOnlyNormalType(): List<Int>

    /** Returns IDs of Pokémon where the habitat was never resolved from Firebase. */
    @Query("SELECT id FROM pokemon WHERE habitat IN ('unknow', 'null', 'unknown', '')")
    suspend fun getPokemonIdsWithUnknownHabitat(): List<Int>

    @Query("UPDATE pokemon SET habitat = :habitat WHERE id = :pokemonId")
    suspend fun updatePokemonHabitat(pokemonId: Int, habitat: String)

    @Query("DELETE FROM pokemon_type WHERE pokemon_id = :pokemonId")
    suspend fun deleteTypesByPokemonId(pokemonId: Int)

    @Query("DELETE FROM type WHERE id IN (:typeIds)")
    suspend fun deleteTypesByIds(typeIds: List<Int>)

    @Query("""
        UPDATE pokemon SET
            generation = :generation,
            is_legendary = :isLegendary,
            is_mythical = :isMythical,
            is_baby = :isBaby,
            shape = :shape,
            growth_rate = :growthRate,
            egg_groups = :eggGroups
        WHERE id = :pokemonId
    """)
    suspend fun updatePokemonEnrichment(
        pokemonId: Int,
        generation: String,
        isLegendary: Boolean,
        isMythical: Boolean,
        isBaby: Boolean,
        shape: String,
        growthRate: String,
        eggGroups: String
    )

    /** Returns IDs of Pokémon that still have empty generation (not yet enriched). */
    @Query("SELECT id FROM pokemon WHERE generation = ''")
    suspend fun getPokemonIdsNotEnriched(): List<Int>

    // Move detail cache
    @Query("SELECT * FROM move_detail WHERE name = :name")
    suspend fun getMoveDetail(name: String): MoveDetailEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMoveDetail(moveDetail: MoveDetailEntity)

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