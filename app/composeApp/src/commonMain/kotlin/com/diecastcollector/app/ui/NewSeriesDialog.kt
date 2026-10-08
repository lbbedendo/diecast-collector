package com.diecastcollector.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.diecastcollector.app.model.Brand
import com.diecastcollector.app.model.Series
import com.diecastcollector.app.model.SeriesRequest
import kotlinx.coroutines.launch

/**
 * Creates a Series without leaving the Model form. A Series needs a Brand and a name; the year is
 * optional (see Series in CONTEXT.md). The API rejects a duplicate Brand + name + year with a
 * message, which is shown here so the Collector can pick the existing Series instead.
 */
@Composable
internal fun NewSeriesDialog(
    brands: List<Brand>,
    onCreate: suspend (SeriesRequest) -> Result<Series>,
    onCreated: (Series) -> Unit,
    onDismiss: () -> Unit
) {
    var brandId by remember { mutableStateOf<Long?>(null) }
    var name by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = { if (!saving) onDismiss() },
        title = { Text("New series") },
        text = {
            Column {
                SearchablePicker(
                    label = "Brand",
                    options = brands.map { it.id to it.name },
                    selectedId = brandId,
                    onSelect = { brandId = it },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    placeholder = { Text("e.g. HW Starting Grid") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
                )
                OutlinedTextField(
                    value = year,
                    onValueChange = { input -> year = input.filter(Char::isDigit).take(4) },
                    label = { Text("Year (optional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
                )
                error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 12.dp))
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = brandId != null && name.isNotBlank() && !saving,
                onClick = {
                    val request = SeriesRequest(brandId = brandId ?: return@TextButton, name = name.trim(), year = year.toIntOrNull())
                    saving = true
                    error = null
                    scope.launch {
                        onCreate(request)
                            .onSuccess(onCreated)
                            .onFailure { error = it.message ?: "Couldn't create the series" }
                        saving = false
                    }
                }
            ) { Text("Create") }
        },
        dismissButton = {
            TextButton(enabled = !saving, onClick = onDismiss) { Text("Cancel") }
        }
    )
}
