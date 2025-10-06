package com.hindu.pooja.util

/**
 * Parse a leading ASCII number like "1. ", "2) ", "3- ", "4: " (we only ever add ASCII digits).
 * Returns (number, restOfLine) or null if no such prefix.
 */
private fun parseLeadingAsciiNumber(line: String): Pair<Int, String>? {
    val m = Regex("^\\s*([0-9]+)[\\.)\\-:]?\\s+(.*)$").matchEntire(line) ?: return null
    val num = m.groupValues[1].toIntOrNull() ?: return null
    val rest = m.groupValues[2]
    return num to rest
}

/**
 * Strip ONLY our auto-added sequential ASCII line numbers (1., 2., 3., …).
 * - Requires the first two numbered lines to be sequential (n, n+1).
 * - If no sequential pattern is detected, the text is returned untouched.
 * - We do NOT strip on single-line pages anymore (to avoid removing real numbers).
 * - Telugu digits (౦–౯) are NOT treated as auto numbering and will be read.
 */
fun stripOnlySequentialPageNumbers(text: String): String {
    val lines = text.lines()
    if (lines.isEmpty()) return text

    val first = parseLeadingAsciiNumber(lines.firstOrNull().orEmpty()) ?: return text
    // find the first *subsequent* line that has a leading ASCII number
    val second = lines.drop(1).firstNotNullOfOrNull { parseLeadingAsciiNumber(it) }
        ?: return text // no second numbered line => don't strip (may be a real number)

    // Must look like n, n+1 to consider it our pagination numbering
    if (second.first != first.first + 1) return text

    var expected = first.first
    val out = ArrayList<String>(lines.size)
    for (line in lines) {
        val p = parseLeadingAsciiNumber(line)
        if (p != null && p.first == expected) {
            out += p.second
            expected += 1
        } else {
            out += line
        }
    }
    return out.joinToString("\n")
}
