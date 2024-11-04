package com.vinithius.poke10.datasource.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface PokemonDao {

    // Inserção
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPokemon(pokemon: PokemonCard)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertType(type: Type)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertEvolution(evolution: Evolution)

    // Consultas
    @Query("SELECT * FROM POKEMON WHERE id = :id")
    suspend fun getPokemonById(id: Int): PokemonCard?

    @Query("SELECT * FROM TYPE WHERE id = :id")
    suspend fun getTypeById(id: Int): Type?

    @Query("SELECT * FROM EVOLUTION WHERE id = :id")
    suspend fun getEvolutionById(id: Int): Evolution?

    // Lista de todos os Pokemons com tipos e evoluções
    @Transaction
    @Query("SELECT * FROM POKEMON")
    suspend fun getAllPokemon(): List<PokemonCard>

    // Deleção
    @Delete
    suspend fun deletePokemon(pokemon: PokemonCard)

    @Delete
    suspend fun deleteType(type: Type)

    @Delete
    suspend fun deleteEvolution(evolution: Evolution)
}
