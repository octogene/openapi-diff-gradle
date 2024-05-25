package fr.hadaly.gradle.openapidiff

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.RevTree
import org.eclipse.jgit.treewalk.TreeWalk
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.openapitools.openapidiff.core.OpenApiCompare
import org.openapitools.openapidiff.core.output.MarkdownRender
import java.io.File
import java.io.FileNotFoundException
import java.io.FileWriter
import java.io.IOException
import java.nio.charset.StandardCharsets
import kotlin.io.path.Path

abstract class OpenAPIDiffTask : DefaultTask() {
    init {
        description = "Just a sample template task"

        // Don't forget to set the group here.
        group = BasePlugin.BUILD_GROUP
    }

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @get:Input
    @get:Option(option = "fromTag", description = "Tag from which the diff will be generated")
    abstract val fromTag: Property<String>

    @get:Input
    @get:Option(option = "toTag", description = "Tag to which the diff will be generated")
    @get:Optional
    abstract val toTag: Property<String>

    @get:InputFile
    abstract val source: Property<String>

    fun sampleAction() {
        val prettyTag = fromTag.orNull?.let { "[$it]" } ?: ""

        logger.lifecycle("$prettyTag outputFile is: ${outputFile.orNull}")

        outputFile.get().asFile.writeText("$prettyTag")
    }

    /**
     * Use openapi-diff to generate a diff between the HEAD & the [fromTag] property.
     * If [fromTag] is not provided, the latest tag will be used.
     */
    @TaskAction
    fun generateDiff() {
        val fromTag = fromTag.orNull
        val toTag = toTag.orNull
        val sourcePath = Path(source.get())
        val git = Git.open(project.rootDir)
        val tag: Ref = if (fromTag != null) {
            git.tagList().call().first { it.name.endsWith(fromTag) }
        } else {
            git.tagList().call().last()
        }
        logger.quiet("Generating $sourcePath openapi diff from ${tag.name}")
        val commit: RevCommit = git.repository.parseCommit(tag.objectId)
        val oldContent = readElementAt(git.repository, commit, sourcePath.parent?.toString(), sourcePath.fileName.toString())
        val newContent = String(sourcePath.toFile().readBytes())
        val diff = OpenApiCompare.fromContents(oldContent, newContent)
        try {
            val cleanTag = tag.name.removePrefix("refs/tags/")
            val diffFilename = "openapi-diff-${this.fromTag}.md"
            FileWriter(diffFilename).use { MarkdownRender().render(diff, it) }
        } catch (error: IOException) {
            error.printStackTrace()
        }
    }

    @Throws(IOException::class)
    fun readElementAt(repository: Repository, revCommit: RevCommit, path: String?, filename: String): String? {
        val tree: RevTree = revCommit.tree
        logger.quiet("Tree is $tree")
        var item: String?

        // now try to find a specific file
        buildTreeWalk(repository, tree, path).use { treeWalk ->
            TreeWalk(repository).use { dirWalk ->
                dirWalk.addTree(treeWalk.getObjectId(0))
                dirWalk.isRecursive = false
                item = findFile(dirWalk, filename, repository)
            }
        }
        return item
    }

    @Throws(IOException::class)
    fun buildTreeWalk(
        repository: Repository,
        tree: RevTree,
        path: String?
    ): TreeWalk {
        return path?.let { TreeWalk.forPath(repository, path, tree) } ?: TreeWalk(repository).apply { addTree(tree) }
    }

    private fun findFile(
        dirWalk: TreeWalk,
        filename: String,
        repository: Repository,
    ): String? {
        var fileContent: String? = null
        while (dirWalk.next()) {
            logger.quiet("Walking ${dirWalk.pathString}")
            if (dirWalk.pathString.contains(filename)) {
                val blobId = dirWalk.getObjectId(0)
                repository.newObjectReader().use { objectReader ->
                    val objectLoader = objectReader.open(blobId)
                    val bytes = objectLoader.bytes
                    fileContent = String(bytes, StandardCharsets.UTF_8)
                }
            }
        }
        return fileContent
    }
}
