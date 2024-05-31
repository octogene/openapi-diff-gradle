# OpenAPI Diff Gradle plugin 🐘

[![Pre Merge Checks](https://github.com/cortinico/kotlin-gradle-plugin-template/workflows/Pre%20Merge%20Checks/badge.svg)](https://github.com/cortinico/kotlin-gradle-plugin-template/actions?query=workflow%3A%22Pre+Merge+Checks%22)  [![License](https://img.shields.io/github/license/cortinico/kotlin-android-template.svg)](LICENSE) ![Language](https://img.shields.io/github/languages/top/cortinico/kotlin-android-template?color=blue&logo=kotlin)

A simple **Gradle Plugin** 🐘 to generate an OpenAPI diff from git tags and integrate it to your changelog.

## How to use 👣

### Apply the plugin

```kotlin
// build.gradle.kts
plugins {
    id("dev.octogene.openapidff") version "0.1.0"
}
```

### Configure the plugin

```kotlin
openapiDiff {
    targetFile = "${project.projectDir}/doc/openapi/petstore.yaml"
    // Optional (will use targetFile as default)
    baseFile = "${project.projectDir}/petstore.yaml"
    baseTag = "example-1.0.0"
    outputFile = "${project.projectDir}/CHANGELOG.md"
    // Optional
    section = "### OpenAPI"
    // Optional (will use HEAD as default target)
    targetTag = "example-1.0.2"
}
```

## Features 🎨

- Generate openapi diff from tags
- Insert diff at a section in markdown or asciidoc document

## Example

You can try to use directly the plugin with the example project by running:

```
./gradlew :example:patchOpenAPIDiff
```

Or you can use the task from the [Gradle Changelog Plugin](https://github.com/JetBrains/gradle-changelog-plugin) we hooked with our custom task :

```
./gradlew :example:patchChangelog
```

## Contributing 🤝

Feel free to open a issue or submit a pull request for any bugs/improvements.

## License 📄

This project is licensed under the MIT License - see the [License](License) file for details.
This project is using the [kotlin-gradle-plugin-template](https://github.com/cortinico/kotlin-gradle-plugin-template) by [Nicola Corti](https://github.com/cortinico).
[![Use the same template](https://img.shields.io/badge/-Use%20this%20template-brightgreen)](https://github.com/cortinico/kotlin-gradle-plugin-template/generate)
