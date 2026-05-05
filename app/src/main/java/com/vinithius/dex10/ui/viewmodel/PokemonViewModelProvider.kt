package com.vinithius.dex10.ui.viewmodel

import androidx.compose.runtime.Composable
import org.koin.androidx.compose.get

/**
 * Returns the application-wide [PokemonViewModel] instance directly from Koin.
 *
 * IMPORTANT: [PokemonViewModel] is registered as a Koin `single`, so it MUST NOT be obtained
 * through `getViewModel()` / `by viewModel()` (which would register the singleton in a
 * `ViewModelStore`). Doing so causes `onCleared()` to be invoked — and `viewModelScope` to
 * be cancelled — whenever any owning store is cleared (e.g. a `NavBackStackEntry` is popped
 * after navigating to a detail screen). After that, every coroutine launched from
 * `viewModelScope` becomes a no-op, freezing filters, search and clear-all actions.
 *
 * Using Koin's `get()` keeps the singleton entirely outside any `ViewModelStore`, so its
 * lifetime is tied to the application — exactly what a `single` is for.
 */
@Composable
fun rememberPokemonViewModel(): PokemonViewModel = get()

