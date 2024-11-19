import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vinithius.poke10.datasource.database.Ability
import com.vinithius.poke10.datasource.database.PokemonEntity
import com.vinithius.poke10.datasource.database.PokemonWithDetails
import com.vinithius.poke10.datasource.database.Stat
import com.vinithius.poke10.datasource.database.StatType
import com.vinithius.poke10.datasource.database.Type
import com.vinithius.poke10.extension.capitalize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GetFilterBar(pokemonWithDetails: List<PokemonWithDetails>) {

    val sheetState = rememberModalBottomSheetState()
    val coroutineScope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }

    // Extração de filtros
    val types =
        pokemonWithDetails.flatMap { pokemon -> pokemon.types.map { it.typeName } }.distinct()
    val abilities =
        pokemonWithDetails.flatMap { pokemon -> pokemon.abilities.map { it.name } }.distinct()
    val colors = pokemonWithDetails.map { it.pokemon.color }.distinct()
    val habitats = pokemonWithDetails.map { it.pokemon.habitat }.distinct()
    val favorite = listOf(true, false) // Filtro booleano para favoritos

    // Montagem da lista de filtros
    val filterList = listOf(
        "type" to types,
        "ability" to abilities,
        "color" to colors,
        "habitat" to habitats,
        "favorite" to favorite.map { if (it) "Favorite" else "Not Favorite" }
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.padding(10.dp)
    ) {
        items(filterList) { filter ->
            FilterChip(
                label = filter.first,
                onClick = {
                    showBottomSheet = showBottomSheet.not()
                }
            )
        }
    }

    // BottomSheet
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showBottomSheet = false
            },
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 16.dp,
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .width(50.dp)
                        .height(6.dp)
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Book Your Flight",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            showBottomSheet = false
                        }
                    }
                }) {
                    Text("Hide Bottom Sheet")
                }
            }
        }
    }
}

@Composable
fun FilterChip(
    label: String,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .clip(shape = RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.onSecondary)
            .shadow(
                elevation = 1.dp,
                shape = RoundedCornerShape(4.dp),
                clip = true
            )
            .clickable {
                onClick.invoke()
            },
    ) {
        Text(
            text = label.capitalize(),
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(4.dp),
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Normal,
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheet(
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    sheetState: SheetState = rememberModalBottomSheetState(),
) {
    var showBottomSheet by remember { mutableStateOf(false) }
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showBottomSheet = false
            },
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 16.dp,
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .width(50.dp)
                        .height(6.dp)
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Book Your Flight",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            showBottomSheet = false
                        }
                    }
                }) {
                    Text("Hide Bottom Sheet")
                }
            }
        }
    }
}

@Preview
@Composable
fun Preview() {
    GetFilterBar(getMockupPokemonList())
}

private fun getMockupPokemonList(): List<PokemonWithDetails> {
    return listOf(
        PokemonWithDetails(
            pokemon = PokemonEntity(
                1,
                "bulbasaur",
                "green",
                "grassland",
                true,
                "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/showdown/1.gif",
            ),
            types = listOf(Type(1, "grass"), Type(2, "poison")),
            abilities = listOf(
                Ability(1, "overgrow", true, 10),
                Ability(2, "chlorophyll", false, 20)
            ),
            stats = listOf(
                Stat(1, StatType.HP, 10, 10),
                Stat(2, StatType.ATTACK, 10, 10),
                Stat(3, StatType.DEFENSE, 10, 10),
                Stat(4, StatType.SPECIAL_ATTACK, 10, 10),
                Stat(5, StatType.SPECIAL_DEFENSE, 10, 10),
                Stat(6, StatType.SPEED, 10, 10),
            )
        ),
        PokemonWithDetails(
            pokemon = PokemonEntity(
                2,
                "pikachu",
                "yellow",
                "grassland",
                false,
                "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/showdown/1.gif",
            ),
            types = listOf(Type(1, "grass"), Type(2, "poison")),
            abilities = listOf(
                Ability(1, "overgrow", true, 11),
                Ability(2, "chlorophyll", false, 22)
            ),
            stats = listOf(
                Stat(1, StatType.HP, 11, 10),
                Stat(2, StatType.ATTACK, 16, 10),
                Stat(3, StatType.DEFENSE, 20, 10),
                Stat(4, StatType.SPECIAL_ATTACK, 5, 10),
                Stat(5, StatType.SPECIAL_DEFENSE, 90, 10),
                Stat(6, StatType.SPEED, 5, 10),
            )
        ),
    )
}