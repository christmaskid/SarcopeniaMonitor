package com.example.llmchat

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.sarcopeniamonitor.PhysicalTestList
import com.example.sarcopeniamonitor.Prediction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

@Composable
fun MyLLMChatScreen(
    prediction: MutableState<Prediction>,
    physicalTests: MutableState<PhysicalTestList>
) {
    var messages by remember { mutableStateOf(listOf<Pair<String, String>>()) }
    var userInput by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Chat History
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(messages) { (role, text) ->
                Text(text = "$role: $text", modifier = Modifier.padding(8.dp))
            }
        }

        // User Input
        Row(modifier = Modifier.fillMaxWidth()) {
            TextField(
                value = userInput,
                onValueChange = { userInput = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Enter message") }
            )
            Button(
                onClick = {
                    val inputText = userInput
                    userInput = ""
                    messages = messages + ("User" to inputText)

                    coroutineScope.launch {
                        val response = sendToLlama(inputText)
                        messages = messages + ("Llama" to (response ?: "Error"))
                    }
                },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text("Send")
            }
        }
    }
}

// Function to send text to Llama API
suspend fun sendToLlama(prompt: String): String? {
    return withContext(Dispatchers.IO) {
        try {
            val url = URL("http://localhost:11434/api/generate")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true

            val requestBody = JSONObject().apply {
                put("model", "llama3")  // Change model name if needed
                put("prompt", prompt)
                put("stream", false)  // Set to true if streaming
            }.toString()

            connection.outputStream.use { it.write(requestBody.toByteArray()) }

            val response = connection.inputStream.bufferedReader().readText()
            val responseJson = JSONObject(response)
            responseJson.getString("response")
        } catch (e: Exception) {
            Log.e("LlamaChat", "Error: ${e.message}")
            null
        }
    }
}
