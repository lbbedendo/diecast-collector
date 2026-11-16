package com.diecastcollector.app.ui

import com.diecastcollector.app.model.DiecastModel
import com.diecastcollector.app.model.displayName

/**
 * Narrows the Collection to Models whose Automaker + Name, Series name or Brand contains [query],
 * ignoring case and accents. The order of [models] is kept; a blank query returns them all.
 */
internal fun searchModels(models: List<DiecastModel>, query: String): List<DiecastModel> {
    val needle = fold(query.trim())
    if (needle.isEmpty()) return models
    return models.filter { model ->
        listOfNotNull(model.displayName, model.series?.name, model.series?.brand?.name)
            .any { fold(it).contains(needle) }
    }
}
