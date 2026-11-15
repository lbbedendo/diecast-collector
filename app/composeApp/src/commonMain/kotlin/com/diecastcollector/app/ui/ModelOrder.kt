package com.diecastcollector.app.ui

import com.diecastcollector.app.model.DiecastModel
import com.diecastcollector.app.model.displayName

/**
 * Orders the Collection alphabetically by the name the list shows ("Ferrari F2004"), ignoring
 * case and accents, so "Škoda" sorts with the S's.
 */
internal fun sortedForList(models: List<DiecastModel>): List<DiecastModel> =
    models.sortedBy { fold(it.displayName) }
