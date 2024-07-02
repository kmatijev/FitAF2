package com.example.fitaf

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp

@Composable
fun RoutineItem(
    routine: Routine,
    onClick: () -> Unit,
    onEdit: ((Routine) -> Unit)? = null,
    onDelete: ((Routine) -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(Color.LightGray, RoundedCornerShape(8.dp))
            .padding(16.dp)
            .clickable { onClick() }
    ) {
        Text(text = routine.name, style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = routine.description, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(8.dp))

        routine.exercises.forEach { exercise ->
            Text(
                text = "${exercise.name}: $exercise",
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            onEdit?.let {
                IconButton(onClick = { it(routine) }) {
                    Icon(
                        ImageVector.vectorResource(id = R.drawable.edit),
                        contentDescription = "Edit Routine"
                    )
                }
            }
            onDelete?.let {
                IconButton(onClick = { it(routine) }) {
                    Icon(
                        ImageVector.vectorResource(id = R.drawable.delete),
                        contentDescription = "Delete Routine"
                    )
                }
            }
        }
    }
}