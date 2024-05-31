import org.jetbrains.changelog.date

plugins {
    java
    id("dev.octogene.openapidiff")
    alias(libs.plugins.changelog)
}

version = "example-1.0.2"

openapiDiff {
    targetFile = "${project.rootDir}/example/doc/openapi/petstore.yaml"
    baseFile = "${project.projectDir}/petstore.yaml"
    baseTag = "example-1.0.0"
    outputFile = "${project.projectDir}/CHANGELOG.md"
    section = "### OpenAPI"
}

changelog {
    version.set(project.version.toString())
    path.set(file("CHANGELOG.md").canonicalPath)
    header.set(provider { "[${version.get().removePrefix("example-")}] - ${date()}" })
    headerParserRegex.set("""(\d+\.\d+.\d+)""".toRegex())
    itemPrefix.set("-")
    keepUnreleasedSection.set(true)
    unreleasedTerm.set("[Unreleased]")
    groups.set(listOf("Added", "Changed", "Deprecated", "Removed", "Fixed", "Security", "OpenAPI"))
    lineSeparator.set("\n")
    combinePreReleases.set(true)
}

tasks.getByName("patchChangelog").apply {
    finalizedBy("patchOpenAPIDiff")
}
