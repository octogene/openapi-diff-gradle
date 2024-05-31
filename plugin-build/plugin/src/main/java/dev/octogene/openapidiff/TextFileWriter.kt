package dev.octogene.openapidiff

import org.gradle.api.GradleException
import org.slf4j.Logger
import java.io.File

class TextFileWriter(private val logger: Logger) {

    companion object {
        private const val MARKDOWN_SECTION_PREFIX = '#'
        private const val ASCIIDOC_SECTION_PREFIX = '='
    }

    fun writeAt(header: String, file: File, content: String) {
        val headerLevelPrefix = when (file.extension) {
            "md", "txt" -> MARKDOWN_SECTION_PREFIX
            "adoc" -> ASCIIDOC_SECTION_PREFIX
            else -> throw GradleException("Filetype ${file.extension} is not supported.")
        }
        require(header.startsWith(headerLevelPrefix)) { "Not a valid ${file.extension} section" }
        val fileContent = file.readLines().toMutableList()

        val headerIndex = fileContent.indexOfFirst { it == header }
        logger.debug("Found section at line $headerIndex")
        val sectionLevel = header.countPrefixChar(headerLevelPrefix)
        logger.debug("Section is at level $sectionLevel")
        // Find next section, after the targeted section. For now, we assume that the generated content
        // does not add a section of a higher level than the targeted section.
        val headerOffset = headerIndex + 1
        val nextHeaderIndex: Int = fileContent.subList(headerOffset, fileContent.size)
            .indexOfFirst {
                it.startsWithAtMost(headerLevelPrefix, sectionLevel)
            } + headerOffset
        logger.debug("Found next section at $nextHeaderIndex")
        if (headerIndex != -1) {
            // Clear the content previously added between the selected section
            // and the next section
            val toIndex = if (nextHeaderIndex != -1) {
                nextHeaderIndex
            } else {
                fileContent.size
            }
            fileContent.removeRange(headerOffset, toIndex)
            val contentLines = "\n${content.trimEnd()}\n".lines()
            fileContent.addAll(headerOffset, contentLines)
            file.writeText(fileContent.joinToString("\n"))
            logger.debug("Content inserted successfully.")
        } else {
            throw GradleException("Header [$header] not found in $file.")
        }
    }

    /**
     * Checks if the string starts with at most `max` occurrences of the given `char`.
     *
     * @param char The character to check for.
     * @param max The maximum number of occurrences allowed.
     * @return `true` if the string starts with at most `max` occurrences of `char`, `false` otherwise.
     */
    private fun String.startsWithAtMost(char: Char, max: Int): Boolean {
        if (isEmpty() && max != 0) return false
        var count = 0
        for (c in this) {
            if (c == char) {
                count++
                if (count > max) {
                    return false
                }
            } else {
                break
            }
        }
        return count >= 1
    }


    /**
     * Counts the number of characters at the beginning of this string that are equal to the specified character.
     *
     * @param char The character to compare against.
     * @return The number of characters at the beginning of the string that are equal to `char`.
     */
    private fun String.countPrefixChar(char: Char): Int {
        var count = 0
        for (c in this) {
            if (c == char) {
                count++
            } else {
                break
            }
        }
        return count
    }

    /**
     * Removes item in this list between the specified fromIndex (inclusive) and toIndex (exclusive).
     */
    private fun <T : Any> MutableList<T>.removeRange(fromIndex: Int, toIndex: Int) {
        for ((offset, i) in (fromIndex until toIndex).withIndex()) {
            removeAt(i - offset)
        }
    }
}
