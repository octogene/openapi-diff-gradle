pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
    }
}

plugins {
    id("com.gradle.develocity") version "3.17.5"
}

develocity {
    buildScan {
        publishing.onlyIf {
            it.buildResult.failures.isNotEmpty() && !System.getenv("CI").isNullOrEmpty()
        }
    }
}

rootProject.name = "kotlin-gradle-plugin-template"

include(":example")
includeBuild("plugin-build")
