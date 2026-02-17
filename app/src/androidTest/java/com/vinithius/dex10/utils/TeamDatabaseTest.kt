package com.vinithius.dex10.utils

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.vinithius.dex10.datasource.database.AppDatabase
import com.vinithius.dex10.datasource.database.TeamDao
import com.vinithius.dex10.datasource.database.TeamEntity
import com.vinithius.dex10.datasource.database.TeamMemberEntity
import com.vinithius.dex10.datasource.repository.TeamRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.IOException

class TeamDatabaseTest {
    private lateinit var teamDao: TeamDao
    private lateinit var db: AppDatabase
    private lateinit var repository: TeamRepository

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries().build()
        teamDao = db.teamDao()
        repository = TeamRepository(teamDao)
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun deleteTeamWithMembers_ShouldSucceed_UsingNewLogic() = runBlocking {
        // 1. Cria um time com todos os campos obrigatórios preenchidos
        val now = System.currentTimeMillis()
        val teamId = teamDao.insertTeam(TeamEntity(name = "Time Teste", createdAt = now))
        val team = TeamEntity(id = teamId.toInt(), name = "Time Teste", createdAt = now)

        // 1.1 Insere um Pokémon fake com id 1 (campos obrigatórios)
        val pokemon = com.vinithius.dex10.datasource.database.PokemonEntity(
            id = 1,
            name = "Bulbasaur",
            color = "green",
            habitat = "grassland",
            favorite = false,
            imagePath = null
        )
        db.pokemonDao().insertPokemon(pokemon)

        // 2. Adiciona um membro (Simula o problema de Constraint)
        val member = TeamMemberEntity(teamId = teamId.toInt(), pokemonId = 1, position = 0)
        teamDao.insertMember(member)

        // Verifica se inseriu
        assertEquals(1, teamDao.getMemberCount(teamId.toInt()))

        // 3. Tenta Deletar usando a Lógica Corrigida
        repository.deleteTeam(team)

        // 4. Validação (Auditoria)
        val count = teamDao.getTeamCount()
        val memberCount = teamDao.getMemberCount(teamId.toInt())

        // Se falhar aqui, o bug persiste. Se passar, está resolvido.
        assertEquals("Membros devem ser deletados", 0, memberCount)
        assertEquals("Time deve ser deletado", 0, count)
    }
}
