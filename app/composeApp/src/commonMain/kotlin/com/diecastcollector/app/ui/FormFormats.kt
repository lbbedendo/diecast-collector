package com.diecastcollector.app.ui

import kotlin.math.roundToLong

/*
 * The Model form shows prices and dates the Brazilian way ("12,90", "14/03/2026") while the API
 * takes a JSON number and an ISO date ("2026-03-14"). These convert between the two; a parse
 * returns null for input that isn't a valid value, so the form can flag it instead of dropping it.
 */

/** "12,90" or "12.90" → 12.9; null for anything that isn't a non-negative amount with up to 2 decimals. */
internal fun parsePrice(text: String): Double? {
    val normalized = text.trim().replace(',', '.')
    if (!Regex("""\d+(\.\d{1,2})?""").matches(normalized)) return null
    return normalized.toDouble()
}

/** 12.9 → "12,90". */
internal fun formatPrice(price: Double): String {
    val cents = (price * 100).roundToLong()
    return "${cents / 100},${(cents % 100).toString().padStart(2, '0')}"
}

/** "14/03/2026" → "2026-03-14"; null unless it's a real calendar date (no 31/02 or 29/02/2025). */
internal fun parseDate(text: String): String? {
    val match = Regex("""(\d{2})/(\d{2})/(\d{4})""").matchEntire(text.trim()) ?: return null
    val (day, month, year) = match.destructured.toList().map { it.toInt() }
    if (month !in 1..12 || day !in 1..daysInMonth(month, year)) return null
    return "${year.toString().padStart(4, '0')}-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"
}

/** "2026-03-14" → "14/03/2026". */
internal fun formatDate(isoDate: String): String {
    val (year, month, day) = isoDate.split('-')
    return "$day/$month/$year"
}

private fun daysInMonth(month: Int, year: Int): Int = when (month) {
    2 -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
    4, 6, 9, 11 -> 30
    else -> 31
}
