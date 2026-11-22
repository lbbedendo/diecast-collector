package com.diecastcollector.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.diecastcollector.app.AppUiState
import com.diecastcollector.app.model.DiecastModel
import com.diecastcollector.app.model.displayName
import com.diecastcollector.app.network.photoAddress

@Composable
fun CollectionListScreen(
    uiState: AppUiState,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onAddClick: () -> Unit,
    onModelClick: (DiecastModel) -> Unit,
    onDelete: (Long) -> Unit,
    onLogout: () -> Unit
) {
    // A delete can't be undone, so "Excluir" on a row only asks; the dialog does the deleting.
    var pendingDelete by remember { mutableStateOf<DiecastModel?>(null) }

    pendingDelete?.let { model ->
        DeleteModelDialog(
            model = model,
            onConfirm = {
                pendingDelete = null
                onDelete(model.id)
            },
            onDismiss = { pendingDelete = null }
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Minha Coleção")
            TextButton(onClick = onLogout) { Text("Sair") }
        }

        Button(onClick = onAddClick, modifier = Modifier.padding(vertical = 12.dp)) {
            Text("Adicionar modelo")
        }

        if (uiState.isLoading) {
            CircularProgressIndicator()
        }

        uiState.error?.let { Text(it) }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            label = { Text("Buscar na coleção") },
            placeholder = { Text("Montadora, nome, série ou marca") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )

        val shown = searchModels(uiState.models, searchQuery)
        if (shown.isEmpty() && searchQuery.isNotBlank()) {
            Text("Nenhum modelo encontrado para \"${searchQuery.trim()}\".")
        }

        LazyColumn {
            items(shown) { model: DiecastModel ->
                ModelRow(model = model, onClick = { onModelClick(model) }, onDelete = { pendingDelete = model })
            }
        }
    }
}

@Composable
private fun ModelRow(model: DiecastModel, onClick: () -> Unit, onDelete: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ModelPhoto(
            photo = photoAddress(model.photoUrl),
            contentDescription = null,
            modifier = Modifier.size(56.dp)
        )
        Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
            Text(model.displayName)
            // A Model's Brand comes from its Series; a Model with no Series has no known Brand.
            // The Automaker is already in the title.
            Text(listOfNotNull(model.series?.brand?.name, model.series?.name).joinToString(" · "))
        }
        TextButton(onClick = onDelete) { Text("Excluir") }
    }
}
