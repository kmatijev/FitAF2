package com.example.fitaf

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import java.util.UUID

@Composable
fun AddExerciseDialog(
    onDismiss: () -> Unit,
    onSave: (Exercise) -> Unit,
    initialData: Exercise? = null,
) {
    var name by remember { mutableStateOf(initialData?.name ?: "") }
    var sets by remember { mutableStateOf(initialData?.sets?.toString() ?: "") }
    var reps by remember { mutableStateOf(initialData?.reps?.toString() ?: "") }
    var weight by remember { mutableStateOf(initialData?.weight?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("${if (initialData != null) "Edit" else "Add"} Exercise") },
        text = {
            Column {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") }
                )
                TextField(
                    value = sets,
                    onValueChange = { sets = it },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    label = { Text("Sets") }
                )
                TextField(
                    value = reps,
                    onValueChange = { reps = it },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    label = { Text("Reps") }
                )
                TextField(
                    value = weight,
                    onValueChange = { weight = it },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Decimal),
                    label = { Text("Weight") }
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val newExercise = initialData?.copy(
                    name = name,
                    sets = sets.toInt(),
                    reps = reps.toInt(),
                    weight = weight.toInt(),
                )
                    ?: Exercise(
                        name = name,
                        sets = sets.toInt(),
                        reps = reps.toInt(),
                        weight = weight.toInt()
                    )
                onSave(newExercise)
                onDismiss()
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = { onDismiss() }) {
                Text("Cancel")
            }
        }
    )
}