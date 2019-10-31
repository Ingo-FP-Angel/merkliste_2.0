plugins {
    id("com.moowork.node") version "1.3.1" apply true
}

tasks {
    register<GradleBuild>("buildApplication") {
        tasks = listOf("yarn_install", "yarn_build")
    }
}