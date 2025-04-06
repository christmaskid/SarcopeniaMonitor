package com.example.sarcopeniamonitor

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri


class MyRecordIntEntryInput(
    defaultValue: Int?,
) {
    var entryInput = mutableStateOf(defaultValue?.toString() ?: "" )
}
class MyRecordDoubleEntryInput(
    defaultValue: Double?,
) {
    var entryInput = mutableStateOf(defaultValue?.toString() ?: "" )
}
class MyRecordStringEntryInput(
    defaultValue: String?,
    replaceValue: String = ""
) {
    var entryInput = mutableStateOf(defaultValue ?: replaceValue )
}

class MyRecordStringUriInput(
    defaultValue: String?,
) {
    var entryInput = mutableStateOf(defaultValue?.toUri())
}


@Composable
fun MyRecordEntryField(
    value: String,
    onValueChange: (String) -> Unit,
    entryName: String,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null
            )
        },
        modifier = Modifier.padding(12.dp),
        label = { Text(text = entryName) },
        singleLine = true,
    )
}
@Composable
fun MyRecordDescriptionEntryField(
    description: String,
    value: String,
    onValueChange: (String) -> Unit,
    entryName: String,
) {
    Column {
        Text(
            description,
            modifier = Modifier.padding(12.dp)
        )
        TextField(
            value = value,
            onValueChange = onValueChange,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null
                )
            },
            modifier = Modifier.padding(12.dp),
            label = { Text(text = entryName) },
            singleLine = true,
        )
    }
}
@SuppressLint("DefaultLocale")
@Composable
fun MyRecordReadOnlyDoubleField(
    value: String,
    entryName: String,
) {
    Log.d("value", value)
    TextField(
        value = value,
        onValueChange = { },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null
            )
        },
        modifier = Modifier.padding(12.dp),
        label = { Text(text = entryName) },
        singleLine = true,
        readOnly = true,
    )
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyRecordSelectionField(
    value: String,
    onValueChange: (String) -> Unit,
    entryName: String,
    options: List<String>,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it } // Enables clicking to expand
    ) {
        TextField(
            label = { Text(text = entryName) },
            value = value,
            onValueChange = onValueChange, // No-op since this field is read-only
            readOnly = true,
            modifier = Modifier
                .padding(12.dp)
                .menuAnchor(),
//                .clickable { expanded = true },
            trailingIcon = {
                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
            },
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onValueChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}