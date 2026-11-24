package com.en1y.alias.ui.activities

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.en1y.alias.R
import com.en1y.alias.logic.DeviceLanguage
import com.en1y.alias.logic.SupportedLanguages
import com.en1y.alias.logic.normalizeLanguage
import com.en1y.alias.ui.theme.AliasButton
import com.en1y.alias.ui.theme.AliasTheme
import com.en1y.alias.ui.theme.Panel
import com.en1y.alias.ui.theme.ThemeSettings
import com.en1y.alias.ui.theme.secondaryColors
import com.en1y.alias.ui.theme.verticalScrollWithBar
import androidx.compose.material3.ButtonDefaults
import androidx.core.content.edit

class WelcomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeSettings.load(this)
        enableEdgeToEdge()
        setContent {
            var step by rememberSaveable { mutableIntStateOf(0) }

            // first Back shows a warning, a second Back within 2 seconds exits
            var lastBackPress by remember { mutableLongStateOf(0L) }
            BackHandler {
                val now = System.currentTimeMillis()
                if (now - lastBackPress < 2000) finish()
                else {
                    lastBackPress = now
                    Toast.makeText(this, R.string.press_back_again, Toast.LENGTH_SHORT).show()
                }
            }
            AliasTheme {
                Crossfade(step, animationSpec = tween(250), label = "step") { s ->
                    when (s) {
                        0 -> Greeting(onNext = { step = 1 })
                        1 -> ChooseLanguageScreen(onNext = { switchTo(GameActivity::class.java) })
                    }
                }
            }
        }
    }
}

@Composable
@Preview
fun Greeting(modifier: Modifier = Modifier, onNext: () -> Unit = {}) {
    AliasTheme {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box {
                    Image(painterResource(R.drawable.logo_letters), stringResource(R.string.app_name), Modifier.width(320.dp).aspectRatio(63 / 27.5f), colorFilter = ColorFilter.tint(LocalContentColor.current))
                    Image(painterResource(R.drawable.logo_dot), null, Modifier.width(320.dp).aspectRatio(63 / 27.5f))
                }
                Text(
                    text = stringResource(R.string.welcome_message),
                    modifier = modifier, style = MaterialTheme.typography.headlineSmall
                )
                AliasButton(onClick = onNext, colors = ButtonDefaults.secondaryColors()) {
                    Text(stringResource(R.string.continue_button))
                }
            }
        }
    }
}

@Composable
@Preview
fun ChooseLanguageScreen(onNext: () -> Unit = {}) {
    var selectedLanguage by remember {
        mutableStateOf(
            AppCompatDelegate.getApplicationLocales()[0]?.language?.let(::normalizeLanguage) ?: DeviceLanguage
        )
    }
    val context = LocalContext.current

    AliasTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                stringResource(R.string.choose_language),
                modifier = Modifier.padding(top = 20.dp), style = MaterialTheme.typography.headlineSmall
            ) // because of camera in the top and the notifications
            Text(stringResource(R.string.choose_language_description), style = MaterialTheme.typography.bodyMedium)
            Panel(
                modifier = Modifier
                    .weight(1f)
                    .verticalScrollWithBar()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                stringArrayResource(R.array.languages).forEachIndexed { i, s ->
                    val selected = SupportedLanguages[i] == selectedLanguage
                    AliasButton(
                        onClick = {
                            selectedLanguage = SupportedLanguages[i]
                            // reloads the activity in the new language
                            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(selectedLanguage))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selected) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary,
                            contentColor = if (selected) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onSecondary
                        )
                    ) {
                        Text(s)
                    }
                }
            }
            AliasButton(
                onClick = {
                    context.getSharedPreferences("settings", Context.MODE_PRIVATE).edit {
                        putBoolean("welcomed", true)
                    }
                    onNext()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.secondaryColors()
            ) {
                Text(text = stringResource(R.string.continue_button))
            }
        }
    }
}
