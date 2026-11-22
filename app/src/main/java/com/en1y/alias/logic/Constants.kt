package com.en1y.alias.logic

import android.content.res.Resources

const val MaxTeamsSize = 8
const val MinTeamsSize = 1;

val SupportedLanguages = listOf(
    "hr", "cs", "da", "nl", "en", "fi", "fr", "de", "el", "hu", "id",
    "it", "nb", "pl", "pt", "ro", "ru", "sk", "es", "sv", "tr", "uk"
)

// some codes are old: Indonesian in, Norwegian no
fun normalizeLanguage(lang: String) = when (lang) {
    "in" -> "id"
    "no" -> "nb"
    else -> lang
}

val DeviceLanguage: String
    get() {
        var lang = normalizeLanguage(Resources.getSystem().configuration.locales[0].language)
        if (lang !in SupportedLanguages) lang =  "en"
        return lang
    }
