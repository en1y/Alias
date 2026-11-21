package com.en1y.alias.ui.activities

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.en1y.alias.R
import com.en1y.alias.logic.DeviceLanguage
import com.en1y.alias.logic.SupportedLanguages
import com.en1y.alias.logic.normalizeLanguage
import com.en1y.alias.ui.theme.AliasTheme
import androidx.core.content.edit

class WelcomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
            when (step) {
                0 -> Greeting(onNext = {step = 1})
                1 -> ChooseLanguageScreen()
            }
        }
    }
}

fun Modifier.verticalScrollbar(state: ScrollState, color: Color, width: Dp = 4.dp) = drawWithContent {
    drawContent()
    if (state.maxValue == 0) return@drawWithContent // content fits, no scrollbar
    val w = width.toPx()
    val barHeight = size.height * size.height / (size.height + state.maxValue)
    val y = state.value.toFloat() / state.maxValue * (size.height - barHeight)
    drawRoundRect(color, Offset(size.width - w, y), Size(w, barHeight), CornerRadius(w / 2))
}

@Composable @Preview
fun Greeting(modifier: Modifier = Modifier, onNext: () -> Unit = {}) {
    AliasTheme {
        Row (
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ){
            Column (
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ){
                Text(
                    text = stringResource(R.string.welcome_message),
                    modifier = modifier,
                    color = MaterialTheme.colorScheme.onBackground)
                Button(onClick = onNext,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    )) {
                    Text(stringResource(R.string.continue_button))
                }
            }
        }
    }
}

@Composable @Preview
fun ChooseLanguageScreen(onNext: () -> Unit = {}) {
    var selectedLanguage by remember {
        mutableStateOf(AppCompatDelegate.getApplicationLocales()[0]?.language?.let(::normalizeLanguage) ?: DeviceLanguage)
    }
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    AliasTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(stringResource(R.string.choose_language),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 20.dp)) // because of camera in the top and the notifications
            Text(stringResource(R.string.choose_language_description), color = MaterialTheme.colorScheme.onBackground)
            Column (
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .verticalScrollbar(scrollState, MaterialTheme.colorScheme.onPrimary)
                    .padding(16.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                stringArrayResource(R.array.languages).forEachIndexed { i, s ->
                    val selected = SupportedLanguages[i] == selectedLanguage
                    Button(
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
            Button(
                onClick = {
                        context.getSharedPreferences("settings", Context.MODE_PRIVATE).edit {
                            putBoolean("welcomed", true)
                        }
                    onNext()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                )
            ) {
                Text(text = stringResource(R.string.continue_button))
            }
        }
    }
}
