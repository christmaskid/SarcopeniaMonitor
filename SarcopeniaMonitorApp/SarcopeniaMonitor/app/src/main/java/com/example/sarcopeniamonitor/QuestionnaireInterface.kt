package com.example.sarcopeniamonitor

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


// Composable UI for Questionnaire
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionnaireInterface(
    navigationController: NavHostController,
    questionnaire: MutableState<Questionnaire>,
    prediction: MutableState<Prediction>
) {
    val context = LocalContext.current.applicationContext
    val questionKeys = MyConstants.questionKeysList
    val questionList = questionKeys.map { key ->
        key to context.resources.getStringArray(
            MyConstants.optionResources[key] ?: R.array.unknown_options // default fallback
        ).toList() // Convert the Array<String> to List<String>
    }
    var isRefreshing by remember { mutableStateOf(false) }

    Log.d("QuestionnaireInterface", "Start with values ${questionnaire.value.getAllFields()}")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        PullToRefreshBox (
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                CoroutineScope(Dispatchers.IO).launch {
                    val latestQuestionnaire = refreshQuestionnaire() // Fetch latest data
                    var updatedQuestionnaire: Questionnaire

                    latestQuestionnaire.let { newQuestionnaire ->
                        updatedQuestionnaire = newQuestionnaire // Replace with the latest data
                    }
                    withContext(Dispatchers.Main) {
                        questionnaire.value = updatedQuestionnaire
                        isRefreshing = false
                    }
                }
            },
            modifier = Modifier
        ) {
            LaunchedEffect(isRefreshing) {
                if (isRefreshing) {
                    delay(1000)
                    isRefreshing = false
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Questionnaire",
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        .background(color = MaterialTheme.colorScheme.background),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )

                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    item {
                        BirthDateEntry(questionnaire.value)
                    }
                    items(questionList.size) { index ->
                        val (shortKey, options) = questionList[index]
                        val descriptionResId =
                            MyConstants.questionDescriptions[shortKey] ?: R.string.unknown_description
                        if (shortKey != "birthDate") {
                            QuestionnaireEntry(
                                shortKey, descriptionResId, options, questionnaire.value
                            )
                        }
                    }
                }

                SubmitButton(navigationController, questionnaire.value, prediction)
            }
        }
    }
}

@Composable
fun QuestionnaireInterfaceFirst(
    questionnaire: MutableState<Questionnaire>,
    prediction: MutableState<Prediction>
) {
    val context = LocalContext.current.applicationContext
    val questionKeys = MyConstants.questionKeysList
    val questionList = questionKeys.map { key ->
        key to context.resources.getStringArray(
            MyConstants.optionResources[key] ?: R.array.unknown_options // default fallback
        ).toList() // Convert the Array<String> to List<String>
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Questionnaire",
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    .background(color = MaterialTheme.colorScheme.background),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text="No previous answers were found. Please fill out the form to help us better evaluate your health status.",
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    .background(color = MaterialTheme.colorScheme.background),
                fontSize = 12.sp,
                fontWeight =  FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,
            )

            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                item {
                    BirthDateEntry(questionnaire.value)
                }
                items(questionList.size) { index ->
                    val (shortKey, options) = questionList[index]
                    val descriptionResId =
                        MyConstants.questionDescriptions[shortKey] ?: R.string.unknown_description
                    if (shortKey != "birthDate") {
                        QuestionnaireEntry(
                            shortKey, descriptionResId, options, questionnaire.value
                        )
                    }
                }
            }
            SubmitButtonFirst(questionnaire.value, prediction)
        }
    }
}

@Composable
fun QuestionnaireEntry(
    shortKey: String,
    descriptionResId: Int,
    options: List<String>,
    questionnaire: Questionnaire
) {
    var descriptionString = stringResource(descriptionResId)
    val descriptionParts = descriptionString.split("{description}")


    Column(modifier = Modifier.padding(vertical = 8.dp)) {

        if (descriptionParts.size > 1) {
            // If there's a {description} part, display it in a separate text box
            Card(
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = descriptionParts[0], // Display the part after {description}
                    fontSize = 16.sp,
                    modifier = Modifier
                        .padding(8.dp)

                )
            }
            descriptionString = descriptionParts[1].trim()
        }

        // Description for the question
        Text(
            text = descriptionString.replace("{input}", ""),
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(8.dp)
        )
        val isNumericInputRequired = descriptionString.contains("{input}")

        if (isNumericInputRequired) {
            // If numeric input is required, show a TextField
            var inputValue by remember { mutableStateOf(questionnaire.getField(shortKey)?.toString() ?: "") }

            TextField(
                value = inputValue,
                onValueChange = {
                    inputValue = it
                    questionnaire.setField(shortKey, inputValue.toIntOrNull() ?: 0)
                },
                label = { Text(stringResource(R.string.input_default)) },
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Update the questionnaire with the numeric input value
            if (inputValue.isNotEmpty()) {
                questionnaire.setField(shortKey, inputValue)
            }
        } else {
            // Otherwise, show the options as radio buttons
            Column (
                modifier = Modifier.fillMaxWidth(),
            ) {
                var selectedIndex by remember { mutableStateOf((questionnaire.getField(shortKey) as? Int) ?: -1) }
                Log.d("Here", "$shortKey, ${questionnaire.getField(shortKey)}, ${questionnaire.getField(shortKey) as? Int} $selectedIndex")
                val defaultOption = remember { if (selectedIndex != -1) options[selectedIndex] else null }
                val selectedOption = remember { mutableStateOf(defaultOption) }
                options.forEachIndexed { idx, option ->
                    // Row for each option with radio button and text
                    val selected = selectedOption.value == option
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.widthIn(min = 0.dp, max = 200.dp) // Limit width for each option
                    ) {
                        RadioButton(
                            selected = selected,
                            onClick = {
                                selectedOption.value = option
                                questionnaire.setField(shortKey, idx) // Update the selected option
                                Log.d("Update", questionnaire.getField(shortKey).toString())
                            }
                        )
                        Text(
                            text = option,
                            style = TextStyle(fontSize = 16.sp),
                            modifier = Modifier.padding(start = 4.dp).fillMaxWidth(),
                            //                        maxLines = 1, // Prevent multi-line wrapping
                            overflow = TextOverflow.Ellipsis // Handle overflow with ellipsis
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun BirthDateEntry(questionnaire: Questionnaire) {
    val localContext = LocalContext.current
    val birthDate = remember { mutableStateOf(questionnaire.getField("birthDate") as? String ?: "") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
//            .padding(8.dp)
    ) {
        Text(
            text = localContext.getString(R.string.birthDate),
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(8.dp)
        )

        DateOrTimeSelectionButton(
            value = birthDate.value,
            onValueChange = {
                birthDate.value = it
                questionnaire.setField("birthDate", it)
            },
            entryName = localContext.getString(R.string.date_item),
            onclick = {
                showDatePickerDialog(
                    context = localContext,
                    onDateSelected = { selectedDate ->
                        birthDate.value = selectedDate
                        questionnaire.setField("birthDate", selectedDate)
                    }
                )
            },
            buttonName = localContext.getString(R.string.date_btn),
        )
    }
}

@Composable
fun SubmitButton(
    navigationController: NavHostController,
    questionnaire: Questionnaire,
    prediction: MutableState<Prediction>
) {
    // List of required fields (you can adjust this based on your actual fields)
    val requiredFields = MyConstants.questionKeysList

    // State for the warning message
    var showWarning by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        if (showWarning) {
            Text(
                text = "Please fill out all required fields.",
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Button(
            onClick = {
                // Check if all required fields are filled
                val allFieldsFilled = questionnaire.isAllRequiredFieldsFilled(requiredFields)

                if (!allFieldsFilled) {
                    showWarning = true
                } else {
                    showWarning = false
                }

                if (allFieldsFilled) {
                    submitQuestionnaire(questionnaire)
                    Log.d("Questionnaire", questionnaire.getAllFields().toString())
                    navigationController.navigate(Screens.Home.screen)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Text(text = stringResource(R.string.submit_btn))
        }
    }
}

@Composable
fun SubmitButtonFirst(
    questionnaire: Questionnaire,
    prediction: MutableState<Prediction>
) {
    // List of required fields (you can adjust this based on your actual fields)
    val requiredFields = MyConstants.questionKeysList

    // State for the warning message
    var showWarning by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        if (showWarning) {
            Text(
                text = "Please fill out all required fields.",
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Button(
            onClick = {
                // Check if all required fields are filled
                val allFieldsFilled = questionnaire.isAllRequiredFieldsFilled(requiredFields)

                if (!allFieldsFilled) {
                    showWarning = true
                } else {
                    showWarning = false
                    submitQuestionnaire(questionnaire)
                    Log.d("Questionnaire", questionnaire.getAllFields().toString())
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Text(text = stringResource(R.string.submit_btn))
        }
    }
}
