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
import com.diecastcollector.app.model.DiecastModel
import com.diecastcollector.app.model.ModelRequest
import com.diecastcollector.app.model.Packaging
import com.diecastcollector.app.model.Scale
import com.diecastcollector.app.model.Series
import com.diecastcollector.app.model.SeriesRequest
import com.diecastcollector.app.model.toRequest

/** Creates a Model, or edits [model] when one is given. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelEditScreen(
    uiState: AppUiState,
    model: DiecastModel? = null,
    onSave: (ModelRequest) -> Unit,
    onCreateSeries: suspend (SeriesRequest) -> Result<Series>,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf(model?.name ?: "") }
    var scale by remember { mutableStateOf(model?.scale) }
    var vehicleYear by remember { mutableStateOf(model?.vehicleYear?.toString() ?: "") }
    var packaging by remember { mutableStateOf(model?.packaging) }
    var condition by remember { mutableStateOf(model?.condition) }
    var chase by remember { mutableStateOf(model?.chase ?: false) }
    var color by remember { mutableStateOf(model?.color ?: "") }
    var notes by remember { mutableStateOf(model?.notes ?: "") }
    var seriesNumber by remember { mutableStateOf(model?.seriesNumber ?: "") }
    var purchasePrice by remember { mutableStateOf(model?.purchasePrice?.let(::formatPrice) ?: "") }
    var purchaseDate by remember { mutableStateOf(model?.purchaseDate?.let(::formatDate) ?: "") }
    var purchasedFrom by remember { mutableStateOf(model?.purchasedFrom ?: "") }
    // A typed value that doesn't parse is flagged and blocks saving rather than being dropped.
    val priceInvalid = purchasePrice.isNotBlank() && parsePrice(purchasePrice) == null
    val dateInvalid = purchaseDate.isNotBlank() && parseDate(purchaseDate) == null
    var selectedAutomakerId by remember { mutableStateOf(model?.automaker?.id) }
    var selectedSeriesId by remember { mutableStateOf(model?.series?.id) }
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
        Text(if (model == null) "Novo modelo" else "Editar modelo")

        // Automaker first: the Name that follows leaves it out ("F2004", not "Ferrari F2004").
        SearchablePicker(
            label = "Montadora",
            options = uiState.automakers.map { it.id to it.name },
            selectedId = selectedAutomakerId,
            onSelect = { selectedAutomakerId = it }
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nome") },
            placeholder = { Text("Nome do veículo, sem a montadora (ex.: F2004)") },
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
        )

        OutlinedTextField(
            value = vehicleYear,
            onValueChange = { input -> vehicleYear = input.filter(Char::isDigit).take(4) },
            label = { Text("Ano do veículo (carro real, ex.: 2004)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
        ) {
            LabeledPicker(
                label = "Série",
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

        OutlinedTextField(
            value = seriesNumber,
            onValueChange = { seriesNumber = it },
            label = { Text("Número na série") },
            placeholder = { Text("Como impresso na embalagem (ex.: 10/10)") },
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
        )

        LabeledPicker(
            label = "Escala",
            options = Scale.entries.map { it to it.label },
            selectedId = scale,
            onSelect = { scale = it }
        )

        LabeledPicker(
            label = "Embalagem",
            options = Packaging.entries.map { it to it.label },
            selectedId = packaging,
            onSelect = { packaging = it }
        )

        LabeledPicker(
            label = "Condição",
            options = Condition.entries.map { it to it.label },
            selectedId = condition,
            onSelect = { condition = it }
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
        ) {
            Checkbox(checked = chase, onCheckedChange = { chase = it })
            Text("Chase (variante rara, ex.: um Treasure Hunt)")
        }

        OutlinedTextField(
            value = color,
            onValueChange = { color = it },
            label = { Text("Cor") },
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
        )

        OutlinedTextField(
            value = purchasePrice,
            onValueChange = { input -> purchasePrice = input.filter { it.isDigit() || it == ',' || it == '.' } },
            label = { Text("Preço pago (R$)") },
            placeholder = { Text("ex.: 12,90") },
            isError = priceInvalid,
            supportingText = if (priceInvalid) {
                { Text("Use um valor como 12,90") }
            } else {
                null
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
        )

        OutlinedTextField(
            value = purchaseDate,
            onValueChange = { input -> purchaseDate = input.filter { it.isDigit() || it == '/' }.take(10) },
            label = { Text("Data da compra") },
            placeholder = { Text("DD/MM/AAAA") },
            isError = dateInvalid,
            supportingText = if (dateInvalid) {
                { Text("Use uma data válida no formato DD/MM/AAAA") }
            } else {
                null
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
        )

        OutlinedTextField(
            value = purchasedFrom,
            onValueChange = { purchasedFrom = it },
            label = { Text("Comprado de") },
            placeholder = { Text("Loja, site ou pessoa") },
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
        )

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Observações") },
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
        )

        Row(modifier = Modifier.padding(top = 24.dp)) {
            Button(
                enabled = !priceInvalid && !dateInvalid,
                onClick = {
                    // Start from the edited Model: PUT replaces the whole Model, so a field the
                    // form doesn't set is sent back as it was instead of being wiped.
                    val base = model?.toRequest() ?: ModelRequest(
                        name = "", automakerId = null, seriesId = null, scale = null, packaging = null,
                        condition = null, vehicleYear = null, color = null, notes = null
                    )
                    onSave(
                        base.copy(
                            name = name,
                            automakerId = selectedAutomakerId,
                            seriesId = selectedSeriesId,
                            scale = scale,
                            packaging = packaging,
                            condition = condition,
                            chase = chase,
                            vehicleYear = vehicleYear.toIntOrNull(),
                            color = color.ifBlank { null },
                            notes = notes.ifBlank { null },
                            seriesNumber = seriesNumber.trim().ifBlank { null },
                            purchasePrice = parsePrice(purchasePrice),
                            purchaseDate = parseDate(purchaseDate),
                            purchasedFrom = purchasedFrom.ifBlank { null }
                        )
                    )
                }
            ) {
                Text("Salvar")
            }
            TextButton(onClick = onCancel, modifier = Modifier.padding(start = 8.dp)) {
                Text("Cancelar")
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
