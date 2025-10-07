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

/**
 * Lightweight sanitizer used before TTS:
 * 1) Strip our auto page numbers (ASCII 1., 2., …) — preserves real Telugu digits.
 * 2) Normalize excessive spaces.
 * 3) Collapse 3+ newlines to 2.
 * 4) Drop simple citation artifacts like [12].
 * 5) Remove stray bullet glyphs.
 */
class TtsSanitizer(
    private val normalizeWhitespace: Boolean = true,
    private val collapseMultipleNewlines: Boolean = true
) {
    fun sanitize(input: String): String {
        var t = input

        // 1) remove our generated page numbering (safe heuristic)
        t = stripOnlySequentialPageNumbers(t)

        // 2) normalize whitespace (keep single spaces; preserve newlines)
        if (normalizeWhitespace) {
            t = t.replace(Regex("[\\t\\x0B\\f\\r\\u00A0]+"), " ")
            t = t.replace(Regex(" {2,}"), " ")
        }

        // 3) collapse many newlines to max 2 in a row
        if (collapseMultipleNewlines) {
            t = t.replace(Regex("\\n{3,}"), "\n\n")
        }

        // 4) drop simple [1], [23] style citation markers
        t = t.replace(Regex("\\[[0-9]{1,3}]"), "")

        // 5) clean stray bullet dots
        t = t.replace(Regex("[•]+"), "")

        return t.trim()
    }
}
