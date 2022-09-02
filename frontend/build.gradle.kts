plugins {
    id("com.github.node-gradle.node") version "3.4.0"
}

node {
    download.set(true)
}

tasks {
    register<GradleBuild>("buildApplication") {
        group = "build"
        tasks = listOf("yarn_install", "yarn_build")
    }
}