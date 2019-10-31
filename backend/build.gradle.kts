import java.nio.charset.StandardCharsets
import org.apache.tools.ant.filters.ReplaceTokens
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val ktor_version = "1.2.4"

plugins {
	id("idea")
	id("java")
	id("org.springframework.boot") version "2.2.0.RELEASE"
	id("io.spring.dependency-management") version "1.0.8.RELEASE"
	kotlin("jvm") version "1.3.50"
	kotlin("plugin.spring") version "1.3.50"
}

val appName = "merkliste_20"
group = "de.the-one-brack.$appName"
version = File("${project.projectDir}/..", "version").readText(StandardCharsets.UTF_8).trim()
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
	mavenCentral()
}

dependencies {
	implementation(group = "org.springframework.boot", name = "spring-boot-starter-web")
	implementation(group = "com.fasterxml.jackson.module", name = "jackson-module-kotlin")
	implementation(group = "org.jetbrains.kotlin", name = "kotlin-reflect")
	implementation(group = "org.jetbrains.kotlin", name = "kotlin-stdlib-jdk8")
	implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version = "1.3.2")
	implementation(group = "io.ktor", name = "ktor-client-core", version = ktor_version)
	implementation(group = "io.ktor", name = "ktor-client-apache", version = ktor_version)
	implementation(group = "org.jsoup", name = "jsoup", version = "1.12.1")

	testImplementation(group ="com.github.tomakehurst", name = "wiremock-jre8", version = "2.25.1")
	testImplementation(group = "org.assertj", name = "assertj-core")
	testImplementation(group ="org.springframework.boot", name = "spring-boot-starter-test")
}

tasks.withType<Test> {
	useJUnitPlatform()
	filter {
		includeTestsMatching("*Tests")
	}
	testLogging {
		events("passed", "skipped", "failed")
	}
}

task<Test>("integration") {
	group = "verification"
	useJUnitPlatform()
	filter {
		includeTestsMatching("*IT")
	}
	dependsOn("test")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "11"
	}
}

val backendBaseName = (project.group as String) + ".backend"
springBoot {
	buildInfo {
		properties {
			artifact = backendBaseName
			version = project.version as String
			group = project.group as String
			name = backendBaseName
		}
	}
}

tasks {
	bootJar {
		archiveBaseName.set(backendBaseName)
		manifest {
			attributes("Implementation-Title" to backendBaseName)
			attributes("Implementation-Version" to project.version)
		}
		dependsOn("copyFrontEnd")
	}
	register("copyFrontEnd") {
		dependsOn("buildFrontEnd")
		doFirst {
			println("Copy frontend build into resources")
		}
		finalizedBy("copyFrontEndFiles")
	}
	register<GradleBuild>("buildFrontEnd") {
		tasks = listOf(":frontend:buildApplication")
	}
	register<Copy>("copyFrontEndFiles") {
		from("../frontend/build/")
		into("${buildDir}/resources/main/static/")
	}
}

/* Docker */
val dockerBuildDirectory = "build/docker"
val dockerImageWithVersion = "ingofpangel/$appName:${project.version}"
val remoteDockerImageWithVersion = "docker.pkg.github.com/ingo-fp-angel/merkliste_2.0/$appName:${project.version}"
task<Copy>("copyBackendJar") {
	dependsOn("bootJar")
	from("build/libs/${backendBaseName}-${project.version}.jar")
	into(dockerBuildDirectory)
}

task<Copy>("copyDockerfiles") {
	from("src/main/docker/")
	from("..") {
		include("version")
	}
	into(dockerBuildDirectory)
	filter<ReplaceTokens>("tokens" to mapOf(
			"backendArtifactName" to "${backendBaseName}-${project.version}.jar"
	))
}

task<Exec>("buildDockerImage") {
	dependsOn("copyBackendJar", "copyDockerfiles")
	workingDir(dockerBuildDirectory)
	commandLine("docker", "build", "-t", dockerImageWithVersion, ".")
}

task<Exec>("tagDockerImage") {
	dependsOn("buildDockerImage")
	commandLine("docker", "tag", dockerImageWithVersion, remoteDockerImageWithVersion)
}

task<Exec>("pushDockerImage") {
	dependsOn("tagDockerImage")
	commandLine("docker", "push", remoteDockerImageWithVersion)
}
