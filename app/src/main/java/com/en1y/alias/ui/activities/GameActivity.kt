package com.en1y.alias.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
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
import com.en1y.alias.data.Round
import com.en1y.alias.data.WordPack
import com.en1y.alias.data.rememberGame
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

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
            var round: Round? = null
            when (step) {
                0 -> TeamSelect(onNext = {step = 1}, tm = tm)
                1 -> GameSettings(
                    onNext = {step = 1},
                    onSettings = {step = 10},
                    onTeamSelect = {step = 0},
                    onWordPackSelect = {step = 11},
                    tm = tm,
                    selectedWordPacks = selectedWordPacks,
                    onStartGame = {
                        setting -> activeGameSetting = setting
                        step = 2
                    }
                )
                2 -> {
                    val activeGame = game
                        ?: return@setContent

                    PreRoundScreen(game = activeGame, onNext = {step = 3}, onPrevious = {step = 1})
                }
                3 -> {
                    val activeGame = game
                        ?: return@setContent
                    round = remember { activeGame.createRound() }
                    RoundScreen(round, onNext = {step = 4})
                }
                4 -> {
                    val activeGame = game
                        ?: return@setContent
                    PostRoundScreen(game = activeGame, onNext = {step = 2})
                }
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
        Image(painter = team.teamImage, contentDescription = stringResource(R.string.team_image_description, team.teamName))
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
            Text(stringResource(R.string.teams), color = MaterialTheme.colorScheme.onBackground)
            Text(stringResource(R.string.teams_description), color = MaterialTheme.colorScheme.onBackground)

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
                    Text(stringResource(R.string.remove_team))
                }
                Button(
                    onClick = {tm.addTeam()},
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )) {
                    Text(stringResource(R.string.add_team))
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
            Text(stringResource(R.string.theme), color = MaterialTheme.colorScheme.onBackground)
            Text(stringResource(R.string.theme_description), color = MaterialTheme.colorScheme.onBackground)
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
            Text(stringResource(R.string.settings), color = MaterialTheme.colorScheme.onBackground)

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
                    Text(stringResource(R.string.language), color = MaterialTheme.colorScheme.onPrimary)
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
                    Text(stringResource(R.string.choose_theme), color = MaterialTheme.colorScheme.onPrimary)
                    Button(onClick = onThemeChange, colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary)) {
                        Text(">")
                    }
                }
            }
            Text(stringResource(R.string.external_links), color = MaterialTheme.colorScheme.onBackground)
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
                    Text(stringResource(R.string.github_page), color = MaterialTheme.colorScheme.onPrimary)
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
                    Text(stringResource(R.string.creator_page), color = MaterialTheme.colorScheme.onPrimary)
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

@Composable
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
            Text(stringResource(R.string.word_packs), color = MaterialTheme.colorScheme.onBackground)
            Text(stringResource(R.string.word_packs_description), color = MaterialTheme.colorScheme.onBackground)
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
                            Text(pluralStringResource(R.plurals.words_count, words.size, words.size), color = MaterialTheme.colorScheme.onPrimary)
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
    val context = LocalContext.current

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
                        Text(text = stringResource(R.string.new_game_title))
                        Text(text = stringResource(R.string.new_game_description))
                        Button({}) {
                            Text(stringResource(R.string.new_game))
                        }
                        Button({showDialog = false}) {
                            Text(stringResource(R.string.cancel))
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
                Text(modifier = Modifier.padding(end = 10.dp), text = stringResource(R.string.game_settings), color = MaterialTheme.colorScheme.onBackground)
                Button(
                    onClick = {showDialog = true},
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    )
                ) {
                    Text(stringResource(R.string.reset))
                }
                Button(
                    onClick = onSettings,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    )
                ) {
                    Text(stringResource(R.string.settings))
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
                        Text(stringResource(R.string.word_packs))
                        if (selectedWordPacks.isEmpty())
                            Text(stringResource(R.string.choose_word_packs))
                        else
                            Text(stringResource(R.string.selected_word_packs, selectedWordPacks.size))
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
                    Text(stringResource(R.string.teams), color = MaterialTheme.colorScheme.onBackground)
                    Button(onClick = onTeamSelect) {
                        Text(stringResource(R.string.edit))
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
                Text(stringResource(R.string.round_length), color = MaterialTheme.colorScheme.onBackground)
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
                Text(stringResource(R.string.seconds_short, roundLengthSliderPosition.toInt()), color = MaterialTheme.colorScheme.onBackground)

                Text(stringResource(R.string.words_to_win), color = MaterialTheme.colorScheme.onBackground)
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
                Text(pluralStringResource(R.plurals.words_count, wordsRequiredForWinningSliderPosition.toInt(), wordsRequiredForWinningSliderPosition.toInt()), color = MaterialTheme.colorScheme.onBackground)
            }
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ){
                Text(stringResource(R.string.rules), color = MaterialTheme.colorScheme.onBackground)
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
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ){
                            Text(stringResource(R.string.last_word), color = MaterialTheme.colorScheme.onPrimary)
                            Text(stringResource(R.string.last_word_description), color = MaterialTheme.colorScheme.onPrimary)
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
                            Text(stringResource(R.string.skipped_word), color = MaterialTheme.colorScheme.onPrimary)
                            Text(stringResource(R.string.skipped_word_description), color = MaterialTheme.colorScheme.onPrimary)
                        }
                        Switch(checked = skippedWordSetting, onCheckedChange = {skippedWordSetting = it})
                    }
                }
            }
            var errorLabel by remember { mutableStateOf("") }
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
                        errorLabel = gameSetting.gameStartupCheck(context)
                        errorVisible = true
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(text = stringResource(R.string.start_game))
            }
        }
    }
}

@Composable
fun PreRoundScreen(
    modifier: Modifier = Modifier,
    game: Game = remember { Game(GameSetting(
        wordPacks = mutableListOf(WordPack.pack1),
        tm = TeamManager(),
        timePerRound = 60,
        wordsWinCondition = 100,
        lastWordCanBeGuessed = false,
        skippedWordSubtractsPoints = true
    ), mutableListOf("a"))},
    onNext: () -> Unit = {},
    onPrevious: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val t = game.gameOver()

    AliasTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(all = 16.dp)
                .padding(top = 30.dp)
                .verticalScrollbar(scrollState, MaterialTheme.colorScheme.onBackground)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            Column {
                Row {
                    Button({}) {
                        Text(stringResource(R.string.end_game))
                    }
                    Text(stringResource(R.string.round_number, game.getRound()))
                }
                val team: Team = remember { game.getCurrentTeam() }

                Image(painter = team.teamImage, contentDescription = stringResource(R.string.team_image_description, team.teamName))

                if (t != null) {
                    Text(stringResource(R.string.team_won, t.teamName))
                } else {
                    Text(stringResource(R.string.next_team))
                    Text(team.teamName)
                    Text(stringResource(R.string.hand_phone))
                }
            }
            Column{
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ){
                    Text(stringResource(R.string.standings))
                    Text(stringResource(R.string.to_points, game.gameSetting.wordsWinCondition))
                }
                Column (
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)

                ){
                    game.gameSetting.tm.teams.forEach { t -> key(t) { TeamIcon(team = t, isRemovable = false)} }
                }
                Button(modifier = Modifier.fillMaxWidth().padding(top = 20.dp), onClick = {
                    if (t == null) onNext()
                    else onPrevious()
                }) {
                    if (t != null)Text(stringResource(R.string.start_round))
                    else Text(stringResource(R.string.back_to_menu))
                }
            }
        }
    }
}

@Composable
fun CountdownTimer(initialSeconds: Int = 60, isRunning: Boolean = false, onFinish: () -> Unit = {}) {
    var remainingSeconds by remember { mutableIntStateOf(initialSeconds) }

    LaunchedEffect(isRunning, remainingSeconds) {
        if (isRunning && remainingSeconds > 0) {
            delay(1000L.milliseconds)
            remainingSeconds--
        } else if (isRunning && remainingSeconds == 0) {
            onFinish()
        }
    }

    Column {
        Text(text = "%d:%02d".format(remainingSeconds / 60, remainingSeconds % 60))
        val rawProgress = if (initialSeconds > 0) {
            remainingSeconds.toFloat() / initialSeconds.toFloat()
        } else {
            0f
        }

        val animatedProgress by animateFloatAsState(
            targetValue = rawProgress,
            animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
            label = "TimerProgress"
        )

        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
        )
    }
}

@Composable @Preview
fun RoundScreen(
    round: Round = Round(
        gameSetting = GameSetting(
            wordPacks = mutableListOf(WordPack.pack1),
            tm = TeamManager(),
            timePerRound = 60,
            wordsWinCondition = 100,
            lastWordCanBeGuessed = false,
            skippedWordSubtractsPoints = true
        ),
        words = mutableListOf("a"),
        currentTeam = Team()),
    onNext: () -> Unit = {}
) {

    @Composable
    fun WordCard(word: String = "word", counter: Int = 0) {
        Column(
            verticalArrangement = Arrangement.SpaceEvenly
        ){
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ){
                Text(counter.toString())
                Text(stringResource(R.string.guessed_hint))
            }
            Text(word)
            Row {
                Text(stringResource(R.string.skipped_hint))
            }
        }
    }

    var isRunning by remember { mutableStateOf(false) }
    var started by remember { mutableStateOf(false) }

    AliasTheme { Box {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(all = 16.dp)
                .padding(top = 30.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            Row {
                Button({}) {
                    Text("<")
                }
                Text(round.currentTeam.teamName)
                Text(stringResource(R.string.points_short, round.countPoints()))
            }
            Row {
                CountdownTimer(round.gameSetting.timePerRound, isRunning = isRunning, onFinish = {isRunning = false})
            }
            Column {
                if (started && !isRunning)
                    Text(stringResource(R.string.last_word))
                if (started) WordCard(round.currentWord, round.counter) else WordCard()
            }
            Row {
                Button({
                    round.skipped()
                    if(!isRunning) {
                        onNext()
                        round.end()
                    }
                }) {
                    Text(stringResource(R.string.skip))
                }
                Text(round.countPoints().toString())
                Button({
                    if (!isRunning && round.gameSetting.lastWordCanBeGuessed) {
                        //TODO: handle last word can be guessed
                    } else round.guessed()
                    if(!isRunning) {
                        round.end()
                        onNext()
                    }
                }) {
                    Text(stringResource(R.string.guessed))
                }
            }
        }
        if (!started) Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .pointerInput(Unit) { detectTapGestures { } }, // swallow taps so the screen below is inactive
            contentAlignment = Alignment.Center
        ) {
            Surface(shape = RoundedCornerShape(12.dp), shadowElevation = 6.dp) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(stringResource(R.string.swipe_hint))
                    Button({ started = true; isRunning = true; round.start()}) {
                        Text(stringResource(R.string.start))
                    }
                }
            }
        }
    } }
}

@Composable @Preview
fun PostRoundScreen(
    game: Game = remember { Game(GameSetting(
        wordPacks = mutableListOf(WordPack.pack1),
        tm = TeamManager(),
        timePerRound = 60,
        wordsWinCondition = 100,
        lastWordCanBeGuessed = false,
        skippedWordSubtractsPoints = true
    ), mutableListOf("a"))},
    onNext: () -> Unit = {}
) {
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
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            val round = game.currentRound
            if (round != null) {
                Column {
                    Row {
                        Button({}) {
                            Text("<")
                        }
                        Image(
                            painter = round.currentTeam.teamImage,
                            contentDescription = stringResource(R.string.team_image_description, round.currentTeam.teamName)
                        )
                        Text(round.currentTeam.teamName)
                        Text(stringResource(R.string.round_number, game.getRound()))
                    }
                    Row {
                        Column {
                            Text(stringResource(R.string.round_review))
                            Text(stringResource(R.string.review_guesses))
                        }
                        // roundWords is a state map, so this recomputes whenever a status is toggled
                        val points = round.countPoints()
                        Text("${if (points > 0) "+" else ""}$points")
                    }
                    Column {
                        round.roundWords.forEach { (string, status) ->
                            Row {
                                Text(string)
                                TextButton({
                                    round.roundWords[string] = Round.GuessStatus.Guessed
                                }) { Text(text = "✓", modifier = Modifier.background(if (status == Round.GuessStatus.Guessed) Color.Green else Color.Transparent)) }
                                TextButton({
                                    round.roundWords[string] = Round.GuessStatus.Skipped
                                }) { Text(text = "x", modifier = Modifier.background(if (status == Round.GuessStatus.Skipped) Color.Red else Color.Transparent)) }
                                TextButton({
                                    round.roundWords[string] = Round.GuessStatus.Deleted
                                }) { Text(text = "\uD83D\uDDD1", modifier = Modifier.background(if (status == Round.GuessStatus.Deleted) Color.Gray else Color.Transparent)) }
                            }

                        }
                    }
                    Button({
                        game.endRound()
                        onNext()
                    }) {
                        Text(stringResource(R.string.next))
                    }
                }
            }
            else {
                Text(stringResource(R.string.game_error))
            }
        }
    }
}