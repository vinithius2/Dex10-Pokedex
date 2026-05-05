package com.vinithius.dex10.ui.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.MutableLiveData
import com.vinithius.dex10.datasource.data.AppPreferences
import com.vinithius.dex10.datasource.data.PremiumManager
import com.vinithius.dex10.datasource.database.PokemonEntity
import com.vinithius.dex10.datasource.database.PokemonWithDetails
import com.vinithius.dex10.datasource.repository.IPokemonRepository
import com.vinithius.dex10.datasource.response.Pokemon
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.Collections

import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
@OptIn(ExperimentalCoroutinesApi::class)
class PokemonViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val repository: IPokemonRepository = mockk(relaxed = true)
    private val premiumManager: PremiumManager = mockk(relaxed = true)
    private val appPreferences: AppPreferences = mockk(relaxed = true)

    private lateinit var viewModel: PokemonViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        every { appPreferences.viewMode } returns MutableStateFlow(AppPreferences.ViewMode.LIST)

        // Mock AnalyticsManager
        mockkObject(com.vinithius.dex10.analytics.AnalyticsManager)
        every { com.vinithius.dex10.analytics.AnalyticsManager.logLimitReached(any()) } returns Unit

        viewModel = PokemonViewModel(repository, premiumManager, appPreferences, testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createPokemonList(count: Int, favorites: Int): List<PokemonWithDetails> {
        val list = ArrayList<PokemonWithDetails>()
        for (i in 1..count) {
            val isFav = i <= favorites
            val pokemon = PokemonEntity(
                id = i,
                name = "Pokemon $i",
                color = "Red",
                habitat = "Forest",
                favorite = isFav,
                imagePath = null
            )
            list.add(PokemonWithDetails(
                pokemon = pokemon,
                types = emptyList(),
                abilities = emptyList(),
                stats = emptyList()
            ))
        }
        return list
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> getMutableLiveData(fieldName: String): MutableLiveData<T> {
        val field = PokemonViewModel::class.java.getDeclaredField(fieldName)
        field.isAccessible = true
        return field.get(viewModel) as MutableLiveData<T>
    }

    @Test
    fun `setFavorite_add_succeeds_whenPremium`() = runTest(testDispatcher) {
        // Given user is premium
        every { premiumManager.isPremium } returns MutableStateFlow(true)
        
        // And list has 60 favorites (above free limit)
        val initialList = createPokemonList(100, 60)
        // Access private _pokemonList via reflection or public setter if available? 
        // Or mock repository to return it. ViewModel.getPokemonList populates it.
        // For testing, we can inject logic or use a spy.
        // But checking `setFavorite` logic: it reads `_pokemonList.value`. 
        // We can set it directly using reflection for testing
        val field = PokemonViewModel::class.java.getDeclaredField("_pokemonList")
        field.isAccessible = true
        (field.get(viewModel) as MutableLiveData<List<PokemonWithDetails>>).value = initialList

        // When toggling a non-favorite to favorite
        val targetId = 61
        viewModel.setFavorite(targetId)
        testScheduler.advanceUntilIdle()

        // Then it succeeds (calls repository)
        coVerify { repository.setFavorite(any()) }
        // And NO upsell
        verify(exactly = 0) { premiumManager.triggerUpsell() }
    }

    @Test
    fun `setFavorite_add_succeeds_whenFreemium_andUnderLimit`() = runTest(testDispatcher) {
        // Given user is freemium
        every { premiumManager.isPremium } returns MutableStateFlow(false)
        
        // And list has 49 favorites (under limit of 50)
        val initialList = createPokemonList(100, 49)
        val field = PokemonViewModel::class.java.getDeclaredField("_pokemonList")
        field.isAccessible = true
        (field.get(viewModel) as MutableLiveData<List<PokemonWithDetails>>).value = initialList

        // When toggling a non-favorite to favorite (making it 50)
        val targetId = 50
        viewModel.setFavorite(targetId)
        testScheduler.advanceUntilIdle()

        // Then it succeeds
        coVerify { repository.setFavorite(any()) }
    }

    @Test
    fun `setFavorite_add_fails_whenFreemium_andLimitReached`() = runTest(testDispatcher) {
        // Given user is freemium
        every { premiumManager.isPremium } returns MutableStateFlow(false)
        
        // And list has 50 favorites (limit reached)
        val initialList = createPokemonList(100, 50)
        val field = PokemonViewModel::class.java.getDeclaredField("_pokemonList")
        field.isAccessible = true
        (field.get(viewModel) as MutableLiveData<List<PokemonWithDetails>>).value = initialList

        // When toggling a non-favorite to favorite (trying to make it 51)
        val targetId = 51
        viewModel.setFavorite(targetId)
        testScheduler.advanceUntilIdle()

        // Then it does NOT call repository
        coVerify(exactly = 0) { repository.setFavorite(any()) }
        
        // And triggers Upsell
        verify { premiumManager.triggerUpsell() }
        
        // And logs analytics
        verify { com.vinithius.dex10.analytics.AnalyticsManager.logLimitReached("favorite_limit") }
    }

    @Test
    fun `setFavorite_updatesListReference_toTriggerCompose`() = runTest(testDispatcher) {
        // Given
        every { premiumManager.isPremium } returns MutableStateFlow(true)
        val initialList = createPokemonList(5, 0)
        
        val field = PokemonViewModel::class.java.getDeclaredField("_pokemonList")
        field.isAccessible = true
        (field.get(viewModel) as MutableLiveData<List<PokemonWithDetails>>).value = initialList

        // When
        viewModel.setFavorite(1)
        testScheduler.advanceUntilIdle()

        // Then
        val updatedList = viewModel.pokemonList.value!!
        // Reference check: List should be new (not same object)
        assertTrue("List reference should change", initialList !== updatedList)
        
        // Item check: Item should be new (not same object)
        val updatedItem = updatedList.find { it.pokemon.id == 1 }!!
        val originalItem = initialList.find { it.pokemon.id == 1 }!!
        assertTrue("Item reference should change", updatedItem !== originalItem)
        
        // Value check
        assertTrue("Favorite should be true", updatedItem.pokemon.favorite)
    }

    @Test
    fun `setFavorite_keepsBackupListWhole_whenCurrentListIsFilteredSubset`() = runTest(testDispatcher) {
        every { premiumManager.isPremium } returns MutableStateFlow(true)

        val backupList = createPokemonList(5, 0)
        val filteredList = backupList.take(2)

        getMutableLiveData<List<PokemonWithDetails>>("_pokemonList").value = filteredList
        getMutableLiveData<List<PokemonWithDetails>>("_pokemonListBackup").value = backupList

        viewModel.setFavorite(1)
        testScheduler.advanceUntilIdle()

        val updatedBackup = viewModel.pokemonListBackup.value!!
        assertTrue("Backup list should keep the full dataset", updatedBackup.size == 5)
        assertTrue("Backup list should still contain items outside the filtered subset", updatedBackup.any { it.pokemon.id == 5 })
        assertTrue("Updated favorite state should also be reflected in backup", updatedBackup.first { it.pokemon.id == 1 }.pokemon.favorite)
    }

    @Test
    fun `clearAllFilters_resets_search_favorite_and_selected_filters`() = runTest(testDispatcher) {
        val context = mockk<android.content.Context>(relaxed = true)
        val typeFilterKey = "type"
        val filter = mapOf(
            typeFilterKey to mutableStateMapOf(
                "fire" to true,
                "water" to false,
            )
        )

        getMutableLiveData<Map<String, androidx.compose.runtime.snapshots.SnapshotStateMap<String, Boolean>>>("_filterMap").value = filter
        getMutableLiveData<Map<String, androidx.compose.runtime.snapshots.SnapshotStateMap<String, Boolean>>>("_pokemonFilterList").value = filter
        getMutableLiveData<Boolean>("_isFavoriteFilter").value = true
        getMutableLiveData<String>("_searchNameFilter").value = "pikachu"

        viewModel.clearAllFilters(context)
        testScheduler.advanceUntilIdle()

        assertFalse(viewModel.isFavoriteFilter.value ?: true)
        assertTrue(viewModel.searchNameFilter.value.isNullOrEmpty())
        assertTrue(viewModel.filterMap.value?.get(typeFilterKey)?.values?.none { it } == true)
        assertTrue(viewModel.pokemonFilterList.value?.get(typeFilterKey)?.values?.none { it } == true)
    }
}
