import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.changelog.date

plugins {
    alias(libs.plugins.kotlin) apply false
    alias(libs.plugins.pluginPublish) apply false
    alias(libs.plugins.detekt)
    alias(libs.plugins.versionCheck)
    alias(libs.plugins.semver)
    alias(libs.plugins.changelog)
}

allprojects {
    group = property("GROUP").toString()
    version = property("VERSION").toString()

    apply {
        plugin(rootProject.libs.plugins.detekt.get().pluginId)
    }

    detekt {
        baseline = rootProject.file("../config/detekt/baseline.xml")
        config.setFrom(rootProject.files("../config/detekt/detekt.yml"))
        buildUponDefaultConfig = true
    }
}

tasks {
    withType<Detekt>().configureEach {
        baseline = file("config/detekt/baseline.xml")
        reports {
            html.required.set(true)
            html.outputLocation.set(file("build/reports/detekt.html"))
        }
    }
}

tasks.register<Detekt>("detektFormat") {
    description = "Runs over whole code base without the starting overhead for each module."
    parallel = true
    autoCorrect = true
    setSource(files(projectDir))
    include("**/*.kt")
    include("**/*.kts")
    exclude("**/resources/**")
    exclude("**/build/**")
    reports {
        xml.required = false
        html.required = false
        txt.required = true
    }
}

tasks.wrapper {
    distributionType = Wrapper.DistributionType.ALL
}

changelog {
    version.set(property("VERSION").toString())
    path.set(rootProject.file("../CHANGELOG.md").canonicalPath)
    header.set(provider { "[${version.get()}] - ${date()}" })
    headerParserRegex.set("""(\d+\.\d+.\d+)""".toRegex())
    itemPrefix.set("-")
    keepUnreleasedSection.set(true)
    unreleasedTerm.set("[Unreleased]")
    groups.set(listOf("Added", "Changed", "Deprecated", "Removed", "Fixed", "Security"))
    lineSeparator.set("\n")
    combinePreReleases.set(true)
}

