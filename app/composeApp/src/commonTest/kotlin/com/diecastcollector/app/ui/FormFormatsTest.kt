package com.diecastcollector.app.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FormFormatsTest {

    @Test
    fun priceAcceptsCommaOrDot() {
        assertEquals(12.9, parsePrice("12,90"))
        assertEquals(12.9, parsePrice("12.90"))
        assertEquals(5.0, parsePrice("5"))
        assertEquals(0.5, parsePrice(" 0,5 "))
    }

    @Test
    fun priceRejectsWhatIsNotAnAmount() {
        assertNull(parsePrice(""))
        assertNull(parsePrice("12,999"))
        assertNull(parsePrice("1.234,56"))
        assertNull(parsePrice("-3"))
        assertNull(parsePrice("abc"))
    }

    @Test
    fun priceFormatsWithTwoDecimals() {
        assertEquals("12,90", formatPrice(12.9))
        assertEquals("5,00", formatPrice(5.0))
        assertEquals("0,05", formatPrice(0.05))
    }

    @Test
    fun dateConvertsToIso() {
        assertEquals("2026-03-14", parseDate("14/03/2026"))
        assertEquals("2024-02-29", parseDate("29/02/2024"))
    }

    @Test
    fun dateRejectsImpossibleDates() {
        assertNull(parseDate("31/02/2026"))
        assertNull(parseDate("29/02/2025"))
        assertNull(parseDate("29/02/1900"))
        assertNull(parseDate("31/04/2026"))
        assertNull(parseDate("00/01/2026"))
        assertNull(parseDate("10/13/2026"))
        assertNull(parseDate("1/3/2026"))
        assertNull(parseDate("2026-03-14"))
    }

    @Test
    fun dateFormatsFromIso() {
        assertEquals("14/03/2026", formatDate("2026-03-14"))
    }
}
