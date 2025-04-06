package com.example.sarcopeniamonitor

import Home
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.llmchat.MyLLMChatScreen
import com.example.sarcopeniamonitor.ui.theme.SarcopeniaMonitorTheme
import kotlinx.coroutines.suspendCancellableCoroutine
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SarcopeniaMonitorTheme {
                Surface (
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MyApp()
                }
            }
        }
    }
}

@Composable
fun BottomTab(
    navigationController: NavHostController,
    selectedTab: MutableIntState,
    tabScreen: String,
    iconVector: ImageVector,
    tabName: String,
    tabNumber: Int,
    modifier: Modifier
) {
    IconButton(
        onClick =  {
            selectedTab.intValue = tabNumber
            navigationController.navigate(tabScreen)
        },
        modifier = modifier,
    ) {
        Icon(
            iconVector,
            contentDescription = tabName,
            modifier = Modifier.size(26.dp),
            tint = if (selectedTab.intValue == tabNumber) Color.White else Color.DarkGray,
        )
    }
}

@Composable
fun BottomTabAdd(
    onClick: () -> Unit,
    modifier: Modifier,
) {

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        FloatingActionButton(
            onClick = onClick
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@SuppressLint("MutableCollectionMutableState", "RememberReturnType")
@Preview
@Composable
fun MyApp() {

    val myRecordList = remember { MyRecordList() }
    val showRecord = remember { mutableStateOf( MyRecord()) }

    val prevRecords = fetchUserRecords()
    LaunchedEffect(prevRecords.value) {
        myRecordList.clear()
        prevRecords.value.forEach { record ->
            val newRecord = myRecordList.addElement() // import the old data
            newRecord.setPrivateRecord(record)
        }
    }

    val questionnaire = remember { mutableStateOf(Questionnaire()) }
    val latestQuestionnaire = fetchLatestQuestionnaire() // Fetch latest data
    var questionnaireFetchState by remember { mutableStateOf( false ) }

    // Update the questionnaire whenever the latest data changes
    LaunchedEffect(latestQuestionnaire, questionnaire, questionnaireFetchState) {
        latestQuestionnaire?.toQuestionnaire()?.let { newQuestionnaire ->
            questionnaire.value = newQuestionnaire // Replace with the latest data
            Log.d("questionnaireFetchState", "True")
            questionnaireFetchState = true
        } ?: Questionnaire()
        if (!questionnaireFetchState) {
            Log.d("questionnaireFetchState", "False -> page 2")
        }
    }

    val prediction = remember { mutableStateOf(Prediction()) }
//    val latestPrediction = fetchLatestPrediction()
//    LaunchedEffect(latestPrediction) {
//        latestPrediction?.let { newPrediction ->
//            prediction.value = newPrediction
//        }?: Prediction()
//    }

    val physicalTests = remember{ mutableStateOf(PhysicalTestList()) }

    if (!questionnaireFetchState) {
        QuestionnaireInterfaceFirst(questionnaire, prediction)
    } else {
        val navigationController = rememberNavController()
        LocalContext.current.applicationContext
        val selectedTab = remember {
            mutableIntStateOf(0)
        }
        // Update selected tab based on navigation
        val navBackStackEntry = navigationController.currentBackStackEntryAsState()
        LaunchedEffect(navBackStackEntry.value?.destination?.route) {
            selectedTab.intValue =
                when (navBackStackEntry.value?.destination?.route) {
                    Screens.Home.screen -> 0
                    Screens.RecordList.screen -> 1
                    Screens.Questionnaire.screen -> 2
                    Screens.MyLLMChatScreen.screen -> 3
                    else -> 0
                }
        }

        Scaffold (
            bottomBar = {
                if (questionnaireFetchState) {
                    BottomAppBar (
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        BottomTab(
                            navigationController, selectedTab, Screens.Home.screen,
                            Icons.Default.Home, "Home", 0, Modifier.weight(1f)
                        )
                        BottomTab(
                            navigationController, selectedTab, Screens.RecordList.screen,
                            Icons.Default.DateRange, "recordList", 1, Modifier.weight(1f)
                        )
                        BottomTab(
                            navigationController, selectedTab, Screens.Questionnaire.screen,
                            Icons.Default.Info, "Questionnaire", 2, Modifier.weight(1f)
                        )
                        BottomTab(
                            navigationController, selectedTab, Screens.MyLLMChatScreen.screen,
                            Icons.Default.Face, "chatScreen", 3, Modifier.weight(1f)
                        )
                    }
                }
            }
        ) { paddingValues ->
            NavHost (
                navController = navigationController,
                startDestination = Screens.Home.screen,
                modifier = Modifier.padding(paddingValues),
            ) {
                composable(Screens.Home.screen) {
                    Home(prediction, physicalTests) }
                composable(Screens.RecordList.screen) {
                    RecordList(navigationController, myRecordList, showRecord, prediction) }
                composable(Screens.Questionnaire.screen) {
                    QuestionnaireInterface(navigationController, questionnaire, prediction)
                }
    //                composable(Screens.Profile.screen) { Profile() }
                composable(Screens.MyRecordIOInterface.screen) {
                    MyRecordIOInterface(navigationController, showRecord)
                }
                composable(Screens.MyLLMChatScreen.screen) {
                    MyLLMChatScreen(prediction, physicalTests)
                }
            }
        }
    }
}

fun getAPIService(): ApiService {
    val retrofit = Retrofit.Builder()
        .baseUrl("http://10.0.2.2:8000/")
//        .baseUrl("https://web-production-b8b9.up.railway.app/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api = retrofit.create(ApiService::class.java)
    return api
}

fun postMyRecord(
    record: MyRecord
) {
    val api = getAPIService()
    api.postUserRecord(record.getPrivateRecord()).enqueue(object : Callback<UserRecord> {
        override fun onResponse(call: Call<UserRecord>, response: Response<UserRecord>) {
            response.body()?.let { record.setPrivateRecord(it) }
            if (response.isSuccessful) {
                Log.d("Record", "MyRecord posted successfully: ${response.body()}")
            } else {
                Log.e("API Error", "Failed to post record")
            }
        }
        override fun onFailure(call: Call<UserRecord>, t: Throwable) {
            Log.e("API Error", record.getPrivateRecord().toString())
            Log.e("API Error", t.message ?: "Unknown error")
        }
    })
}

fun updateMyRecord(
    record: MyRecord
) {

//    Log.d("Update", "Update ${record.getHeight()}, ${record.getWeight()}")
    val api = getAPIService()
    api.updateUserRecord(record.getRecordID(), record.getPrivateRecord()).enqueue(object : Callback<UserRecord> {
        override fun onResponse(call: Call<UserRecord>, response: Response<UserRecord>) {
            if (response.isSuccessful) {
                Log.d("Record", "MyRecord updated successfully: ${response.body()}")
            } else {
                Log.e("API Error", "Failed to post record")
            }
        }
        override fun onFailure(call: Call<UserRecord>, t: Throwable) {
            Log.e("API Error", t.message ?: "Unknown error")
        }
    })
}

@Composable
fun fetchUserRecords(): MutableState<List<UserRecord>> {
    val recordsState = remember { mutableStateOf<List<UserRecord>>(emptyList()) }
    val api = getAPIService()

    LaunchedEffect(Unit) {  // Ensures API call happens when composable loads
        api.getUserRecords().enqueue(object : Callback<List<UserRecord>> {
            override fun onResponse(call: Call<List<UserRecord>>, response: Response<List<UserRecord>>) {
                if (response.isSuccessful) {
                    Log.d("Fetch", response.body().toString())
                    val records = response.body()?.map { record ->
                        Log.d("Fetch images", "Meal images for record ${record.recordID}: ${record.mealImages}")
                        record
                    } ?: emptyList()
                    recordsState.value = records
                    Log.d("Fetch", "Successfully fetched records with meal images: ${recordsState.value}")
                } else {
                    Log.e("API Error", "Failed to fetch records")
                }
            }

            override fun onFailure(call: Call<List<UserRecord>>, t: Throwable) {
                Log.e("API Error", t.message ?: "Unknown error")
            }
        })
    }
    return recordsState
}

suspend fun refreshUserRecords(): List<UserRecord> {
    return suspendCancellableCoroutine { continuation ->
        val api = getAPIService()

        // Make the API request
        api.getUserRecords().enqueue(object : Callback<List<UserRecord>> {
            override fun onResponse(call: Call<List<UserRecord>>, response: Response<List<UserRecord>>) {
                if (response.isSuccessful) {
                    val records = response.body() ?: emptyList()
                    continuation.resume(records)  // Resume the coroutine with the fetched records
//                    Log.d("Fetch", "Successfully fetched records: $records")
                } else {
                    continuation.resumeWithException(Exception("API error: ${response.code()}"))  // Resume with an exception on failure
//                    Log.e("API Error", "Failed to fetch records")
                }
            }

            override fun onFailure(call: Call<List<UserRecord>>, t: Throwable) {
                continuation.resumeWithException(t)  // Resume with exception on failure
//                Log.e("API Error", t.message ?: "Unknown error")
            }
        })
    }
}

/* Questionnaire*/
fun submitQuestionnaire(
    questionnaire: Questionnaire
) {
    val api = getAPIService()

    // Collect the answers from the questionnaire
    val answers = mutableMapOf<String, Any>()
    MyConstants.questionKeysList.forEach { key ->
        val answer = questionnaire.getField(key)  // Get answer for each question
        if (answer != null) {
            answers[key] = answer
        }
    }

    // Create the request object
    val requestData = QuestionnaireData(answers)
    Log.d("data", requestData.toString())

    // Enqueue the API call for submission
    api.submitQuestionnaireData(requestData).enqueue(object : Callback<QuestionnaireResponse> {
        override fun onResponse(
            call: Call<QuestionnaireResponse>,
            response: Response<QuestionnaireResponse>
        ) {
            if (response.isSuccessful) {
                // Successfully submitted the questionnaire
                Log.d("Questionnaire", "Questionnaire submitted successfully: ${response.body()}")
            } else {
                // Handle error response
                Log.e("API Error", "Failed to submit questionnaire: ${response.errorBody()}")
            }
        }

        override fun onFailure(call: Call<QuestionnaireResponse>, t: Throwable) {
            // Handle failure
            Log.e("API Error", "Request failed: ${t.message ?: "Unknown error"}")
        }
    })
}
@Composable
fun fetchLatestQuestionnaire(): QuestionnaireData? {
    Log.d("Fetch", "")
    val questionnaireState = remember { mutableStateOf<QuestionnaireResponse?>(null)}
    val api = getAPIService()

    LaunchedEffect (Unit) {  // Ensures API call happens when composable loads
        api.getLatestQuestionnaireData().enqueue(object : Callback<QuestionnaireResponse> {
            override fun onResponse(
                call: Call<QuestionnaireResponse>,
                response: Response<QuestionnaireResponse>
            ) {
                if (response.isSuccessful) {
                    questionnaireState.value = response.body()
                    Log.d("Fetch", response.body().toString())
                    Log.d("Fetch", "Successfully fetched questionnaire: ${questionnaireState.value}")
                } else {
                    Log.e("API Error", "Failed to fetch records")
                }
            }

            override fun onFailure(call: Call<QuestionnaireResponse>, t: Throwable) {
                Log.e("API Error", t.message ?: "Unknown error")
            }
        })
    }
    if (questionnaireState.value == null) {
        Log.e("Debug", "questionnaireMap is null!")
    } else {
        Log.d("Debug", "questionnaireMap is not null: ${questionnaireState}")
    }
    return questionnaireState.value?.toQuestionnaireData()
}
suspend fun refreshQuestionnaire(): Questionnaire {
    return suspendCancellableCoroutine { continuation ->
        val api = getAPIService()
        var questionnaire = Questionnaire()

        // Make the API request
        api.getLatestQuestionnaireData().enqueue(object : Callback<QuestionnaireResponse> {
            override fun onResponse(
                call: Call<QuestionnaireResponse>,
                response: Response<QuestionnaireResponse>
            ) {
                if (response.isSuccessful) {
                    questionnaire = response.body()?.toQuestionnaireData()?.toQuestionnaire() ?: Questionnaire()
                    continuation.resume(questionnaire)
                    Log.d("Fetch", "Successfully fetched records: $questionnaire")
                } else {
//                    continuation.resumeWithException(Exception("API error: ${response.code()}"))  // Resume with an exception on failure
                    continuation.resume(questionnaire) // return empty questionnaire
                }
            }

            override fun onFailure(call: Call<QuestionnaireResponse>, t: Throwable) {
                continuation.resumeWithException(t)  // Resume with exception on failure
//                Log.e("API Error", t.message ?: "Unknown error")
            }
        })
    }
}

fun fetchLatestPrediction(): Prediction {
    Log.d("Fetch", "")
    var prediction: Prediction? = Prediction()
    val api = getAPIService()

    api.getLatestPrediction().enqueue(object : Callback<Prediction> {
        override fun onResponse(
            call: Call<Prediction>,
            response: Response<Prediction>
        ) {
            if (response.isSuccessful) {
                prediction = response.body()
                Log.d("Fetch", "Successfully fetched records: ${prediction}")
            } else {
                Log.e("API Error", "Failed to fetch records")
            }
        }

        override fun onFailure(call: Call<Prediction>, t: Throwable) {
            Log.e("API Error", t.message ?: "Unknown error")
        }
    })
    return prediction?:Prediction()
}