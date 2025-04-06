package com.example.sarcopeniamonitor

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@SuppressLint("SimpleDateFormat", "DefaultLocale")
@Composable
fun MyRecordIOInterface(
    navigationController: NavHostController,
    showRecord: MutableState<MyRecord>,
) {
    val scrollState = rememberScrollState()

    Box {
        val localContext = LocalContext.current
        val cal = Calendar.getInstance()

        val height = remember { MyRecordDoubleEntryInput(showRecord.value.getHeight()) }
        val weight = remember { MyRecordDoubleEntryInput(showRecord.value.getWeight()) }
        val bmi = remember { MyRecordDoubleEntryInput(showRecord.value.getBMI()) }
        val sbp = remember { MyRecordDoubleEntryInput(showRecord.value.getSBP()) }
        val dbp = remember { MyRecordDoubleEntryInput(showRecord.value.getDBP()) }
        val imageList = remember { ImageList(showRecord.value.getMealImages()) }

        val dateInput = remember {
            MyRecordStringEntryInput(
                showRecord.value.getDate(),
                SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(cal.time)
            )
        }
        val timeInput = remember {
            MyRecordStringEntryInput(
                showRecord.value.getTime(),
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(cal.time)
            )
        }

        Column(
            modifier = Modifier.padding(12.dp)
                .verticalScroll(scrollState)
        ) {
            DateOrTimeSelectionButton(
                value = dateInput.entryInput.value,
                onValueChange = { dateInput.entryInput.value = it },
                entryName = localContext.getString(R.string.date_item),
                onclick = {
                    showDatePickerDialog(
                        context = localContext,
                        onDateSelected = { selectedDate ->
                            dateInput.entryInput.value = selectedDate
                        }
                    )
                },
                buttonName = localContext.getString(R.string.date_btn),
            )
            DateOrTimeSelectionButton(
                value = timeInput.entryInput.value,
                entryName = localContext.getString(R.string.time_item),
                onValueChange = { timeInput.entryInput.value = it },
                onclick = {
                    showTimePickerDialog(
                        context = localContext,
                        onTimeSelected = { selectedTime ->
                            timeInput.entryInput.value = selectedTime
                        }
                    )
                },
                buttonName = localContext.getString(R.string.time_btn),
            )
            fun calculateBMI(height: String, weight: String): String {
                val h = height.toDoubleOrNull() ?: return "0.0"
                val w = weight.toDoubleOrNull() ?: return "0.0"
                return if (h > 0) (w / ((h / 100) * (h / 100))).toString() else "0.0"
            }
            MyRecordEntryField(
                value = height.entryInput.value,
                onValueChange = {
                    if ((it.matches(Regex("^\\d*\\.?\\d*\$")))) {
                        height.entryInput.value = it
                        bmi.entryInput.value = calculateBMI(height.entryInput.value, weight.entryInput.value)
                    }
                },
                entryName = localContext.getString(R.string.height_item)
            )
            MyRecordEntryField(
                value = weight.entryInput.value,
                onValueChange = {
                    if ((it.matches(Regex("^\\d*\\.?\\d*\$")))) {
                        weight.entryInput.value = it
                        bmi.entryInput.value = calculateBMI(height.entryInput.value, weight.entryInput.value)
                    }
                },
                entryName = localContext.getString(R.string.weight_item)
            )
            MyRecordReadOnlyDoubleField(
                value = bmi.entryInput.value,
                entryName = localContext.getString(R.string.bmi_item)
            )
            MyRecordEntryField(
                value = sbp.entryInput.value,
                onValueChange = {
                    if ((it.matches(Regex("^\\d*\\.?\\d*\$"))))
                        sbp.entryInput.value = it
                },
                entryName = localContext.getString(R.string.sbp_item)
            )
            MyRecordEntryField(
                value = dbp.entryInput.value,
                onValueChange = {
                    if ((it.matches(Regex("^\\d*\\.?\\d*\$"))))
                        dbp.entryInput.value = it
                },
                entryName = localContext.getString(R.string.dbp_item)
            )

            // Display image upload fields
            Log.d("Images", "${imageList.images}")
            imageList.images.forEachIndexed { index, mealImage ->
                Log.d("Images", "$index $mealImage")
                MyImageUploadField(
                    descriptionString = "Meal Image ${index + 1}",
                    currentImageUri = remember { mutableStateOf(mealImage.imageUri?.toUri()) },
                    mealType = remember { mutableStateOf(mealImage.mealType ?: "Other") },
                    onImageSelected = { uri, mealType ->
                        imageList.updateImage(index, uri.toString(), mealType)  // Update existing entry by index
                        Log.d("MealImage", "Updated index=$index, URI=${uri}, mealType=$mealType")
                    }
                )
            }

            // Add "+" button for creating new empty image entries
            Button(
                onClick = { imageList.addImage("Other") },  // Create a new empty entry
                modifier = Modifier.padding(8.dp)
            ) {
                Row {
                    Icon(Icons.Default.Add, contentDescription = "Add Image")
                    Text("Add Food Photo")
                }
            }
        }

        Box(
            modifier = Modifier.padding(8.dp).align(Alignment.BottomEnd)
        ) {
            ConfirmButton(
                navigationController, showRecord,
                dateInput.entryInput.value, timeInput.entryInput.value,
                height.entryInput.value, weight.entryInput.value,
                sbp.entryInput.value, dbp.entryInput.value,
                imageList
            )
        }
    }
}

@Composable
fun ConfirmButton(
    navigationController: NavHostController,
    showRecord: MutableState<MyRecord>,
    dateInput: String,
    timeInput: String,
    heightInput: String,
    weightInput: String,
    sbpInput: String,
    dbpInput: String,
    imageList: ImageList
) {
    val localContext = LocalContext.current

    FloatingActionButton(
        onClick = {
            Log.d("Record", "Update ${showRecord.value.getRecordID()}")
            showRecord.value.update(
                recordDateString = dateInput,
                recordTimeString = timeInput,
                heightValue = heightInput.toDoubleOrNull(),
                weightValue = weightInput.toDoubleOrNull(),
                sbpValue = sbpInput.toDoubleOrNull(),
                dbpValue = dbpInput.toDoubleOrNull(),
                mealImages = imageList.getAllImages()
            )
            if (showRecord.value.getRecordID() == 0) {
                postMyRecord(showRecord.value)
            } else {
                updateMyRecord(showRecord.value)
            }
            navigationController.navigate(Screens.RecordList.screen)
        },
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = Color.White,
        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp)
    ) {
        Icon(
            Icons.Default.Done,
            contentDescription = localContext.getString(R.string.create_btn),
            modifier = Modifier,
            tint = Color.White
        )
    }
}

@Composable
fun MyImageUploadField(
    descriptionString: String,
    currentImageUri: MutableState<Uri?>,
    mealType: MutableState<String>,
    onImageSelected: (Uri, String) -> Unit
) {
    val context = LocalContext.current
    val mealTypes = listOf("Breakfast", "Lunch", "Dinner", "Snack", "Other")
    val imageBitmap = remember { mutableStateOf<Bitmap?>(null) }

    // Fetch image content if the URI is available
    LaunchedEffect(currentImageUri.value) {
        currentImageUri.value?.let { uri ->
            fetchImage(uri.toString()) { bitmap ->
                imageBitmap.value = bitmap
            }
        }
    }

    // Launcher for gallery
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                uploadImageToServer(it, context) { uploadedUri ->
                    currentImageUri.value = uploadedUri.toUri()  // Update the current entry's URI
                    onImageSelected(uploadedUri.toUri(), mealType.value)  // Update the existing entry
                }
            }
        }
    )

    // Launcher for camera
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success: Boolean ->
            if (success && currentImageUri.value != null) {
                uploadImageToServer(currentImageUri.value!!, context) { uploadedUri ->
                    currentImageUri.value = uploadedUri.toUri()  // Update the current entry's URI
                    currentImageUri.value?.let { onImageSelected(it, mealType.value) }  // Update the existing entry
                }
            }
        }
    )

    fun captureImage() {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }
        val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        currentImageUri.value = uri
        uri?.let { cameraLauncher.launch(it) }
    }

    Column {
        MyRecordSelectionField(
            value = mealType.value,
            onValueChange = {
                mealType.value = it
                currentImageUri.value?.let { onImageSelected(currentImageUri.value!!, mealType.value) }
            },
            entryName = "Meal Type",
            options = mealTypes
        )
        Row {
            Button(onClick = { galleryLauncher.launch("image/*") }) {
                Text("Pick from Gallery")
            }
            Button(onClick = { captureImage() }) {
                Text("Take a Photo")
            }
        }
        imageBitmap.value?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = "Selected Image",
                modifier = Modifier.size(100.dp)
            )
        }
    }
}

fun fetchImage(imageUrl: String, onSuccess: (Bitmap) -> Unit) {
    val api = getAPIService()
    api.fetchImage(imageUrl).enqueue(object : Callback<ResponseBody> {
        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
            if (response.isSuccessful) {
                response.body()?.byteStream()?.use { inputStream ->
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    onSuccess(bitmap)
                }
            } else {
                Log.e("FetchImage", "Failed to fetch image: ${response.errorBody()?.string()}")
            }
        }

        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
            Log.e("FetchImage", "Error: ${t.message}")
        }
    })
}

fun uploadImageToServer(imageUri: Uri, context: Context, onSuccess: (String) -> Unit) {
    val contentResolver = context.contentResolver
    val inputStream = contentResolver.openInputStream(imageUri)
    val requestBody = inputStream?.let { RequestBody.create("image/*".toMediaTypeOrNull(), it.readBytes()) }
    val imagePart = requestBody?.let { MultipartBody.Part.createFormData("image", "upload.jpg", it) }

    if (imagePart == null) {
        Log.e("Upload", "Failed to create imagePart")
        return
    }

    val apiService = getAPIService()
    val call: Call<ResponseBody> = apiService.uploadImage(imagePart)

    call.enqueue(object : Callback<ResponseBody> {
        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
            if (response.isSuccessful) {
                val responseBody = response.body()?.string()
                if (responseBody != null) {
                    try {
                        // Parse the JSON response to extract the "image_uri" value
                        val jsonResponse = JSONObject(responseBody)
                        val uploadedUri = jsonResponse.getString("image_uri")
                        onSuccess(uploadedUri)  // Pass the extracted URI to the callback
                    } catch (e: Exception) {
                        Log.e("Upload", "Failed to parse response: ${e.message}")
                    }
                } else {
                    Log.e("Upload", "Response body is null")
                }
            } else {
                Log.e("Upload", "Upload failed: ${response.errorBody()?.string()}")
            }
        }

        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
            Log.e("Upload", "Error: ${t.message}")
        }
    })
}
