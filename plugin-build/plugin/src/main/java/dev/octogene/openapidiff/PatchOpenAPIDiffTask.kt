package dev.octogene.openapidiff

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets

abstract class PatchOpenAPIDiffTask : DefaultTask() {
    init {
        description = "Generate an OpenAPI specification diff from Git tags"
        group = BasePlugin.BUILD_GROUP
    }

    @get:Input
    abstract val outputPath: Property<String>

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val diffFile: RegularFileProperty

    @get:Input
    @get:Optional
    abstract val section: Property<String>

    @TaskAction
    fun execute() {
        val outputFile = File(outputPath.get())
        val content = diffFile.get().asFile.readText(StandardCharsets.UTF_8)
        try {
            if (section.isPresent) {
                TextFileWriter(logger).writeAt(section.get(), outputFile, content)
            } else {
                outputFile.writeText(content, StandardCharsets.UTF_8)
            }
        } catch (error: IOException) {
            throw GradleException(
                "Failed to write to output file $outputPath : ${error.message}",
                error
            )
        }
    }
}
