package com.diecastcollector.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.diecastcollector.app.AppUiState
import com.diecastcollector.app.model.DiecastModel

@Composable
fun CollectionListScreen(
    uiState: AppUiState,
    onAddClick: () -> Unit,
    onDelete: (Long) -> Unit,
    onLogout: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("My Collection")
            TextButton(onClick = onLogout) { Text("Log out") }
        }

        Button(onClick = onAddClick, modifier = Modifier.padding(vertical = 12.dp)) {
            Text("Add model")
        }

        if (uiState.isLoading) {
            CircularProgressIndicator()
        }

        uiState.error?.let { Text(it) }

        LazyColumn {
            items(uiState.models) { model: DiecastModel ->
                ModelRow(model = model, onDelete = { onDelete(model.id) })
            }
        }
    }
}

@Composable
private fun ModelRow(model: DiecastModel, onDelete: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(model.name)
            Text("${model.brand.name}${model.automaker?.let { " · ${it.name}" } ?: ""}")
        }
        TextButton(onClick = onDelete) { Text("Delete") }
    }
}
