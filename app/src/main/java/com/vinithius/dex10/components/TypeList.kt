package com.vinithius.dex10.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vinithius.dex10.extension.getDrawableIco
import com.vinithius.dex10.extension.getDrawableIcoColor
import com.vinithius.dex10.extension.getStringType
import com.vinithius.dex10.datasource.database.Type as TypeDataBase
import com.vinithius.dex10.datasource.response.Type as TypeResponse

@Composable
fun TypeListResponse(
    types: List<TypeResponse>
) {
    if (types.isNotEmpty()) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(start = 8.dp),
        ) {
            items(
                items = types,
                key = { data -> data.type.name!! }
            ) { type ->
                TypeItem(type.type.name ?: String())
            }
        }
    } else {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(start = 8.dp),
        ) {
            TypeItemShimmer()
        }
    }
}

@Composable
fun TypeListDataBase(
    types: List<TypeDataBase>,
    choiceOfTheDayStatus: Boolean = false,
    hidePokemonOfTheDay: Boolean = false
) {
    if (types.isNotEmpty()) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .padding(start = 8.dp)
                .alpha(if (hidePokemonOfTheDay && choiceOfTheDayStatus) 0f else 1f)
        ) {
            items(
                items = types,
                key = { data -> data.id }
            ) { type ->
                TypeItem(type.typeName)
            }
        }
    } else {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(start = 8.dp),
        ) {
            TypeItemShimmer()
        }
    }
}

@Composable
fun TypeItem(typeName: String) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .background(
                color = typeName.getDrawableIcoColor(),
                shape = RoundedCornerShape(100)
            )
            .shadow(
                elevation = 1.dp,
                shape = RoundedCornerShape(16.dp),
                clip = false
            )
            .padding(horizontal = 5.dp, vertical = 5.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Image(
                painter = painterResource(
                    id = typeName.getDrawableIco()
                ),
                contentDescription = typeName,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = typeName.getStringType(context),
                color = Color.White,
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Normal,
                    shadow = Shadow(
                        color = Color.Black,
                        offset = Offset(
                            1f,
                            1f
                        ),
                        blurRadius = 0.5f
                    )
                ),
                modifier = Modifier.padding(end = 2.dp)
            )
        }
    }
}

@Composable
fun TypeItemShimmer() {
    Box(
        modifier = Modifier
            .background(
                color = Color.LightGray,
                shape = RoundedCornerShape(100)
            )
            .shadow(
                elevation = 1.dp,
                shape = RoundedCornerShape(16.dp),
                clip = false
            )
            .padding(horizontal = 5.dp, vertical = 5.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(
                        color = Color.Gray,
                        shape = RoundedCornerShape(100)
                    )
            )
            Text(
                text = "Loading",
                color = Color.White,
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Normal,
                    shadow = Shadow(
                        color = Color.Black,
                        offset = Offset(
                            1f,
                            1f
                        ),
                        blurRadius = 0.5f
                    )
                ),
                modifier = Modifier.padding(end = 2.dp)
            )
        }
    }
}