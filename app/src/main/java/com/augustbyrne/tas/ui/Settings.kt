package com.augustbyrne.tas.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.ArrowDropUp
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.augustbyrne.tas.ui.components.RadioItemsDialog
import com.augustbyrne.tas.ui.notes.NoteViewModel
import com.augustbyrne.tas.ui.values.blue500
import com.augustbyrne.tas.util.BarType
import com.augustbyrne.tas.util.DarkMode
import com.augustbyrne.tas.util.TimerTheme
import com.augustbyrne.tas.util.classicSystemBarScrollBehavior
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsUI(
    myViewModel: NoteViewModel,
    onNavBack: () -> Unit,
    appBarScrollState: TopAppBarState
) {
    val uriHandler = LocalUriHandler.current
    val localCoroutineScope = rememberCoroutineScope()
    var showDarkModeDialog by rememberSaveable { mutableStateOf(false) }
    var showTimerThemeDialog by rememberSaveable { mutableStateOf(false) }
    val darkModeState by myViewModel.isDarkThemeLiveData.observeAsState(initial = DarkMode.System)
    val timerThemeState by myViewModel.timerThemeLiveData.observeAsState(initial = TimerTheme.Original)
    val vibrationState by myViewModel.vibrationLiveData.observeAsState(initial = true)
    val startDelayState by myViewModel.startDelayPrefLiveData.observeAsState(initial = 5)
    var expanded by remember { mutableStateOf(false) }
    val appBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(appBarScrollState)
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        appBarScrollState.heightOffset = 0f
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            // attach as a parent to the nested scroll system
            .nestedScroll(appBarScrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Settings")
                },
                modifier = Modifier
                    .statusBarsPadding()
                    .classicSystemBarScrollBehavior(appBarScrollState, BarType.Top)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .padding(top = it.calculateTopPadding())
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(16.dp),
                text = "Theme",
                style = MaterialTheme.typography.titleSmall,
                color = blue500
            )
            Column(
                modifier = Modifier
                    .wrapContentHeight()
                    .fillMaxWidth()
                    .clickable(
                        onClick = {
                            showDarkModeDialog = true
                        },
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple()
                    )
                    .padding(16.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    text = "Dark mode",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = when (darkModeState) {
                        DarkMode.System -> "Follow system"
                        DarkMode.Off -> "Off"
                        DarkMode.On -> "On"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
            Column(
                modifier = Modifier
                    .wrapContentHeight()
                    .fillMaxWidth()
                    .clickable(
                        onClick = {
                            showTimerThemeDialog = true
                        },
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple()
                    )
                    .padding(16.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    text = "Timer theme",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = when (timerThemeState) {
                        TimerTheme.Original -> "Basic"
                        TimerTheme.Vibrant -> "Vibrant"
                        TimerTheme.VaporWave -> "Vaporwave"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
            HorizontalDivider(modifier = Modifier.fillMaxWidth())
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(16.dp),
                text = "Timer",
                style = MaterialTheme.typography.titleSmall,
                color = blue500
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clickable(
                        onClick = {
                            localCoroutineScope.launch {
                                myViewModel.setVibration(!vibrationState)
                            }
                        },
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple()
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(16.dp),
                    text = "Vibrate",
                    style = MaterialTheme.typography.bodyLarge
                )
                Switch(
                    modifier = Modifier.padding(end = 14.dp),
                    checked = vibrationState,
                    onCheckedChange = {
                        localCoroutineScope.launch {
                            myViewModel.setVibration(!vibrationState)
                        }
                    }
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clickable(
                        onClick = { expanded = true },
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple()
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(16.dp),
                    text = "Start Delay",
                    style = MaterialTheme.typography.bodyLarge
                )
                TextButton(
                    modifier = Modifier.padding(4.dp),
                    onClick = {
                        expanded = true
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                ) {
                    Text(
                        text = if (startDelayState >= 1) {
                            "$startDelayState sec"
                        } else {
                            "None"
                        }
                    )
                    Icon(
                        imageVector = if (expanded) {
                            Icons.Rounded.ArrowDropUp
                        } else {
                            Icons.Rounded.ArrowDropDown
                        },
                        contentDescription = "time increment selector"
                    )
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        DropdownMenuItem(text = { Text("None") }, onClick = {
                            expanded = false
                            localCoroutineScope.launch {
                                myViewModel.setStartDelayPref(0)
                            }
                        })
                        DropdownMenuItem(text = { Text("3 sec") }, onClick = {
                            expanded = false
                            localCoroutineScope.launch {
                                myViewModel.setStartDelayPref(3)
                            }
                        })
                        DropdownMenuItem(text = { Text("5 sec") }, onClick = {
                            expanded = false
                            localCoroutineScope.launch {
                                myViewModel.setStartDelayPref(5)
                            }
                        })
                        DropdownMenuItem(text = { Text("10 sec") }, onClick = {
                            expanded = false
                            localCoroutineScope.launch {
                                myViewModel.setStartDelayPref(10)
                            }
                        })
                    }
                }
            }
            HorizontalDivider(modifier = Modifier.fillMaxWidth())
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(16.dp),
                text = "Other",
                style = MaterialTheme.typography.titleSmall,
                color = blue500
            )
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clickable(
                        onClick = {
                            uriHandler.openUri("https://ko-fi.com/augustb")
                        },
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple()
                    )
                    .padding(16.dp),
                text = "Buy me a tea",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clickable(
                        onClick = {
                            uriHandler.openUri("https://github.com/august-byrne/ProtoSuite")
                        },
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple()
                    )
                    .padding(16.dp),
                text = "Project github",
                style = MaterialTheme.typography.bodyLarge
            )
            if (showTimerThemeDialog) {
                RadioItemsDialog(
                    title = "Timer theme",
                    radioItemNames = listOf("basic", "vibrant", "vaporwave"),
                    currentState = timerThemeState.theme,
                    onClickItem = { indexClicked ->
                        localCoroutineScope.launch {
                            myViewModel.setTimerTheme(TimerTheme.getTheme(indexClicked))
                        }
                        showTimerThemeDialog = false
                    },
                    onDismissRequest = {
                        showTimerThemeDialog = false
                    }
                )
            }
            if (showDarkModeDialog) {
                RadioItemsDialog(
                    title = "Dark mode",
                    radioItemNames = listOf("Follow system", "Off", "On"),
                    currentState = darkModeState.mode,
                    onClickItem = {
                        showDarkModeDialog = false
                        localCoroutineScope.launch {
                            myViewModel.setIsDarkTheme(DarkMode.getMode(it))
                        }
                    },
                    onDismissRequest = { showDarkModeDialog = false }
                )
            }
        }
    }
}
