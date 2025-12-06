package you.yearof.build

import com.diffplug.gradle.spotless.BaseKotlinExtension
import com.diffplug.gradle.spotless.SpotlessCheck
import com.diffplug.gradle.spotless.SpotlessExtension
import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektCreateBaselineTask
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.withType
import kotlin.jvm.optionals.getOrNull

class CodeQualityPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val catalog = target.extensions.getByType(VersionCatalogsExtension::class.java).named("libs")

        applySpotless(target, catalog)
        applyDetekt(target, catalog)

        val tasks = target.tasks
        val lint = tasks.maybeCreate("lint")
        lint.group = "verification"
        lint.dependsOn(tasks.withType<Detekt>(), tasks.withType<SpotlessCheck>())
    }

    fun applySpotless(
        target: Project,
        catalog: VersionCatalog,
    ) {
        val spotless = catalog.library("spotless")
        applyPlugin(target, spotless)

        val ktlint = catalog.library("ktlint")
        target.buildscript.dependencies.add("classpath", ktlint)

        target.extensions.configure(SpotlessExtension::class.java) {
            kotlin {
                target("**/*.kt")
                applyKtlintConfiguration(target, catalog, this)
            }
            kotlinGradle {
                target("**/*.gradle.kts")
                applyKtlintConfiguration(target, catalog, this)
            }
        }
    }

    fun applyKtlintConfiguration(
        target: Project,
        catalog: VersionCatalog,
        extension: BaseKotlinExtension,
    ) {
        val ktlint = catalog.library("ktlint")

        val composeRules = catalog.library("ktlint-rules-compose")
        val rulesets = listOf(composeRules.toString())

        val editorConfig = target.rootProject.file(".editorconfig")

        extension
            .ktlint(ktlint.version)
            .setEditorConfigPath(editorConfig.absolutePath)
            .editorConfigOverride(
                mapOf(
                    "ktlint_function_naming_ignore_when_annotated_with" to "Composable",
                    "ktlint_property_naming_constant_naming" to "pascal_case",
                ),
            ).customRuleSets(rulesets)
    }

    fun applyDetekt(
        target: Project,
        catalog: VersionCatalog,
    ) {
        val detekt = catalog.library("detekt")
        applyPlugin(target, detekt)

        target.extensions.configure(DetektExtension::class.java) {
            toolVersion = checkNotNull(detekt.version)
            buildUponDefaultConfig = true
            allRules = false

            val configs = buildList {
                add(target.rootProject.file("detekt.yml"))
                val moduleConfig = target.file("detekt.yml")
                if (moduleConfig.exists()) add(moduleConfig)
            }
            config.setFrom(configs)
            baseline = target.rootProject.file("detekt-baseline.xml")
        }

        val composeRules = catalog.library("detekt-rules-compose")
        target.dependencies.add("detektPlugins", composeRules)

        target.tasks.withType(Detekt::class.java).configureEach {
            exclude { it.file.invariantSeparatorsPath.contains("/build/generated/") }
        }
        target.tasks.withType(DetektCreateBaselineTask::class.java).configureEach {
            exclude { it.file.invariantSeparatorsPath.contains("/build/generated/") }
        }
    }

    fun applyPlugin(
        target: Project,
        plugin: MinimalExternalModuleDependency,
    ) {
        target.plugins.apply(plugin.group)
        target.buildscript.dependencies.add("classpath", plugin)
    }
}

private fun VersionCatalog.library(name: String) = checkNotNull(findLibrary(name).getOrNull()).get()
