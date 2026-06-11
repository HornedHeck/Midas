plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.composeHotReload) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.androidKotlinMultiplatformLibrary) apply false
    alias(libs.plugins.androidLint) apply false
    alias(libs.plugins.sqldelight) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.buildkonfig) apply false
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

    afterEvaluate {
        // Use a nested afterEvaluate so our source override runs AFTER the Kotlin/detekt plugin's
        // afterEvaluate (which wires compilation sources — including generated files — into the tasks).
        afterEvaluate {
            // Rules requiring type resolution (e.g. UnusedImport) are only active in
            // compilation-based detekt tasks. Wire those into the aggregate `detekt` task.
            val allDetektTasks = tasks
                .withType(dev.detekt.gradle.Detekt::class.java)
                .matching { it.name != "detekt" }

            allDetektTasks.configureEach {
                config.setFrom(file("${rootProject.projectDir}/misc/detekt.yml"))
                buildUponDefaultConfig = true
                // Restrict source to hand-written Kotlin files under src/ — excludes all generated
                // code under build/. FileTree is configuration-cache-safe.
                setSource(project.fileTree("src") { include("**/*.kt") })
            }

            // Snapshot to names inside afterEvaluate to avoid ConcurrentModificationException
            // when Gradle iterates a live TaskCollection during task graph calculation.
            val typeResolutionTaskNames = allDetektTasks
                .toList()
                .filter { !it.classpath.isEmpty }
                .map { it.name }

            tasks.named("detekt").configure {
                dependsOn(typeResolutionTaskNames.map { tasks.named(it) })
            }
        }
    }
}
