plugins {
    id("com.github.node-gradle.node") version "7.0.2"
}

node {
    download.set(true)
}

tasks {
    register<GradleBuild>("buildApplication") {
        group = "build"
        tasks = listOf("pnpm_install", "pnpm_build")
    }
}