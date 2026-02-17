package com.vinithius.dex10.ui.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.vinithius.dex10.datasource.data.PremiumManager
import com.vinithius.dex10.datasource.repository.IPokemonRepository
import com.vinithius.dex10.datasource.repository.ITeamRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
@OptIn(ExperimentalCoroutinesApi::class)
class TeamViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val teamRepository: ITeamRepository = mockk(relaxed = true)
    private val pokemonRepository: IPokemonRepository = mockk(relaxed = true)
    private val premiumManager: PremiumManager = mockk(relaxed = true)

    private lateinit var viewModel: TeamViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        // Mock AnalyticsManager static calls
        mockkObject(com.vinithius.dex10.analytics.AnalyticsManager)
        every { com.vinithius.dex10.analytics.AnalyticsManager.logLimitReached(any()) } returns Unit

        viewModel = TeamViewModel(teamRepository, pokemonRepository, premiumManager, testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `createTeam_succeeds_whenPremium`() = runTest(testDispatcher) {
        // Given user is premium
        every { premiumManager.isPremium } returns MutableStateFlow(true)
        // And has 5 teams (above free limit)
        coEvery { teamRepository.getTeamCount() } returns 5
        coEvery { teamRepository.createTeam(any()) } returns 10L

        // When creating a team
        var resultId: Long? = null
        viewModel.createTeam("New Team") { resultId = it }
        testScheduler.advanceUntilIdle()

        // Then it succeeds
        assertTrue(resultId != null)
        verify(exactly = 0) { com.vinithius.dex10.analytics.AnalyticsManager.logLimitReached(any()) }
    }

    @Test
    fun `createTeam_succeeds_whenFreemium_andUnderLimit`() = runTest(testDispatcher) {
        // Given user is freemium
        every { premiumManager.isPremium } returns MutableStateFlow(false)
        // And has 0 teams (under limit of 1)
        coEvery { teamRepository.getTeamCount() } returns 0
        coEvery { teamRepository.createTeam(any()) } returns 10L

        // When creating a team
        var resultId: Long? = null
        viewModel.createTeam("New Team") { resultId = it }
        testScheduler.advanceUntilIdle()

        // Then it succeeds
        assertTrue(resultId != null)
    }

    @Test
    fun `createTeam_fails_whenFreemium_andLimitReached`() = runTest(testDispatcher) {
        // Given user is freemium
        every { premiumManager.isPremium } returns MutableStateFlow(false)
        // And has 1 team (limit reached)
        coEvery { teamRepository.getTeamCount() } returns 1

        // When creating a team
        var resultId: Long? = -1
        viewModel.createTeam("New Team") { resultId = it }
        testScheduler.advanceUntilIdle()

        // Then it fails (returns null)
        assertTrue(resultId == null)
        
        // And Upsell is triggered
        assertTrue(viewModel.showUpsell.value)
        
        // And Analytics is logged
        verify { com.vinithius.dex10.analytics.AnalyticsManager.logLimitReached("team_limit") }
    }
}
