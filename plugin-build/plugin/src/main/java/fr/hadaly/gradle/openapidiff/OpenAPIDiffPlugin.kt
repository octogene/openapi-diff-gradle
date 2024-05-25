package fr.hadaly.gradle.openapidiff

import org.gradle.api.Plugin
import org.gradle.api.Project

const val EXTENSION_NAME = "openapiDiff"
const val TASK_NAME = "generateDiffFromTag"

abstract class OpenAPIDiffPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        // Add the 'template' extension object
        val extension = project.extensions.create(EXTENSION_NAME, OpenAPIDiffExtension::class.java, project)

        // Add a task that uses configuration from the extension object
        project.tasks.register(TASK_NAME, OpenAPIDiffTask::class.java) {
            it.outputFile.set(extension.outputFile)
            it.fromTag.set(extension.from)
            it.toTag.set("")
            it.source.set(extension.source)
        }
    }
}
