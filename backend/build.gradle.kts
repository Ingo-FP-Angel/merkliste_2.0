import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val ktor_version = "1.2.4"

plugins {
	id("org.springframework.boot") version "2.2.0.RELEASE"
	id("io.spring.dependency-management") version "1.0.8.RELEASE"
	kotlin("jvm") version "1.3.50"
	kotlin("plugin.spring") version "1.3.50"
}

group = "de.the-one-brack"
version = "0.0.1-SNAPSHOT"
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

	testImplementation(group ="org.springframework.boot", name = "spring-boot-starter-test") {
		exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "11"
	}
}
