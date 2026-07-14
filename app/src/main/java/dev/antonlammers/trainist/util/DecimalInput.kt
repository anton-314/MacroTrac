package dev.antonlammers.trainist.util

/** Replaces ',' with '.' so both decimal separators are accepted from user input. */
fun String.normalizeDecimal(): String = replace(',', '.')
