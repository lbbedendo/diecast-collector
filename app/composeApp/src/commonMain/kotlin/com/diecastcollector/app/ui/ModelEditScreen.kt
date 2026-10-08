package com.diecastcollector.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.diecastcollector.app.AppUiState
import com.diecastcollector.app.model.Condition
import com.diecastcollector.app.model.ModelRequest
import com.diecastcollector.app.model.Packaging
import com.diecastcollector.app.model.Scale
import com.diecastcollector.app.model.Series
import com.diecastcollector.app.model.SeriesRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelEditScreen(
    uiState: AppUiState,
    onSave: (ModelRequest) -> Unit,
    onCreateSeries: suspend (SeriesRequest) -> Result<Series>,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var scale by remember { mutableStateOf<Scale?>(null) }
    var vehicleYear by remember { mutableStateOf("") }
    var packaging by remember { mutableStateOf<Packaging?>(null) }
    var condition by remember { mutableStateOf<Condition?>(null) }
    var chase by remember { mutableStateOf(false) }
    var color by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedAutomakerId by remember { mutableStateOf<Long?>(null) }
    var selectedSeriesId by remember { mutableStateOf<Long?>(null) }
    var showNewSeries by remember { mutableStateOf(false) }
    val newSeriesButtonFocus = remember { FocusRequester() }

    if (showNewSeries) {
        NewSeriesDialog(
            brands = uiState.brands,
            onCreate = onCreateSeries,
            onCreated = { created ->
                selectedSeriesId = created.id
                showNewSeries = false
            },
            onDismiss = { showNewSeries = false }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)
    ) {
        Text("New model")

        // Automaker first: the Name that follows leaves it out ("F2004", not "Ferrari F2004").
        SearchablePicker(
            label = "Automaker",
            options = uiState.automakers.map { it.id to it.name },
            selectedId = selectedAutomakerId,
            onSelect = { selectedAutomakerId = it }
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            placeholder = { Text("Vehicle name, without the automaker (e.g. F2004)") },
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
        )

        OutlinedTextField(
            value = vehicleYear,
            onValueChange = { input -> vehicleYear = input.filter(Char::isDigit).take(4) },
            label = { Text("Vehicle year (real car, e.g. 2004)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
        ) {
            LabeledPicker(
                label = "Series",
                options = uiState.series.map { it.id to seriesLabel(it) },
                selectedId = selectedSeriesId,
                onSelect = { selectedSeriesId = it },
                modifier = Modifier.weight(1f)
            )
            // Create a Series in place instead of leaving the form.
            TextButton(
                onClick = {
                    // Park focus on this button: when the dialog closes, focus comes back here
                    // instead of jumping to the first text field and reopening the keyboard.
                    newSeriesButtonFocus.requestFocus()
                    showNewSeries = true
                },
                modifier = Modifier.padding(start = 8.dp).focusRequester(newSeriesButtonFocus)
            ) {
                Text("+", style = MaterialTheme.typography.headlineSmall)
            }
        }

        LabeledPicker(
            label = "Scale",
            options = Scale.entries.map { it to it.label },
            selectedId = scale,
            onSelect = { scale = it }
        )

        LabeledPicker(
            label = "Packaging",
            options = Packaging.entries.map { it to it.label },
            selectedId = packaging,
            onSelect = { packaging = it }
        )

        LabeledPicker(
            label = "Condition",
            options = Condition.entries.map { it to it.label },
            selectedId = condition,
            onSelect = { condition = it }
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
        ) {
            Checkbox(checked = chase, onCheckedChange = { chase = it })
            Text("Chase (rare variant, e.g. a Treasure Hunt)")
        }

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
                            scale = scale,
                            packaging = packaging,
                            condition = condition,
                            chase = chase,
                            vehicleYear = vehicleYear.toIntOrNull(),
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
private fun <T> LabeledPicker(
    label: String,
    options: List<Pair<T, String>>,
    selectedId: T?,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedText = options.firstOrNull { it.first == selectedId }?.second ?: ""

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
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
