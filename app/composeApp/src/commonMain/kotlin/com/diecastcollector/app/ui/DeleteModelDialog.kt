package com.diecastcollector.app.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.diecastcollector.app.model.DiecastModel
import com.diecastcollector.app.model.displayName

/**
 * Asks before deleting [model], since a delete can't be undone. Used from both the list row and
 * the edit screen, so the Collector sees the same question wherever they delete from.
 */
@Composable
fun DeleteModelDialog(model: DiecastModel, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Excluir ${model.displayName}?") },
        text = { Text("O modelo será removido da sua coleção. Essa ação não pode ser desfeita.") },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Excluir") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
