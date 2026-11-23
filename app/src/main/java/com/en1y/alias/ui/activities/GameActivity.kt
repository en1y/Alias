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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.foundation.shape.RoundedCornerShape
import com.en1y.alias.ui.theme.AliasIcon
import com.en1y.alias.ui.theme.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import com.en1y.alias.ui.theme.AliasButton
import com.en1y.alias.ui.theme.AliasCheckbox
import com.en1y.alias.ui.theme.AliasSlider
import com.en1y.alias.ui.theme.AliasSwitch
import com.en1y.alias.ui.theme.AliasTheme
import com.en1y.alias.ui.theme.Panel
import com.en1y.alias.ui.theme.aliasColors
import com.en1y.alias.ui.theme.screen
import com.en1y.alias.ui.theme.secondaryColors
import com.en1y.alias.ui.theme.verticalScrollWithBar
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
    Panel(color = MaterialTheme.colorScheme.secondary) { Row (
        modifier = Modifier
            .fillMaxWidth()
            .padding(15.dp),
        horizontalArrangement = Arrangement.spacedBy(22.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    )
    {
        Image(painter = team.teamImage, contentDescription = stringResource(R.string.team_image_description, team.teamName))
        Text(modifier = Modifier.weight(1f), text = team.teamName)
        Text(text = "%d".format(team.points))
        if (isRemovable)
            IconButton(onClick = remove) {
                Icon(AliasIcon.Close, stringResource(R.string.remove_team))
            }
    } }
}

@Composable
fun TeamSelect(modifier: Modifier = Modifier, tm: TeamManager = remember { TeamManager() }, onNext: () -> Unit = {}) {

    AliasTheme {
        Column(
            modifier = Modifier
                .screen(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(stringResource(R.string.teams))
            Text(stringResource(R.string.teams_description))

            Panel(
                modifier = Modifier
                    .weight(1f)
                    .verticalScrollWithBar()
                    .padding(all = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                tm.teams.forEach { t -> key(t) { TeamIcon(team = t, remove = { tm.removeTeam(t) }) } }
            }
            Row (
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally)
            ) {
                AliasButton(
                    onClick = {tm.removeTeam()}) {
                    Text(stringResource(R.string.remove_team))
                }
                AliasButton(
                    onClick = {tm.addTeam()}) {
                    Text(stringResource(R.string.add_team))
                }
            }
            AliasButton(
                onClick = onNext
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
                .screen(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            AliasButton(onClick = onPrevious) {
                Icon(AliasIcon.Back, null)
            }
            Text(stringResource(R.string.theme))
            Text(stringResource(R.string.theme_description))
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
    val context = LocalContext.current

    AliasTheme {
        Column(
            modifier = Modifier
                .screen()
                .verticalScrollWithBar(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            AliasButton(onClick = onPrevious) {
                Icon(AliasIcon.Back, null)
            }
            Text(stringResource(R.string.settings))

            Panel(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Text(stringResource(R.string.language))
                    AliasButton(onClick = onLanguageChange, colors = ButtonDefaults.secondaryColors()) {
                        Icon(AliasIcon.Forward, null)
                    }
                }
                HorizontalDivider(thickness = 2.dp, color = LocalContentColor.current)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Text(stringResource(R.string.choose_theme))
                    AliasButton(onClick = onThemeChange, colors = ButtonDefaults.secondaryColors()) {
                        Icon(AliasIcon.Forward, null)
                    }
                }
            }
            Text(stringResource(R.string.external_links))
            Panel(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Text(stringResource(R.string.github_page))
                    AliasButton(onClick = {
                        val urlIntent = Intent(
                            Intent.ACTION_VIEW,
                            "https://github.com/en1y/Alias".toUri()
                        )
                        context.startActivity(urlIntent)
                    }, colors = ButtonDefaults.secondaryColors()) {
                        Icon(AliasIcon.Forward, null)
                    }
                }
                HorizontalDivider(thickness = 2.dp, color = LocalContentColor.current)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Text(stringResource(R.string.creator_page))
                    AliasButton(onClick = {
                        val urlIntent = Intent(
                            Intent.ACTION_VIEW,
                            ("https://github.com/en1y").toUri()
                        )
                        context.startActivity(urlIntent)
                    }, colors = ButtonDefaults.secondaryColors()) {
                        Icon(AliasIcon.Forward, null)
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

    AliasTheme {
        Column(
            modifier = Modifier
                .screen()
                .verticalScrollWithBar(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            AliasButton(onClick = onPrevious) {
                Icon(AliasIcon.Back, null)
            }
            Text(stringResource(R.string.word_packs))
            Text(stringResource(R.string.word_packs_description))
            @Composable fun WordPackElement(
                wordPack: WordPack,
                name: String,
                description: String,
                words: List<String>
            ) {
                Panel { Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ){
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ){
                            Text(name)
                            Text(pluralStringResource(R.plurals.words_count, words.size, words.size))
                        }
                        Text(description)
                    }
                    AliasCheckbox(
                        checked = selectedWordPacks.contains(wordPack),
                        onCheckedChange = { checked ->
                            val updatedSelection = selectedWordPacks.toMutableSet()
                            if (checked) updatedSelection.add(wordPack)
                            else updatedSelection.remove(wordPack)
                            onSelectionChange(updatedSelection)
                        }
                    )
                } }
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
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.tertiary,
                    shadowElevation = 6.dp
                ) {
                    Column(
                        modifier = Modifier.padding(15.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ){
                        Text(text = stringResource(R.string.new_game_title))
                        Text(text = stringResource(R.string.new_game_description))
                        AliasButton({}) {
                            Text(stringResource(R.string.new_game))
                        }
                        AliasButton({showDialog = false}) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                }
            }
        }
    }



    AliasTheme {
        Column(
            modifier = Modifier
                .screen()
                .verticalScrollWithBar(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Panel { Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(modifier = Modifier.padding(end = 10.dp), text = stringResource(R.string.game_settings))
                AliasButton(onClick = {showDialog = true}, colors = ButtonDefaults.secondaryColors()) {
                    Icon(AliasIcon.Reset, stringResource(R.string.reset))
                }
                AliasButton(onClick = onSettings, colors = ButtonDefaults.secondaryColors()) {
                    Icon(AliasIcon.Settings, stringResource(R.string.settings))
                }
            } }
            AliasButton(
                modifier = Modifier
                    .fillMaxWidth(),
                onClick = onWordPackSelect
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
                        Icon(AliasIcon.Forward, null)
                    }
                }
            }
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ){
                    Text(stringResource(R.string.teams))
                    AliasButton(onClick = onTeamSelect) {
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
                Text(stringResource(R.string.round_length))
                AliasSlider(
                    value = roundLengthSliderPosition,
                    onValueChange = {roundLengthSliderPosition = it},
                    steps = 6,
                    valueRange = 30f..240f)
                Text(stringResource(R.string.seconds_short, roundLengthSliderPosition.toInt()))

                Text(stringResource(R.string.words_to_win))
                AliasSlider(
                    value = wordsRequiredForWinningSliderPosition,
                    onValueChange = {wordsRequiredForWinningSliderPosition = it},
                    steps = 8,
                    valueRange = 25f..250f)
                Text(pluralStringResource(R.plurals.words_count, wordsRequiredForWinningSliderPosition.toInt(), wordsRequiredForWinningSliderPosition.toInt()))
            }
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ){
                Text(stringResource(R.string.rules))
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ){
                    Panel(Modifier.padding(horizontal = 30.dp)) { Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ){
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ){
                            Text(stringResource(R.string.last_word))
                            Text(stringResource(R.string.last_word_description))
                        }
                        AliasSwitch(checked = lastWordSetting, onCheckedChange = {lastWordSetting = it})
                    } }
                    HorizontalDivider(thickness = 2.dp)
                    Panel(Modifier.padding(horizontal = 30.dp)) { Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ){
                        Column{
                            Text(stringResource(R.string.skipped_word))
                            Text(stringResource(R.string.skipped_word_description))
                        }
                        AliasSwitch(checked = skippedWordSetting, onCheckedChange = {skippedWordSetting = it})
                    } }
                }
            }
            var errorLabel by remember { mutableStateOf("") }
            var errorVisible by remember { mutableStateOf(false) }
            if (errorVisible)
                Text(errorLabel, color = MaterialTheme.colorScheme.error)
            AliasButton(
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
                }
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
    val t = game.gameOver()

    AliasTheme {
        Column(
            modifier = Modifier
                .screen()
                .verticalScrollWithBar(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            Column {
                Row {
                    AliasButton({}) {
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
                AliasButton(modifier = Modifier.fillMaxWidth().padding(top = 20.dp), onClick = {
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
                .screen(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            Row {
                AliasButton({}) {
                    Icon(AliasIcon.Back, null)
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
                AliasButton({
                    round.skipped()
                    if(!isRunning) {
                        onNext()
                        round.end()
                    }
                }) {
                    Text(stringResource(R.string.skip))
                }
                Text(round.countPoints().toString())
                AliasButton({
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
            Surface(shape = MaterialTheme.shapes.medium, shadowElevation = 6.dp) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(stringResource(R.string.swipe_hint))
                    AliasButton({ started = true; isRunning = true; round.start()}) {
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

    AliasTheme {
        Column(
            modifier = Modifier
                .screen()
                .verticalScrollWithBar(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            val round = game.currentRound
            if (round != null) {
                Column {
                    Row {
                        AliasButton({}) {
                            Icon(AliasIcon.Back, null)
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
                                val c = MaterialTheme.aliasColors
                                val idle = LocalContentColor.current
                                IconButton({
                                    round.updateByWord(string, Round.GuessStatus.Guessed)
                                }) { Icon(AliasIcon.Check, stringResource(R.string.guessed), tint = if (status == Round.GuessStatus.Guessed) c.guessed else idle) }
                                IconButton({
                                    round.updateByWord(string, Round.GuessStatus.Skipped)
                                }) { Icon(AliasIcon.Close, stringResource(R.string.skip), tint = if (status == Round.GuessStatus.Skipped) c.skipped else idle) }
                                IconButton({
                                    round.updateByWord(string, Round.GuessStatus.Deleted)
                                }) { Icon(AliasIcon.Delete, null, tint = if (status == Round.GuessStatus.Deleted) c.deleted else idle) }
                            }

                        }
                    }
                    AliasButton({
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