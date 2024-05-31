package dev.octogene.openapidiff

import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.equals.shouldBeEqual
import org.slf4j.LoggerFactory
import java.io.File

class TextFileWriterTest : AnnotationSpec() {

    private val baseContent = """
            # Changelog

            ## [Unreleased]

            ### OpenAPI

            ## [1.0.0]
        """.trimIndent()

    @Test
    fun `Content should be inserted at specified location`() {
        val expectedContent = """
            # Changelog

            ## [Unreleased]

            ### OpenAPI

            #### What's New
            ---

            ##### `GET` /pet

            > Get all pets in the store

            ## [1.0.0]
        """.trimIndent()
        val file = File.createTempFile("test", ".md")
        file.writeText(baseContent)
        val writer = TextFileWriter(LoggerFactory.getLogger("TextFileWriterTest"))

        writer.writeAt(
            "### OpenAPI", file, """
            #### What's New
            ---

            ##### `GET` /pet

            > Get all pets in the store

        """.trimIndent()
        )

        file.readText() shouldBeEqual expectedContent
    }

    @Test
    fun `Content should be updated properly`() {
        val expectedContent = """
            # Changelog

            ## [Unreleased]

            ### OpenAPI

            #### What's New
            ---

            ##### `GET` /plant

            > Get all plants in the store

            ## [1.0.0]
        """.trimIndent()
        val file = File.createTempFile("test", ".md")
        val oldContent = """
            # Changelog

            ## [Unreleased]

            ### OpenAPI

            #### What's New
            ---

            ##### `GET` /pet

            > Get all pets in the store

            ## [1.0.0]
        """.trimIndent()
        file.writeText(oldContent)
        val writer = TextFileWriter(LoggerFactory.getLogger("TextFileWriterTest"))

        writer.writeAt(
            "### OpenAPI", file, """
            #### What's New
            ---

            ##### `GET` /plant

            > Get all plants in the store

        """.trimIndent()
        )

        file.readText() shouldBeEqual expectedContent
    }


}
