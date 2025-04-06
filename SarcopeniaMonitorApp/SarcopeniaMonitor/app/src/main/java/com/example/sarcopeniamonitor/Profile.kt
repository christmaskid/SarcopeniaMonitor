package com.example.navigationpractice

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.sarcopeniamonitor.R

@Composable
fun Profile(){
    var nameInput by remember { mutableStateOf( "Name" ) }
    var genderInput by remember { mutableStateOf( "Unknown" ) }
    val context = LocalContext.current

    Box(modifier =  Modifier.fillMaxSize()) {
        Column (
            modifier = Modifier.fillMaxSize()
                .align(Alignment.Center)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
//            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painterResource(R.drawable.baseline_person_24),
                contentDescription = context.getString(R.string.profile_image),
                modifier = Modifier.fillMaxWidth()
            )
            MyProfileEntryField(
                value = nameInput,
                onValueChange = { nameInput = it},
                entryName = context.getString(R.string.profile_name),
            )
            MyProfileEntryField(
                value = genderInput,
                onValueChange = { genderInput = it},
                entryName = context.getString(R.string.profile_gender),
            )
        }
    }
}
@Composable
fun MyProfileEntryField(
    value: String,
    onValueChange: (String) -> Unit,
    entryName: String,
) {
    var enableEditing by remember { mutableStateOf( false ) }
    Row (
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            readOnly = !enableEditing,
            modifier = Modifier.padding(12.dp),
            label = { Text(text = entryName) },
            singleLine = true,
        )
        Spacer(modifier = Modifier.width(8.dp))
        IconButton (
            modifier = Modifier.background(color = MaterialTheme.colorScheme.primary),
            onClick = { enableEditing = !enableEditing}
        ) {
            Icon(
                imageVector = if (enableEditing) Icons.Default.Done else Icons.Default.Edit,
                contentDescription = if (enableEditing) "Save" else "Edit",
                tint = Color.White
            )
        }
    }
}