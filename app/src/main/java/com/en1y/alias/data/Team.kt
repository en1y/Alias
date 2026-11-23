package com.en1y.alias.data

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.painter.Painter
import com.en1y.alias.data.TeamInfo.Companion.random

class Team {
    val info = random()
    var points by mutableIntStateOf(0)
    var roundNum by mutableIntStateOf(0)

    fun addPoints(pointsToAdd: Int) {
        points += pointsToAdd
    }

    fun addRoundPlayed() {
        roundNum++
    }

    val teamName: String
        @Composable get() = info.title

    val teamImage: Painter
        @Composable get() = info.image
}
