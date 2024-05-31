package dev.octogene.openapidiff

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.ObjectLoader
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.treewalk.TreeWalk
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.nio.charset.StandardCharsets

@CacheableTask
abstract class GitFileRetrieverTask: DefaultTask() {

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @get:Input
    abstract val filePath: Property<String>

    @get:Input
    abstract val tag: Property<String>

    @TaskAction
    fun execute() {
        val git = Git.open(project.rootDir)
        val repository = git.repository

        val revWalk = RevWalk(repository)

        val tagsObject = repository.resolve(tag.get())
        val commit: RevCommit = revWalk.parseCommit(tagsObject)

        val file = File(filePath.get()).relativeTo(project.rootDir)
        val content = getFileContent(repository, commit, file.path)
        outputFile.get().asFile.writeText(content, StandardCharsets.UTF_8)
    }

    private fun getFileContent(
        repository: Repository,
        commit: RevCommit,
        filePath: String
    ): String {
        TreeWalk.forPath(repository, filePath, commit.tree).use { treeWalk ->
            checkNotNull(treeWalk) {
                "File $filePath not found in commit. If it was it moved or renamed, " +
                    "you can specify the old filePath with the baseFile configuration option."
            }
            val objectId: ObjectId = treeWalk.getObjectId(0)
            val loader: ObjectLoader = repository.open(objectId)

            return String(loader.bytes, StandardCharsets.UTF_8)
        }
    }
}
