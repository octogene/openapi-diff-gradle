package dev.octogene.openapidiff

import org.gradle.api.Project
import org.gradle.api.provider.Property
import javax.inject.Inject

@Suppress("UnnecessaryAbstractClass")
abstract class OpenAPIDiffExtension
@Inject
constructor(project: Project) {
    private val objects = project.objects

    /**
     * The file where the diff output will be written.
     */
    val outputFile:  Property<String> = objects.property(String::class.java)

    /**
     * The tag from which the diff will be generated (i.e., the oldest)
     */
    val baseTag: Property<String> = objects.property(String::class.java)

    /**
     * The tag until which the diff will be generated (i.e., the most recent).
     * If not provided will always default to the HEAD.
     */
    val targetTag: Property<String> =
        objects.property(String::class.java).convention("HEAD")

    /**
     * The openapi specification file to be used for diffing
     */
    val targetFile: Property<String> = objects.property(String::class.java)

    /**
     * The old openapi specification in case it is different of [targetFile]
     */
    val baseFile: Property<String> = objects.property(String::class.java)

    /**
     * Section marker under which the diff will be inserted. (markdown or asciidoc only)
     */
    val section: Property<String> = objects.property(String::class.java)
}
