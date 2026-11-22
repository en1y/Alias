package com.en1y.alias.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import com.en1y.alias.R
import com.en1y.alias.data.Team
import com.en1y.alias.manager.TeamManager
import com.en1y.alias.ui.theme.AliasTheme
import androidx.core.net.toUri
import com.en1y.alias.data.Game
import com.en1y.alias.data.GameSetting
import com.en1y.alias.data.WordPack
import com.en1y.alias.data.rememberGame

class GameActivity: AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var step by rememberSaveable { mutableIntStateOf(0) }
            val tm = remember { TeamManager() }
            var selectedWordPacks by remember { mutableStateOf(emptySet<WordPack>()) }

            var lastBackPress by remember { mutableLongStateOf(0L) }
            BackHandler {
                if (step == 0) {
                    val now = System.currentTimeMillis()
                    if (now - lastBackPress < 2000) finish()
                    else {
                        lastBackPress = now
                        Toast.makeText(this, R.string.press_back_again, Toast.LENGTH_SHORT).show()
                    }
                } else if(step in 10..20) {
                    step = 1
                } else if(step in 100..200) {
                    step = 10
                } else step--
            }

            var activeGameSetting by remember { mutableStateOf<GameSetting?>(null) }
            val game = activeGameSetting?.let { rememberGame(it) }

            when (step) {
                0 -> TeamSelect(onNext = {step = 1}, tm = tm)
                1 -> GameSettings(
                    onNext = {step = 1},
                    onSettings = {step = 10},
                    onTeamSelect = {step = 0},
                    onWordPackSelect = {step = 11},
                    tm = tm,
                    selectedWordPacks = selectedWordPacks,
                    onStartGame = { setting -> activeGameSetting = setting }
                )
                10 -> AppSettings(onPrevious = {step = 1}, onLanguageChange = {step = 101}, onThemeChange = {step = 102})
                11 -> WordPacksSettings(
                    onPrevious = {step = 1},
                    selectedWordPacks = selectedWordPacks,
                    onSelectionChange = { selectedWordPacks = it }
                )
                101 -> ChooseLanguageScreen(onNext = {step = 10})
                102 -> ThemeChoosingScreen(onPrevious = {step = 10})
            }
        }
    }
}

@Composable
fun TeamIcon(team: Team = Team(), isRemovable: Boolean = true, remove: () -> Unit = {}) {
    Row (
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.secondary)
            .padding(15.dp),
        horizontalArrangement = Arrangement.spacedBy(22.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    )
    {
        Image(painter = team.teamImage, contentDescription = "Team %s image".format(team.teamName))
        Text(modifier = Modifier.weight(1f),
            text = team.teamName,
            color = MaterialTheme.colorScheme.onSecondary)
        Text(text = "%d".format(team.points), color = MaterialTheme.colorScheme.onSecondary)
        if (isRemovable)
            Button(
                onClick = remove,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )){
                Text("x")
            }
    }
}

@Composable
fun TeamSelect(modifier: Modifier = Modifier, tm: TeamManager = remember { TeamManager() }, onNext: () -> Unit = {}) {
    val scrollState = rememberScrollState()

    AliasTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(all = 16.dp)
                .padding(top = 30.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Teams", color = MaterialTheme.colorScheme.onBackground)
            Text("Add and remove teams to prepare for your game", color = MaterialTheme.colorScheme.onBackground)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.primary)
                    .verticalScrollbar(scrollState, MaterialTheme.colorScheme.onPrimary)
                    .padding(all = 16.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                tm.teams.forEach { t -> key(t) { TeamIcon(team = t, remove = { tm.removeTeam(t) }) } }
            }
            Row (
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally)
            ) {
                Button(
                    onClick = {tm.removeTeam()},
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )) {
                    Text("Remove team")
                }
                Button(
                    onClick = {tm.addTeam()},
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )) {
                    Text("Add team")
                }
            }
            Button(
                onClick = onNext,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(text = stringResource(R.string.continue_button))
            }
        }
    }
}

@Composable
fun ThemeChoosingScreen(modifier: Modifier = Modifier, onPrevious: () -> Unit = {}) {
    AliasTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(all = 16.dp)
                .padding(top = 30.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Button(onClick = onPrevious, colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )) {
                Text("<")
            }
            Text("Theme", color = MaterialTheme.colorScheme.onBackground)
            Text("Choose your theme color", color = MaterialTheme.colorScheme.onBackground)
            // TODO: Implement theme color changing
        }
    }
}

@Composable
fun AppSettings(
    modifier: Modifier = Modifier,
    onPrevious: () -> Unit = {},
    onLanguageChange: () -> Unit = {},
    onThemeChange: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    AliasTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScrollbar(scrollState, MaterialTheme.colorScheme.onBackground)
                .verticalScroll(scrollState)
                .background(MaterialTheme.colorScheme.background)
                .padding(all = 16.dp)
                .padding(top = 30.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Button(onClick = onPrevious, colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )) {
                Text("<")
            }
            Text("Settings", color = MaterialTheme.colorScheme.onBackground)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Text("Language", color = MaterialTheme.colorScheme.onPrimary)
                    Button(onClick = onLanguageChange, colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary)) {
                        Text(">")
                    }
                }
                HorizontalDivider(thickness = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Text("Choose theme", color = MaterialTheme.colorScheme.onPrimary)
                    Button(onClick = onThemeChange, colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary)) {
                        Text(">")
                    }
                }
            }
            Text("External links", color = MaterialTheme.colorScheme.onBackground)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Text("App's GitHub page", color = MaterialTheme.colorScheme.onPrimary)
                    Button(onClick = {
                        val urlIntent = Intent(
                            Intent.ACTION_VIEW,
                            "https://github.com/en1y/Alias".toUri()
                        )
                        context.startActivity(urlIntent)
                    }, colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary)) {
                        Text(">")
                    }
                }
                HorizontalDivider(thickness = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Text("Creator's page", color = MaterialTheme.colorScheme.onPrimary)
                    Button(onClick = {
                        val urlIntent = Intent(
                            Intent.ACTION_VIEW,
                            ("https://github.com/en1y").toUri()
                        )
                        context.startActivity(urlIntent)
                    }, colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary)) {
                        Text(">")
                    }
                }
            }

        }
    }
}

@Composable @Preview
fun WordPacksSettings(
    modifier: Modifier = Modifier,
    onPrevious: () -> Unit = {},
    selectedWordPacks: Set<WordPack> = mutableSetOf(),
    onSelectionChange: (Set<WordPack>) -> Unit = {}
){
    val scrollState = rememberScrollState()

    AliasTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(all = 16.dp)
                .padding(top = 30.dp)
                .verticalScrollbar(scrollState, MaterialTheme.colorScheme.onBackground)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Button(onClick = onPrevious, colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )) {
                Text("<")
            }
            Text("Word packs", color = MaterialTheme.colorScheme.onBackground)
            Text("Choose one or multiple word packs", color = MaterialTheme.colorScheme.onBackground)
            @Composable fun WordPackElement(
                wordPack: WordPack,
                name: String,
                description: String,
                words: List<String>
            ) {
                Row(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primary)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ){
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ){
                            Text(name, color = MaterialTheme.colorScheme.onPrimary)
                            Text("%d words".format(words.size), color = MaterialTheme.colorScheme.onPrimary)
                        }
                        Text(description, color = MaterialTheme.colorScheme.onPrimary)
                    }
                    Checkbox(
                        checked = selectedWordPacks.contains(wordPack),
                        onCheckedChange = { checked ->
                            val updatedSelection = selectedWordPacks.toMutableSet()
                            if (checked) updatedSelection.add(wordPack)
                            else updatedSelection.remove(wordPack)
                            onSelectionChange(updatedSelection)
                        }
                    )
                }
            }
            WordPack.entries.forEach {
                WordPackElement(it, it.title, it.description, it.words)
            }
        }
    }
}

@Composable
fun GameSettings(
    modifier: Modifier = Modifier,
    tm: TeamManager = remember { TeamManager() },
    onNext: () -> Unit = {},
    onSettings: () -> Unit = {},
    onTeamSelect: () -> Unit = {},
    onWordPackSelect: () -> Unit = {},
    selectedWordPacks: Set<WordPack> = emptySet(),
    onStartGame: (GameSetting) -> Unit = {}
) {
    var roundLengthSliderPosition by remember { mutableFloatStateOf(60f) }
    var wordsRequiredForWinningSliderPosition by remember { mutableFloatStateOf(100f) }
    var lastWordSetting by remember { mutableStateOf(false) }
    var skippedWordSetting by remember { mutableStateOf(true) }

    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        AliasTheme {
            Popup(
                alignment = Alignment.Center,
                onDismissRequest = { showDialog = false }
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    shadowElevation = 6.dp
                ) {
                    Column(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.tertiary)
                            .padding(15.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ){
                        Text(text = "Start a new game?")
                        Text(text = "This will reset this game completely, including both the scores and the rounds")
                        Button({}) {
                            Text("New Game")
                        }
                        Button({showDialog = false}) {
                            Text("Cancel")
                        }
                    }
                }
            }
        }
    }


    val scrollState = rememberScrollState()

    AliasTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScrollbar(scrollState, MaterialTheme.colorScheme.onBackground)
                .verticalScroll(scrollState)
                .background(MaterialTheme.colorScheme.background)
                .padding(all = 16.dp)
                .padding(top = 30.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(modifier = Modifier.padding(end = 10.dp), text = "Game Settings", color = MaterialTheme.colorScheme.onBackground)
                Button(
                    onClick = {showDialog = true},
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    )
                ) {
                    Text("Reset")
                }
                Button(
                    onClick = onSettings,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    )
                ) {
                    Text("Settings")
                }
            }
            Button(
                modifier = Modifier
                    .fillMaxWidth(),
                onClick = onWordPackSelect,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column (
                        horizontalAlignment = Alignment.Start
                    ){
                        Text("Word packs")
                        if (selectedWordPacks.isEmpty())
                            Text("Choose word packs")
                        else
                            Text("Amount of selected word packs: %d".format(selectedWordPacks.size))
                    }
                    Column (
                        verticalArrangement = Arrangement.Center
                    ){
                        Text(">")
                    }
                }
            }
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ){
                    Text("Teams", color = MaterialTheme.colorScheme.onBackground)
                    Button(onClick = onTeamSelect) {
                        Text("Edit")
                    }
                }
                Column (
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)

                ){
                    tm.teams.forEach { t -> key(t) { TeamIcon(team = t, isRemovable = false)} }
                }
            }
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ){
                Text("Round length", color = MaterialTheme.colorScheme.onBackground)
                Slider(
                    modifier = Modifier.padding(horizontal = 10.dp),
                    value = roundLengthSliderPosition,
                    onValueChange = {roundLengthSliderPosition = it},
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.secondary,
                        activeTrackColor = MaterialTheme.colorScheme.secondary,
                        inactiveTrackColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    steps = 6,
                    valueRange = 30f..240f)
                Text("%.0fs".format(roundLengthSliderPosition), color = MaterialTheme.colorScheme.onBackground)

                Text("Words required for winning", color = MaterialTheme.colorScheme.onBackground)
                Slider(
                    modifier = Modifier.padding(horizontal = 10.dp),
                    value = wordsRequiredForWinningSliderPosition,
                    onValueChange = {wordsRequiredForWinningSliderPosition = it},
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.secondary,
                        activeTrackColor = MaterialTheme.colorScheme.secondary,
                        inactiveTrackColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    steps = 8,
                    valueRange = 25f..250f)
                Text("%.0f words".format(wordsRequiredForWinningSliderPosition), color = MaterialTheme.colorScheme.onBackground)
            }
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ){
                Text("Rules", color = MaterialTheme.colorScheme.onBackground)
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ){
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 30.dp)
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primary),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ){
                        Column(
                            modifier = Modifier.weight(1f).fillMaxWidth()
                        ){
                            Text("Last word", color = MaterialTheme.colorScheme.onPrimary)
                            Text("Any team can guess the last word after the time runs out", color = MaterialTheme.colorScheme.onPrimary)
                        }
                        Switch(checked = lastWordSetting, onCheckedChange = {lastWordSetting = it})
                    }
                    HorizontalDivider(thickness = 2.dp)
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 30.dp)
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primary),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ){
                        Column{
                            Text("Skipped word", color = MaterialTheme.colorScheme.onPrimary)
                            Text("-1 point for each skipped word", color = MaterialTheme.colorScheme.onPrimary)
                        }
                        Switch(checked = skippedWordSetting, onCheckedChange = {skippedWordSetting = it})
                    }
                }
            }
            var errorLabel by remember { mutableStateOf("Initial text") }
            var errorVisible by remember { mutableStateOf(false) }
            if (errorVisible)
                Text(errorLabel, color = Color.Red)
            Button(
                onClick = {
                    val gameSetting = GameSetting(
                        lastWordCanBeGuessed = lastWordSetting,
                        skippedWordSubtractsPoints = skippedWordSetting,
                        timePerRound = roundLengthSliderPosition.toInt(),
                        tm = tm,
                        wordPacks = selectedWordPacks.toList(),
                        wordsWinCondition = wordsRequiredForWinningSliderPosition.toInt()
                    )
                    if (gameSetting.canStartUpGame())
                        onStartGame(gameSetting)
                    else {
                        errorLabel = gameSetting.gameStartupCheck()
                        errorVisible = true
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(text = "Start game")
            }
        }
    }
}