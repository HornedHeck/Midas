plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeHotReload) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.androidKotlinMultiplatformLibrary) apply false
    alias(libs.plugins.androidLint) apply false
    alias(libs.plugins.sqldelight) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.detekt)
}

subprojects {
    apply(plugin = rootProject.libs.plugins.detekt.get().pluginId)

    val kotlinSourceDirs = File(project.projectDir, "src").listFiles()
        ?.map { File(it, "kotlin") }
        ?.filter { it.exists() && it.isDirectory }
        ?: emptyList()

    detekt {
        toolVersion = rootProject.libs.versions.detekt.get()
        config.setFrom(file("${rootProject.projectDir}/misc/detekt.yml"))
        buildUponDefaultConfig = true
        parallel = true
        source.setFrom(files(kotlinSourceDirs))
    }
}