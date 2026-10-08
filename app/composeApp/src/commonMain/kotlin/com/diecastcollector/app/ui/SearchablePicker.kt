package com.diecastcollector.app.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp

/**
 * A drop-down the Collector can type into to narrow a long list (e.g. ~270 Automakers); see
 * [filterOptions] for how matching works. Clearing the text clears the selection, so use this only
 * where "nothing selected" is valid, or check for null before saving.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun <T> SearchablePicker(
    label: String,
    options: List<Pair<T, String>>,
    selectedId: T?,
    onSelect: (T?) -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
) {
    val selectedText = options.firstOrNull { it.first == selectedId }?.second ?: ""
    var expanded by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    // What's typed; reset to the selection's label whenever the selection changes.
    var query by remember(selectedText) { mutableStateOf(selectedText) }
    // Until the Collector edits the text, the field shows the current selection: list everything.
    val visible = if (query == selectedText) options else filterOptions(options, query)

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = { typed ->
                query = typed
                expanded = true
                if (typed.isBlank()) onSelect(null)
            },
            label = { Text(label) },
            placeholder = { Text("Type to search") },
            singleLine = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable).fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded && visible.isNotEmpty(),
            onDismissRequest = {
                expanded = false
                query = selectedText
            }
        ) {
            visible.forEach { (id, text) ->
                DropdownMenuItem(
                    text = { Text(text) },
                    onClick = {
                        onSelect(id)
                        query = text
                        expanded = false
                        // Done choosing: drop focus so the keyboard closes.
                        focusManager.clearFocus()
                    }
                )
            }
        }
    }
}
