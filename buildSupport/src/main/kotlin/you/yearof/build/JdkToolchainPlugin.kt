package you.yearof.build

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinBasePluginWrapper
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

class JdkToolchainPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        configureJavaToolchain(target)
        configureKotlinToolchain(target)
    }

    private fun configureJavaToolchain(target: Project) {
        target.plugins.withType(JavaBasePlugin::class.java) {
            target.extensions.configure(JavaPluginExtension::class.java) {
                toolchain.languageVersion.set(JAVA_LANGUAGE_VERSION)
            }
        }
    }

    private fun configureKotlinToolchain(target: Project) {
        target.plugins.withType(KotlinBasePluginWrapper::class.java) {
            target.extensions.findByType(KotlinProjectExtension::class.java)?.jvmToolchain(JvmToolchainVersion)

            target.tasks.withType(KotlinJvmCompile::class.java).configureEach {
                compilerOptions.jvmTarget.set(JVM_TARGET)
            }
        }
    }

    companion object {
        private const val JvmToolchainVersion = 21
        private val JAVA_LANGUAGE_VERSION: JavaLanguageVersion = JavaLanguageVersion.of(JvmToolchainVersion)
        private val JVM_TARGET: JvmTarget = JvmTarget.JVM_21
    }
}
