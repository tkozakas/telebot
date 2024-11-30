plugins {
    kotlin("plugin.jpa") version "2.1.0"
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.3.0"
    id("io.spring.dependency-management") version "1.1.6"
}

group = "com.telebot"
version = "1.0-SNAPSHOT"

var telegramBotVersion = "0.11.3"
var springBootVersion = "3.3.0"

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.dehuckakpyt.telegrambot:telegram-bot-core:$telegramBotVersion")
    implementation("io.github.dehuckakpyt.telegrambot:telegram-bot-spring:$telegramBotVersion")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa:$springBootVersion")
    implementation("org.hibernate:hibernate-core:6.6.3.Final")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.postgresql:postgresql:42.7.4")

    implementation("org.springframework.cloud:spring-cloud-starter-openfeign:4.1.4")
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.8")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
