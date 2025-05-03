import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "3.3.0"
	id("io.spring.dependency-management") version "1.1.5"
	kotlin("jvm") version "2.1.10"
	kotlin("plugin.spring") version "2.1.10"
}

group = "funkotlin"
version = "0.0.1-SNAPSHOT"

object Versions {
	const val kotestVersion = "5.9.1"
	const val kotlinxCoroutinesVersion = "1.5.1"
}

java {
	sourceCompatibility = JavaVersion.VERSION_21
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
//	val arrowVersion = "2.0.1"
	val arrowVersion = "1.2.4"
	val arrowMtlVersion = "0.11.0"
	val retrofitVersion = "2.11.0"

	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
//	implementation("org.jetbrains.kotlin:kotlinx-coroutines")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")

	implementation("com.squareup.retrofit2:retrofit:$retrofitVersion")

	// FP lib
	implementation("io.arrow-kt:arrow-mtl-data:$arrowMtlVersion")
	implementation("io.arrow-kt:arrow-mtl:$arrowMtlVersion")

//	implementation("io.arrow-kt:arrow-core:$arrowVersion")
//	implementation("io.arrow-kt:arrow-optics:$arrowVersion")
//	implementation("io.arrow-kt:arrow-resilience:$arrowVersion")
//	implementation("io.arrow-kt:arrow-fx-stm:$arrowVersion")
//	implementation("io.arrow-kt:arrow-atomic:$arrowVersion")
//	implementation("io.arrow-kt:arrow-eval:$arrowVersion")
//	implementation("io.arrow-kt:arrow-fx-coroutines:$arrowVersion")

	compileOnly("org.projectlombok:lombok")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	annotationProcessor("org.projectlombok:lombok")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("io.projectreactor:reactor-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	testImplementation("io.kotest:kotest-framework-api-jvm:${Versions.kotestVersion}")
	testImplementation("io.kotest:kotest-property:${Versions.kotestVersion}")
	testImplementation("io.kotest:kotest-runner-junit5:${Versions.kotestVersion}")
	testImplementation("io.mockk:mockk:1.13.13")

	testImplementation("io.arrow-kt:arrow-incubator-test:$arrowMtlVersion")
	testImplementation("com.willowtreeapps.assertk:assertk:0.10")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs += "-Xjsr305=strict"
		jvmTarget = "21"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
