import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import io.gitlab.arturbosch.detekt.Detekt

plugins {
    alias(libs.plugins.kotlin) apply false
    alias(libs.plugins.detekt)
    alias(libs.plugins.versionCheck)
}

subprojects {
    apply {
        plugin(rootProject.libs.plugins.detekt.get().pluginId)
    }

    detekt {
        config.setFrom(rootProject.files("config/detekt/detekt.yml"))
    }

    dependencies {
        detekt(rootProject.libs.detekt.formating)
    }
}

tasks.withType<Detekt>().configureEach {
    baseline.set(file("config/detekt/baseline.xml"))
    reports {
        html.required.set(true)
        txt.required.set(true)
        html.outputLocation.set(file("build/reports/detekt.html"))
        txt.outputLocation.set(file("build/reports/detekt.txt"))
    }
}

tasks.withType<DependencyUpdatesTask> {
    rejectVersionIf {
        candidate.version.isNonStable()
    }
}

fun String.isNonStable() = "^[0-9,.v-]+(-r)?$".toRegex().matches(this).not()

tasks.register("clean", Delete::class.java) {
    delete(rootProject.layout.buildDirectory)
}

tasks.register("reformatAll") {
    description = "Reformat all the Kotlin Code"

    dependsOn("detektFormat")
    dependsOn(gradle.includedBuild("plugin-build").task(":plugin:detektFormat"))
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

tasks.register("preMerge") {
    description = "Runs all the tests/verification tasks on both top level and included build."

    dependsOn(":example:check")
    dependsOn(gradle.includedBuild("plugin-build").task(":plugin:check"))
    dependsOn(gradle.includedBuild("plugin-build").task(":plugin:validatePlugins"))
    dependsOn(gradle.includedBuild("plugin-build").task(":plugin:apiCheck"))
}

tasks.wrapper {
    distributionType = Wrapper.DistributionType.ALL
}
