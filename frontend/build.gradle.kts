plugins {
    id("com.github.node-gradle.node") version "3.2.1" apply true
}

tasks {
    register<GradleBuild>("buildApplication") {
        group = "build"
        tasks = listOf("yarn_install", "yarn_build")
    }
}