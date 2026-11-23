package com.en1y.alias.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.en1y.alias.R
import com.en1y.alias.logic.MaxTeamsSize
import com.en1y.alias.logic.MinTeamsSize
import com.en1y.alias.manager.TeamManager

class GameSetting {
    var wordPacks by mutableStateOf(emptySet<WordPack>())
    val tm = TeamManager()
    var timePerRound by mutableIntStateOf(60)
    var wordsWinCondition by mutableIntStateOf(100)
    var lastWordCanBeGuessed by mutableStateOf(false)
    var skippedWordSubtractsPoints by mutableStateOf(true)

    fun canStartUpGame(): Boolean {
        return wordPacks.isNotEmpty() && tm.teams.size in MinTeamsSize..MaxTeamsSize
    }

    fun gameStartupCheck(context: Context): String {
        if (wordPacks.isEmpty())
            return context.getString(R.string.error_no_word_packs)
        if (tm.teams.size < MinTeamsSize)
            return context.getString(R.string.error_too_few_teams, MinTeamsSize)
        if (tm.teams.size > MaxTeamsSize)
            return context.getString(R.string.error_too_many_teams, MaxTeamsSize)
        return ""
    }

}
