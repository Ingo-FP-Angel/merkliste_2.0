plugins {
    id("com.moowork.node") version "1.3.1" apply true
}

node {
    download = true
}

tasks {
    register<GradleBuild>("buildApplication") {
        group = "build"
        tasks = listOf("yarn_install", "yarn_build")
    }
}