package dev.octogene.openapidiff

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.openapitools.openapidiff.core.OpenApiCompare
import org.openapitools.openapidiff.core.model.ChangedOpenApi
import org.openapitools.openapidiff.core.output.AsciidocRender
import org.openapitools.openapidiff.core.output.ConsoleRender
import org.openapitools.openapidiff.core.output.MarkdownRender
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

@CacheableTask
abstract class GenerateDiffTask : DefaultTask() {
    init {
        description = "Generate a diff from two openapi files"
        group = BasePlugin.BUILD_GROUP
    }

    @get:Input
    abstract val format: Property<String>

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val targetFile: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val baseFile: RegularFileProperty

    @get:Input
    @get:Optional
    abstract val section: Property<String>

    @get:OutputFile
    abstract val diffFile: RegularFileProperty

    @TaskAction
    fun execute() {
        val baseContent = baseFile.get().asFile.readText(StandardCharsets.UTF_8)
        val targetContent = targetFile.get().asFile.readText(StandardCharsets.UTF_8)
        val diff = OpenApiCompare.fromContents(baseContent, targetContent)
        try {
            val content = renderDiffAsContent(diff)
            if (section.isPresent) {
                TextFileWriter(logger).writeAt(section.get(), diffFile.get().asFile, content)
            } else {
                diffFile.get().asFile.writeText(content)
            }
        } catch (error: IOException) {
            throw GradleException(
                "Failed to write to output file ${diffFile.get().asFile}: ${error.message}",
                error
            )
        }
    }

    private fun renderDiffAsContent(diff: ChangedOpenApi?): String =
        ByteArrayOutputStream().use { byteArrayOutputStream ->
            OutputStreamWriter(byteArrayOutputStream, StandardCharsets.UTF_8).use { writer ->
                val render = when (format.get()) {
                    "md" -> MarkdownRender()
                    "adoc" -> AsciidocRender()
                    else -> ConsoleRender()
                }
                render.render(diff, writer)
                byteArrayOutputStream.toString("UTF-8")
            }
        }
}
