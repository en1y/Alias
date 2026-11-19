package com.en1y.alias.data

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.en1y.alias.R

enum class TeamInfo(private val imageId: Int, private val titleId: Int) {
    Team0(R.drawable.team0, R.string.team0),
    Team1(R.drawable.team1, R.string.team1),
    Team2(R.drawable.team2, R.string.team2),
    Team3(R.drawable.team3, R.string.team3),
    Team4(R.drawable.team4, R.string.team4),
    Team5(R.drawable.team5, R.string.team5),
    Team6(R.drawable.team6, R.string.team6),
    Team7(R.drawable.team7, R.string.team7),
    Team8(R.drawable.team8, R.string.team8),
    Team9(R.drawable.team9, R.string.team9),
    Team10(R.drawable.team10, R.string.team10),
    Team11(R.drawable.team11, R.string.team11),
    Team12(R.drawable.team12, R.string.team12),
    Team13(R.drawable.team13, R.string.team13),
    Team14(R.drawable.team14, R.string.team14),
    Team15(R.drawable.team15, R.string.team15),
    Team16(R.drawable.team16, R.string.team16);

    // resolved post load when the ids are in
    val image: Painter @Composable get() = painterResource(imageId)
    val title: String @Composable get() = stringResource(titleId)

    companion object {
        fun random() = entries.random()
    }
}
