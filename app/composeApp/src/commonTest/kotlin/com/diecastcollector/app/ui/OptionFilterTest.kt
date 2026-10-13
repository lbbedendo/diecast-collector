package com.diecastcollector.app.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class OptionFilterTest {

    private val automakers = listOf("Alfa Romeo", "Citroën", "Ferrari", "Ford", "Škoda", "Zündapp", "Toyota")
        .mapIndexed { i, name -> i to name }

    private fun names(query: String) = filterOptions(automakers, query).map { it.second }

    @Test
    fun blankQueryKeepsEverything() {
        assertEquals(automakers.map { it.second }, names("  "))
    }

    @Test
    fun matchesIgnoringCase() {
        assertEquals(listOf("Ferrari"), names("FERR"))
    }

    @Test
    fun matchesIgnoringAccents() {
        assertEquals(listOf("Citroën"), names("citroen"))
        assertEquals(listOf("Škoda"), names("skoda"))
        assertEquals(listOf("Zündapp"), names("zundapp"))
    }

    @Test
    fun prefixMatchesComeFirst() {
        // "o" is inside Alfa Romeo, Citroën, Ford, Škoda and Toyota, but no label starts with it...
        assertEquals(listOf("Alfa Romeo", "Citroën", "Ford", "Škoda", "Toyota"), names("o"))
        // ...while "f" starts Ferrari and Ford, which move ahead of Alfa Romeo.
        assertEquals(listOf("Ferrari", "Ford", "Alfa Romeo"), names("f"))
    }

    @Test
    fun noMatchIsEmpty() {
        assertEquals(emptyList(), names("xyz"))
    }
}
