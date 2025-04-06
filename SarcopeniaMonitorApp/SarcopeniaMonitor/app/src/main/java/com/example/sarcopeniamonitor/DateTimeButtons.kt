package com.example.sarcopeniamonitor

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


@Composable
fun DateOrTimeSelectionButton(
    value: String,
    onValueChange: (String) -> Unit,
    entryName: String,
    onclick: () -> Unit,
    buttonName: String,
) {
    Row (
        verticalAlignment = Alignment.CenterVertically
    ) {
        MyRecordEntryField(
            value = value,
            onValueChange = onValueChange,
            entryName = entryName,
        )
        Button(
            onClick = onclick
        ) {
            Text(text = buttonName)
        }
    }
}

fun showTimePickerDialog (
    context: Context,
    onTimeSelected: (String) -> Unit,
) {
    val cal = Calendar.getInstance()
    val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
        cal.set(Calendar.HOUR_OF_DAY, hour)
        cal.set(Calendar.MINUTE, minute)
        val formattedTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(cal.time)
        onTimeSelected(formattedTime)
    }

    TimePickerDialog(
        /* context = */ context,
        /* listener = */ timeSetListener,
        /* hourOfDay = */ cal.get(Calendar.HOUR_OF_DAY),
        /* minute = */ cal.get(Calendar.MINUTE),
        /* is24HourView = */ true
    ).show()
}

fun showDatePickerDialog (
    context: Context,
    onDateSelected: (String) -> Unit,
) {
    val cal = Calendar.getInstance()
    val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month)
        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        val formattedDate = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(cal.time)
        onDateSelected(formattedDate)
    }

    DatePickerDialog(
        context,
        dateSetListener,
        cal.get(Calendar.YEAR),
        cal.get(Calendar.MONTH),
        cal.get(Calendar.DAY_OF_MONTH)
    ).show()
}