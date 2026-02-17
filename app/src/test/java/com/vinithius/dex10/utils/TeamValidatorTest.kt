package com.vinithius.dex10.utils

import com.vinithius.dex10.datasource.database.EntityMock
import com.vinithius.dex10.datasource.database.TeamMemberEntity
import com.vinithius.dex10.datasource.database.TeamWithMembers
import com.vinithius.dex10.datasource.model.EVs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TeamValidatorTest {

    private fun createMember(id: Int, pokemonId: Int, position: Int, item: String? = null, evs: String? = null): TeamMemberEntity {
        return TeamMemberEntity(
            id = id,
            teamId = 1,
            pokemonId = pokemonId,
            position = position,
            nickname = null,
            ability = null,
            item = item,
            move1 = null,
            move2 = null,
            move3 = null,
            move4 = null,
            evs = evs ?: EVs().toJson(),
            ivs = null
        )
    }

    @Test
    fun `validateTeam should return DuplicateSpecies when team has duplicate pokemon`() {
        val team = TeamWithMembers(
            team = EntityMock.createTeam(1, "Test Team"),
            members = listOf(
                createMember(1, 6, 0), // Charizard
                createMember(2, 6, 1), // Duplicate Charizard
                createMember(3, 25, 2)
            )
        )

        val issues = TeamValidator.validateTeam(team)
        
        val speciesIssue = issues.filterIsInstance<TeamValidator.ValidationIssue.DuplicateSpecies>().firstOrNull()
        assertTrue("Should detect duplicate species", speciesIssue != null)
        assertEquals(6, speciesIssue?.pokemonId)
        assertEquals(listOf(0, 1), speciesIssue?.positions)
    }

    @Test
    fun `validateTeam should return DuplicateItem for case-insensitive and trimmed item names`() {
        val team = TeamWithMembers(
            team = EntityMock.createTeam(1, "Test Team"),
            members = listOf(
                createMember(1, 1, 0, item = "Life Orb"),
                createMember(2, 2, 1, item = " life orb "), // Duplicate with space and case difference
                createMember(3, 3, 2, item = "none"), // Should be ignored
                createMember(4, 4, 3, item = "")     // Should be ignored
            )
        )

        val issues = TeamValidator.validateTeam(team)
        
        val itemIssue = issues.filterIsInstance<TeamValidator.ValidationIssue.DuplicateItem>().firstOrNull()
        assertTrue("Should detect duplicate item", itemIssue != null)
        assertEquals("life orb", itemIssue?.item)
        assertEquals(listOf(0, 1), itemIssue?.positions)
    }

    @Test
    fun `validateTeam should return empty list for a valid competitive team`() {
        val team = TeamWithMembers(
            team = EntityMock.createTeam(1, "Test Team"),
            members = listOf(
                createMember(1, 1, 0, item = "Choice Band"),
                createMember(2, 2, 1, item = "Choice Specs"),
                createMember(3, 3, 2, item = "none"),
                createMember(4, 4, 3, item = null)
            )
        )

        val issues = TeamValidator.validateTeam(team)
        assertTrue("Valid team should have no issues", issues.isEmpty())
    }

    @Test
    fun `validateMember should detect invalid EV totals over 510`() {
        val invalidEvJson = "{\"hp\":252, \"atk\":252, \"def\":8}" // 512 total
        val invalidMember = createMember(1, 1, 0, evs = invalidEvJson)
        val validMember = createMember(2, 1, 1, evs = EVs(hp = 252, atk = 252, def = 6).toJson())    // 510 total

        val invalidIssues = TeamValidator.validateMember(invalidMember)
        val validIssues = TeamValidator.validateMember(validMember)

        assertTrue("Should detect invalid EVs", invalidIssues.any { it is TeamValidator.ValidationIssue.InvalidEVs })
        assertTrue("Should not detect issues for 510 EVs", validIssues.isEmpty())
        
        val evIssue = invalidIssues.filterIsInstance<TeamValidator.ValidationIssue.InvalidEVs>().first()
        assertEquals(512, evIssue.total)
    }
}
