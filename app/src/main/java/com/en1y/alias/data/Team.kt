package com.en1y.alias.data

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import com.en1y.alias.data.TeamInfo.Companion.random

class Team {
    val info = random()
    var points: Int = 0
        private set

    fun addPoints(pointsToAdd: Int) {
        points += pointsToAdd
    }

    val teamName: String
        @Composable get() = info.title

    val teamImage: Painter
        @Composable get() = info.image
}
