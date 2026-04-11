plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.koin.compiler)
}

kotlin{
    jvm()

    sourceSets {
        commonMain {
            dependencies {
                // Koin
                implementation(project.dependencies.platform(libs.koin.bom))
                implementation(libs.koin.core)

                // Date/Time
                implementation(libs.kotlinx.datetime)
                implementation(libs.kotlinx.coroutines.core)
            }
        }

        commonTest {
            dependencies {
                implementation(libs.kotlin.test)

                // Koin
                implementation(libs.koin.test)
            }
        }
    }
}
