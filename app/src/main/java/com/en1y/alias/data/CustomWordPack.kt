package com.en1y.alias.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateListOf
import androidx.core.content.edit
import com.en1y.alias.logic.AppLanguage
import com.en1y.alias.logic.DeviceLanguage

object CustomWordPack {
    val words = mutableStateListOf<String>()
    private var preferences : SharedPreferences? = null
    private var language: String? = null

    fun load(context: Context) {
        if (AppLanguage == language) return
        language = AppLanguage
        val p = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        preferences = p
        words.clear()
        words += p.getString("customWords${language}", "")!!.lines().filter { it.isNotBlank() }
    }

    fun add(word: String) {
        val w = word.replace("\n", " ").trim()
        if (w.isEmpty() || words.any { it.equals(w, ignoreCase = true) } || w.toIntOrNull() != null || w.length > 100) return
        words += w
        save()
    }

    fun remove(word: String) {
        words -= word
        save()
    }

    private fun save() = preferences?.edit { putString("customWords${language}", words.joinToString("\n")) }
}