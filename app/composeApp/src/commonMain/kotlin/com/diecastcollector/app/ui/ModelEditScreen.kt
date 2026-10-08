package com.diecastcollector.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.diecastcollector.app.AppUiState
import com.diecastcollector.app.model.ModelRequest
import com.diecastcollector.app.model.Series

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelEditScreen(
    uiState: AppUiState,
    onSave: (ModelRequest) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var scale by remember { mutableStateOf("") }
    var condition by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedAutomakerId by remember { mutableStateOf<Long?>(null) }
    var selectedSeriesId by remember { mutableStateOf<Long?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)
    ) {
        Text("New model")

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
        )

        LabeledPicker(
            label = "Automaker",
            options = uiState.automakers.map { it.id to it.name },
            selectedId = selectedAutomakerId,
            onSelect = { selectedAutomakerId = it }
        )

        LabeledPicker(
            label = "Series",
            options = uiState.series.map { it.id to seriesLabel(it) },
            selectedId = selectedSeriesId,
            onSelect = { selectedSeriesId = it }
        )

        OutlinedTextField(
            value = scale,
            onValueChange = { scale = it },
            label = { Text("Scale (e.g. 1:64)") },
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
        )

        OutlinedTextField(
            value = condition,
            onValueChange = { condition = it },
            label = { Text("Condition") },
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
        )

        OutlinedTextField(
            value = color,
            onValueChange = { color = it },
            label = { Text("Color") },
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
        )

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Notes") },
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
        )

        Row(modifier = Modifier.padding(top = 24.dp)) {
            Button(
                onClick = {
                    onSave(
                        ModelRequest(
                            name = name,
                            automakerId = selectedAutomakerId,
                            seriesId = selectedSeriesId,
                            scale = scale.ifBlank { null },
                            condition = condition.ifBlank { null },
                            yearReleased = null,
                            color = color.ifBlank { null },
                            notes = notes.ifBlank { null }
                        )
                    )
                }
            ) {
                Text("Save")
            }
            TextButton(onClick = onCancel, modifier = Modifier.padding(start = 8.dp)) {
                Text("Cancel")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LabeledPicker(
    label: String,
    options: List<Pair<Long, String>>,
    selectedId: Long?,
    onSelect: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedText = options.firstOrNull { it.first == selectedId }?.second ?: ""

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
    ) {
        OutlinedTextField(
            value = selectedText,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { (id, text) ->
                DropdownMenuItem(
                    text = { Text(text) },
                    onClick = {
                        onSelect(id)
                        expanded = false
                    }
                )
            }
        }
    }
}

/** e.g. "Hot Wheels · HW Starting Grid (2026)" — name alone is ambiguous across Brands and years. */
private fun seriesLabel(series: Series): String =
    listOfNotNull(series.brand?.name, series.name).joinToString(" · ") +
        (series.year?.let { " ($it)" } ?: "")
