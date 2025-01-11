import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val javaTarget = JavaVersion.VERSION_21

plugins {
    kotlin("plugin.jpa") version "2.1.0"
    kotlin("jvm") version "2.1.0"
    kotlin("plugin.spring") version "2.1.0"
    kotlin("plugin.serialization") version "2.1.0"
    id("org.springframework.boot") version "3.4.0"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.google.devtools.ksp") version "2.1.0-1.0.29"
    id("eu.vendeli.telegram-bot") version "7.8.0"
}

group = "com.telebot"
version = "1.0-SNAPSHOT"
java.sourceCompatibility = javaTarget

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
    maven("https://mvn.vendeli.eu/telegram-bot")
}

dependencies {
    implementation("eu.vendeli:spring-ktgram-starter:7.8.0")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign:4.2.0")
    implementation("org.springframework.cloud:spring-cloud-starter-circuitbreaker-resilience4j:3.2.0")

    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.postgresql:postgresql:42.7.4")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.8")
    implementation("com.arthenica:ffmpeg-kit-full-gpl:4.5.1-1")

    implementation("ch.qos.logback:logback-classic:1.5.15")
    implementation("ch.qos.logback:logback-core:1.5.15")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
}

kotlin {
    jvmToolchain(javaTarget.majorVersion.toInt())
}

tasks {
    compileJava {
        targetCompatibility = javaTarget.majorVersion
        sourceCompatibility = javaTarget.majorVersion
    }

    withType<Test> {
        useJUnitPlatform()
    }
    withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = javaTarget.majorVersion
        }
    }
}