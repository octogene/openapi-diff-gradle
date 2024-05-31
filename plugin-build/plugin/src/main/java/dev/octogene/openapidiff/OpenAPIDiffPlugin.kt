package dev.octogene.openapidiff

import org.gradle.api.Plugin
import org.gradle.api.Project
import kotlin.io.path.Path
import kotlin.io.path.extension

internal const val EXTENSION_NAME = "openapiDiff"
internal const val FETCH_BASE_TASK = "fetchBaseOpenAPIFromGit"
internal const val FETCH_TARGET_TASK = "fetchTargetOpenAPIFromGit"
internal const val GENERATE_TASK = "generateOpenAPIDiff"
internal const val PATCH_TASK_NAME = "patchOpenAPIDiff"
internal const val WORK_DIR = "generated/openapi-diff"

abstract class OpenAPIDiffPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension =
            project.extensions.create(EXTENSION_NAME, OpenAPIDiffExtension::class.java, project)

        project.tasks.register(FETCH_BASE_TASK, GitFileRetrieverTask::class.java) {
            it.outputFile.convention(project.provider {
                with(project.layout.buildDirectory) {
                    dir("$WORK_DIR/base").get().asFile.mkdirs()
                    file("$WORK_DIR/base/openapi.yml").get().asFile.createNewFile()
                    file("$WORK_DIR/base/openapi.yml").get()
                }
            })
            it.filePath.set(extension.baseFile.convention(extension.targetFile))
            it.tag.set(extension.baseTag)
        }

        project.tasks.register(FETCH_TARGET_TASK, GitFileRetrieverTask::class.java) {
            it.outputFile.convention(project.provider {
                with(project.layout.buildDirectory) {
                    dir("$WORK_DIR/target").get().asFile.mkdirs()
                    file("$WORK_DIR/target/openapi.yml").get().asFile.createNewFile()
                    file("$WORK_DIR/target/openapi.yml").get()
                }
            })
            it.filePath.set(extension.targetFile)
            it.tag.set(extension.targetTag.convention("HEAD"))
        }

        project.tasks.register(GENERATE_TASK, GenerateDiffTask::class.java) {
            it.dependsOn(FETCH_BASE_TASK, FETCH_TARGET_TASK)
            it.format.set(project.provider {
                Path(extension.outputFile.get()).extension
            })
            it.diffFile.convention(project.provider {
                with(project.layout.buildDirectory) {
                    dir("$WORK_DIR/diff").get().asFile.mkdirs()
                    file("$WORK_DIR/diff/openapidiff").get().asFile.createNewFile()
                    file("$WORK_DIR/diff/openapidiff").get()
                }
            })
            it.baseFile.set(project.layout.buildDirectory.file("$WORK_DIR/base/openapi.yml"))
            it.targetFile.set(project.layout.buildDirectory.file("$WORK_DIR/target/openapi.yml"))
        }

        project.tasks.register(PATCH_TASK_NAME, PatchOpenAPIDiffTask::class.java) {
            it.dependsOn(GENERATE_TASK)
            it.outputPath.set(extension.outputFile)
            it.diffFile.set(project.layout.buildDirectory.file("$WORK_DIR/diff/openapidiff"))
            it.section.set(extension.section)
        }

    }
}
