package com.diecastcollector.app.ui

/**
 * Narrows a picker's options to those whose label contains [query], ignoring case and common
 * accents (so "citroen" finds "Citroën" and "skoda" finds "Škoda"). Labels that start with the
 * query come first; otherwise the original order is kept. A blank query returns every option.
 */
internal fun <T> filterOptions(options: List<Pair<T, String>>, query: String): List<Pair<T, String>> {
    val needle = fold(query.trim())
    if (needle.isEmpty()) return options
    val matches = options.filter { fold(it.second).contains(needle) }
    val (prefix, rest) = matches.partition { fold(it.second).startsWith(needle) }
    return prefix + rest
}

private val accents = mapOf(
    'à' to 'a', 'á' to 'a', 'â' to 'a', 'ã' to 'a', 'ä' to 'a', 'å' to 'a',
    'ç' to 'c', 'č' to 'c', 'ć' to 'c',
    'è' to 'e', 'é' to 'e', 'ê' to 'e', 'ë' to 'e', 'ě' to 'e',
    'ì' to 'i', 'í' to 'i', 'î' to 'i', 'ï' to 'i',
    'ñ' to 'n', 'ň' to 'n',
    'ò' to 'o', 'ó' to 'o', 'ô' to 'o', 'õ' to 'o', 'ö' to 'o', 'ø' to 'o',
    'ř' to 'r', 'š' to 's', 'ś' to 's', 'ť' to 't',
    'ù' to 'u', 'ú' to 'u', 'û' to 'u', 'ü' to 'u', 'ů' to 'u',
    'ý' to 'y', 'ÿ' to 'y', 'ž' to 'z', 'ź' to 'z', 'ż' to 'z'
)

private fun fold(text: String): String =
    text.lowercase().map { accents[it] ?: it }.joinToString("")
