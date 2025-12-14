plugins {
    kotlin("js") version "1.9.24"
    id("org.jetbrains.dokka") version "1.9.20"
}

group = "com.minesweeper"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    js(IR) {
        browser {
            commonWebpackConfig {
                cssSupport {
                    enabled.set(true)
                }
            }
        }
        binaries.executable()
    }
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-html-js:0.9.0")
}