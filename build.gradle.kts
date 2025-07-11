/*
 * For more details on building Java & JVM projects, please refer to
 * https://docs.gradle.org/8.13/userguide/building_java_projects.html in the Gradle documentation.
 */

plugins {
    val kotlinVersion = "2.1.20"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion

    id("org.jetbrains.dokka") version "1.9.20"

    `java-library`
}

group = "dev.hirth"

repositories {
    mavenCentral()
}

dependencies {
    val cmVer = "0.24.0"
    api("org.commonmark:commonmark:$cmVer")
    api("org.commonmark:commonmark-ext-autolink:${cmVer}")
    api("org.commonmark:commonmark-ext-footnotes:${cmVer}")
    api("org.commonmark:commonmark-ext-gfm-strikethrough:${cmVer}")
    api("org.commonmark:commonmark-ext-gfm-tables:${cmVer}")
    api("org.commonmark:commonmark-ext-heading-anchor:${cmVer}")
    api("org.commonmark:commonmark-ext-image-attributes:${cmVer}")
    api("org.commonmark:commonmark-ext-ins:$cmVer")
    api("org.commonmark:commonmark-ext-task-list-items:${cmVer}")
    api("org.commonmark:commonmark-ext-yaml-front-matter:$cmVer")

    testImplementation("org.junit.jupiter:junit-jupiter:5.12.1")
    testImplementation("org.assertj:assertj-core:3.27.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    jvmToolchain(17)
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

tasks.dokkaHtml {
    outputDirectory.set(file("$rootDir/docs"))

    dokkaSourceSets {
        named("main") {
            includes.from("DOC.md")
        }
    }
}
