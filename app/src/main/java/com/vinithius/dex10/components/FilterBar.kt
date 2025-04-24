import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vinithius.dex10.R
import com.vinithius.dex10.extension.capitalize
import com.vinithius.dex10.ui.MainActivity
import com.vinithius.dex10.ui.viewmodel.PokemonViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel

@Composable
private fun getActivity(): MainActivity? {
    val context = LocalContext.current
    val activity = context as? MainActivity
    return activity
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GetFilterBar(
    viewModel: PokemonViewModel = getViewModel(),
    onCallBackClearFavoriteFilter: () -> Unit,
    onCallBackFilter: (filter: Map<String, SnapshotStateMap<String, Boolean>>) -> Unit = {}
) {
    val activity = getActivity()
    val filterMap by viewModel.pokemonFilterList.observeAsState(mapOf())
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    var labelTitle by remember { mutableStateOf(String()) }
    var loading by remember { mutableStateOf(false) }

    val filterList = mutableListOf<String>().apply {
        add("first")
        addAll(filterMap.keys)
    }

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.padding(10.dp)
    ) {
        itemsIndexed(filterList) { _, filter ->
            when (filter) {
                "first" -> {
                    ViewHolderFirst(
                        loading = loading,
                        onClick = {
                            onCallBackClearFavoriteFilter.invoke()
                            activity?.trackButtonClick("Menu filter: Clear All")
                            loading = true
                            filterMap.keys.toList().forEach {
                                filterMap[it]?.let { clearMap ->
                                    clearAllFilter(clearMap)
                                }
                            }
                            onCallBackFilter.invoke(filterMap)
                            CoroutineScope(Dispatchers.Main).launch {
                                delay(500)
                                loading = false
                            }
                        }
                    )
                }

                else -> {
                    ViewHolder(
                        label = filter,
                        filterMap = filterMap[filter],
                        onClick = { label ->
                            activity?.trackButtonClick("Menu filter: $label")
                            labelTitle = label
                            showBottomSheet = showBottomSheet.not()
                        }
                    )
                }
            }
        }
    }

    // BottomSheet
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showBottomSheet = false
                onCallBackFilter.invoke(filterMap)
            },
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            containerColor = MaterialTheme.colorScheme.secondary,
            tonalElevation = 16.dp,
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .width(50.dp)
                        .height(6.dp)
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.onSecondary)
                )
            }
        ) {
            ContentBottomSheet(
                labelTitle,
                filterMap[labelTitle]!!,
                sheetState
            ) {
                showBottomSheet = it
                onCallBackFilter.invoke(filterMap)
            }
        }
    }
}

@Composable
fun ViewHolder(
    label: String,
    filterMap: SnapshotStateMap<String, Boolean>?,
    onClick: (label: String) -> Unit = {}
) {
    BadgedBox(
        badge = {
            val count = filterMap?.values?.count { it }
            count?.takeIf { it > 0 }?.run {
                Badge(
                    containerColor = Color.Red,
                    contentColor = Color.White
                ) {
                    Text(count.toString())
                }
            }
        }
    ) {
        Box(
            modifier = Modifier
                .clip(shape = RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.secondary)
                .clickable {
                    onClick.invoke(label)
                }
                .padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = label.capitalize(),
                    color = MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier.padding(4.dp),
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Normal,
                    )
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Close",
                    tint = MaterialTheme.colorScheme.onSecondary
                )
            }
        }
    }
}

@Composable
fun ViewHolderFirst(
    loading: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(shape = RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.secondary)
            .clickable {
                if (!loading) {
                    onClick.invoke()
                }
            }
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.clear_all),
                color = MaterialTheme.colorScheme.onSecondary,
                modifier = Modifier.padding(4.dp),
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Normal,
                )
            )
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onSecondary
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.clear_all),
                    tint = MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier
                        .width(20.dp)
                        .height(20.dp)
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentBottomSheet(
    labelTitle: String,
    filterMap: SnapshotStateMap<String, Boolean>,
    sheetState: SheetState,
    onClickListener: (value: Boolean) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val activity = getActivity()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 80.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = labelTitle.capitalize(),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                )
                IconButton(
                    onClick = {
                        activity?.trackButtonClick("Close bottom sheet")
                        coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (sheetState.isVisible.not()) {
                                onClickListener.invoke(false)
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = stringResource(R.string.close),
                        tint = MaterialTheme.colorScheme.onSecondary
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(8.dp)
            ) {
                items(filterMap.keys.toList()) { filter ->
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f))
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = filterMap[filter] == true,
                            onCheckedChange = { isChecked ->
                                activity?.trackButtonClick("$filter : $isChecked")
                                filterMap[filter] = isChecked
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = filter.capitalize(), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
        // Buttons fixed bellow
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    activity?.trackButtonClick("Bottom sheet: Clear all")
                    clearAllFilter(filterMap)
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.clear_all))
            }
        }
    }
}

private fun clearAllFilter(filterMap: SnapshotStateMap<String, Boolean>) {
    filterMap.keys.toList().forEach {
        filterMap[it] = false
    }
}


@Preview
@Composable
fun Preview() {
    GetFilterBar(
        onCallBackClearFavoriteFilter = {}
    )
}
