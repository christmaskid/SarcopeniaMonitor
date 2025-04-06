package com.example.sarcopeniamonitor

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun RecordList(
    navigationController: NavHostController,
    records: MyRecordList,
    showRecord: MutableState<MyRecord>,
    prediction: MutableState<Prediction>
) {
    var safeRecords by remember { mutableStateOf(records) }
    var updatedRecords: List<UserRecord>
    var isRefreshing by remember { mutableStateOf(false) }
    val context = LocalContext.current.applicationContext

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                CoroutineScope(Dispatchers.IO).launch {
                    updatedRecords = refreshUserRecords()
                    //                Log.d("Fetch", "Get ${updatedRecords.size}")

                    val newSafeRecords = MyRecordList() // Assuming this creates a new list
                    updatedRecords.forEach { record ->
                        val newRecord = newSafeRecords.addElement()
                        newRecord.setPrivateRecord(record)
                        // set updated record as content
//                        Log.d("Fetch", "Update $record")
                    }
                    safeRecords = newSafeRecords
                    // Update the state with the updated records list on the main thread
                    withContext(Dispatchers.Main) {
                        safeRecords = newSafeRecords
                        prediction.value = fetchLatestPrediction()
                        isRefreshing = false
                    }

                    //                Log.d("Fetch", "Complete refreshing with ${newSafeRecords.recordList.size} records")
                }
            },
            modifier = Modifier
        ) {
            LaunchedEffect(isRefreshing) {
                if (isRefreshing) {
                    delay(1000)
                    isRefreshing = false
                    //                Log.d("Refresh", "Set to false")
                }
            }
            val updatedList = remember { derivedStateOf { records.recordList.reversed() } }
            LazyColumn (
                modifier = Modifier
                    .padding(8.dp)
                    //                .verticalScroll(rememberScrollState())
                    .background(color = MaterialTheme.colorScheme.background),
                //            horizontalAlignment = Alignment.CenterHorizontally,
                //            verticalArrangement = Arrangement.Top
            ) {
                stickyHeader {
                    Row (
                        modifier = Modifier
                            .padding(8.dp)
                            .heightIn(min = 64.dp, max = 64.dp)
                            .fillMaxWidth()
                            .background(color = MaterialTheme.colorScheme.background),
                        verticalAlignment = Alignment.CenterVertically // Vertically and horizontally center the content
                    ) {
                        Text(
                            text = "All Records",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
//            items(safeRecords.recordList, key = { it.getRecordID() }) { record ->
//                DisplayRecord(navigationController, record, showRecord)
//            }
                items(updatedList.value, key = { it.getRecordID() }) { record ->
                    DisplayRecord(navigationController, record, showRecord)
                }
            }
        }

        BottomTabAdd(
            onClick = {
                navigationController.navigate(Screens.MyRecordIOInterface.screen)
                val lastRecord = records.getLastElement()
                val newRecord = records.addElement()
                Log.d("LastRecord", lastRecord.toString())
                if (lastRecord != null) {
                    newRecord.setPrivateRecord(lastRecord.getPrivateRecord().copy(
                        recordID = 0, 
                        recordDate = null, 
                        recordTime = null,
                        sbp = null,  // Clear sbp
                        dbp = null,  // Clear dbp
                        mealImages = mutableListOf()  // Clear mealImages
                    ))
                }
                Log.d("NewRecord",  newRecord.getRecordID().toString())
                showRecord.value = newRecord
                showRecord.value.enableEditing(true)

                Toast.makeText(
                    context,
                    "Add a new record",
                    Toast.LENGTH_SHORT
                ).show()
                Log.d("records", records.toString())
            },
            modifier = Modifier.padding(16.dp).align(Alignment.BottomEnd)
        )
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun DisplayRecord(
    navigationController: NavHostController,
    record: MyRecord,
    showRecord: MutableState<MyRecord>
) {
    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        onClick = {
            showRecord.value = record
            showRecord.value.enableEditing(true)
            Log.d("Click", "Click to edit record ${showRecord.value.getRecordID()}")
            navigationController.navigate(Screens.MyRecordIOInterface.screen)
        }
    ) {
        Column {
            RecordEntryDisplay(
                R.string.datetime_format, record.getDate(), record.getTime()
            )
            RecordEntryDisplay(
                R.string.height_format, record.getHeight()
            )
            RecordEntryDisplay(
                R.string.weight_format, record.getWeight()
            )
            RecordEntryDisplay(
                R.string.bmi_format, record.getBMI()
            )
            RecordEntryDisplay(
                R.string.bp_format, record.getSBP(), record.getDBP()
            )

            // Display meal images
            if (record.getPrivateRecord().mealImages.isNotEmpty()) {
                Text(
                    text = "Meal Images:",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(8.dp)
                )
                Row(
                    modifier = Modifier.padding(8.dp)
                ) {
                    record.getPrivateRecord().mealImages.forEach { mealImage ->
                        mealImage.imageUri?.let { imagePath ->
                            val imageBitmap = remember { mutableStateOf<Bitmap?>(null) }

                            // Fetch image content
                            LaunchedEffect(imagePath) {
                                fetchImage(imagePath) { bitmap ->
                                    imageBitmap.value = bitmap
                                }
                            }

                            imageBitmap.value?.let {
                                Image(
                                    bitmap = it.asImageBitmap(),
                                    contentDescription = "Meal Image",
                                    modifier = Modifier
                                        .size(100.dp)
                                        .padding(4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecordEntryDisplay(
    recordEntryFormat: Int,
    vararg recordEntries: Any?, // Accepts multiple values
) {
    if (recordEntries.size == 1 && recordEntries[0] == null) {
        return // Don't render anything if there's only one null entry
    }

    val context = LocalContext.current
    val formattedText = try {
        if (recordEntries.all { it != null }) {
            context.getString(recordEntryFormat, *recordEntries)
        } else {
            null
        }
    } catch (e: Exception) {
        Log.e("RecordEntryDisplay", "Invalid format or resource ID", e)
        null
    }

    if (formattedText != null) {
        Text(
            text = formattedText,
            fontSize = 24.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.padding(8.dp)
        )
    }
}
