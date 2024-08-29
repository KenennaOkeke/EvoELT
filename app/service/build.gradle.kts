import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "3.2.0"
	id("io.spring.dependency-management") version "1.1.3"
	kotlin("jvm") version "1.9.20"
	kotlin("plugin.spring") version "1.8.22"
	kotlin("plugin.jpa") version "1.8.22"
	//id("org.graalvm.buildtools.native") version "0.10.2"
}



group = "org.kenenna"
version = "1.0.0"

java {
	sourceCompatibility = JavaVersion.VERSION_20
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.0")
	implementation("org.jetbrains.kotlin:kotlin-reflect:1.9.23")
	implementation("software.amazon.awssdk:sqs:2.25.43")

	implementation("org.postgresql:postgresql:42.7.3")

	compileOnly("org.projectlombok:lombok:1.18.32")
	//compileOnly("org.projectlombok:lom:1.18.32")

	developmentOnly("org.springframework.boot:spring-boot-devtools")

	implementation("org.springframework.boot:spring-boot-configuration-processor")
	annotationProcessor("org.projectlombok:lombok:1.18.32")

	testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.boot:spring-boot-configuration-processor") //json
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs += "-Xjsr305=strict"
		jvmTarget = "20"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootBuildImage>("bootBuildImage") {
	imageName = "org.kenenna/evoelt"
	builder = "paketobuildpacks/builder:tiny"
}