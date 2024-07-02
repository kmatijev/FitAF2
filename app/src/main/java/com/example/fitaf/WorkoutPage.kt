package com.example.fitaf

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@Composable
fun WorkoutPage(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        AppBar(title = "Workout")
        Text(
            text = "Quick start:",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        AppButton(
            label = "Log a workout",
            icon = Icons.Outlined.AddCircle,
            onClick = { navController.navigate("routine_picker") },
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Routines:",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        AppButton(
            label = "Create routine",
            icon = Icons.Outlined.AddCircle,
            onClick = { navController.navigate("routines") },
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "${Firebase.auth.currentUser?.email}:",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        AppButton(
            label = "Log out",
            icon = Icons.AutoMirrored.Outlined.ExitToApp,
            onClick = {
                Firebase.auth.signOut()
                navController.popBackStack(navController.graph.startDestinationId, inclusive = true)
                navController.navigate("login") {
                    launchSingleTop = true
                    restoreState = true
                    popUpTo(navController.graph.startDestinationId) {
                        saveState = true
                    }
                }
            },
        )
    }
}
