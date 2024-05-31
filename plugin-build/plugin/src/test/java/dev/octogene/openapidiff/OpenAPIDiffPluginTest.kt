package dev.octogene.openapidiff

import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.gradle.testfixtures.ProjectBuilder
import java.io.File

class OpenAPIDiffPluginTest: AnnotationSpec() {
    @Test
    fun `plugin is applied correctly to the project`() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("dev.octogene.openapidiff")

        assert(project.tasks.getByName(PATCH_TASK_NAME) is PatchOpenAPIDiffTask)
    }

    @Test
    fun `extension openapiDiff is created correctly`() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("dev.octogene.openapidiff")

        project.extensions.getByName(EXTENSION_NAME).shouldNotBeNull()
    }

    @Test
    fun `parameters are passed correctly from extension to task`() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("dev.octogene.openapidiff")
        val aFile = File(project.projectDir, ".tmp")
        (project.extensions.getByName(EXTENSION_NAME) as OpenAPIDiffExtension).apply {
            baseTag.set("0.5.0")
            targetTag.set("1.0.0")
            section.set("## Diff")
            baseFile.set(aFile.name)
            targetFile.set(aFile.name)
            outputFile.set(aFile.name)
        }

        val task = project.tasks.getByName(PATCH_TASK_NAME) as PatchOpenAPIDiffTask

        task.section.get() shouldBe "## Diff"
        task.outputPath.get() shouldBe aFile.name
    }
}
