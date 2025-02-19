package com.augustbyrne.tas.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.ArrowDropUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.augustbyrne.tas.data.db.entities.DataItem
import com.augustbyrne.tas.data.db.entities.NoteItem
import kotlin.math.pow

/**
 * A reusable Dialog with a single text entry field that follows Material Design 3 specifications
 * @param headerName the string used for the header of the dialog box.
 * @param fieldName the optional label to be displayed inside the text field container.
 * @param initialValue the starting value inside the text field container.
 * @param maxChars the maximum number of character you can input in the text field.
 * @param singleLine the singleLine parameter of a TextField. When set to true, this text field becomes a single horizontally scrolling text field instead of wrapping onto multiple lines.
 * @param inputType the keyboard type to be used in the text field. This also transforms the visual representation of the input value (obscures passwords when KeyboardType.Password is chosen, for example).
 * @param onDismissRequest the action to take when a dismiss is requested (back press or cancel button is clicked).
 * @param onAccepted the action to take when the ok button is clicked. The value of the text field container is returned, to be acted upon.
 */
@Composable
fun EditOneFieldDialog(
    headerName: String,
    fieldName: String? = null,
    initialValue: String,
    maxChars: Int? = null,
    singleLine: Boolean = true,
    inputType: KeyboardType = KeyboardType.Text,
    onDismissRequest: () -> Unit,
    onAccepted: (returnedValue: String) -> Unit
) {
    var fieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = initialValue,
                selection = TextRange(initialValue.length)
            )
        )
    }
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    AlertDialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        ),
        title = {
            Text(headerName)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                OutlinedTextField(
                    modifier = Modifier.focusRequester(focusRequester = focusRequester),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onBackground),
                    label = { if (fieldName != null) Text(fieldName) },
                    value = fieldValue,
                    onValueChange = {
                        if (maxChars == null) {
                            fieldValue = it
                        } else {
                            if (it.text.length <= maxChars) {
                                fieldValue = it
                            }
                        }
                    },
                    maxLines = 4,
                    singleLine = singleLine,
                    visualTransformation = if (inputType == KeyboardType.Password) PasswordVisualTransformation(
                        '●'
                    ) else VisualTransformation.None,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = inputType,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            onAccepted(fieldValue.text)
                            focusManager.clearFocus()
                        }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.outline,
                    )
                )
                if (maxChars != null) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End,
                        text = "${fieldValue.text.length}/$maxChars"
                    )
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest
            ) {
                Text("Cancel")
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onAccepted(fieldValue.text) }
            ) {
                Text("Ok")
            }
        }
    )
}

@Composable
fun EditExpandedNoteHeaderDialog(
    initialValue: NoteItem = NoteItem(),
    onDismissRequest: () -> Unit,
    onAccepted: (returnedValue: NoteItem) -> Unit
) {
    val titleMaxChars = 26
    val descriptionMaxChars = 100
    var titleFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = initialValue.title,
                selection = TextRange(initialValue.title.length)
            )
        )
    }
    var descriptionFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = initialValue.description,
                selection = TextRange(initialValue.description.length)
            )
        )
    }
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    AlertDialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        ),
        title = {
            Text("${if (initialValue.id == 0) "New" else "Edit"} routine")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                OutlinedTextField(
                    modifier = Modifier.focusRequester(focusRequester = focusRequester),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onBackground),
                    label = { Text("Title") },
                    value = titleFieldValue,
                    onValueChange = {
                        if (it.text.length <= titleMaxChars) {
                            titleFieldValue = it
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.moveFocus(FocusDirection.Next)
                        }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.outline,
                    )
                )
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End,
                    text = "${titleFieldValue.text.length}/$titleMaxChars"
                )
                OutlinedTextField(
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onBackground),
                    label = { Text("Description") },
                    value = descriptionFieldValue,
                    onValueChange = {
                        if (it.text.length <= descriptionMaxChars) {
                            descriptionFieldValue = it
                        }
                    },
                    maxLines = 5,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            onAccepted(
                                initialValue.copy(
                                    title = titleFieldValue.text,
                                    description = descriptionFieldValue.text
                                )
                            )
                            focusManager.clearFocus()
                        }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.outline,
                    )
                )
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End,
                    text = "${descriptionFieldValue.text.length}/$descriptionMaxChars"
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest
            ) {
                Text("Cancel")
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onAccepted(
                        initialValue.copy(
                            title = titleFieldValue.text,
                            description = descriptionFieldValue.text
                        )
                    )
                    focusManager.clearFocus()
                }
            ) {
                Text("Ok")
            }
        }
    )
}

@Composable
fun EditDataItemDialog(
    initialDataItem: DataItem,
    onDismissRequest: () -> Unit,
    onAccepted: (returnedValue: DataItem) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var timeUnitValue by remember { mutableIntStateOf(initialDataItem.unit) }
    var activityFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = initialDataItem.activity,
                selection = TextRange(initialDataItem.activity.length)
            )
        )
    }
    var timeFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = if (initialDataItem.time == 0) "" else initialDataItem.time.toString(),
                selection = TextRange(initialDataItem.time.toString().length)
            )
        )
    }
    var timeError by rememberSaveable { mutableStateOf(false) }
    var activityError by rememberSaveable { mutableStateOf(false) }
    val activityMaxChars = 66
    val timeMaxChars = 5
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }
    AlertDialog(
        modifier = Modifier.wrapContentSize(),
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        ),
        title = {
            Text(
                text = "${
                    if (initialDataItem.id == 0) {
                        "Add"
                    } else {
                        "Edit"
                    }
                } activity"
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(unbounded = true),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    label = { Text("Activity") },
                    modifier = Modifier.focusRequester(focusRequester),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onBackground),
                    maxLines = 3,
                    value = activityFieldValue,
                    onValueChange = {
                        if (it.text.length <= activityMaxChars) {
                            activityFieldValue = it
                        }
                    },
                    trailingIcon = {
                        Text(
                            text = "${activityFieldValue.text.length}/$activityMaxChars"
                        )
                    },
                    isError = activityError,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.moveFocus(FocusDirection.Next)
                        }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.outline,
                    )
                )
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        modifier = Modifier.weight(1f),
                        label = { Text("Time") },
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onBackground),
                        singleLine = true,
                        value = timeFieldValue,
                        onValueChange = {
                            if (it.text.length <= timeMaxChars) {
                                timeFieldValue = it
                            }
                        },
                        trailingIcon = {
                            Text(
                                text = "${timeFieldValue.text.length}/$timeMaxChars"
                            )
                        },
                        isError = timeError,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                onAccepted(
                                    initialDataItem.copy(
                                        activity = activityFieldValue.text,
                                        time = timeFieldValue.text.toInt(),
                                        unit = timeUnitValue
                                    )
                                )
                                focusManager.clearFocus()
                            }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.outline,
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = {
                            focusManager.clearFocus()
                            expanded = true
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                    ) {
                        Text(
                            text =
                            when (timeUnitValue) {
                                0 -> "sec"
                                1 -> "min"
                                2 -> "hr"
                                else -> "unit"
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
                        DropdownMenu(
                            modifier = Modifier.background(MaterialTheme.colorScheme.background),
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(text = "seconds") },
                                onClick = {
                                    expanded = false
                                    timeUnitValue = 0
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(text = "minutes") },
                                onClick = {
                                    expanded = false
                                    timeUnitValue = 1
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(text = "hours") },
                                onClick = {
                                    expanded = false
                                    timeUnitValue = 2
                                }
                            )
                        }
                    }
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest
            ) {
                Text("Cancel")
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    activityError = activityFieldValue.text.isBlank()
                    if (activityError) {
                        Toast.makeText(
                            context,
                            "Activity name is empty",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    timeFieldValue.text.toIntOrNull().let {
                        timeError = if (it != null && it * 60f.pow(timeUnitValue) > 86400) {
                            Toast.makeText(
                                context,
                                "Time length is over 24 hours",
                                Toast.LENGTH_SHORT
                            ).show()
                            true
                        } else {
                            it == null || it == 0
                        }
                    }

                    if (!activityError && !timeError) {
                        onAccepted(
                            initialDataItem.copy(
                                activity = activityFieldValue.text,
                                time = timeFieldValue.text.toInt(),
                                unit = timeUnitValue
                            )
                        )
                    }
                }
            ) {
                Text("Ok")
            }
        }
    )
}

/**
 * A flexible Dialog which populates the given list of names.
 * Returns the index of the clicked item.
 */
@Composable
fun RadioItemsDialog(
    title: String,
    radioItemNames: List<String>,
    currentState: Int? = null,
    onClickItem: (Int) -> Unit,
    onDismissRequest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        ),
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                for (index in 0..radioItemNames.lastIndex) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(CornerSize(30.dp)))
                            .clickable(
                                onClick = { onClickItem(index) },
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple()
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentState == index,
                            onClick = { onClickItem(index) }
                        )
                        Spacer(Modifier.width(16.dp))
                        Text(
                            text = radioItemNames[index],
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismissRequest
            ) { Text("Cancel") }
        }
    )
}
